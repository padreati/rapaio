/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

package rapaio.ml.eval.metric;

import rapaio.data.Var;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.dense.SolidDMatrix;
import rapaio.printer.Format;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POption;

import java.util.List;

import static rapaio.printer.Format.floatFlex;

/**
 * Confusion matrix utility.
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class Confusion implements Printable {

    public static Confusion from(Var actual, Var predict) {
        return new Confusion(actual, predict);
    }

    private static final String TEXT_AC_PR = "Ac\\Pr";

    private final Var actual;
    private final Var predict;
    private final List<String> factors;
    private final DMatrix cmFrequency;
    private final DMatrix cmProbability;
    private final boolean binary;

    // true positive - predicted true and actual true
    private double tp;
    // true negatives - predicted false and actual false
    private double tn;
    // false positive - predicted true and actual false
    private double fp;
    // false negatives - predicted false and actual true
    private double fn;

    private double acc;
    private double mcc;
    private double f1;
    private double g;
    private double precision;
    private double recall;
    private double completeCases = 0;
    private double acceptedCases = 0;
    private double errorCases = 0;

    private Confusion(Var actual, Var predict) {
        this.actual = actual;
        this.predict = predict;
        this.factors = actual.levels();
        this.cmFrequency = SolidDMatrix.empty(factors.size() - 1, factors.size() - 1);
        this.cmProbability = SolidDMatrix.empty(factors.size() - 1, factors.size() - 1);
        this.binary = actual.levels().size() == 3;
        validate();
        compute();
    }

    private void validate() {
        if (!actual.type().isNominal()) {
            throw new IllegalArgumentException("Actual values variable must be nominal.");
        }
        if (!predict.type().isNominal()) {
            throw new IllegalArgumentException("Predicted values variable must be nominal.");
        }
        if (actual.levels().size() != predict.levels().size()) {
            throw new IllegalArgumentException("Actual and predict variables does not have the same nominal level size.");
        }
        for (int i = 0; i < actual.levels().size(); i++) {
            if (!actual.levels().get(i).equals(predict.levels().get(i))) {
                throw new IllegalArgumentException(
                        String.format("Actual not the same nominal levels (actual:%s, predict:%s).",
                                String.join(",", actual.levels()),
                                String.join(",", predict.levels())));
            }
        }
    }

    private void compute() {
        for (int i = 0; i < actual.rowCount(); i++) {
            if (actual.getInt(i) != 0 && predict.getInt(i) != 0) {
                completeCases++;
                cmFrequency.set(actual.getInt(i) - 1, predict.getInt(i) - 1, cmFrequency.get(actual.getInt(i) - 1, predict.getInt(i) - 1) + 1);
            }
        }
        acc = cmFrequency.trace();
        acceptedCases = acc;
        errorCases = completeCases - acceptedCases;

        if (completeCases == 0) {
            acc = 0;
        } else {
            acc = acc / completeCases;
        }
        cmProbability.plus(cmFrequency).times(1.0 / completeCases);

        if (binary) {
            tp = cmFrequency.get(0, 0);
            tn = cmFrequency.get(1, 1);
            fp = cmFrequency.get(1, 0);
            fn = cmFrequency.get(0, 1);

            mcc = (tp * tn - fp * fn) / Math.sqrt((tp + fp) * (tp + fn) * (tn + fp) * (tn + fn));
            f1 = 2 * tp / (2 * tp + fp + fn);
            precision = tp / (tp + fp);
            recall = tp / (tp + fn);
            g = Math.sqrt(precision * recall);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ConfusionMatrix(levels:").append(String.join(",", factors)).append(")");
        return sb.toString();
    }

    @Override
    public String toSummary(Printer printer, POption... options) {
        StringBuilder sb = new StringBuilder();
        addConfusionMatrix(sb);
        addDetails(sb);
        return sb.toString();
    }

    @Override
    public String toContent(Printer printer, POption... options) {
        return toSummary(printer, options);
    }

    @Override
    public String toFullContent(Printer printer, POption... options) {
        return toSummary(printer, options);
    }

    private void addConfusionMatrix(StringBuilder sb) {
        sb.append("> Confusion matrix\n");

        sb.append(" - Frequency table\n");

        TextTable tt = TextTable.empty(factors.size() + 3, factors.size() + 3);
        setupTable(tt);

        double[] rowTotals = new double[factors.size() - 1];
        double[] colTotals = new double[factors.size() - 1];
        double grandTotal = 0;

        for (int i = 0; i < factors.size() - 1; i++) {
            for (int j = 0; j < factors.size() - 1; j++) {
                tt.textRight(i + 2, j + 2, ((i == j) ? ">" : " ") + Format.floatFlex(cmFrequency.get(i, j)));
                grandTotal += cmFrequency.get(i, j);
                rowTotals[i] += cmFrequency.get(i, j);
                colTotals[j] += cmFrequency.get(i, j);
            }
        }
        for (int i = 0; i < factors.size() - 1; i++) {
            tt.textRight(factors.size() + 2, i + 2, Format.floatFlex(colTotals[i]));
            tt.textRight(i + 2, factors.size() + 2, Format.floatFlex(rowTotals[i]));
        }
        tt.textRight(factors.size() + 2, factors.size() + 2, Format.floatFlex(grandTotal));
        sb.append(tt.getRawText());


        sb.append(" - Probability table\n");
        tt = TextTable.empty(factors.size() + 3, factors.size() + 3);
        setupTable(tt);

        for (int i = 0; i < factors.size() - 1; i++) {
            for (int j = 0; j < factors.size() - 1; j++) {
                tt.textRight(i + 2, j + 2, ((i == j) ? ">" : " ") + Format.floatShort(cmProbability.get(i, j)));
            }
        }
        for (int i = 0; i < factors.size() - 1; i++) {
            tt.textRight(factors.size() + 2, i + 2, Format.floatShort(colTotals[i] / completeCases));
            tt.textRight(i + 2, factors.size() + 2, Format.floatShort(rowTotals[i] / completeCases));
        }
        tt.textRight(factors.size() + 2, factors.size() + 2, Format.floatShort(grandTotal / completeCases));
        sb.append(tt.getRawText());

        sb.append("\n");
    }

    private void setupTable(TextTable tt) {
        tt.textCenter(0, 0, TEXT_AC_PR);

        for (int i = 0; i < factors.size() - 1; i++) {
            tt.textRight(i + 2, 0, factors.get(i + 1));
            tt.textCenter(i + 2, 1, "|");
            tt.textCenter(i + 2, factors.size() + 1, "|");
            tt.textRight(0, i + 2, factors.get(i + 1));
            tt.textRight(1, i + 2, line(factors.get(i + 1).length()));
            tt.textRight(factors.size() + 1, i + 2, line(factors.get(i + 1).length()));
        }
        tt.textRight(factors.size() + 2, 0, "total");
        tt.textRight(0, factors.size() + 2, "total");

        tt.textCenter(1, 0, line(TEXT_AC_PR.length()));
        tt.textCenter(factors.size() + 1, 0, line(TEXT_AC_PR.length()));
        tt.textCenter(1, factors.size() + 2, line(TEXT_AC_PR.length()));
        tt.textCenter(factors.size() + 1, factors.size() + 2, line(TEXT_AC_PR.length()));

        tt.textCenter(0, 1, "|");
        tt.textCenter(1, 1, "|");
        tt.textCenter(factors.size() + 1, 1, "|");
        tt.textCenter(factors.size() + 2, 1, "|");

        tt.textCenter(0, factors.size() + 1, "|");
        tt.textCenter(1, factors.size() + 1, "|");
        tt.textCenter(factors.size() + 1, factors.size() + 1, "|");
        tt.textCenter(factors.size() + 2, factors.size() + 1, "|");
    }

    private void addDetails(StringBuilder sb) {
        sb.append(String.format("\nComplete cases %d from %d\n", (int) Math.rint(completeCases), actual.rowCount()));
        sb.append(String.format("Acc: %s         (Accuracy )\n", floatFlex(acc)));
        if (binary) {
            sb.append(String.format("F1:  %s         (F1 score / F-measure)\n", floatFlex(f1)));
            sb.append(String.format("MCC: %s         (Matthew correlation coefficient)\n", floatFlex(mcc)));
            sb.append(String.format("Pre: %s         (Precision)\n", floatFlex(precision)));
            sb.append(String.format("Rec: %s         (Recall)\n", floatFlex(recall)));
            sb.append(String.format("G:   %s         (G-measure)\n", floatFlex(g)));
        }
    }

    private String line(int len) {
        char[] lineChars = new char[len];
        for (int i = 0; i < len; i++) {
            lineChars[i] = '-';
        }
        return String.valueOf(lineChars);
    }

    /**
     * Proportion of correctly predicted cases
     *
     * @return accuracy
     */
    public double accuracy() {
        return acc;
    }

    /**
     * Proportion of incorrectly predicted cases
     *
     * @return error rate
     */
    public double error() {
        return 1.0 - acc;
    }

    /**
     * Number of cases which were correctly predicted.
     */
    public int acceptedCases() {
        return (int) Math.rint(acceptedCases);
    }

    /**
     * Number of cases which were predicted incorrectly.
     */
    public int errorCases() {
        return (int) Math.rint(errorCases);
    }

    /**
     * All umber of observations.
     *
     * @return total number of observations
     */
    public int completeCases() {
        return (int) Math.rint(completeCases);
    }

    /**
     * True positive count: number of observations predicted
     * as true and actually true.
     *
     * @return true positive count
     */
    public double tp() {
        return tp;
    }

    /**
     * True negative count: number of observations predicted as
     * false and actually false.
     *
     * @return true negative count
     */
    public double tn() {
        return tn;
    }

    /**
     * False positive count: number of cases predicted as true,
     * but actually false.
     *
     * @return false positive count
     */
    public double fp() {
        return fp;
    }

    /**
     * False negative count: number of cases predicted as false,
     * but actually true.
     *
     * @return false negative count
     */
    public double fn() {
        return fn;
    }

    /**
     * F1 score: a metric which describes in one number
     * the balance between precision and recall and consists
     * in the harmonic mean of precision and recall.
     *
     * @return F1 score
     */
    public double f1() {
        return f1;
    }

    /**
     * Mathew correlation coefficient: describes in one number
     * the performance of a binary classifier. It is acconted as
     * a more informative and more balanced version of F1 because
     * it takes into account all fur types of error and it works
     * well also with unbalanced problems.
     * <p>
     * For more details one can consult the wikipedia page:
     * <a href="https://en.wikipedia.org/wiki/Matthews_correlation_coefficient">
     * Wikipedia - MCC</a>.
     *
     * @return Matthew correlation coefficient
     */
    public double mcc() {
        return mcc;
    }

    /**
     * Precision (positive predicted value): the fraction of relevant instances among
     * the retrieved instances.
     *
     * @return precision
     */
    public double precision() {
        return precision;
    }

    /**
     * Recall (sensitivity): the fraction of relevant instances among all instances retrieved.
     *
     * @return recall
     */
    public double recall() {
        return recall;
    }

    /**
     * G score or Fowlkes–Mallows index: the geometric mean of precision and recall,
     * while the F1 score is the harmonic mean.
     *
     * @return G score
     */
    public double gScore() {
        return g;
    }

    /**
     * Frequency matrix. A matrix which contains
     * the count of observations where in each cell
     * we have count of observations which were predicted as
     * level corresponding to column and were actual
     * level corresponding to row.
     * <p>
     * Thus, to count the total number of correctly predicted
     * cases one has to compute the trace of this matrix.
     *
     * @return frequency confusion matrix
     */
    public DMatrix frequencyMatrix() {
        return cmFrequency;
    }

    /**
     * Probability matrix contains the proportion of cases from
     * confusion matrix. Actually this matrix contains the frequency
     * matrix divided by the total number of cases.
     *
     * @return probability confusion matrix
     */
    public DMatrix probabilityMatrix() {
        return cmProbability;
    }
}
