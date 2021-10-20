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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static rapaio.math.linear.Algebra.*;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import rapaio.math.MathTools;
import rapaio.math.linear.dense.DMatrixDenseC;
import rapaio.math.linear.dense.DMatrixDenseR;
import rapaio.math.linear.dense.DVectorDense;
import rapaio.math.linear.dense.DVectorMap;
import rapaio.math.linear.dense.DVectorStride;
import rapaio.util.collection.IntArrays;

public class DMatrixTest {

    private static final MType[] mTypes = new MType[] {MType.RDENSE, MType.CDENSE, MType.MAP};
    private static final VType[] vTypes = new VType[] {VType.DENSE, VType.STRIDE, VType.MAP};

    public DMatrix newMatrix(MType type) {
        double[] values = new double[100];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                values[i * 10 + j] = i + j + 1;
            }
        }
        return switch (type) {
            case RDENSE -> new DMatrixDenseR(10, 10, values);
            case CDENSE -> new DMatrixDenseC(10, 10, values);
            case MAP -> new DMatrixDenseR(10, 10, values)
                    .mapRows(IntArrays.newSeq(0, 10))
                    .mapCols(IntArrays.newSeq(0, 10))
                    .mapRows(IntArrays.newSeq(0, 10));
        };
    }

    public DVector newSeq(VType type) {
        switch (type) {
            case DENSE -> {
                double[] values = new double[20];
                for (int i = 0; i < 10; i++) {
                    values[i + 10] = i + 1;
                }
                return new DVectorDense(10, 10, values);
            }
            case STRIDE -> {
                double[] values = new double[30];
                for (int i = 0; i < 10; i++) {
                    values[10 + i * 2] = i + 1;
                }
                return new DVectorStride(10, 10, 2, values);
            }
            case MAP -> {
                double[] values = new double[30];
                int[] indexes = new int[10];
                for (int i = 0; i < 10; i++) {
                    values[10 + i * 2] = i + 1;
                    indexes[i] = 10 + i * 2;
                }
                return new DVectorMap(new DVectorDense(0, 10, values), indexes);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    @Test
    void testOneMatrix() {
        for (MType mType : mTypes) {

            DMatrix m = newMatrix(mType);
            assertTrue(m.add(10, copy()).deepEquals(m.add(10)));
            m = newMatrix(mType);
            assertTrue(m.sub(10, copy()).deepEquals(m.sub(10)));
            m = newMatrix(mType);
            assertTrue(m.mul(10, copy()).deepEquals(m.mul(10)));
            m = newMatrix(mType);
            assertTrue(m.div(10, copy()).deepEquals(m.div(10)));

            m = newMatrix(mType);
            assertEquals(IntStream.range(0, 10).mapToObj(i -> 2 * i + 1).mapToInt(i -> i).sum(), m.trace());

            m = newMatrix(mType);
            assertTrue(DVector.wrap(IntStream.range(0, 10).mapToDouble(i -> 2.0 * i + 1).toArray()).deepEquals(m.diag()));

            m = newMatrix(mType);
            assertTrue(m.scatter().deepEquals(m.copy().scatter()));

            assertEquals(1000, m.sum());
            assertTrue(m.sum(0).deepEquals(m.sum(1)));
            assertEquals(16.666666666666668, m.variance());
            assertTrue(m.variance(0).deepEquals(m.variance(1)));

            assertTrue(m.mapValues(m.argmax(0), 0).deepEquals(m.amax(0)));
            assertTrue(m.mapValues(m.argmax(1), 1).deepEquals(m.amax(1)));
            assertTrue(m.mapValues(m.argmin(0), 0).deepEquals(m.amin(0)));
            assertTrue(m.mapValues(m.argmin(1), 1).deepEquals(m.amin(1)));

            m = newMatrix(mType);
            assertTrue(m.apply(MathTools::sqrt, Algebra.copy()).deepEquals(m.apply(MathTools::sqrt)));

            m = newMatrix(mType);
            assertTrue(m.t(copy()).t(copy()).deepEquals(m.t().t()));

            m = newMatrix(mType);
            assertEquals(m.sum(), m.valueStream().sum());

            m = newMatrix(mType);
            assertTrue(m.deepEquals(m.resizeCopy(10, 10, 100)));
            assertEquals(m.sum(), m.resizeCopy(19, 19, 0).sum());
            assertEquals(m.rangeCols(0, 5).rangeRows(0, 5).sum(), m.resizeCopy(5, 5, 0).sum());

            assertEquals(mType, m.type());
        }
    }

    @Test
    void testOneMatrixOneVector() {
        for (MType mType : mTypes) {
            for (VType vType : vTypes) {
                DMatrix m = newMatrix(mType);
                DVector v = newSeq(vType);
                assertTrue(m.add(v, 0, copy()).deepEquals(m.add(v, 0)));
                m = newMatrix(mType);
                v = newSeq(vType);
                assertTrue(m.add(v, 1, copy()).deepEquals(m.add(v, 1)));


                m = newMatrix(mType);
                v = newSeq(vType);
                assertTrue(m.mul(v, 0, copy()).deepEquals(m.mul(v, 0)));
                m = newMatrix(mType);
                v = newSeq(vType);
                assertTrue(m.mul(v, 1, copy()).deepEquals(m.mul(v, 1)));

                m = newMatrix(mType);
                v = newSeq(vType);
                assertTrue(m.div(v, 0, copy()).deepEquals(m.div(v, 0)));
                m = newMatrix(mType);
                v = newSeq(vType);
                assertTrue(m.div(v, 1, copy()).deepEquals(m.div(v, 1)));

                m = newMatrix(mType);
                v = newSeq(vType);
                assertTrue(m.dot(v).deepEquals(m.copy().dot(v.copy())));
            }
        }
    }

    @Test
    void testTwoMatrices() {

        for (MType mType1 : mTypes) {
            for (MType mType2 : mTypes) {

                DMatrix m1 = newMatrix(mType1);
                DMatrix m2 = newMatrix(mType2);
                assertTrue(m1.add(m2, copy()).deepEquals(m1.add(m2)));

                m1 = newMatrix(mType1);
                m2 = newMatrix(mType2);
                assertTrue(m1.sub(m2, copy()).deepEquals(m1.sub(m2)));

                m1 = newMatrix(mType1);
                m2 = newMatrix(mType2);
                assertTrue(m1.mul(m2, copy()).deepEquals(m1.mul(m2)));

                m1 = newMatrix(mType1);
                m2 = newMatrix(mType2);
                assertTrue(m1.div(m2, copy()).deepEquals(m1.div(m2)));

                m1 = newMatrix(mType1);
                m2 = newMatrix(mType2);
                assertTrue(m1.dot(m2).deepEquals(m1.copy().dot(m2.copy())));
            }
        }
    }

}
