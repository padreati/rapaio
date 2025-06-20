/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.experiment.math.linear;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import rapaio.experiment.math.linear.dense.DMatrixDenseC;
import rapaio.experiment.math.linear.dense.DMatrixDenseR;
import rapaio.experiment.math.linear.dense.DVectorStride;
import rapaio.util.collection.Ints;

public class LinearAlgebraIdentitiesTests {

    private static Random random;

    @BeforeAll
    static void beforeAll() {
        random = new Random(42);
    }

    private static final MatrixFactory[] mFactories = new MatrixFactory[] {
            DMatrixDenseC::random,
            DMatrixDenseR::random,
            (rows, cols) -> DMatrix.random(rows, cols).mapRows(Ints.seq(rows)).mapCols(Ints.seq(cols))
    };

    private static final VectorFactory[] vFactories = new VectorFactory[] {
            DVector::random,
            size -> {
                int offset = 10;
                double[] values = new double[offset + size * 2];
                for (int i = 0; i < size; i++) {
                    values[offset + i * 2] = random.nextDouble();
                }
                return new DVectorStride(10, 2, size, values);
            },
            size -> {
                DVector v = DVector.random(size);
                v = v.map(Ints.seq(0, v.size()));
                return v;
            }
    };

    interface MatrixFactory {
        DMatrix randomMatrix(int rows, int cols);
    }

    interface VectorFactory {
        DVector randomVector(int size);
    }

    @Test
    void additiveAssociationTest() {

        // A*c+B*c = (A+B)*c

        for (MatrixFactory type1 : mFactories) {
            var a = type1.randomMatrix(400, 80);
            for (MatrixFactory type2 : mFactories) {
                var b = type2.randomMatrix(400, 80);
                for (MatrixFactory type3 : mFactories) {
                    var c = type3.randomMatrix(80, 120);
                    assertTrue(a.dot(c).add(b.dot(c)).deepEquals(a.copy().add(b).dot(c)));
                }
            }
        }
    }

//    @Test
    void productTransposeTest() {

        // (A*B)' = B'*A'

        for (MatrixFactory type1 : mFactories) {
            var a = type1.randomMatrix(100, 150);
            for (MatrixFactory type2 : mFactories) {
                var b = type2.randomMatrix(150, 200);

                var x1 = a.dot(b).t();
                var x2 = b.t().dot(a.t());

                assertTrue(x1.deepEquals(x2, 1e-100));
            }
        }
    }

    @Test
    void matrixVectorAdditiveAssociation() {

        // A*v + B*v = (A+B)*v

        for (MatrixFactory mType1 : mFactories) {
            var a = mType1.randomMatrix(400, 80);
            for (MatrixFactory mType2 : mFactories) {
                var b = mType2.randomMatrix(400, 80);
                for (VectorFactory vf : vFactories) {
                    var v = vf.randomVector(80);


                    var v1 = a.dot(v).add(b.dot(v));
                    var v2 = a.copy().add(b).dot(v);
                    assertTrue(v1.deepEquals(v2));
                }
            }
        }
    }


    @Test
    void diagVectorQuadratic() {

        // A'*diag(w)*A = (A'*diag(w)*A)'

        for (MatrixFactory mType1 : mFactories) {
            var a = mType1.randomMatrix(400, 80);
            for (VectorFactory vf : vFactories) {
                var v = vf.randomVector(400);

                var x1 = a.mulNew(v, 1).t().dot(a);
                var x2 = a.t().mulNew(v, 0).dot(a);
                var x3 = a.t().dot(DMatrix.diagonal(v)).dot(a);

                assertTrue(x1.deepEquals(x1.t()));
                assertTrue(x1.deepEquals(x2));
                assertTrue(x1.deepEquals(x3));
            }
        }

    }
}
