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
import rapaio.core.tools.DVector;
import rapaio.data.Nominal;
import rapaio.data.Var;
import rapaio.printer.Printable;
import rapaio.sys.WS;

import java.util.Arrays;

/**
 * Implements goodness of fit test with chi-square distribution.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/9/15.
 */
public class ChiSquareGoodnessOfFitTest implements Printable {

    public static ChiSquareGoodnessOfFitTest fromCountAndExpected(Var values, double... expected) {

        // degrees of freedom
        DVector dv = DVector.newFromCount(values);
        if (expected.length != dv.rowCount()) {
            throw new IllegalArgumentException("number of expected value elements is not the same as number of levels");
        }
        return new ChiSquareGoodnessOfFitTest(dv, expected);
    }

    public static ChiSquareGoodnessOfFitTest fromCountAndProb(Var values, double... p) {

        // degrees of freedom
        DVector dv = DVector.newFromCount(values);
        if (p.length != dv.rowCount() - 1) {
            throw new IllegalArgumentException("number of expected value elements is not the same as number of levels");
        }
        double[] expected = Arrays.stream(p).map(pi -> pi * dv.sum(false)).toArray();
        return new ChiSquareGoodnessOfFitTest(dv, expected);
    }

    private final int df; // degrees of freedom
    private final double chiValue; // chi-square statistic's value
    private final double pValue;

    private ChiSquareGoodnessOfFitTest(DVector dv, double... expected) {

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


    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("X-squared = ").append(chiValue).append("\n");
        sb.append("pValue = ").append(WS.formatFlex(pValue)).append("\n");
        return sb.toString();
    }

    public static void main(String[] args) {
        Nominal nom = Nominal.newEmpty();
        for (int i = 0; i < 11; i++) {
            nom.addLabel("Heavy");
        }
        for (int i = 0; i < 189; i++) {
            nom.addLabel("Never");
        }
        for (int i = 0; i < 19; i++) {
            nom.addLabel("Occas");
        }
        for (int i = 0; i < 17; i++) {
            nom.addLabel("Regul");
        }

        ChiSquareGoodnessOfFitTest test = ChiSquareGoodnessOfFitTest.fromCountAndProb(nom, 0.045, 0.795, 0.085, 0.075);
        test.printSummary();
    }
}
