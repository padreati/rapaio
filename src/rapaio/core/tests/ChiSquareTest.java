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

package rapaio.core.tests;

import rapaio.core.distributions.ChiSquare;
import rapaio.core.tools.DTable;
import rapaio.core.tools.DVector;
import rapaio.data.Nominal;
import rapaio.data.Var;
import rapaio.printer.Printable;
import rapaio.sys.WS;

import java.util.Arrays;

/**
 * Offers tools for chi-square based hypothesis testing.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/10/15.
 */
public class ChiSquareTest {

    /**
     * Implements goodness of fit test with chi-square distribution.
     * <p>
     * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/9/15.
     */
    public static class GoodnessOfFit implements Printable {

        public static GoodnessOfFit fromCountAndExpected(Var values, double... expected) {

            // degrees of freedom
            DVector dv = DVector.newFromCount(values);
            if (expected.length != dv.rowCount()) {
                throw new IllegalArgumentException("number of expected value elements is not the same as number of levels");
            }
            return new GoodnessOfFit(dv, expected);
        }

        public static GoodnessOfFit fromCountAndProb(Var values, double... p) {

            // degrees of freedom
            DVector dv = DVector.newFromCount(values);
            if (p.length != dv.rowCount() - 1) {
                throw new IllegalArgumentException("number of expected value elements is not the same as number of levels");
            }
            double[] expected = Arrays.stream(p).map(pi -> pi * dv.sum(false)).toArray();
            return new GoodnessOfFit(dv, expected);
        }


        private final int df; // degrees of freedom
        private final double chiValue; // chi-square statistic's value
        private final double pValue;

        private GoodnessOfFit(DVector dv, double... expected) {

            df = expected.length - 1;
            if (df <= 0) {
                throw new IllegalArgumentException("should be over 0");
            }

            double sum = 0;
            for (int i = 0; i < expected.length; i++) {
                double o = dv.get(i + 1);
                double e = expected[i];

                if (Math.abs(e) < 1e-50) {
                    sum += Double.POSITIVE_INFINITY;
                    break;
                }
                if (Math.abs(e) < 1e-50 && Math.abs(o - e) < 1e50) {
                    continue;
                }
                sum += Math.pow(o - e, 2) / expected[i];
            }
            chiValue = sum;

            ChiSquare chi = new ChiSquare(df);
            pValue = 1.0 - chi.cdf(chiValue);
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
        public String summary() {
            StringBuilder sb = new StringBuilder();
            sb.append("X-squared = ").append(chiValue).append("\n");
            sb.append("pValue = ").append(WS.formatFlex(pValue)).append("\n");
            return sb.toString();
        }
    }

    public static class Independence implements Printable {

        public static Independence fromCounts(Var x, Var y) {
            DTable dt = DTable.newFromCounts(x, y, false);
            return new Independence(dt);
        }

        private final DTable dt;
        private final int df; // degrees of freedom
        private final double chiValue; // chi-square statistic's value
        private final double pValue;

        private Independence(DTable dt) {
            this.dt = dt;
            df = (dt.rowCount() - 1 - dt.start()) * (dt.colCount() - 1 - dt.start());

            double[] rowTotals = dt.rowTotals();
            double[] colTotals = dt.colTotals();
            double total = Arrays.stream(rowTotals).sum();

            double sum = 0.0;
            for (int i = dt.start(); i < dt.rowCount(); i++) {
                for (int j = dt.start(); j < dt.colCount(); j++) {
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
        public String summary() {
            return null;
        }
    }
}
