/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.ml.classifier.tree.ctree;

import org.junit.Before;
import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.ml.classifier.tree.CTree;
import rapaio.ml.classifier.tree.CTreeMissingHandler;
import rapaio.util.Pair;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests splitters implementations for CTree
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/29/15.
 */
public class CTreeMissingHandlerTest {

    private Frame df;
    private Var w;
    private CTree.Candidate c;

    @Before
    public void setUp() throws Exception {
        Numeric values = Numeric.newWrapOf(1, 2, 3, 4, Double.NaN, Double.NaN, Double.NaN, -3, -2, -1);
        df = SolidFrame.newWrapOf(values.solidCopy().withName("x"));
        w = values.solidCopy().stream().transValue(x -> Double.isNaN(x) ? x : Math.abs(x)).toMappedVar().withName("w");
        c = new CTree.Candidate(1, "test");
        c.addGroup("> 0", s -> s.value("x") > 0);
        c.addGroup("< 0", s -> s.value("x") < 0);
    }

    @Test
    public void testIgnored() {
        Pair<List<Frame>, List<Var>> pairs = CTreeMissingHandler.Ignored.get().performSplit(df, w, c);
        assertEquals(2, pairs._1.size());
        assertEquals(2, pairs._2.size());

        assertEquals(4, pairs._1.get(0).stream().filter(s -> s.value("x") > 0).count());
        assertEquals(4, pairs._2.get(0).stream().filter(s -> s.value() > 0).count());

        assertEquals(3, pairs._1.get(1).stream().filter(s -> s.value("x") < 0).count());
        assertEquals(3, pairs._2.get(1).stream().filter(s -> s.value() > 0).count());
    }

    @Test
    public void testMajority() {
        Pair<List<Frame>, List<Var>> pairs = CTreeMissingHandler.ToMajority.get().performSplit(df, w, c);

        assertEquals(2, pairs._1.size());
        assertEquals(2, pairs._2.size());

        assertEquals(7, pairs._1.get(0).stream().filter(s -> s.missing() || s.value("x") > 0).count());
        assertEquals(7, pairs._2.get(0).stream().filter(s -> s.missing() || s.value() > 0).count());

        assertEquals(3, pairs._1.get(1).stream().filter(s -> s.value("x") < 0).count());
        assertEquals(3, pairs._2.get(1).stream().filter(s -> s.value() > 0).count());
    }

    @Test
    public void testToAllWeighted() {
        Pair<List<Frame>, List<Var>> pairs = CTreeMissingHandler.ToAllWeighted.get().performSplit(df, w, c);

        assertEquals(2, pairs._1.size());
        assertEquals(2, pairs._2.size());

        assertEquals(7, pairs._1.get(0).stream().filter(s -> s.missing() || s.value("x") > 0).count());
        assertEquals(7, pairs._2.get(0).stream().filter(s -> s.missing() || s.value() > 0).count());

        assertEquals(6, pairs._1.get(1).stream().filter(s -> s.missing() || s.value("x") < 0).count());
        assertEquals(6, pairs._2.get(1).stream().filter(s -> s.missing() || s.value() > 0).count());

        assertEquals(1 + 2 + 3 + 4 + 3 * 4 / 7.0, pairs._2.get(0).stream().mapToDouble().sum(), 1e-20);
        assertEquals(1 + 2 + 3 + 3 * 3 / 7.0, pairs._2.get(1).stream().mapToDouble().sum(), 1e-20);
    }

    @Test
    public void testToRandom() {
        Pair<List<Frame>, List<Var>> pairs = CTreeMissingHandler.ToRandom.get().performSplit(df, w, c);

        df.printLines();

        assertEquals(2, pairs._1.size());
        assertEquals(2, pairs._2.size());

        long firstCount1 = pairs._1.get(0).stream().filter(s -> s.missing() || s.value("x") > 0).count();
        assertTrue(4 <= firstCount1);
        assertTrue(7 >= firstCount1);
        long firstCount2 = pairs._1.get(1).stream().filter(s -> s.missing() || s.value("x") < 0).count();
        assertTrue(3 <= firstCount2);
        assertTrue(6 >= firstCount2);

        long secondCount1 = pairs._2.get(0).stream().count();
        assertTrue(4 <= secondCount1);
        assertTrue(7 >= secondCount1);
        long secondCount2 = pairs._2.get(1).stream().count();
        assertTrue(3 <= secondCount2);
        assertTrue(6 >= secondCount2);
    }
}
