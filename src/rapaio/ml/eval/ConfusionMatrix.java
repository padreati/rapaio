/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

import rapaio.printer.Printable;
import rapaio.data.Var;
import rapaio.printer.Printer;

import java.text.DecimalFormat;
import java.util.stream.IntStream;

/**
 * Confusion matrix utility.
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class ConfusionMatrix implements Printable {

    private final Var actual;
    private final Var predict;
    private final String[] dict;
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

    public ConfusionMatrix(Var actual, Var predict) {
        this(actual, predict, false);
    }

    public ConfusionMatrix(Var actual, Var predict, boolean percents) {
        validate(actual, predict);
        this.actual = actual;
        this.predict = predict;
        this.dict = actual.dictionary();
        this.cmf = new int[dict.length - 1][dict.length - 1];
        this.percents = percents;
        this.binary = actual.dictionary().length == 3;
        compute();
    }

    private void validate(Var actual, Var predict) {
        if (!actual.type().isNominal()) {
            throw new IllegalArgumentException("actual values var must be nominal");
        }
        if (!predict.type().isNominal()) {
            throw new IllegalArgumentException("predict values var must be nominal");
        }
        if (actual.dictionary().length != predict.dictionary().length) {
            throw new IllegalArgumentException("actual and predict does not have the same nominal dictionary");
        }
        for (int i = 0; i < actual.dictionary().length; i++) {
            if (!actual.dictionary()[i].equals(predict.dictionary()[i])) {
                throw new IllegalArgumentException("actual and predict does not have the same nominal dictionary");
            }
        }
    }

    private void compute() {
        for (int i = 0; i < actual.rowCount(); i++) {
            if (actual.index(i) != 0 && predict.index(i) != 0) {
                completeCases++;
                cmf[actual.index(i) - 1][predict.index(i) - 1]++;
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
    public void buildSummary(StringBuilder sb) {
        addConfusionMatrix(sb);
        addDetails(sb);
    }

    private void addDetails(StringBuilder sb) {
        DecimalFormat fmt = Printer.formatDecShort;
        sb.append(String.format("\nComplete cases %d from %d\n", (int) Math.rint(completeCases), actual.rowCount()));
        sb.append(String.format("Acc: %s         (Accuracy )\n", fmt.format(acc)));
        if (binary) {
            sb.append(String.format("F1:  %s         (F1 score / F-measure)\n", fmt.format(f1)));
            sb.append(String.format("MCC: %s         (Matthew correlation coefficient)\n", fmt.format(mcc)));
            sb.append(String.format("Pre: %s         (Precision)\n", fmt.format(precision)));
            sb.append(String.format("Rec: %s         (Recall)\n", fmt.format(recall)));
            sb.append(String.format("G:   %s         (G-measure)\n", fmt.format(g)));
        }
        sb.append("\n");
    }

    private void addConfusionMatrix(StringBuilder sb) {
        sb.append("> ConfusionMatrix\n");

        sb.append("\n");
        int maxwidth = "Actual".length();
        for (int i = 1; i < dict.length; i++) {
            maxwidth = Math.max(maxwidth, dict[i].length());
            int total = 0;
            for (int j = 1; j < dict.length; j++) {
                maxwidth = Math.max(maxwidth, String.format("%d", cmf[i - 1][j - 1]).length());
                total += cmf[i - 1][j - 1];
            }
            maxwidth = Math.max(maxwidth, String.format("%d", total).length());
        }

        sb.append(String.format("%" + maxwidth + "s", "")).append("|").append(" Predicted\n");
        sb.append(String.format("%" + maxwidth + "s", "Actual")).append("|");
        for (int i = 1; i < dict.length; i++) {
            sb.append(String.format("%" + maxwidth + "s", dict[i]));
            if (i != dict.length - 1) {
                sb.append(" ");
            } else {
                sb.append("|");
            }
        }
        sb.append(String.format("%" + maxwidth + "s ", "Total"));
        sb.append("\n");
        for (int i = 1; i < dict.length + 1; i++) {
            for (int j = 0; j < maxwidth; j++) {
                sb.append("-");
            }
            sb.append(" ");
        }
        for (int j = 0; j < maxwidth; j++) {
            sb.append("-");
        }
        sb.append(" ");
        sb.append("\n");

        for (int i = 1; i < dict.length; i++) {
            sb.append(String.format("%" + maxwidth + "s", dict[i])).append("|");
            int total = 0;
            for (int j = 1; j < dict.length; j++) {
                sb.append(String.format("%" + maxwidth + "d", cmf[i - 1][j - 1]));
                if (j != dict.length - 1) {
                    sb.append(" ");
                } else {
                    sb.append("|");
                }
                total += cmf[i - 1][j - 1];
            }
            sb.append(String.format("%" + maxwidth + "d", total));
            sb.append("\n");
        }

        for (int i = 1; i < dict.length + 1; i++) {
            for (int j = 0; j < maxwidth; j++) {
                sb.append("-");
            }
            sb.append(" ");
        }
        for (int j = 0; j < maxwidth; j++) {
            sb.append("-");
        }
        sb.append(" ");
        sb.append("\n");


        sb.append(String.format("%" + maxwidth + "s", "Total")).append("|");
        for (int j = 1; j < dict.length; j++) {
            int total = 0;
            for (int i = 1; i < dict.length; i++) {
                total += cmf[i - 1][j - 1];
            }
            sb.append(String.format("%" + maxwidth + "d", total));
            if (j != dict.length - 1) {
                sb.append(" ");
            } else {
                sb.append("|");
            }
        }
        sb.append(String.format("%" + maxwidth + "d", (int) Math.rint(completeCases)));
        sb.append("\n");


        // percents

        if (!percents || completeCases == 0.) return;

        sb.append("\n");
        maxwidth = "Actual".length();
        for (int i = 1; i < dict.length; i++) {
            maxwidth = Math.max(maxwidth, dict[i].length());
            int total = 0;
            for (int j = 1; j < dict.length; j++) {
                maxwidth = Math.max(maxwidth, String.format("%.3f", cmf[i - 1][j - 1] / completeCases).length());
                total += cmf[i - 1][j - 1];
            }
            maxwidth = Math.max(maxwidth, String.format("%.3f", total / completeCases).length());
        }

        sb.append(String.format("%" + maxwidth + "s", "")).append("|").append(" Predicted\n");
        sb.append(String.format("%" + maxwidth + "s", "Actual")).append("|");
        for (int i = 1; i < dict.length; i++) {
            sb.append(String.format("%" + maxwidth + "s", dict[i]));
            if (i != dict.length - 1) {
                sb.append(" ");
            } else {
                sb.append("|");
            }
        }
        sb.append(String.format("%" + maxwidth + "s ", "Total"));
        sb.append("\n");

        for (int i = 1; i < dict.length + 1; i++) {
            for (int j = 0; j < maxwidth; j++) {
                sb.append("-");
            }
            sb.append(" ");
        }
        for (int j = 0; j < maxwidth; j++) {
            sb.append("-");
        }
        sb.append(" ");
        sb.append("\n");

        for (int i = 1; i < dict.length; i++) {
            sb.append(String.format("%" + maxwidth + "s", dict[i])).append("|");
            int total = 0;
            for (int j = 1; j < dict.length; j++) {
                sb.append(String.format(" %.3f", cmf[i - 1][j - 1] / completeCases));
                if (j != dict.length - 1) {
                    sb.append(" ");
                } else {
                    sb.append("|");
                }
                total += cmf[i - 1][j - 1];
            }
            sb.append(String.format(" %.3f", total / completeCases));
            sb.append("\n");
        }

        for (int i = 1; i < dict.length + 1; i++) {
            for (int j = 0; j < maxwidth; j++) {
                sb.append("-");
            }
            sb.append(" ");
        }
        for (int j = 0; j < maxwidth; j++) {
            sb.append("-");
        }
        sb.append(" ");
        sb.append("\n");


        sb.append(String.format("%" + maxwidth + "s", "Total")).append("|");
        for (int j = 1; j < dict.length; j++) {
            int total = 0;
            for (int i = 1; i < dict.length; i++) {
                total += cmf[i - 1][j - 1];
            }
            sb.append(String.format(" %.3f", total / completeCases));
            if (j != dict.length - 1) {
                sb.append(" ");
            } else {
                sb.append("|");
            }
        }
        sb.append(String.format(" %.3f", 1.));
        sb.append("\n");

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
