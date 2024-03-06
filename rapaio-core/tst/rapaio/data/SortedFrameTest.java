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

import static rapaio.data.RowComparators.*;

import java.io.IOException;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.transform.RefSort;
import rapaio.io.Csv;
import rapaio.util.IntComparator;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class SortedFrameTest {

    private Random random;
    private Frame df;

    @BeforeEach
    void init() throws IOException {
        random = new Random(1);
        df = Csv.instance()
                .quotes.set(false)
                .types.add(VarType.DOUBLE, "z")
                .types.add(VarType.INT, "y")
                .read(SortedFrameTest.class, "sorted-frame.csv");
    }


    @Test
    void testMultipleStressSortedLayers() {
        Var[] vars = new Var[1_000];
        for (int i = 0; i < 1_000; i++) {
            vars[i] = VarDouble.fill(1_000).name("v" + i);
            for (int j = 0; j < 1_000; j++) {
                vars[i].setDouble(j, random.nextDouble());
            }
        }
        Frame sorted = SolidFrame.byVars(1_000, vars);

        for (int i = 0; i < 100; i++) {
            int col = random.nextInt(sorted.varCount());
            boolean asc = random.nextDouble() >= .5;
            sorted = RefSort.by(doubleComparator(sorted.rvar(col), asc)).fitApply(sorted);
        }

        sorted = RefSort.by(doubleComparator(sorted.rvar(0), true)).fitApply(sorted);
        for (int i = 1; i < sorted.rowCount(); i++) {
            assertTrue(sorted.getDouble(i - 1, 0) <= sorted.getDouble(i, 0));
        }

    }

    @Test
    void smokeTest() {
        assertEquals(3, df.varCount());
        assertEquals(4, df.rowCount());

        Frame sort = RefSort.by(labelComparator(df.rvar(0), true)).fitApply(df);
        assertEquals(3, sort.varCount());
        assertEquals(4, sort.rowCount());

        assertNull(sort.rvar("wrong-getCol-name"));
    }

    @Test
    void testSortNominal() {
        Frame sort = RefSort.by(labelComparator(df.rvar(0), true)).fitApply(df);
        for (int i = 1; i < sort.rowCount(); i++) {
            String label1 = sort.getLabel(i - 1, 0);
            String label2 = sort.getLabel(i, 0);
            assertTrue(label1.compareTo(label2) <= 0);
        }

        sort = RefSort.by(labelComparator(df.rvar(0), false)).fitApply(df);
        for (int i = 1; i < sort.rowCount(); i++) {
            String label1 = sort.getLabel(i - 1, 0);
            String label2 = sort.getLabel(i, 0);
            assertTrue(label1.compareTo(label2) >= 0);
        }
    }

    @Test
    void testSortNumeric() {
        for (int col = 1; col <= 2; col++) {
            Frame sort = RefSort.by(doubleComparator(df.rvar(col), true)).fitApply(df);
            for (int i = 1; i < sort.rowCount(); i++) {
                assertTrue(sort.getDouble(i - 1, col) <= sort.getDouble(i, col));
            }

            sort = RefSort.by(doubleComparator(df.rvar(col), false)).fitApply(df);
            for (int i = 1; i < sort.rowCount(); i++) {
                assertTrue(sort.getDouble(i - 1, col) >= sort.getDouble(i, col));
            }
        }
    }

    @Test
    void testCols() {
        Frame sorted = RefSort.by(labelComparator(df.rvar(0), true)).fitApply(df);

        assertEquals(df.varCount(), sorted.varCount());
        for (int i = 0; i < df.varCount(); i++) {
            assertEquals(df.varNames()[i], sorted.varNames()[i]);
        }
        assertEquals(df.varNames().length, sorted.varNames().length);
        for (int i = 0; i < df.varNames().length; i++) {
            assertEquals(df.varNames()[i], sorted.varNames()[i]);
            assertEquals(df.varIndex(df.varNames()[i]), sorted.varIndex(sorted.varNames()[i]));
            assertEquals(df.varNames()[i], sorted.varNames()[i]);
            assertEquals(df.rvar(df.varNames()[i]).type().isNominal(), sorted.rvar(sorted.varNames()[i]).type().isNominal());
        }
    }

    @Test
    void testMultipleSortedLayers() {
        Frame sorted = df;

        for (int i = 0; i < 10_000; i++) {
            int col = random.nextInt(sorted.varCount());
            boolean asc = random.nextDouble() >= .5;
            IntComparator comp = sorted.rvar(col).type().isNominal() ?
                    labelComparator(sorted.rvar(0), asc) :
                    doubleComparator(sorted.rvar(0), asc);
            sorted = RefSort.by(comp).fitApply(sorted);
        }

        sorted = RefSort.by(labelComparator(sorted.rvar("x"), true)).fitApply(sorted);

        for (int i = 0; i < sorted.rowCount() - 1; i++) {
            assertTrue(sorted.getLabel(i, "x").compareTo(sorted.getLabel(i + 1, "x")) <= 0);
        }
    }
}
