/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.eval.metric;

import rapaio.core.stat.Sum;
import rapaio.data.RowComparators;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.preprocessing.VarCumSum;
import rapaio.data.preprocessing.VarRefSort;
import rapaio.printer.Format;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;
import rapaio.util.IntComparator;

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

        Var index = VarInt.seq(actual.size());
        IntComparator cmp = RowComparators.from(
                RowComparators.doubleComparator(fit, false),
                RowComparators.integerComparator(index, true));
        Var sol = VarRefSort.from(cmp).fapply(actual).copy();

        int n = sol.size();

        double totalLosses = Sum.of(sol).value();
        double giniSum = Sum.of(VarCumSum.transform().fapply(sol)).value() / totalLosses;
        giniSum -= (actual.size() + 1) / 2.;
        return giniSum / actual.size();
    }

    private double wgini(Var actual, Var fit, Var weights) {
        Var index = VarInt.seq(actual.size());
        IntComparator cmp = RowComparators.from(
                RowComparators.doubleComparator(fit, false),
                RowComparators.integerComparator(index, true));
        Var sol = VarRefSort.from(cmp).fapply(actual).copy();
        Var w = VarRefSort.from(cmp).fapply(weights).copy();

        double wsum = Sum.of(w).value();
        Var random = VarCumSum.transform().fapply(VarDouble.from(w, value -> value / wsum).copy());
        double totalPositive = Sum.of(VarDouble.from(actual.size(), row -> sol.getDouble(row) * w.getDouble(row))).value();
        Var lorentz =
                VarCumSum.transform().fapply(VarDouble.from(actual.size(), row -> sol.getDouble(row) * w.getDouble(row) / totalPositive));

        double g = 0.0;
        for (int i = 0; i < actual.size() - 1; i++) {
            g += lorentz.getDouble(i + 1) * random.getDouble(i) - lorentz.getDouble(i) * random.getDouble(i + 1);
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
    public String toSummary(Printer printer, POption<?>... options) {
        return "> Gini" + (weighted ? " (Weighted):\n" : ":\n")
                + "\n"
                + "gini coefficient: " + Format.floatFlex(gini) + "\n"
                + "normalized gini coefficient: " + Format.floatFlex(normalizedGini) + "\n"
                + "\n";
    }
}
