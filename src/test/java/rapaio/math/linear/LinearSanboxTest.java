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

package rapaio.math.linear;

import junit.framework.Assert;
import rapaio.core.CoreTools;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.data.filter.var.VFRefSort;
import rapaio.math.linear.dense.SolidRM;
import rapaio.sys.WS;
import rapaio.util.Util;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/7/15.
 */
public class LinearSanboxTest {

//    @Test
    public void eigenValueTest() {
        int len = 10;
        for (int p = 0; p < 100; p++) {

            len = (int)(len *1.1);

            WS.println("case " + len + " size:");
            WS.println("========================");
            RM A = SolidRM.fill(len, len, (i, j) -> CoreTools.distNormal().sampleNext());

//        A = A.t().dot(A);

//        A.printSummary();

            EigenPair ep = Util.measure(() -> Linear.eigenDecompEjml(A, 1_000, 1e-20));
//        ep.values().printSummary();
//        ep.vectors().printSummary();

            EigenPair ep2 = Util.measure(() -> Linear.eigenDecomp(A, 1_000, 1e-20));
//        ep2.values().printSummary();
//        ep2.vectors().printSummary();

            Var x1 = Numeric.newWrap(ep.values().valueStream().toArray());
            Var x2 = Numeric.newWrap(ep2.values().valueStream().toArray());

            x1 = new VFRefSort(x1.refComparator()).fitApply(x1);
            x2 = new VFRefSort(x2.refComparator()).fitApply(x2);


            Assert.assertTrue(x1.deepEquals(x2));

        }
    }

}
