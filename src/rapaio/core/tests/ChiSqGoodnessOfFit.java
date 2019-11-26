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

package rapaio.core.tests;

import rapaio.core.distributions.ChiSquare;
import rapaio.core.tools.DVector;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.printer.format.Format;
import rapaio.printer.format.TextTable;

/**
 * Pearson Chi Square goodness of predict test.
 * <p>
 * This test allows one to test if a given observed frequency of a categorical variable
 * differs from a theoretical distribution.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/31/17.
 */
public class ChiSqGoodnessOfFit implements HTest {

    private final DVector dv;
    private final Var p;
    private final VarDouble expected;
    private final int df; // degrees of freedom
    private final double chiValue; // chi-square statistic's value
    private final double pValue;

    /**
     * Builds the test from given observed data in input x and
     * from expected probabilities in p.
     * <p>
     * If the observed data x is given as a nominal, binary or
     * ordinal variable, then the observed counts are built from
     * the data into a density vector without missing label.
     * <p>
     * If the observed data x is given as numeric or index, then
     * the observed counts are built directly as a copy of x.
     *
     * @param x vector of observed data
     * @param p vector of expected probabilities
     */
    public static ChiSqGoodnessOfFit from(Var x, Var p) {
        DVector dv = buildDv(x);
        return new ChiSqGoodnessOfFit(dv, p);
    }

    /**
     * Builds the test from given observed data density vector input dv and
     * from expected probabilities in p.
     *
     * @param dv vector of observed data
     * @param p  vector of expected probabilities
     */
    public static ChiSqGoodnessOfFit from(DVector dv, Var p) {
        return new ChiSqGoodnessOfFit(dv, p);
    }

    private static DVector buildDv(Var x) {
        switch (x.type()) {
            case BINARY:
            case NOMINAL:
                return DVector.fromCounts(false, x);
            case DOUBLE:
            case INT:
                DVector dv = DVector.empty(true, x.rowCount());
                for (int i = 0; i < x.rowCount(); i++) {
                    dv.set(i, x.getDouble(i));
                }
                return dv;
            default:
                throw new IllegalArgumentException("variable of given type could not be " +
                        "used to build discrete observed counts");
        }
    }

    private ChiSqGoodnessOfFit(DVector dv, Var p) {

        VarDouble expected = VarDouble.from(p, pi -> pi * dv.sum());

        if (dv.rowCount() - dv.start() != expected.rowCount()) {
            throw new IllegalArgumentException("Different degrees of freedom!");
        }

        this.dv = dv;
        this.p = p;
        this.expected = expected;
        this.df = expected.rowCount() - 1;
        if (df <= 0) {
            throw new IllegalArgumentException("should be over 0");
        }

        double sum = 0;
        for (int i = dv.start(); i < dv.rowCount(); i++) {
            double o = dv.get(i);
            double e = expected.getDouble(i - dv.start());

            if (Math.abs(e) < 1e-50) {
                sum += Double.POSITIVE_INFINITY;
                break;
            }
            if (Math.abs(e) < 1e-50 && Math.abs(o - e) < 1e50) {
                continue;
            }
            sum += Math.pow(o - e, 2) / expected.getDouble(i - dv.start());
        }
        chiValue = sum;
        pValue = 1.0 - ChiSquare.of(df).cdf(chiValue);
    }

    public int df() {
        return df;
    }

    public double getChiValue() {
        return chiValue;
    }

    @Override
    public double ciHigh() {
        return Double.NaN;
    }

    @Override
    public double ciLow() {
        return Double.NaN;
    }

    public double pValue() {
        return pValue;
    }

    @Override
    public String toSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("> ChiSqGoodnessOfFit\n");
        sb.append("\n");

        sb.append("Chi-squared test for given probabilities (goodness of predict)\n");
        sb.append("\n");
        sb.append("X-squared = ").append(Format.floatFlex(chiValue))
                .append(", df = ").append(df)
                .append(", p-value = ").append(Format.pValue(pValue))
                .append("\n");
        sb.append("\n");

        sb.append("Data:  \n");

        TextTable tt = TextTable.empty(5, dv.rowCount() + 1, 1, 1);


        tt.textLeft(2, 0, "Observed count");
        tt.textLeft(3, 0, "Expected count");
        tt.textLeft(4, 0, "Expected prob");

        int off = dv.isFirstUsed() ? 0 : 1;
        for (int i = 0; i < dv.rowCount() - off; i++) {
            tt.textRight(0, i + 1, dv.level(i + off));
            tt.textRight(1, i + 1, new String(new char[dv.level(i + off).length()]).replace("\0", "-"));
            tt.floatFlex(2, i + 1, dv.get(i + off));
            tt.floatFlex(3, i + 1, expected.getDouble(i));
            tt.floatFlex(4, i + 1, p.getDouble(i));
        }

        sb.append(tt.getDynamicText());
        sb.append("\n");

        return sb.toString();
    }
}
