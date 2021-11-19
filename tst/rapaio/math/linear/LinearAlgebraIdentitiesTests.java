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

package rapaio.math.linear;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static rapaio.sys.With.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.math.linear.dense.DVectorStride;
import rapaio.util.collection.IntArrays;

public class LinearAlgebraIdentitiesTests {

    private static final MType[] mTypes = new MType[] {
            MType.RDENSE, MType.CDENSE, MType.MAP
    };

    private static final VectorFactory[] vFactories = new VectorFactory[] {
            DVector::random,
            size -> {
                int offset = 10;
                double[] values = new double[offset + size * 2];
                for (int i = 0; i < size; i++) {
                    values[offset + i*2] = RandomSource.nextDouble();
                }
                return new DVectorStride(10, size, 2, values);
            },
            size -> {
                DVector v = DVector.random(size);
                v = v.map(IntArrays.newSeq(0, v.size()));
                return v;
            }
    };

    interface VectorFactory {
        DVector randomVector(int size);
    }

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(42);
    }

    DMatrix randomMatrix(MType type, int rows, int cols) {
        DMatrix m = DMatrix.random(DMatrix.defaultMType(), rows, cols);
        if (type == MType.MAP) {
            m = m.mapRows(IntArrays.newSeq(0, m.rowCount())).mapCols(IntArrays.newSeq(0, m.colCount()));
        }
        return m;
    }

    @Test
    void additiveAssociationTest() {

        // A*C+B*C = (A+B)*C

        for (MType type1 : mTypes) {
            var a = randomMatrix(type1, 400, 80);
            for (MType type2 : mTypes) {
                var b = randomMatrix(type2, 400, 80);
                for (MType type3 : mTypes) {
                    var c = randomMatrix(type3, 80, 120);
                    assertTrue(a.dot(c).add(b.dot(c)).deepEquals(a.copy().add(b).dot(c)));
                }
            }
        }
    }

    @Test
    void productTransposeTest() {

        // (A*B)' = B'*A'

        for (MType type1 : mTypes) {
            var a = randomMatrix(type1, 100, 150);
            for (MType type2 : mTypes) {
                var b = randomMatrix(type2, 150, 200);

                var x1 = a.dot(b).t();
                var x2 = b.t().dot(a.t());

                assertTrue(x1.deepEquals(x2, 1e-100));
            }
        }
    }

    @Test
    void matrixVectorAdditiveAssociation() {

        // A*v + B*v = (A+B)*v

        for (MType mType1 : mTypes) {
            var a = randomMatrix(mType1, 400, 80);
            for (MType mType2 : mTypes) {
                var b = randomMatrix(mType2, 400, 80);
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

        for (MType mType1 : mTypes) {
            var a = randomMatrix(mType1, 400, 80);
            for (VectorFactory vf : vFactories) {
                var v = vf.randomVector(400);

                var x1 = a.mul(v, 1, copy()).t().dot(a);
                var x2 = a.t().mul(v, 0, copy()).dot(a);
                var x3 = a.t().dot(DMatrix.diagonal(v)).dot(a);

                assertTrue(x1.deepEquals(x1.t()));
                assertTrue(x1.deepEquals(x2));
                assertTrue(x1.deepEquals(x3));
            }
        }

    }
}
