/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.ops;

import static org.junit.jupiter.api.Assertions.*;

import static rapaio.DataTestingTools.*;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Normal;
import rapaio.data.Var;
import rapaio.data.VarBinary;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/11/19.
 */
public class DVarOpTest {

    private static final double TOLERANCE = 1e-12;
    private final Normal normal = Normal.std();

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(123);
    }

    @Test
    void varDoubleSortedTest() {

        VarDouble x = VarDouble.from(100, row -> row % 4 == 0 ? Double.NaN : normal.sampleNext(random));
        VarDouble apply1 = x.copy().dt().apply_(v -> v + 1).dv();
        VarDouble apply2 = x.dtNew().apply_(v -> v + 1).dv();
        VarDouble apply3 = x.dtNew().add_(1.).dv();
        VarDouble apply4 = x.dtNew().add_(VarDouble.fill(100, 1).dt()).dv();

        assertTrue(apply1.deepEquals(apply2));
        assertTrue(apply1.deepEquals(apply3));
        assertTrue(apply1.deepEquals(apply4));

        double sum1 = x.dt().nanSum();
        assertEquals(sum1, x.dtNew().sort_(0, true).nanSum(), 1e-12);
        assertEquals(sum1, x.dtNew().sort_(0, false).nanMean() * 75, 1e-12);
        int[] rows = x.rowsComplete();
        x.dt().indirectSort(rows, true);
        assertEquals(sum1, x.mapRows(rows).dt().nanSum(), TOLERANCE);
        rows = x.rowsComplete();
        x.dt().indirectSort(rows, false);
        assertEquals(sum1, x.mapRows(rows).dt().nanSum(), TOLERANCE);
        rows = x.rowsAll();
        x.dt().indirectSort(rows, true);
        assertEquals(sum1, x.mapRows(rows).dt().nanSum(), TOLERANCE);
        rows = x.rowsAll();
        x.dt().indirectSort(rows, true);
        assertEquals(sum1, x.mapRows(rows).dt().nanSum(), TOLERANCE);
    }

    @Test
    void varIntSortedTest() {

        Var x = VarInt.from(100, row -> row % 4 == 0 ? VarInt.MISSING_VALUE : random.nextInt(100));
        Var apply1 = x.copy();
        apply1.dt().apply_(v -> v + 1);
        Var apply3 = x.copy();
        apply3.dt().add_(1.0);
        Var apply4 = x.copy();
        apply4.dt().add_(VarDouble.fill(100, 1).dt());

        assertTrue(apply1.deepEquals(apply3));
        assertTrue(apply1.deepEquals(apply4));

        double sum1 = x.dt().nanSum();
        assertEquals(sum1, x.dtNew().sort_(0, true).nanSum());
        assertEquals(sum1, x.dtNew().sort_(0, false).nanMean() * 75, 1e-10);

        int[] rows = x.rowsComplete();
        x.dt().indirectSort(rows, true);
        assertEquals(sum1, x.mapRows(rows).dt().nanSum(), TOLERANCE);
        rows = x.rowsComplete();
        x.dt().indirectSort(rows, false);
        assertEquals(sum1, x.mapRows(rows).dt().nanSum(), TOLERANCE);

        rows = x.rowsAll();
        x.dt().indirectSort(rows, true);
        assertEquals(sum1, x.mapRows(rows).dt().nanSum(), TOLERANCE);
        rows = x.rowsAll();
        x.dt().indirectSort(rows, false);
        assertEquals(sum1, x.mapRows(rows).dt().nanSum(), TOLERANCE);
    }

    @Test
    void testDoubleBasicOperations() {

        VarDouble x1 = generateRandomDoubleVariable(10_000, 0.9);
        VarDouble x2 = generateRandomDoubleVariable(10_000, 0.9);
        VarInt x3 = generateRandomIntVariable(10_000, 10, 20, 0.9);
        VarBinary x4 = generateRandomBinaryVariable(10_000, 0.9);

        Var p1 = VarDouble.from(x1.size(), row -> x1.getDouble(row) + x2.getDouble(row));
        assertTrue(p1.deepEquals(x1.copy().dt().add_(x2.dt()).dv()));

        Var p2 = VarDouble.from(x1.size(), row -> x1.getDouble(row) + x3.getDouble(row));
        assertTrue(p2.deepEquals(x1.copy().dt().add_(x3.dt()).dv()));

        Var p3 = VarDouble.from(x1.size(), row -> x1.getDouble(row) + Math.PI);
        assertTrue(p3.deepEquals(x1.copy().dt().add_(Math.PI).dv()));

        Var p4 = VarDouble.from(x1.size(), row -> x1.getDouble(row) + x4.getDouble(row));
        assertTrue(p4.deepEquals(x1.copy().dt().add_(x4.dt()).dv()));


        Var m1 = VarDouble.from(x1.size(), row -> x1.getDouble(row) - x2.getDouble(row));
        assertTrue(m1.deepEquals(x1.copy().dt().sub_(x2.dt()).dv()));

        Var m2 = VarDouble.from(x1.size(), row -> x1.getDouble(row) - x3.getDouble(row));
        assertTrue(m2.deepEquals(x1.copy().dt().sub_(x3.dt()).dv()));

        Var m3 = VarDouble.from(x1.size(), row -> x1.getDouble(row) - Math.PI);
        assertTrue(m3.deepEquals(x1.copy().dt().sub_(Math.PI).dv()));

        Var m4 = VarDouble.from(x1.size(), row -> x1.getDouble(row) - x4.getDouble(row));
        assertTrue(m4.deepEquals(x1.copy().dt().sub_(x4.dt()).dv()));


        Var t1 = VarDouble.from(x1.size(), row -> x1.getDouble(row) * x2.getDouble(row));
        assertTrue(t1.deepEquals(x1.copy().dt().mul_(x2.dt()).dv()));

        Var t2 = VarDouble.from(x1.size(), row -> x1.getDouble(row) * x3.getDouble(row));
        assertTrue(t2.deepEquals(x1.copy().dt().mul_(x3.dt()).dv()));

        Var t3 = VarDouble.from(x1.size(), row -> x1.getDouble(row) * Math.PI);
        assertTrue(t3.deepEquals(x1.copy().dt().mul_(Math.PI).dv()));

        Var t4 = VarDouble.from(x1.size(), row -> x1.getDouble(row) * x4.getDouble(row));
        assertTrue(t4.deepEquals(x1.copy().dt().mul_(x4.dt()).dv()));


        Var d1 = VarDouble.from(x1.size(), row -> x1.getDouble(row) / x2.getDouble(row));
        assertTrue(d1.deepEquals(x1.copy().dt().div_(x2.dt()).dv()));

        Var d2 = VarDouble.from(x1.size(), row -> x1.getDouble(row) / x3.getDouble(row));
        assertTrue(d2.deepEquals(x1.copy().dt().div_(x3.dt()).dv()));

        Var d3 = VarDouble.from(x1.size(), row -> x1.getDouble(row) / Math.PI);
        assertTrue(d3.deepEquals(x1.copy().dt().div_(Math.PI).dv()));

        Var d4 = VarDouble.from(x1.size(), row -> x1.getDouble(row) / x4.getDouble(row));
        assertTrue(d4.deepEquals(x1.copy().dt().div_(x4.dt()).dv()));
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
        tp1.dt().add_(x2.dt());
        assertTrue(p1.deepEquals(tp1));

        Var p2 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x3.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) + x3.getInt(row);
        });
        var tp2 = x1.copy();
        tp2.dt().add_(x3.dt());
        assertTrue(p2.deepEquals(tp2));

        Var p3 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) + 17;
        });
        var tp3 = x1.copy();
        tp3.dt().add_(17.);
        assertTrue(p3.deepEquals(tp3));

        Var p4 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x4.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) + x4.getInt(row);
        });
        var tp4 = x1.copy();
        tp4.dt().add_(x4.dt());
        assertTrue(p4.deepEquals(tp4));

        Var m1 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x2.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) - x2.getInt(row);
        });
        var tm1 = x1.copy();
        tm1.dt().sub_(x2.dt());
        assertTrue(m1.deepEquals(tm1));

        Var m2 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x3.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) - x3.getInt(row);
        });
        var tm2 = x1.copy();
        tm2.dt().sub_(x3.dt());
        assertTrue(m2.deepEquals(tm2));

        Var m3 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) - 17;
        });
        var tm3 = x1.copy();
        tm3.dt().sub_(17.);
        assertTrue(m3.deepEquals(tm3));

        Var m4 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x4.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) - x4.getInt(row);
        });
        var tm4 = x1.copy();
        tm4.dt().sub_(x4.dt());
        assertTrue(m4.deepEquals(tm4));


        Var t1 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x2.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) * x2.getInt(row);
        });
        var tt1 = x1.copy();
        tt1.dt().mul_(x2.dt());
        assertTrue(t1.deepEquals(tt1));

        Var t2 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x3.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return (int) Math.rint(x1.getInt(row) * x3.getDouble(row));
        });
        var tt2 = x1.copy();
        tt2.dt().mul_(x3.dt());
        assertTrue(t2.deepEquals(tt2));

        Var t3 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) * 17;
        });
        var tt3 = x1.copy();
        tt3.dt().mul_(17.);
        assertTrue(t3.deepEquals(tt3));

        Var t4 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x4.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return x1.getInt(row) * x4.getInt(row);
        });
        var tt4 = x1.copy();
        tt4.dt().mul_(x4.dt());
        assertTrue(t4.deepEquals(tt4));


        Var d1 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x2.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return (int) Math.rint(x1.getInt(row) / x2.getDouble(row));
        });
        var td1 = x1.copy();
        td1.dt().div_(x2.dt());
        assertTrue(d1.deepEquals(td1));

        Var d2 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row) || x3.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return (int) Math.rint(x1.getInt(row) / x3.getDouble(row));
        });
        var td2 = x1.copy();
        td2.dt().div_(x3.dt());
        assertTrue(d2.deepEquals(td2));

        Var d3 = VarInt.from(x1.size(), row -> {
            if (x1.isMissing(row)) {
                return VarInt.MISSING_VALUE;
            }
            return (int) Math.rint(x1.getInt(row) / 17.);
        });
        var td3 = x1.copy();
        td3.dt().div_(17.);
        assertTrue(d3.deepEquals(td3));
    }

}
