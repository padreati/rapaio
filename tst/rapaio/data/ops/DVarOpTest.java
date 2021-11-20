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
import static rapaio.sys.With.copy;

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
        VarDouble apply1 = x.copy().dVec().apply(v -> v + 1).dVar();
        VarDouble apply2 = x.dVec(copy()).apply(v -> v + 1).dVar();
        VarDouble apply3 = x.dVec(copy()).add(1).dVar();
        VarDouble apply4 = x.dVec(copy()).add(VarDouble.fill(100, 1).dVec()).dVar();

        assertTrue(apply1.deepEquals(apply2));
        assertTrue(apply1.deepEquals(apply3));
        assertTrue(apply1.deepEquals(apply4));

        double sum1 = x.dVec().nansum();
        assertEquals(sum1, x.dVec(copy()).sortValues(true).nansum(), 1e-12);
        assertEquals(sum1, x.dVec(copy()).sortValues(false).nanmean() * 75, 1e-12);
        int[] rows = x.rowsComplete();
        x.dVec().sortIndexes(rows);
        assertEquals(sum1, x.mapRows(rows).dVec().nansum(), TOLERANCE);
        rows = x.rowsComplete();
        x.dVec().sortIndexes(false, rows);
        assertEquals(sum1, x.mapRows(rows).dVec().nansum(), TOLERANCE);
        rows = x.rowsAll();
        x.dVec().sortIndexes(rows);
        assertEquals(sum1, x.mapRows(rows).dVec().nansum(), TOLERANCE);
        rows = x.rowsAll();
        x.dVec().sortIndexes(rows);
        assertEquals(sum1, x.mapRows(rows).dVec().nansum(), TOLERANCE);
    }

    @Test
    void varIntSortedTest() {

        Var x = VarInt.from(100, row -> row % 4 == 0 ? VarInt.MISSING_VALUE : RandomSource.nextInt(100));
        Var apply1 = x.copy();
        apply1.dVec().apply(v -> v + 1);
        Var apply3 = x.copy();
        apply3.dVec().add(1);
        Var apply4 = x.copy();
        apply4.dVec().add(VarDouble.fill(100, 1).dVec());

        assertTrue(apply1.deepEquals(apply3));
        assertTrue(apply1.deepEquals(apply4));

        double sum1 = x.dVec().nansum();
        assertEquals(sum1, x.dVec(copy()).sortValues(true).nansum());
        assertEquals(sum1, x.dVec(copy()).sortValues(false).nanmean() * 75);

        int[] rows = x.rowsComplete();
        x.dVec().sortIndexes(rows);
        assertEquals(sum1, x.mapRows(rows).dVec().nansum(), TOLERANCE);
        rows = x.rowsComplete();
        x.dVec().sortIndexes(false, rows);
        assertEquals(sum1, x.mapRows(rows).dVec().nansum(), TOLERANCE);

        rows = x.rowsAll();
        x.dVec().sortIndexes(rows);
        assertEquals(sum1, x.mapRows(rows).dVec().nansum(), TOLERANCE);
        rows = x.rowsAll();
        x.dVec().sortIndexes(false, rows);
        assertEquals(sum1, x.mapRows(rows).dVec().nansum(), TOLERANCE);
    }

    @Test
    void testDoubleBasicOperations() {

        VarDouble x1 = generateRandomDoubleVariable(10_000, 0.9);
        VarDouble x2 = generateRandomDoubleVariable(10_000, 0.9);
        VarInt x3 = generateRandomIntVariable(10_000, 10, 20, 0.9);
        VarBinary x4 = generateRandomBinaryVariable(10_000, 0.9);

        Var p1 = VarDouble.from(x1.size(), row -> x1.getDouble(row) + x2.getDouble(row));
        assertTrue(p1.deepEquals(x1.copy().dVec().add(x2.dVec()).dVar()));

        Var p2 = VarDouble.from(x1.size(), row -> x1.getDouble(row) + x3.getDouble(row));
        assertTrue(p2.deepEquals(x1.copy().dVec().add(x3.dVec()).dVar()));

        Var p3 = VarDouble.from(x1.size(), row -> x1.getDouble(row) + Math.PI);
        assertTrue(p3.deepEquals(x1.copy().dVec().add(Math.PI).dVar()));

        Var p4 = VarDouble.from(x1.size(), row -> x1.getDouble(row) + x4.getDouble(row));
        assertTrue(p4.deepEquals(x1.copy().dVec().add(x4.dVec()).dVar()));


        Var m1 = VarDouble.from(x1.size(), row -> x1.getDouble(row) - x2.getDouble(row));
        assertTrue(m1.deepEquals(x1.copy().dVec().sub(x2.dVec()).dVar()));

        Var m2 = VarDouble.from(x1.size(), row -> x1.getDouble(row) - x3.getDouble(row));
        assertTrue(m2.deepEquals(x1.copy().dVec().sub(x3.dVec()).dVar()));

        Var m3 = VarDouble.from(x1.size(), row -> x1.getDouble(row) - Math.PI);
        assertTrue(m3.deepEquals(x1.copy().dVec().sub(Math.PI).dVar()));

        Var m4 = VarDouble.from(x1.size(), row -> x1.getDouble(row) - x4.getDouble(row));
        assertTrue(m4.deepEquals(x1.copy().dVec().sub(x4.dVec()).dVar()));


        Var t1 = VarDouble.from(x1.size(), row -> x1.getDouble(row) * x2.getDouble(row));
        assertTrue(t1.deepEquals(x1.copy().dVec().mul(x2.dVec()).dVar()));

        Var t2 = VarDouble.from(x1.size(), row -> x1.getDouble(row) * x3.getDouble(row));
        assertTrue(t2.deepEquals(x1.copy().dVec().mul(x3.dVec()).dVar()));

        Var t3 = VarDouble.from(x1.size(), row -> x1.getDouble(row) * Math.PI);
        assertTrue(t3.deepEquals(x1.copy().dVec().mul(Math.PI).dVar()));

        Var t4 = VarDouble.from(x1.size(), row -> x1.getDouble(row) * x4.getDouble(row));
        assertTrue(t4.deepEquals(x1.copy().dVec().mul(x4.dVec()).dVar()));


        Var d1 = VarDouble.from(x1.size(), row -> x1.getDouble(row) / x2.getDouble(row));
        assertTrue(d1.deepEquals(x1.copy().dVec().div(x2.dVec()).dVar()));

        Var d2 = VarDouble.from(x1.size(), row -> x1.getDouble(row) / x3.getDouble(row));
        assertTrue(d2.deepEquals(x1.copy().dVec().div(x3.dVec()).dVar()));

        Var d3 = VarDouble.from(x1.size(), row -> x1.getDouble(row) / Math.PI);
        assertTrue(d3.deepEquals(x1.copy().dVec().div(Math.PI).dVar()));

        Var d4 = VarDouble.from(x1.size(), row -> x1.getDouble(row) / x4.getDouble(row));
        assertTrue(d4.deepEquals(x1.copy().dVec().div(x4.dVec()).dVar()));
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
        var tp1 = x1.copy();
        tp1.dVec().add(x2.dVec());
        assertTrue(p1.deepEquals(tp1));

        Var p2 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x3.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) + x3.getInt(row);
        });
        var tp2 = x1.copy();
        tp2.dVec().add(x3.dVec());
        assertTrue(p2.deepEquals(tp2));

        Var p3 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) + 17;
        });
        var tp3 = x1.copy();
        tp3.dVec().add(17);
        assertTrue(p3.deepEquals(tp3));

        Var p4 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x4.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) + x4.getInt(row);
        });
        var tp4 = x1.copy();
        tp4.dVec().add(x4.dVec());
        assertTrue(p4.deepEquals(tp4));

        Var m1 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x2.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) - x2.getInt(row);
        });
        var tm1 = x1.copy();
        tm1.dVec().sub(x2.dVec());
        assertTrue(m1.deepEquals(tm1));

        Var m2 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x3.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) - x3.getInt(row);
        });
        var tm2 = x1.copy();
        tm2.dVec().sub(x3.dVec());
        assertTrue(m2.deepEquals(tm2));

        Var m3 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) - 17;
        });
        var tm3 = x1.copy();
        tm3.dVec().sub(17);
        assertTrue(m3.deepEquals(tm3));

        Var m4 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x4.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) - x4.getInt(row);
        });
        var tm4 = x1.copy();
        tm4.dVec().sub(x4.dVec());
        assertTrue(m4.deepEquals(tm4));


        Var t1 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x2.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) * x2.getInt(row);
        });
        var tt1 = x1.copy();
        tt1.dVec().mul(x2.dVec());
        assertTrue(t1.deepEquals(tt1));

        Var t2 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x3.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return (int) Math.rint(x1.getInt(row) * x3.getDouble(row));
        });
        var tt2 = x1.copy();
        tt2.dVec().mul(x3.dVec());
        assertTrue(t2.deepEquals(tt2));

        Var t3 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) * 17;
        });
        var tt3 = x1.copy();
        tt3.dVec().mul(17);
        assertTrue(t3.deepEquals(tt3));

        Var t4 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x4.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) * x4.getInt(row);
        });
        var tt4 = x1.copy();
        tt4.dVec().mul(x4.dVec());
        assertTrue(t4.deepEquals(tt4));


        Var d1 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x2.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return (int) Math.rint(x1.getInt(row) / x2.getDouble(row));
        });
        var td1 = x1.copy();
        td1.dVec().div(x2.dVec());
        assertTrue(d1.deepEquals(td1));

        Var d2 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x3.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return (int) Math.rint(x1.getInt(row) / x3.getDouble(row));
        });
        var td2 = x1.copy();
        td2.dVec().div(x3.dVec());
        assertTrue(d2.deepEquals(td2));

        Var d3 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return (int) Math.rint(x1.getInt(row) / 17.);
        });
        var td3 = x1.copy();
        td3.dVec().div(17);
        assertTrue(d3.deepEquals(td3));
    }

}
