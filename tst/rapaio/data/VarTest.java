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

package rapaio.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.preprocessing.VarApply;
import rapaio.data.preprocessing.VarRefSort;
import rapaio.data.preprocessing.VarSort;
import rapaio.data.preprocessing.VarStandardScaler;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class VarTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    @Test
    void testDictionary() {
        Var x = VarNominal.copy("x", "y", "x", "z");
        Var y = VarNominal.copy("x", "y", "x", "z");

        x.setLevels("a", "b", "c");
        List<String> dict = new ArrayList<>();
        dict.add("a");
        dict.add("b");
        dict.add("c");
        y.setLevels(dict);

        assertEquals(4, x.size());
        assertEquals(4, y.size());

        for (int i = 0; i < 4; i++) {
            assertEquals(x.getLabel(i), y.getLabel(i));
        }
    }

    @Test
    void testNumericCollector() {
        double[] src = IntStream.range(0, 100_000).mapToDouble(x -> x).toArray();
        Var x = VarDouble.wrap(src);
        Var y = Arrays.stream(src).boxed().parallel().collect(VarDouble.collector());
        y = VarSort.ascending().fapply(y);

        assertTrue(x.deepEquals(y));
    }

    @Test
    void testIndexCollector() {
        int[] src = IntStream.range(0, 100_000).toArray();
        Var x = VarInt.wrap(src);
        Var y = Arrays.stream(src).boxed().parallel().collect(VarInt.collector());
        y = VarSort.ascending().fapply(y);

        assertTrue(x.deepEquals(y));
    }

    @Test
    void copyNameTest() {
        VarDouble num = VarDouble.seq(1, 10, 0.5).name("num");
        assertEquals(num.name(), num.copy().name());
        assertEquals(num.name(), num.mapRows(2, 5).copy().name());

        VarInt idx = VarInt.seq(1, 10).name("idx");
        assertEquals(idx.name(), idx.copy().name());
        assertEquals(idx.name(), idx.mapRows(2, 5).copy().name());

        VarBinary bin = VarBinary.copy(1, 0, 1, 0, 1).name("bin");
        assertEquals(bin.name(), bin.copy().name());
        assertEquals(bin.name(), bin.mapRows(2, 5).copy().name());

        VarNominal nom = VarNominal.copy("a", "b", "a", "c", "a").name("nom");
        assertEquals(nom.name(), nom.copy().name());
        assertEquals(nom.name(), nom.mapRows(2, 4).copy().name());

        VarLong stp = VarLong.seq(1, 10).name("stamp");
        assertEquals(stp.name(), stp.copy().name());
        assertEquals(stp.name(), stp.mapRows(2, 5).copy().name());
    }

    @Test
    void testFilters() {

        double[] x = IntStream.range(0, 100).mapToDouble(v -> v).toArray();
        double[] log1px = Arrays.stream(x).map(Math::log1p).toArray();

        VarDouble vx = VarDouble.wrap(x);
        Var vlog1px = vx.copy().fapply(VarApply.onDouble(Math::log1p));

        assertTrue(vx.deepEquals(VarDouble.wrap(x)));
        assertTrue(vlog1px.deepEquals(VarDouble.wrap(log1px)));

        VarStandardScaler filter = VarStandardScaler.filter();
        filter.fit(vx);
        Var fit1 = vx.copy().apply(filter);
        Var fit2 = vx.copy().fapply(VarStandardScaler.filter());

        assertTrue(fit1.deepEquals(fit2));
    }

    @Test
    void testRefComparator() {
        Var varDouble = VarDouble.from(100, () -> random.nextDouble());
        varDouble = varDouble.fapply(VarRefSort.from(varDouble.refComparator()));
        for (int i = 1; i < varDouble.size(); i++) {
            assertTrue(varDouble.getDouble(i - 1) <= varDouble.getDouble(i));
        }

        Var varLong = VarLong.from(100, row -> (long) random.nextInt(100));
        varLong = varLong.fapply(VarRefSort.from(varLong.refComparator()));
        for (int i = 1; i < varLong.size(); i++) {
            assertTrue(varLong.getLong(i - 1) <= varLong.getLong(i));
        }

        Var varInt = VarInt.from(100, row -> random.nextInt(100));
        varInt = varInt.fapply(VarRefSort.from(varInt.refComparator()));
        for (int i = 1; i < varInt.size(); i++) {
            assertTrue(varInt.getInt(i - 1) <= varInt.getInt(i));
        }

        Var varNominal = VarNominal.from(100, row -> String.valueOf(random.nextInt(100)));
        varNominal = varNominal.fapply(VarRefSort.from(varNominal.refComparator()));
        for (int i = 1; i < varNominal.size(); i++) {
            assertTrue(varNominal.getLabel(i - 1).compareTo(varNominal.getLabel(i)) <= 0);
        }
    }

    @Test
    void testDeepEquals() {
        assertFalse(VarDouble.scalar(1).name("x").deepEquals(VarDouble.scalar(1).name("y")));
        assertFalse(VarDouble.seq(2).name("x").deepEquals(VarDouble.scalar(1).name("x")));
        assertFalse(VarDouble.scalar(1).name("x").deepEquals(VarInt.scalar(1).name("x")));
        assertFalse(VarDouble.scalar(1).deepEquals(VarDouble.scalar(2)));
        assertFalse(VarNominal.copy("a").deepEquals(VarNominal.copy("b")));
    }
}
