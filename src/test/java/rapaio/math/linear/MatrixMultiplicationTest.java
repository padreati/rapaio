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

import org.junit.Test;
import rapaio.core.distributions.Normal;
import rapaio.core.distributions.Uniform;
import rapaio.math.linear.dense.MatrixMultiplication;
import rapaio.math.linear.dense.SolidRM;
import rapaio.util.Util;

import java.util.stream.IntStream;

public class MatrixMultiplicationTest {

    @Test
    public void basicTestMM() {

        RM A = SolidRM.copy(3, 4,
                2.3, 1.2, 1, 7,
                19, 0, -1, 2,
                2, 3, 4, 5
        );

        RM B = SolidRM.copy(4, 5,
                1, 2, 3, 4, 5,
                1.1, 12, 23, 4, 15,
                1.2, 2.2, 23, 4, 5,
                1.3, 2.3, 3, 14, 25
        );

//        A.printSummary();
//        B.printSummary();

        A.dot(B).printSummary();

//        MatrixMultiplication.strassen(A, B).printSummary();
    }

    @Test
    public void largeMatrices() {
        int N = 1_000;
        double p = 0.7;
        RM A = SolidRM.empty(N, N);
        RM B = SolidRM.empty(N, N);

        Normal norm = new Normal(1, 12);
        Uniform unif = new Uniform(0, 1);
        for (int i = 0; i < A.rowCount(); i++) {
            for (int j = 0; j < A.colCount(); j++) {
                if (unif.sampleNext() > p)
                    A.set(i, j, norm.sampleNext());
            }
        }

        for (int i = 0; i < B.rowCount(); i++) {
            for (int j = 0; j < B.colCount(); j++) {
                if (unif.sampleNext() > p)
                    B.set(i, j, norm.sampleNext());
            }
        }

        int[] range = IntStream.range(N - 100, N).toArray();

//        Util.measure(() -> MatrixMultiplication.ijkAlgorithm(A,B).mapRows(range).mapCols(range).printSummary());
//        Util.measure(() -> MatrixMultiplication.ikjAlgorithm(A, B).mapRows(range).mapCols(range).printSummary());
//        Util.measure(() -> MatrixMultiplication.tiledAlgorithm(A, B).mapRows(range).mapCols(range).printSummary());
//        RM C1 = Util.measure(() -> MatrixMultiplication.ijkParallel(A, B).mapRows(range).mapCols(range));
        RM C2 = Util.measure(() -> MatrixMultiplication.ikjParallel(A, B).mapRows(range).mapCols(range));

//        Assert.assertTrue(C1.isEqual(C2));
    }
}
