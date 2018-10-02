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

package rapaio.ml.eval;

import rapaio.data.Frame;
import rapaio.data.RowComparators;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarInt;
import rapaio.data.filter.var.VRefSort;
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
    public static ROC from(Var score, Var actual, Var predict) {
        VarInt classes = VarInt.empty(actual.rowCount());
        for (int i = 0; i < actual.rowCount(); i++) {
            if (actual.getLabel(i).equals(predict.getLabel(i))) {
                classes.setInt(i, 1);
            } else {
                classes.setInt(i, 0);
            }
        }
        return new ROC(score, classes);
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
    public static ROC from(Var score, Var actual, int index) {
        return from(score, actual, actual.levels().get(index));
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
    public static ROC from(Var score, Var actual, String label) {
        VarInt classes = VarInt.empty(actual.rowCount());
        for (int i = 0; i < actual.rowCount(); i++) {
            if (actual.getLabel(i).equals(label)) {
                classes.setInt(i, 1);
            } else {
                classes.setInt(i, 0);
            }
        }
        return new ROC(score, classes);
    }

    private ROC(Var score, Var classes) {
        this.score = score;
        this.classes = classes;
        compute();
    }

    private void compute() {
        int p = 0;
        int n = 0;
        double prevtp = 0;
        double prevfp = 0;
        auc = 0;
        for (int i = 0; i < classes.rowCount(); i++) {
            if (classes.isMissing(i)) {
                continue;
            }
            if (classes.getInt(i) > 0) p++;
            else n++;
        }


        double fp = 0;
        double tp = 0;
        auc = 0;

        Var rows = new VRefSort(RowComparators.doubleComparator(score, false)).fapply(VarInt.seq(score.rowCount()));
        int len = 1;
        double prev = Double.MIN_VALUE;
        for (int i = 0; i < rows.rowCount(); i++) {
            if (score.isMissing(rows.getInt(i)) || classes.isMissing(rows.getInt(i))) continue;
            if (score.getDouble(rows.getInt(i)) != prev) {
                prev = score.getDouble(rows.getInt(i));
                len++;
            }
        }
        data = SolidFrame.matrix(len, threshold, fpr, tpr, acc);
        prev = Double.POSITIVE_INFINITY;
        int pos = 0;

        for (int i = 0; i < rows.rowCount(); i++) {
            if (score.isMissing(rows.getInt(i)) || classes.isMissing(rows.getInt(i))) continue;
            if (score.getDouble(rows.getInt(i)) != prev) {
                auc += Math.abs(prevfp - fp) * Math.abs(prevtp + tp) / 2.;
                double accValue = (tp + n - fp) / (0. + n + p);
                data.setDouble(pos, threshold, prev);
                data.setDouble(pos, fpr, fp / (1. * n));
                data.setDouble(pos, tpr, tp / (1. * p));
                data.setDouble(pos, acc, accValue);
                prevfp = fp;
                prevtp = tp;
                prev = score.getDouble(rows.getInt(i));
                pos++;
            }
            if (classes.getInt(rows.getInt(i)) > 0) tp++;
            else fp++;
        }
        data.setDouble(pos, threshold, prev);
        data.setDouble(pos, fpr, 1.);
        data.setDouble(pos, tpr, 1.);
        data.setDouble(pos, acc, p / (0. + n + p));

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
        Var th = data.rvar(threshold);
        for (int i = 0; i < th.rowCount(); i++) {
            if (th.getDouble(i) <= value) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        final String fmt = "%-10s";

        sb.append("> ROC printSummary").append("\n");
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
                sb.append(String.format(fmt, formatFlex(data.getDouble(i, j))));
            }
            sb.append("\n");
        }
        sb.append("\n");
        sb.append("AUC: ").append(formatFlex(auc)).append("\n");
        return sb.toString();
    }
}
