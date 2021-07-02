/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.core.tests;

import org.junit.jupiter.api.Test;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.math.linear.DMatrix;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChiSqTest {

    private static final double TOL = 1e-4;

    @Test
    void testBasicGoodnessOfFit() {
        VarNominal x1 = VarNominal.empty();
        for (int i = 0; i < 11; i++) {
            x1.addLabel("Heavy");
        }
        for (int i = 0; i < 189; i++) {
            x1.addLabel("Never");
        }
        for (int i = 0; i < 19; i++) {
            x1.addLabel("Occas");
        }
        for (int i = 0; i < 17; i++) {
            x1.addLabel("Regul");
        }

        ChiSqGoodnessOfFit test1 = ChiSqGoodnessOfFit.from(x1, VarDouble.wrap(0.045, 0.795, 0.085, 0.075));
        test1.printSummary();

        assertEquals(3.0, test1.df(), 1e-20);
        assertEquals(0.10744287054977643, test1.getChiValue(), 1e-20);
        assertEquals(0.9909295319532134, test1.pValue(), 1e-20);

        test1 = ChiSqGoodnessOfFit.from(VarDouble.copy(11, 189, 19, 17), VarDouble.wrap(0.045, 0.795, 0.085, 0.075));
        test1.printSummary();

        assertEquals(3.0, test1.df(), 1e-20);
        assertEquals(0.10744287054977643, test1.getChiValue(), 1e-20);
        assertEquals(0.9909295319532134, test1.pValue(), 1e-20);

        VarNominal x2 = VarNominal.empty();
        for (int i = 0; i < 54; i++) {
            x2.addLabel("Male");
        }
        for (int i = 0; i < 46; i++) {
            x2.addLabel("Female");
        }
        ChiSqGoodnessOfFit test2 = ChiSqGoodnessOfFit.from(x2, VarDouble.wrap(0.5, 0.5));
        test2.printSummary();

        assertEquals(1, test2.df());
        assertEquals(0.64, test2.getChiValue(), 1e-20);
        assertEquals(0.4237107971667936, test2.pValue(), 1e-20);
    }

    @Test
    void testIndependence() {

        ChiSqIndependence test1 = ChiSqIndependence.from(DMatrix.copy(2, 2, 38, 11, 14, 51), true);
        assertEquals("> ChiSqIndependence\n" +
                "\n" +
                "Pearson's Chi-squared test with Yates' continuity correction\n" +
                "\n" +
                "X-squared = 33.1119728, df = 1, p-value =   8.70e-09\n" +
                "\n" +
                "Observed data:\n" +
                "      C1 C2 total \n" +
                "   R1 38 11   49  \n" +
                "   R2 14 51   65  \n" +
                "total 52 62  114  \n" +
                "\n" +
                "Expected data:\n" +
                "              C1         C2 total \n" +
                "   R1 22.3508772 26.6491228   49  \n" +
                "   R2 29.6491228 35.3508772   65  \n" +
                "total 52         62          114  \n" +
                "\n", test1.toSummary());

        ChiSqIndependence test2 = ChiSqIndependence.from(DMatrix.copy(2, 2, 38, 11, 14, 51), false);
        assertEquals("> ChiSqIndependence\n" +
                "\n" +
                "Pearson's Chi-squared test\n" +
                "\n" +
                "X-squared = 35.3337785, df = 1, p-value =   2.78e-09\n" +
                "\n" +
                "Observed data:\n" +
                "      C1 C2 total \n" +
                "   R1 38 11   49  \n" +
                "   R2 14 51   65  \n" +
                "total 52 62  114  \n" +
                "\n" +
                "Expected data:\n" +
                "              C1         C2 total \n" +
                "   R1 22.3508772 26.6491228   49  \n" +
                "   R2 29.6491228 35.3508772   65  \n" +
                "total 52         62          114  \n" +
                "\n", test2.toSummary());
    }

    @Test
    void testConditionalIndependence() {
        Var status = VarNominal.empty().name("status");
        Var scout = VarNominal.empty().name("scout");
        Var delinquent = VarNominal.empty().name("delinquent");

        for (int i = 0; i < 11; i++) {
            status.addLabel("low");
            scout.addLabel("yes");
            delinquent.addLabel("yes");
        }
        for (int i = 0; i < 43; i++) {
            status.addLabel("low");
            scout.addLabel("yes");
            delinquent.addLabel("no");
        }

        for (int i = 0; i < 42; i++) {
            status.addLabel("low");
            scout.addLabel("no");
            delinquent.addLabel("yes");
        }
        for (int i = 0; i < 169; i++) {
            status.addLabel("low");
            scout.addLabel("no");
            delinquent.addLabel("no");
        }


        for (int i = 0; i < 14; i++) {
            status.addLabel("medium");
            scout.addLabel("yes");
            delinquent.addLabel("yes");
        }
        for (int i = 0; i < 104; i++) {
            status.addLabel("medium");
            scout.addLabel("yes");
            delinquent.addLabel("no");
        }

        for (int i = 0; i < 20; i++) {
            status.addLabel("medium");
            scout.addLabel("no");
            delinquent.addLabel("yes");
        }
        for (int i = 0; i < 132; i++) {
            status.addLabel("medium");
            scout.addLabel("no");
            delinquent.addLabel("no");
        }


        for (int i = 0; i < 8; i++) {
            status.addLabel("high");
            scout.addLabel("yes");
            delinquent.addLabel("yes");
        }
        for (int i = 0; i < 196; i++) {
            status.addLabel("high");
            scout.addLabel("yes");
            delinquent.addLabel("no");
        }

        for (int i = 0; i < 2; i++) {
            status.addLabel("high");
            scout.addLabel("no");
            delinquent.addLabel("yes");
        }
        for (int i = 0; i < 59; i++) {
            status.addLabel("high");
            scout.addLabel("no");
            delinquent.addLabel("no");
        }

        ChiSqConditionalIndependence test = ChiSqConditionalIndependence.from(scout, delinquent, status);
        assertEquals(3, test.getDegrees(), TOL);
        assertEquals(0.1602389, test.getStatistic(), TOL);
        assertEquals(0.983737, test.pValue(), TOL);
    }
}
