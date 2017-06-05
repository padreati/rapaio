/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package rapaio.ml.eval;

import rapaio.data.Var;
import rapaio.printer.Printable;
import rapaio.printer.format.TextTable;
import rapaio.sys.WS;

import java.util.Arrays;
import java.util.stream.IntStream;

import static rapaio.sys.WS.formatFlex;

/**
 * Confusion matrix utility.
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Confusion implements Printable {

    private final Var actual;
    private final Var predict;
    private final String[] factors;
    private final int[][] cmf;
    private final boolean binary;
    private final boolean percents;
    private double acc;
    private double mcc;
    private double f1;
    private double g;
    private double precision;
    private double recall;
    private double completeCases = 0;
    private double acceptedCases = 0;
    private double errorCases = 0;

    public Confusion(Var actual, Var predict) {
        this(actual, predict, false);
    }

    public Confusion(Var actual, Var predict, boolean percents) {
        validate(actual, predict);
        this.actual = actual;
        this.predict = predict;
        this.factors = actual.getLevels();
        this.cmf = new int[factors.length - 1][factors.length - 1];
        this.percents = percents;
        this.binary = actual.getLevels().length == 3;
        compute();
    }

    private void validate(Var actual, Var predict) {
        if (!actual.getType().isNominal()) {
            throw new IllegalArgumentException("actual values var must be nominal");
        }
        if (!predict.getType().isNominal()) {
            throw new IllegalArgumentException("fit values var must be nominal");
        }
        if (actual.getLevels().length != predict.getLevels().length) {
            throw new IllegalArgumentException("actual and fit does not have the same nominal levels");
        }
        for (int i = 0; i < actual.getLevels().length; i++) {
            if (!actual.getLevels()[i].equals(predict.getLevels()[i])) {
                throw new IllegalArgumentException(
                        String.format("not the same nominal levels (actual:%s, fit:%s)",
                                Arrays.deepToString(actual.getLevels()),
                                Arrays.deepToString(predict.getLevels())));
            }
        }
    }

    private void compute() {
        for (int i = 0; i < actual.getRowCount(); i++) {
            if (actual.getIndex(i) != 0 && predict.getIndex(i) != 0) {
                completeCases++;
                cmf[actual.getIndex(i) - 1][predict.getIndex(i) - 1]++;
            }
        }
        acc = IntStream.range(0, cmf.length).mapToDouble(i -> cmf[i][i]).sum();
        acceptedCases = acc;
        errorCases = completeCases - acceptedCases;

        if (completeCases == 0) {
            acc = 0;
        } else {
            acc = acc / completeCases;
        }

        if (binary) {
            double tp = cmf[0][0];
            double tn = cmf[1][1];
            double fp = cmf[1][0];
            double fn = cmf[0][1];

            mcc = (tp * tn - fp * fn) / Math.sqrt((tp + fp) * (tp + fn) * (tn + fp) * (tn + fn));
            f1 = 2 * tp / (2 * tp + fp + fn);
            precision = tp / (tp + fp);
            recall = tp / (tp + fn);
            g = Math.sqrt(precision * recall);
        }
    }

    @Override
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        addConfusionMatrix(sb);
        addDetails(sb);
        return sb.toString();
    }

    private void addDetails(StringBuilder sb) {
        sb.append(String.format("\nComplete cases %d from %d\n", (int) Math.rint(completeCases), actual.getRowCount()));
        sb.append(String.format("Acc: %s         (Accuracy )\n", formatFlex(acc)));
        if (binary) {
            sb.append(String.format("F1:  %s         (F1 score / F-measure)\n", formatFlex(f1)));
            sb.append(String.format("MCC: %s         (Matthew correlation coefficient)\n", formatFlex(mcc)));
            sb.append(String.format("Pre: %s         (Precision)\n", formatFlex(precision)));
            sb.append(String.format("Rec: %s         (Recall)\n", formatFlex(recall)));
            sb.append(String.format("G:   %s         (G-measure)\n", formatFlex(g)));
        }
    }

    private void addConfusionMatrix(StringBuilder sb) {
        sb.append("> Confusion\n");

        sb.append("\n");
        TextTable tt = TextTable.newEmpty(factors.length + 3, factors.length + 3);
        tt.withSplit();

        tt.set(0, 0, "Ac\\Pr", 0);

        for (int i = 0; i < factors.length - 1; i++) {
            tt.set(i + 2, 0, factors[i + 1], 1);
            tt.set(i + 2, 1, "|", 0);
            tt.set(i + 2, factors.length + 1, "|", 0);
            tt.set(0, i + 2, factors[i + 1], 1);
            tt.set(1, i + 2, line(factors[i + 1].length()), 1);
            tt.set(factors.length + 1, i + 2, line(factors[i + 1].length()), 1);
        }
        tt.set(factors.length + 2, 0, "total", 1);
        tt.set(0, factors.length + 2, "total", 1);

        tt.set(1, 0, line("Ac\\Pr".length()), 0);
        tt.set(factors.length + 1, 0, line("Ac\\Pr".length()), 0);
        tt.set(1, factors.length + 2, line("Ac\\Pr".length()), 0);
        tt.set(factors.length + 1, factors.length + 2, line("Ac\\Pr".length()), 0);

        tt.set(0, 1, "|", 0);
        tt.set(1, 1, "|", 0);
        tt.set(factors.length + 1, 1, "|", 0);
        tt.set(factors.length + 2, 1, "|", 0);

        tt.set(0, factors.length + 1, "|", 0);
        tt.set(1, factors.length + 1, "|", 0);
        tt.set(factors.length + 1, factors.length + 1, "|", 0);
        tt.set(factors.length + 2, factors.length + 1, "|", 0);

        int[] rowTotals = new int[factors.length - 1];
        int[] colTotals = new int[factors.length - 1];
        int grandTotal = 0;

        for (int i = 0; i < factors.length - 1; i++) {
            for (int j = 0; j < factors.length - 1; j++) {
                tt.set(i + 2, j + 2, ((i == j) ? ">" : " ") + cmf[i][j], 1);
                grandTotal += cmf[i][j];
                rowTotals[i] += cmf[i][j];
                colTotals[j] += cmf[i][j];
            }
        }
        for (int i = 0; i < factors.length - 1; i++) {
            tt.set(factors.length + 2, i + 2, String.valueOf(colTotals[i]), 1);
            tt.set(i + 2, factors.length + 2, String.valueOf(rowTotals[i]), 1);
        }
        tt.set(factors.length + 2, factors.length + 2, String.valueOf(grandTotal), 1);
        sb.append(tt.getSummary());

        if (percents && completeCases > 0.) {

            tt = TextTable.newEmpty(factors.length + 3, factors.length + 3);
            tt.withSplit();

            tt.set(0, 0, "Ac\\Pr", 0);

            for (int i = 0; i < factors.length - 1; i++) {
                tt.set(i + 2, 0, factors[i + 1], 1);
                tt.set(i + 2, 1, "|", 0);
                tt.set(i + 2, factors.length + 1, "|", 0);
                tt.set(0, i + 2, factors[i + 1], 1);
                tt.set(1, i + 2, line(factors[i + 1].length()), 1);
                tt.set(factors.length + 1, i + 2, line(factors[i + 1].length()), 1);
            }
            tt.set(factors.length + 2, 0, "total", 1);
            tt.set(0, factors.length + 2, "total", 1);

            tt.set(1, 0, line("Ac\\Pr".length()), 0);
            tt.set(factors.length + 1, 0, line("Ac\\Pr".length()), 0);
            tt.set(1, factors.length + 2, line("Ac\\Pr".length()), 0);
            tt.set(factors.length + 1, factors.length + 2, line("Ac\\Pr".length()), 0);

            tt.set(0, 1, "|", 0);
            tt.set(1, 1, "|", 0);
            tt.set(factors.length + 1, 1, "|", 0);
            tt.set(factors.length + 2, 1, "|", 0);

            tt.set(0, factors.length + 1, "|", 0);
            tt.set(1, factors.length + 1, "|", 0);
            tt.set(factors.length + 1, factors.length + 1, "|", 0);
            tt.set(factors.length + 2, factors.length + 1, "|", 0);

            for (int i = 0; i < factors.length - 1; i++) {
                for (int j = 0; j < factors.length - 1; j++) {
                    tt.set(i + 2, j + 2, ((i == j) ? ">" : " ") + WS.formatShort(cmf[i][j] / completeCases), 1);
                }
            }
            for (int i = 0; i < factors.length - 1; i++) {
                tt.set(factors.length + 2, i + 2, WS.formatShort(colTotals[i] / completeCases), 1);
                tt.set(i + 2, factors.length + 2, WS.formatShort(rowTotals[i] / completeCases), 1);
            }
            tt.set(factors.length + 2, factors.length + 2, WS.formatShort(grandTotal / completeCases), 1);
            sb.append(tt.getSummary());

        }

    }

    private String line(int len) {
        char[] lineChars = new char[len];
        for (int i = 0; i < len; i++) {
            lineChars[i] = '-';
        }
        return String.valueOf(lineChars);
    }

    public double accuracy() {
        return acc;
    }

    public double error() {
        return 1.0 - acc;
    }

    /**
     * Number of cases which were correctly predicted
     */
    public int acceptedCases() {
        return (int) Math.rint(acceptedCases);
    }

    /**
     * Number of cases which were not predicted correctly
     */
    public int errorCases() {
        return (int) Math.rint(errorCases);
    }

    public int completeCases() {
        return (int) Math.rint(completeCases);
    }

    public int[][] matrix() {
        return cmf;
    }
}
