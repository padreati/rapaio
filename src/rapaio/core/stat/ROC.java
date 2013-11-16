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
import rapaio.data.IndexVector;
import rapaio.data.RowComparators;
import rapaio.data.Vector;

import static rapaio.core.BaseMath.*;
import static rapaio.explore.Workspace.*;
import static rapaio.filters.RowFilters.*;

import java.util.ArrayList;
import java.util.List;

import rapaio.data.NumericVector;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class ROC implements Summarizable {

    private final Vector score;
    private final Vector classes;
    private Vector fprate;
    private Vector tprate;
    private Vector acc;
    private double bestpx;
    private double bestpy;
    private double bestacc;
    private double auc;

    public ROC(Vector score, Vector actual, Vector predict) {
        this.score = score;
        this.classes = new IndexVector("class", actual.getRowCount());
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
        this.classes = new IndexVector("class", actual.getRowCount());
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
            if (classes.getIndex(i) > 0) {
                p++;
            } else {
                n++;
            }
        }
        List<Double> fpr = new ArrayList<>();
        List<Double> tpr = new ArrayList<>();
        List<Double> accr = new ArrayList<>();

        fpr.add(0.);
        tpr.add(0.);
        accr.add(n / (0. + n + p));

        double fp = 0;
        double tp = 0;
        bestpx = 0;
        bestpy = 0;
        bestacc = 0;
        auc = 0;

        Vector sort = sort(score, RowComparators.numericComparator(score, false));
        double prev = Double.MIN_VALUE;
        for (int i = 0; i < sort.getRowCount(); i++) {
            if (sort.isMissing(i) || classes.isMissing(sort.getRowId(i))) continue;
            if (classes.getIndex(sort.getRowId(i)) > 0) tp++;
            else fp++;

            if (sort.getValue(i) != prev || i==sort.getRowCount()-1) {
                prev = sort.getValue(i);
                fpr.add(fp / (1. * n));
                tpr.add(tp / (1. * p));
                auc += abs(prevfp - fp) * abs(prevtp + tp) / 2.;
                prevfp = fp;
                prevtp = tp;
                double accValue = (tp + n - fp) / (0. + n + p);
                accr.add(accValue);
                if (accValue > bestacc) {
                    bestacc = accValue;
                    bestpx = fp / (1. * n);
                    bestpy = tp / (1. * p);
                }
            }
        }
        auc += abs(n - prevfp) * (p + prevtp) / 2.;
        auc /= (1.*p * n);

        fprate = new NumericVector("fprate", fpr.size());
        tprate = new NumericVector("tprate", tpr.size());
        acc = new NumericVector("acc", accr.size());

        for (int i = 0; i < fpr.size(); i++) {
            fprate.setValue(i, fpr.get(i));
            tprate.setValue(i, tpr.get(i));
            acc.setValue(i, accr.get(i));
        }
    }

    public Vector getFPRateVector() {
        return fprate;
    }

    public Vector getTPRateVector() {
        return tprate;
    }

    public Vector getAccVector() {
        return acc;
    }

    public double getBestAccFPRate() {
        return bestpx;
    }

    public double getBestAccTPRate() {
        return bestpy;
    }

    public double getBestAccValue() {
        return bestacc;
    }

    public double getAuc() {
        return auc;
    }

    @Override
    public void summary() {
        StringBuilder sb = new StringBuilder();

        code(sb.toString());
    }
}
