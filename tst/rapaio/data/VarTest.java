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

package rapaio.data;

import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.data.filter.var.VApplyDouble;
import rapaio.data.filter.var.VRefSort;
import rapaio.data.filter.var.VSort;
import rapaio.data.filter.var.VStandardize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class VarTest {

    @Test
    public void testDictionary() {
        Var x = VarNominal.copy("x", "y", "x", "z");
        Var y = VarNominal.copy("x", "y", "x", "z");

        x.setLevels("a", "b", "c");
        List<String> dict = new ArrayList<>();
        dict.add("a");
        dict.add("b");
        dict.add("c");
        y.setLevels(dict);

        assertEquals(4, x.rowCount());
        assertEquals(4, y.rowCount());

        for (int i = 0; i < 4; i++) {
            assertEquals(x.getLabel(i), y.getLabel(i));
        }
    }

    @Test
    public void testNumericCollector() {
        double[] src = IntStream.range(0, 100_000).mapToDouble(x -> x).toArray();
        Var x = VarDouble.wrap(src);
        Var y = Arrays.stream(src).boxed().parallel().collect(VarDouble.collector());
        y = VSort.asc().fapply(y);

        assertTrue(x.deepEquals(y));
    }

    @Test
    public void testIndexCollector() {
        int[] src = IntStream.range(0, 100_000).toArray();
        Var x = VarInt.wrap(src);
        Var y = Arrays.stream(src).boxed().parallel().collect(VarInt.collector());
        y = VSort.asc().fapply(y);

        assertTrue(x.deepEquals(y));
    }

    @Test
    public void solidCopyNameTest() {
        VarDouble num = VarDouble.seq(1, 10, 0.5).withName("num");
        assertEquals(num.name(), num.solidCopy().name());
        assertEquals(num.name(), num.mapRows(2, 5).solidCopy().name());

        VarInt idx = VarInt.seq(1, 10).withName("idx");
        assertEquals(idx.name(), idx.solidCopy().name());
        assertEquals(idx.name(), idx.mapRows(2, 5).solidCopy().name());

        VarBinary bin = VarBinary.copy(1, 0, 1, 0, 1).withName("bin");
        assertEquals(bin.name(), bin.solidCopy().name());
        assertEquals(bin.name(), bin.mapRows(2, 5).solidCopy().name());

        VarNominal nom = VarNominal.copy("a", "b", "a", "c", "a").withName("nom");
        assertEquals(nom.name(), nom.solidCopy().name());
        assertEquals(nom.name(), nom.mapRows(2, 4).solidCopy().name());

        VarLong stp = VarLong.seq(1, 10).withName("stamp");
        assertEquals(stp.name(), stp.solidCopy().name());
        assertEquals(stp.name(), stp.mapRows(2, 5).solidCopy().name());
    }

    @Test
    public void testFilters() {

        double[] x = IntStream.range(0, 100).mapToDouble(v -> v).toArray();
        double[] log1px = Arrays.stream(x).map(Math::log1p).toArray();

        VarDouble vx = VarDouble.wrap(x);
        Var vlog1px = vx.solidCopy().fapply(VApplyDouble.with(Math::log1p));

        assertTrue(vx.deepEquals(VarDouble.wrap(x)));
        assertTrue(vlog1px.deepEquals(VarDouble.wrap(log1px)));

        VStandardize filter = VStandardize.filter();
        filter.fit(vx);
        Var fit1 = vx.solidCopy().apply(filter);
        Var fit2 = vx.solidCopy().fapply(VStandardize.filter());

        assertTrue(fit1.deepEquals(fit2));
    }

    @Test
    public void testRefComparator() {
        Var varDouble = VarDouble.from(100, RandomSource::nextDouble);
        varDouble = varDouble.fapply(VRefSort.filter(varDouble.refComparator()));
        for (int i = 1; i < varDouble.rowCount(); i++) {
            assertTrue(varDouble.getDouble(i - 1) <= varDouble.getDouble(i));
        }

        Var varLong = VarLong.from(100, row -> (long) RandomSource.nextInt(100));
        varLong = varLong.fapply(VRefSort.filter(varLong.refComparator()));
        for (int i = 1; i < varLong.rowCount(); i++) {
            assertTrue(varLong.getLong(i - 1) <= varLong.getLong(i));
        }

        Var varInt = VarInt.from(100, row -> RandomSource.nextInt(100));
        varInt = varInt.fapply(VRefSort.filter(varInt.refComparator()));
        for (int i = 1; i < varInt.rowCount(); i++) {
            assertTrue(varInt.getInt(i - 1) <= varInt.getInt(i));
        }

        Var varNominal = VarNominal.from(100, row -> String.valueOf(RandomSource.nextInt(100)));
        varNominal = varNominal.fapply(VRefSort.filter(varNominal.refComparator()));
        for (int i = 1; i < varNominal.rowCount(); i++) {
            assertTrue(varNominal.getLabel(i - 1).compareTo(varNominal.getLabel(i)) <= 0);
        }
    }

    @Test
    public void testDeepEquals() {
        assertFalse(VarDouble.scalar(1).withName("x").deepEquals(VarDouble.scalar(1).withName("y")));
        assertFalse(VarDouble.seq(2).withName("x").deepEquals(VarDouble.scalar(1).withName("x")));
        assertFalse(VarDouble.scalar(1).withName("x").deepEquals(VarInt.scalar(1).withName("x")));
        assertFalse(VarDouble.scalar(1).deepEquals(VarDouble.scalar(2)));
        assertFalse(VarNominal.copy("a").deepEquals(VarNominal.copy("b")));
    }
}
