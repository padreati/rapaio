/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.core.stat;

import rapaio.core.Printable;
import rapaio.data.*;

import static rapaio.data.filters.BaseFilters.sort;


/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class ROC implements Printable {

    private final Var score;
    private final Var classes;
    private Frame data;
    private double auc;

    public ROC(Var score, Var actual, Var predict) {
        this.score = score;
        this.classes = Index.newEmpty(actual.rowCount());
        for (int i = 0; i < actual.rowCount(); i++) {
            if (actual.label(i).equals(predict.label(i))) {
                classes.setIndex(i, 1);
            } else {
                classes.setIndex(i, 0);
            }
        }
        compute();
    }

    public ROC(Var score, Var actual, int index) {
        this(score, actual, actual.dictionary()[index]);
    }

    public ROC(Var score, Var actual, String label) {
        this.score = score;
        this.classes = Index.newEmpty(actual.rowCount());
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

        Var rows = sort(Index.newSeq(score.rowCount()), RowComparators.numericComparator(score, false));
        int len = 1;
        double prev = Double.MIN_VALUE;
        for (int i = 0; i < rows.rowCount(); i++) {
            if (score.missing(rows.index(i)) || classes.missing(rows.index(i))) continue;
            if (score.value(rows.index(i)) != prev) {
                prev = score.value(rows.index(i));
                len++;
            }
        }
        data = Frames.newMatrix(len, "threshold", "fpr", "tpr", "acc");
        prev = Double.POSITIVE_INFINITY;
        int pos = 0;

        for (int i = 0; i < rows.rowCount(); i++) {
            if (score.missing(rows.index(i)) || classes.missing(rows.index(i))) continue;
            if (score.value(rows.index(i)) != prev) {
                auc += Math.abs(prevfp - fp) * Math.abs(prevtp + tp) / 2.;
                double accValue = (tp + n - fp) / (0. + n + p);
                data.setValue(pos, "threshold", prev);
                data.setValue(pos, "fpr", fp / (1. * n));
                data.setValue(pos, "tpr", tp / (1. * p));
                data.setValue(pos, "acc", accValue);
                prevfp = fp;
                prevtp = tp;
                prev = score.value(rows.index(i));
                pos++;
            }
            if (classes.index(rows.index(i)) > 0) tp++;
            else fp++;
        }
        data.setValue(pos, "threshold", prev);
        data.setValue(pos, "fpr", 1.);
        data.setValue(pos, "tpr", 1.);
        data.setValue(pos, "acc", p / (0. + n + p));

        auc += Math.abs(n - prevfp) * (p + prevtp) / 2.;
        auc /= (1. * p * n);
    }

    public Frame getData() {
        return data;
    }

    public double auc() {
        return auc;
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        sb.append("> ROC summary\nnot implemented\n");
    }
}
