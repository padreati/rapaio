/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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
import rapaio.core.tools.DTable;
import rapaio.core.tools.DVector;
import rapaio.data.NumericVar;
import rapaio.data.Var;
import rapaio.printer.Printable;
import rapaio.sys.WS;

import java.util.Arrays;

/**
 * Offers tools for chi-square based hypothesis testing.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/10/15.
 */
public abstract class ChiSquareTest implements Printable {

    public static ChiSquareTest goodnessOfFitTest(Var x, double... p) {
        DVector dv = buildDv(x);
        NumericVar expected = Arrays.stream(p).map(pi -> pi * dv.sum()).boxed().collect(NumericVar.collector());
        return new GoodnessOfFit(dv, expected);
    }

    public static ChiSquareTest goodnessOfFitTest(DVector dv, double... p) {
        NumericVar expected = Arrays.stream(p).map(pi -> pi * dv.sum()).boxed().collect(NumericVar.collector());
        return new GoodnessOfFit(dv, expected);
    }

    /**
     * Tests the independence of given discrete random variables
     *
     * @param x first random variable
     * @param y second random variable
     * @return result object
     */
    public static ChiSquareTest independenceTest(Var x, Var y) {
        return new Independence(DTable.fromCounts(x, y, false));
    }

    public static ChiSquareTest independenceTest(DTable dt) {
        return new Independence(dt);
    }

    public abstract int df();

    public abstract double pValue();

    public abstract double chiValue();

    private static DVector buildDv(Var x) {
        switch (x.getType()) {
            case BINARY:
            case NOMINAL:
            case ORDINAL:
                return DVector.fromCount(false, x);
            case NUMERIC:
            case INDEX:
                DVector dv = DVector.empty(true, x.getRowCount());
                for (int i = 0; i < x.getRowCount(); i++) {
                    dv.set(i, x.getValue(i));
                }
                return dv;
            default:
                throw new IllegalArgumentException("variable of give type could not be " +
                        "used to build discrete observed counts");
        }
    }
}


class GoodnessOfFit extends ChiSquareTest {

    public static GoodnessOfFit fromCountAndExpected(Var o, NumericVar e) {

        // degrees of freedom
        DVector dv = DVector.fromCount(false, o);
        if (e.getRowCount() != dv.getRowCount()) {
            throw new IllegalArgumentException("number of expected value elements is not the same as number of levels");
        }
        return new GoodnessOfFit(dv, e);
    }

    private DVector dv;
    private final int df; // degrees of freedom
    private final double chiValue; // chi-square statistic's value
    private final double pValue;

    public GoodnessOfFit(DVector dv, NumericVar expected) {
        if (dv.getRowCount() - dv.start() != expected.getRowCount()) {
            throw new IllegalArgumentException("Different degrees of freedom!");
        }

        this.dv = dv;
        this.df = expected.getRowCount() - 1;
        if (df <= 0) {
            throw new IllegalArgumentException("should be over 0");
        }

        double sum = 0;
        for (int i = dv.start(); i < dv.getRowCount(); i++) {
            double o = dv.get(i);
            double e = expected.getValue(i - dv.start());

            if (Math.abs(e) < 1e-50) {
                sum += Double.POSITIVE_INFINITY;
                break;
            }
            if (Math.abs(e) < 1e-50 && Math.abs(o - e) < 1e50) {
                continue;
            }
            sum += Math.pow(o - e, 2) / expected.getValue(i - dv.start());
        }
        chiValue = sum;
        pValue = 1.0 - new ChiSquare(df).cdf(chiValue);
    }

    public int df() {
        return df;
    }

    public double chiValue() {
        return chiValue;
    }

    public double pValue() {
        return pValue;
    }

    @Override
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n> ChiSquareTest.goodness\n");
        sb.append("\n");
        sb.append("Chi-squared test for given probabilities (goodness of fit)\n");
        sb.append("\n");
        sb.append("data:  \n");
        sb.append(dv.getSummary());
        sb.append("TODO\n\n");
//        sb.append(dv.getSummary()).append("\n");
        sb.append("X-squared = ").append(WS.formatFlex(chiValue))
                .append(", df = ").append(df)
                .append(", p-value = ").append(pValue)
                .append("\n");
        return sb.toString();
    }
}

/**
 * Implements goodness of fit test with chi-square distribution.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/9/15.
 */
class Independence extends ChiSquareTest {

    private final DTable dt;
    private final int df; // degrees of freedom
    private final double chiValue; // chi-square statistic's value
    private final double pValue;

    public Independence(DTable dt) {
        this.dt = dt;
        df = (dt.getRowCount() - 1 - dt.start()) * (dt.getgetColCount() - 1 - dt.start());

        double[] rowTotals = dt.rowTotals();
        double[] colTotals = dt.colTotals();
        double total = Arrays.stream(rowTotals).sum();

        double sum = 0.0;
        for (int i = dt.start(); i < dt.getRowCount(); i++) {
            for (int j = dt.start(); j < dt.getgetColCount(); j++) {
                double expected = rowTotals[i] * colTotals[j] / total;
                sum += Math.pow(dt.get(i, j) - expected, 2) / expected;
            }
        }
        chiValue = sum;
        pValue = 1.0 - new ChiSquare(df).cdf(sum);
    }

    public int df() {
        return df;
    }

    public double chiValue() {
        return chiValue;
    }

    public double pValue() {
        return pValue;
    }

    @Override
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n> ChiSquareTest.independence \n");
        sb.append("\n");
        sb.append("        Pearson’s Chi-squared test \n");
        sb.append("\n");
        sb.append("data:  \n");
        sb.append(dt.getSummary()).append("\n");
        sb.append("X-squared = ").append(WS.formatFlex(chiValue))
                .append(", df = ").append(df)
                .append(", p-value = ").append(pValue)
                .append("\n");
        return sb.toString();
    }
}

/*
        Pearson’s Chi-squared test

data:  ctbl
X-squared = 3.2328, df = 3, p-value = 0.3571
 */
