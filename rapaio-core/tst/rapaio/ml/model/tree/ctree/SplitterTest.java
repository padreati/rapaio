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

package rapaio.ml.model.tree.ctree;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.transform.VarApply;
import rapaio.ml.model.tree.RowPredicate;
import rapaio.util.Pair;

/**
 * Tests splitters implementations for CTree
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/29/15.
 */
public class SplitterTest {

    private Frame df;
    private Var w;
    private Candidate c;

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
        VarDouble values = VarDouble.wrap(1, 2, 3, 4, Double.NaN, Double.NaN, Double.NaN, -3, -2, -1);
        df = SolidFrame.byVars(values.copy().name("x"));
        w = values.fapply(VarApply.onDouble(x -> Double.isNaN(x) ? 1 : Math.abs(x))).name("w");
        c = new Candidate(1, "test");
        c.addGroup(RowPredicate.numGreater("x", 0));
        c.addGroup(RowPredicate.numLess("x", 0));
    }

    @Test
    void testIgnored() {
        Pair<List<Frame>, List<Var>> pairs = Splitter.Ignore.performSplit(df, w, c.groupPredicates(), random);
        assertEquals(2, pairs.v1.size());
        assertEquals(2, pairs.v2.size());

        assertEquals(4, pairs.v1.get(0).stream().filter(s -> s.getDouble("x") > 0).count());
        assertEquals(4, pairs.v2.get(0).stream().filter(s -> s.getDouble() > 0).count());

        assertEquals(3, pairs.v1.get(1).stream().filter(s -> s.getDouble("x") < 0).count());
        assertEquals(3, pairs.v2.get(1).stream().filter(s -> s.getDouble() > 0).count());
    }

    @Test
    void testMajority() {
        Pair<List<Frame>, List<Var>> pairs = Splitter.Majority.performSplit(df, w, c.groupPredicates(), random);

        assertEquals(2, pairs.v1.size());
        assertEquals(2, pairs.v2.size());

        assertEquals(7, pairs.v1.get(0).stream().filter(s -> s.isMissing() || s.getDouble("x") > 0).count());
        assertEquals(7, pairs.v2.get(0).stream().filter(s -> s.isMissing() || s.getDouble() > 0).count());

        assertEquals(3, pairs.v1.get(1).stream().filter(s -> s.getDouble("x") < 0).count());
        assertEquals(3, pairs.v2.get(1).stream().filter(s -> s.getDouble() > 0).count());
    }

    @Test
    void testToAllWeighted() {
        Pair<List<Frame>, List<Var>> pairs = Splitter.Weighted.performSplit(df, w, c.groupPredicates(), random);

        assertEquals(2, pairs.v1.size());
        assertEquals(2, pairs.v2.size());

        assertEquals(7, pairs.v1.get(0).stream().filter(s -> s.isMissing() || s.getDouble("x") > 0).count());
        assertEquals(7, pairs.v2.get(0).stream().filter(s -> s.isMissing() || s.getDouble() > 0).count());

        assertEquals(6, pairs.v1.get(1).stream().filter(s -> s.isMissing() || s.getDouble("x") < 0).count());
        assertEquals(6, pairs.v2.get(1).stream().filter(s -> s.isMissing() || s.getDouble() > 0).count());

        assertEquals(1 + 2 + 3 + 4 + 3 * 10 / 16., pairs.v2.get(0).stream().mapToDouble().sum(), 1e-20);
        assertEquals(1 + 2 + 3 + 3 * 6 / 16., pairs.v2.get(1).stream().mapToDouble().sum(), 1e-20);
    }

    @Test
    void testToRandom() {
        Pair<List<Frame>, List<Var>> pairs = Splitter.Random.performSplit(df, w, c.groupPredicates(), random);

        assertEquals(2, pairs.v1.size());
        assertEquals(2, pairs.v2.size());

        long firstCount1 = pairs.v1.get(0).stream().filter(s -> s.isMissing() || s.getDouble("x") > 0).count();
        assertTrue(4 <= firstCount1);
        assertTrue(7 >= firstCount1);
        long firstCount2 = pairs.v1.get(1).stream().filter(s -> s.isMissing() || s.getDouble("x") < 0).count();
        assertTrue(3 <= firstCount2);
        assertTrue(6 >= firstCount2);

        long secondCount1 = pairs.v2.get(0).stream().count();
        assertTrue(4 <= secondCount1);
        assertTrue(7 >= secondCount1);
        long secondCount2 = pairs.v2.get(1).stream().count();
        assertTrue(3 <= secondCount2);
        assertTrue(6 >= secondCount2);
    }
}
