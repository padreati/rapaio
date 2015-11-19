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

import rapaio.data.*;
import rapaio.data.filter.VFRefSort;
import rapaio.printer.Printable;

import java.io.Serializable;

import static rapaio.sys.WS.formatFlex;


/**
 * Receiver Operator Characteristic.
 * <p>
 * This utility class computes ROC for a given scores and binary prediction.
 * <p>
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
@Deprecated
public class ROC implements Printable, Serializable {

    public static final String threshold = "threshold";
    public static final String fpr = "fpr";
    public static final String tpr = "tpr";
    public static final String acc = "acc";
    private static final long serialVersionUID = -4598096059703515426L;
    private final Var score;
    private final Var classes;
    private Frame data;
    private double auc;

    /**
     * Builds a new ROC instance from scores and the indicator value.
     * The binary indicator value is obtained by comparing the actual
     * classes with the predicted classes.
     *
     * @param score   variable which contains scores
     * @param actual  variable which contains the actual classes
     * @param predict variable which contains the predicted classes
     */
    public ROC(Var score, Var actual, Var predict) {
        this.score = score;
        this.classes = Index.empty(actual.rowCount());
        for (int i = 0; i < actual.rowCount(); i++) {
            if (actual.label(i).equals(predict.label(i))) {
                classes.setIndex(i, 1);
            } else {
                classes.setIndex(i, 0);
            }
        }
        compute();
    }

    /**
     * Builds a new ROC instance from scores and the truth value is a prediction
     * is correct or not. The truth value is obtained by comparing the actual index value
     * with the given index.
     *
     * @param score  scores variable
     * @param actual actual class
     * @param index  index of the class considered 1, all other index values are 0
     */
    public ROC(Var score, Var actual, int index) {
        this(score, actual, actual.levels()[index]);
    }

    /**
     * Builds a new ROC instance from scores and the truth value is a prediction
     * is correct or not. The truth value is obtained by comparing the actual label value
     * with the given label.
     *
     * @param score  scores variable
     * @param actual actual class
     * @param label  label of the class considered 1, all other labels values are 0
     */
    public ROC(Var score, Var actual, String label) {
        this.score = score;
        this.classes = Index.empty(actual.rowCount());
        for (int i = 0; i < actual.rowCount(); i++) {
            if (actual.label(i).equals(label)) {
                classes.setIndex(i, 1);
            } else {
                classes.setIndex(i, 0);
            }
        }
        compute();
    }

    private void compute() {
        int p = 0;
        int n = 0;
        double prevtp = 0;
        double prevfp = 0;
        auc = 0;
        for (int i = 0; i < classes.rowCount(); i++) {
            if (classes.missing(i)) {
                continue;
            }
            if (classes.index(i) > 0) p++;
            else n++;
        }


        double fp = 0;
        double tp = 0;
        auc = 0;

        Var rows = new VFRefSort(RowComparators.numeric(score, false)).fitApply(Index.seq(score.rowCount()));
        int len = 1;
        double prev = Double.MIN_VALUE;
        for (int i = 0; i < rows.rowCount(); i++) {
            if (score.missing(rows.index(i)) || classes.missing(rows.index(i))) continue;
            if (score.value(rows.index(i)) != prev) {
                prev = score.value(rows.index(i));
                len++;
            }
        }
        data = SolidFrame.newMatrix(len, threshold, fpr, tpr, acc);
        prev = Double.POSITIVE_INFINITY;
        int pos = 0;

        for (int i = 0; i < rows.rowCount(); i++) {
            if (score.missing(rows.index(i)) || classes.missing(rows.index(i))) continue;
            if (score.value(rows.index(i)) != prev) {
                auc += Math.abs(prevfp - fp) * Math.abs(prevtp + tp) / 2.;
                double accValue = (tp + n - fp) / (0. + n + p);
                data.setValue(pos, threshold, prev);
                data.setValue(pos, fpr, fp / (1. * n));
                data.setValue(pos, tpr, tp / (1. * p));
                data.setValue(pos, acc, accValue);
                prevfp = fp;
                prevtp = tp;
                prev = score.value(rows.index(i));
                pos++;
            }
            if (classes.index(rows.index(i)) > 0) tp++;
            else fp++;
        }
        data.setValue(pos, threshold, prev);
        data.setValue(pos, fpr, 1.);
        data.setValue(pos, tpr, 1.);
        data.setValue(pos, acc, p / (0. + n + p));

        auc += Math.abs(n - prevfp) * (p + prevtp) / 2.;
        auc /= (1. * p * n);
    }

    public Frame data() {
        return data;
    }

    public double auc() {
        return auc;
    }

    public int findRowForThreshold(double value) {
        Var th = data.var(threshold);
        for (int i = 0; i < th.rowCount(); i++) {
            if (th.value(i) <= value) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        final String fmt = "%-10s";

        sb.append("\n > ROC printSummary").append("\n");
        sb.append("\n");
        for (int j = 0; j < data.varCount(); j++) {
            if (j > 0) {
                sb.append(", ");
            }
            sb.append(String.format(fmt, data.varNames()[j]));
        }
        sb.append("\n");

        int[] rows = new int[11];
        rows[0] = 0;
        rows[1] = data.rowCount() / 10;
        rows[2] = 2 * data.rowCount() / 10;
        rows[3] = 3 * data.rowCount() / 10;
        rows[4] = 4 * data.rowCount() / 10;
        rows[5] = 5 * data.rowCount() / 10;
        rows[6] = 6 * data.rowCount() / 10;
        rows[7] = 7 * data.rowCount() / 10;
        rows[8] = 8 * data.rowCount() / 10;
        rows[9] = 9 * data.rowCount() / 10;
        rows[10] = data.rowCount() - 1;

        for (int i : rows) {
            for (int j = 0; j < data.varCount(); j++) {
                if (j > 0) {
                    sb.append(", ");
                }
                sb.append(String.format(fmt, formatFlex(data.value(i, j))));
            }
            sb.append("\n");
        }
        sb.append("\n");
        sb.append("AUC: ").append(formatFlex(auc)).append("\n");
        return sb.toString();
    }
}
