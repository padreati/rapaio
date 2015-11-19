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

package rapaio.math.linear;

import junit.framework.Assert;
import org.junit.Test;
import rapaio.core.distributions.Normal;
import rapaio.util.Util;

public class MatrixMultiplicationTest {

    //    @Test
    public void basicTestMM() {

        RM A = Linear.newRMWrapOf(3, 4,
                2.3, 1.2, 1, 7,
                19, 0, -1, 2,
                2, 3, 4, 5
        );

        RM B = Linear.newRMWrapOf(4, 5,
                1, 2, 3, 4, 5,
                1.1, 12, 23, 4, 15,
                1.2, 2.2, 23, 4, 5,
                1.3, 2.3, 3, 14, 25
        );

        A.printSummary();
        B.printSummary();

        A.dot(B).printSummary();

        MatrixMultiplication.strassen(A, B).printSummary();
    }

    @Test
    public void bigMatricesMM() {

        Normal n = new Normal(0, 2);

        int N = 1000;
        int M = 1000;
        int K = 1000;

        RM A = Linear.newRMFill(N, M, (i, j) -> n.sampleNext());
        RM B = Linear.newRMFill(M, K, (i, j) -> n.sampleNext());

//        A.printSummary();
//        B.printSummary();

        System.out.println("naive: ");
        RM m1 = Util.measure(() -> A.dot(B));
        System.out.println("naive parallel: ");
        RM m2 = Util.measure(() -> MatrixMultiplication.ijkParralel(A, B));

        Assert.assertTrue(m1.isEqual(m2, 1e-20));
    }
}
