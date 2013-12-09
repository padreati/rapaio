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

import rapaio.core.Summarizable;
import rapaio.data.*;

import static rapaio.core.BaseMath.*;
import static rapaio.session.Workspace.*;
import static rapaio.filters.RowFilters.*;


/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class ROC implements Summarizable {

    private final Vector score;
    private final Vector classes;
    private Frame data;
    private double auc;

    public ROC(Vector score, Vector actual, Vector predict) {
        this.score = score;
        this.classes = new IndexVector(actual.getRowCount());
        for (int i = 0; i < actual.getRowCount(); i++) {
            if (actual.getLabel(i).equals(predict.getLabel(i))) {
                classes.setIndex(i, 1);
            } else {
                classes.setIndex(i, 0);
            }
        }
        compute();
    }

    public ROC(Vector score, Vector actual, int index) {
        this(score, actual, actual.getDictionary()[index]);
    }

    public ROC(Vector score, Vector actual, String label) {
        this.score = score;
        this.classes = new IndexVector(actual.getRowCount());
        for (int i = 0; i < actual.getRowCount(); i++) {
            if (actual.getLabel(i).equals(label)) {
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
        for (int i = 0; i < classes.getRowCount(); i++) {
            if (classes.isMissing(i)) {
                continue;
            }
            if (classes.getIndex(i) > 0) p++;
            else n++;
        }


        double fp = 0;
        double tp = 0;
        auc = 0;

        Vector sort = sort(score, RowComparators.numericComparator(score, false));
        int len = 1;
        double prev = Double.MIN_VALUE;
        for (int i = 0; i < sort.getRowCount(); i++) {
            if (sort.isMissing(i) || classes.isMissing(sort.getRowId(i))) continue;
            if (sort.getValue(i) != prev) {
                prev = sort.getValue(i);
                len++;
            }
        }
        data = Frames.newMatrixFrame(len, new String[]{"threshold", "fpr", "tpr", "acc"});
        prev = Double.POSITIVE_INFINITY;
        int pos = 0;

        for (int i = 0; i < sort.getRowCount(); i++) {
            if (sort.isMissing(i) || classes.isMissing(sort.getRowId(i))) continue;

            if (sort.getValue(i) != prev) {
                auc += abs(prevfp - fp) * abs(prevtp + tp) / 2.;
                double accValue = (tp + n - fp) / (0. + n + p);
                data.setValue(pos, "threshold", prev);
                data.setValue(pos, "fpr", fp / (1. * n));
                data.setValue(pos, "tpr", tp / (1. * p));
                data.setValue(pos, "acc", accValue);
                prevfp = fp;
                prevtp = tp;
                prev = sort.getValue(i);
                pos++;
            }

            if (classes.getIndex(sort.getRowId(i)) > 0) tp++;
            else fp++;
        }
        data.setValue(pos, "threshold", prev);
        data.setValue(pos, "fpr", 1.);
        data.setValue(pos, "tpr", 1.);
        data.setValue(pos, "acc", p / (0. + n + p));

        auc += abs(n - prevfp) * (p + prevtp) / 2.;
        auc /= (1. * p * n);
    }

    public Frame getData() {
        return data;
    }

    public double getAuc() {
        return auc;
    }

    @Override
    public void summary() {
        StringBuilder sb = new StringBuilder();

        sb.append("ROC summary");

        code(sb.toString());
    }
}
