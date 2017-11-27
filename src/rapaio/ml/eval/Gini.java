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

import rapaio.core.stat.Sum;
import rapaio.data.IdxVar;
import rapaio.data.NumVar;
import rapaio.data.RowComparators;
import rapaio.data.Var;
import rapaio.data.filter.var.VFCumulativeSum;
import rapaio.data.filter.var.VFRefSort;
import rapaio.printer.Printable;
import rapaio.sys.WS;

import java.util.Comparator;

/**
 * This evaluation tool computes Gini and Normalized Gini Coefficients
 * int the normal and weighted versions, depending if a weight is provided.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/17/15.
 */
public class Gini implements Printable {

    public static Gini from(Var actual, Var fit) {
        return new Gini(actual, fit, null);
    }

    public static Gini from(Var actual, Var fit, Var w) {
        return new Gini(actual, fit, w);
    }

    private final double gini;
    private final double normalizedGini;
    private final boolean weighted;

    private Gini(Var actual, Var fit, Var weights) {
        if (weights == null) {
            weighted = false;
            gini = gini(actual, fit);
            normalizedGini = gini / gini(actual, actual);
        } else {
            weighted = true;
            gini = wgini(actual, fit, weights);
            normalizedGini = gini / wgini(actual, actual, weights);
        }
    }

    private double gini(Var actual, Var fit) {

        Var index = IdxVar.seq(actual.rowCount());
        Comparator<Integer> cmp = RowComparators.from(
                RowComparators.numeric(fit, false),
                RowComparators.index(index, true));
        Var sol = new VFRefSort(cmp).fitApply(actual).solidCopy();

        int n = sol.rowCount();

        double totalLosses = Sum.from(sol).value();
        double giniSum = Sum.from(VFCumulativeSum.filter().fitApply(sol)).value() / totalLosses;
        giniSum -= (actual.rowCount() + 1) / 2.;
        return giniSum / actual.rowCount();
    }

    private double wgini(Var actual, Var fit, Var weights) {
        Var index = IdxVar.seq(actual.rowCount());
        Comparator<Integer> cmp = RowComparators.from(
                RowComparators.numeric(fit, false),
                RowComparators.index(index, true));
        Var sol = new VFRefSort(cmp).fitApply(actual).solidCopy();
        Var w = new VFRefSort(cmp).fitApply(weights).solidCopy();

        double wsum = Sum.from(w).value();
        Var random = VFCumulativeSum.filter().fitApply(NumVar.from(w, value -> value / wsum).solidCopy());
        double totalPositive = Sum.from(NumVar.from(actual.rowCount(), row -> sol.value(row) * w.value(row))).value();
        Var lorentz = new VFCumulativeSum().fitApply(NumVar.from(actual.rowCount(), row -> sol.value(row) * w.value(row) / totalPositive));

        double g = 0.0;
        for (int i = 0; i < actual.rowCount() - 1; i++) {
            g += lorentz.value(i + 1) * random.value(i) - lorentz.value(i) * random.value(i + 1);
        }
        return g;
    }


    public double gini() {
        return gini;
    }

    public double normalizedGini() {
        return normalizedGini;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();

        sb.append("> Gini" + (weighted ? " (Weighted):\n" : ":\n"));
        sb.append("\n");
        sb.append("gini coefficient: " + WS.formatFlex(gini) + "\n");
        sb.append("normalized gini coefficient: " + WS.formatFlex(normalizedGini) + "\n");
        sb.append("\n");

        return sb.toString();
    }
}
