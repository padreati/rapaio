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

import junit.framework.Assert;
import org.junit.Test;
import rapaio.data.NominalVar;
import rapaio.data.NumericVar;

public class ChiSquareTestTest {

    @Test
    public void testBasicGoodness() {

        NominalVar x1 = NominalVar.empty();
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

        ChiSquareTest test1 = ChiSquareTest.goodnessOfFitTest(x1, 0.045, 0.795, 0.085, 0.075);
        test1.printSummary();

        Assert.assertEquals(3.0, test1.df(), 1e-20);
        Assert.assertEquals(0.10744287054977643, test1.chiValue(), 1e-20);
        Assert.assertEquals(0.9909295319532134, test1.pValue(), 1e-20);

        test1 = ChiSquareTest.goodnessOfFitTest(NumericVar.copy(11, 189, 19, 17), 0.045, 0.795, 0.085, 0.075);
        test1.printSummary();

        Assert.assertEquals(3.0, test1.df(), 1e-20);
        Assert.assertEquals(0.10744287054977643, test1.chiValue(), 1e-20);
        Assert.assertEquals(0.9909295319532134, test1.pValue(), 1e-20);

        NominalVar x2 = NominalVar.empty();
        for (int i = 0; i < 54; i++) {
            x2.addLabel("Male");
        }
        for (int i = 0; i < 46; i++) {
            x2.addLabel("Female");
        }
        ChiSquareTest test2 = ChiSquareTest.goodnessOfFitTest(x2, 0.5, 0.5);
        test2.printSummary();

        Assert.assertEquals(1, test2.df());
        Assert.assertEquals(0.64, test2.chiValue(), 1e-20);
        Assert.assertEquals(0.4237107971667936, test2.pValue(), 1e-20);
    }

//    @Test
//    public void testBasicIndependence() {
//
//        Nominal nom
//    }
}
