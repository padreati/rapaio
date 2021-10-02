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

package rapaio.data.ops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static rapaio.DataTestingTools.generateRandomBinaryVariable;
import static rapaio.DataTestingTools.generateRandomDoubleVariable;
import static rapaio.DataTestingTools.generateRandomIntVariable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.Var;
import rapaio.data.VarBinary;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/11/19.
 */
public class DVarOpTest {

    private static final double TOLERANCE = 1e-12;
    private Normal normal = Normal.std();

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
    }

    @Test
    void varDoubleSortedTest() {

        VarDouble x = VarDouble.from(100, row -> row % 4 == 0 ? Double.NaN : normal.sampleNext());
        VarDouble apply1 = x.copy().op().apply(v -> v + 1);
        VarDouble apply2 = x.op().capply(v -> v + 1);
        VarDouble apply3 = x.copy().op().plus(1);
        VarDouble apply4 = x.copy().op().plus(VarDouble.fill(100, 1));

        assertTrue(apply1.deepEquals(apply2));
        assertTrue(apply1.deepEquals(apply3));
        assertTrue(apply1.deepEquals(apply4));

        double sum1 = x.op().nansum();
        double sum2 = x.copy().op().sort(true).op().nansum();
        double sum3 = x.copy().op().sort(false).op().nanmean() * 75;
        double sum4 = x.copy().op().sort(x.refComparator()).op().nansum();
        double sum5 = x.mapRows(x.op().sortedCompleteRows()).op().nansum();
        double sum6 = x.mapRows(x.op().sortedCompleteRows(false)).op().nansum();
        double sum7 = x.mapRows(x.op().sortedRows()).op().nansum();
        double sum8 = x.mapRows(x.op().sortedRows(false)).op().nansum();

        assertEquals(sum1, sum2, TOLERANCE);
        assertEquals(sum1, sum3, TOLERANCE);
        assertEquals(sum1, sum4, TOLERANCE);
        assertEquals(sum1, sum5, TOLERANCE);
        assertEquals(sum1, sum6, TOLERANCE);
        assertEquals(sum1, sum7, TOLERANCE);
        assertEquals(sum1, sum8, TOLERANCE);
    }

    @Test
    void varIntSortedTest() {

        Var x = VarInt.from(100, row -> row % 4 == 0 ? VarInt.MISSING_VALUE : RandomSource.nextInt(100));
        Var apply1 = x.copy().op().apply(v -> v + 1);
        Var apply3 = x.copy().op().plus(1);
        Var apply4 = x.copy().op().plus(VarDouble.fill(100, 1));

        assertTrue(apply1.deepEquals(apply3));
        assertTrue(apply1.deepEquals(apply4));

        double sum1 = x.op().nansum();
        double sum2 = x.copy().op().sort(true).op().nansum();
        double sum3 = x.copy().op().sort(false).op().nanmean() * 75;
        double sum4 = x.copy().op().sort(x.refComparator()).op().nansum();
        double sum5 = x.mapRows(x.op().sortedCompleteRows()).op().nansum();
        double sum6 = x.mapRows(x.op().sortedCompleteRows(false)).op().nansum();
        double sum7 = x.mapRows(x.op().sortedRows()).op().nansum();
        double sum8 = x.mapRows(x.op().sortedRows(false)).op().nansum();

        assertEquals(sum1, sum2, TOLERANCE);
        assertEquals(sum1, sum3, TOLERANCE);
        assertEquals(sum1, sum4, TOLERANCE);
        assertEquals(sum1, sum5, TOLERANCE);
        assertEquals(sum1, sum6, TOLERANCE);
        assertEquals(sum1, sum7, TOLERANCE);
        assertEquals(sum1, sum8, TOLERANCE);
    }

    @Test
    void testDoubleBasicOperations() {

        VarDouble x1 = generateRandomDoubleVariable(10_000, 0.9);
        VarDouble x2 = generateRandomDoubleVariable(10_000, 0.9);
        VarInt x3 = generateRandomIntVariable(10_000, 10, 20, 0.9);
        VarBinary x4 = generateRandomBinaryVariable(10_000, 0.9);

        Var p1 = VarDouble.from(x1.size(), row -> x1.getDouble(row) + x2.getDouble(row));
        assertTrue(p1.deepEquals(x1.copy().op().plus(x2)));

        Var p2 = VarDouble.from(x1.size(), row -> x1.getDouble(row) + x3.getDouble(row));
        assertTrue(p2.deepEquals(x1.copy().op().plus(x3)));

        Var p3 = VarDouble.from(x1.size(), row -> x1.getDouble(row) + Math.PI);
        assertTrue(p3.deepEquals(x1.copy().op().plus(Math.PI)));

        Var p4 = VarDouble.from(x1.size(), row -> x1.getDouble(row) + x4.getDouble(row));
        assertTrue(p4.deepEquals(x1.copy().op().plus(x4)));


        Var m1 = VarDouble.from(x1.size(), row -> x1.getDouble(row) - x2.getDouble(row));
        assertTrue(m1.deepEquals(x1.copy().op().minus(x2)));

        Var m2 = VarDouble.from(x1.size(), row -> x1.getDouble(row) - x3.getDouble(row));
        assertTrue(m2.deepEquals(x1.copy().op().minus(x3)));

        Var m3 = VarDouble.from(x1.size(), row -> x1.getDouble(row) - Math.PI);
        assertTrue(m3.deepEquals(x1.copy().op().minus(Math.PI)));

        Var m4 = VarDouble.from(x1.size(), row -> x1.getDouble(row) - x4.getDouble(row));
        assertTrue(m4.deepEquals(x1.copy().op().minus(x4)));


        Var t1 = VarDouble.from(x1.size(), row -> x1.getDouble(row) * x2.getDouble(row));
        assertTrue(t1.deepEquals(x1.copy().op().mult(x2)));

        Var t2 = VarDouble.from(x1.size(), row -> x1.getDouble(row) * x3.getDouble(row));
        assertTrue(t2.deepEquals(x1.copy().op().mult(x3)));

        Var t3 = VarDouble.from(x1.size(), row -> x1.getDouble(row) * Math.PI);
        assertTrue(t3.deepEquals(x1.copy().op().mult(Math.PI)));

        Var t4 = VarDouble.from(x1.size(), row -> x1.getDouble(row) * x4.getDouble(row));
        assertTrue(t4.deepEquals(x1.copy().op().mult(x4)));


        Var d1 = VarDouble.from(x1.size(), row -> x1.getDouble(row) / x2.getDouble(row));
        assertTrue(d1.deepEquals(x1.copy().op().divide(x2)));

        Var d2 = VarDouble.from(x1.size(), row -> x1.getDouble(row) / x3.getDouble(row));
        assertTrue(d2.deepEquals(x1.copy().op().divide(x3)));

        Var d3 = VarDouble.from(x1.size(), row -> x1.getDouble(row) / Math.PI);
        assertTrue(d3.deepEquals(x1.copy().op().divide(Math.PI)));

        Var d4 = VarDouble.from(x1.size(), row -> x1.getDouble(row) / x4.getDouble(row));
        assertTrue(d4.deepEquals(x1.copy().op().divide(x4)));
    }


    @Test
    void testIntBasicOperations() {

        VarInt x1 = generateRandomIntVariable(10_000, 10, 100, 0.9);
        VarInt x2 = generateRandomIntVariable(10_000, 10, 20, 0.9);
        VarDouble x3 = generateRandomDoubleVariable(10_000, 0.9);
        VarBinary x4 = generateRandomBinaryVariable(10_000, 0.9);

        Var p1 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x2.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) + x2.getInt(row);
        });
        assertTrue(p1.deepEquals(x1.copy().op().plus(x2)));

        Var p2 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x3.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) + x3.getInt(row);
        });
        assertTrue(p2.deepEquals(x1.copy().op().plus(x3)));

        Var p3 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) + 17;
        });
        assertTrue(p3.deepEquals(x1.copy().op().plus(17)));

        Var p4 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x4.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) + x4.getInt(row);
        });
        assertTrue(p4.deepEquals(x1.copy().op().plus(x4)));


        Var m1 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x2.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) - x2.getInt(row);
        });
        assertTrue(m1.deepEquals(x1.copy().op().minus(x2)));

        Var m2 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x3.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) - x3.getInt(row);
        });
        assertTrue(m2.deepEquals(x1.copy().op().minus(x3)));

        Var m3 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) - 17;
        });
        assertTrue(m3.deepEquals(x1.copy().op().minus(17)));

        Var m4 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x4.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) - x4.getInt(row);
        });
        assertTrue(m4.deepEquals(x1.copy().op().minus(x4)));


        Var t1 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x2.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) * x2.getInt(row);
        });
        assertTrue(t1.deepEquals(x1.copy().op().mult(x2)));

        Var t2 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x3.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return (int) Math.rint(x1.getInt(row) * x3.getDouble(row));
        });
        assertTrue(t2.deepEquals(x1.copy().op().mult(x3)));

        Var t3 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) * 17;
        });
        assertTrue(t3.deepEquals(x1.copy().op().mult(17)));

        Var t4 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x4.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) * x4.getInt(row);
        });
        assertTrue(t4.deepEquals(x1.copy().op().mult(x4)));


        Var d1 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x2.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return (int) Math.rint(x1.getInt(row) / x2.getDouble(row));
        });
        assertTrue(d1.deepEquals(x1.copy().op().divide(x2)));

        Var d2 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x3.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return (int) Math.rint(x1.getInt(row) / x3.getDouble(row));
        });
        assertTrue(d2.deepEquals(x1.copy().op().divide(x3)));

        Var d3 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return (int) Math.rint(x1.getInt(row) / 17.);
        });
        assertTrue(d3.deepEquals(x1.copy().op().divide(17)));

//        Var d4 = VarInt.from(x1.rowCount(), row -> x1.getInt(row) / x4.getInt(row));
//        assertTrue(d4.deepEquals(x1.copy().op().divide(x4)));
    }

}
