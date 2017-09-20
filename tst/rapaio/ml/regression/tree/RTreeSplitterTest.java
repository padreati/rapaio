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

package rapaio.ml.regression.tree;

import org.junit.Before;
import org.junit.Test;
import rapaio.core.distributions.Uniform;
import rapaio.data.Frame;
import rapaio.data.NumericVar;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.util.Pair;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class RTreeSplitterTest {

    private static final double TOL = 1e-20;

    private Frame df;
    private Var w;
    private RTree.Candidate candidate;

    @Before
    public void setUp() throws Exception {
        w = NumericVar.empty();
        df = SolidFrame.byVars(NumericVar.empty().withName("x"));

        candidate = new RTree.Candidate(0, "");
        candidate.addGroup("x < 10", s -> s.getValue("x") < 10);
        candidate.addGroup("x > 20", s -> s.getValue("x") > 20);
    }

    private void populate(int group, int count, double weight) {
        Uniform u;
        switch (group) {
            case 0:
                u = new Uniform(0, 9);
                break;
            case 1:
                u = new Uniform(21, 30);
                break;
            default:
                u = new Uniform(11, 19);
        }
        NumericVar sample = u.sample(count);
        for (int i = 0; i < sample.rowCount(); i++) {
            df.addRows(1);
            df.var("x").setValue(df.rowCount() - 1, sample.value(i));
            w.addValue(weight);
        }
    }

    @Test
    public void testIgnoreMissing() {

        RTreeSplitter splitter = RTreeSplitter.REMAINS_IGNORED;
        populate(0, 2, 1);
        populate(1, 2, 2);
        populate(2, 2, 3);
        assertEquals("REMAINS_IGNORED", splitter.name());

        Pair<List<Frame>, List<Var>> result = splitter.performSplit(df, w, candidate.getGroupPredicates());
        assertEquals(2, result._1.size());
        assertEquals(2, result._2.size());

        assertEquals(2, result._1.get(0).rowCount());
        assertEquals(2, result._2.get(0).rowCount());

        assertEquals(2, result._1.get(1).rowCount());
        assertEquals(2, result._2.get(1).rowCount());

        assertEquals(2, result._2.get(0).stream().mapToDouble().sum(), TOL);
        assertEquals(4, result._2.get(1).stream().mapToDouble().sum(), TOL);
    }

    @Test
    public void testRemainsWithMajority() {

        RTreeSplitter splitter = RTreeSplitter.REMAINS_TO_MAJORITY;
        populate(0, 10, 1);
        populate(1, 7, 2);
        populate(2, 2, 3);
        assertEquals("REMAINS_TO_MAJORITY", splitter.name());

        Pair<List<Frame>, List<Var>> result = splitter.performSplit(df, w, candidate.getGroupPredicates());

        // groups
        assertEquals(2, result._1.size());
        assertEquals(2, result._2.size());

        // group 1
        assertEquals(12, result._1.get(0).rowCount());
        assertEquals(12, result._2.get(0).rowCount());
        assertEquals(16, result._2.get(0).stream().mapToDouble().sum(), TOL);

        // group 2
        assertEquals(7, result._2.get(1).rowCount());
        assertEquals(7, result._2.get(1).rowCount());
        assertEquals(14, result._2.get(1).stream().mapToDouble().sum(), TOL);
    }

    @Test
    public void testRemainsToAllWeighted() {
        RTreeSplitter splitter = RTreeSplitter.REMAINS_TO_ALL_WEIGHTED;
        populate(0, 10, 1);
        populate(1, 7, 2);
        populate(2, 2, 3);
        assertEquals("REMAINS_TO_ALL_WEIGHTED", splitter.name());

        Pair<List<Frame>, List<Var>> result = splitter.performSplit(df, w, candidate.getGroupPredicates());

        // groups
        assertEquals(2, result._1.size());
        assertEquals(2, result._2.size());

        // group 1
        assertEquals(12, result._1.get(0).rowCount());
        assertEquals(12, result._2.get(0).rowCount());
        assertEquals(10 + 6 * 10 / 24., result._2.get(0).stream().mapToDouble().sum(), TOL);

        // group 2
        assertEquals(9, result._2.get(1).rowCount());
        assertEquals(9, result._2.get(1).rowCount());
        assertEquals(14 + 6 * 14 / 24., result._2.get(1).stream().mapToDouble().sum(), TOL);
    }

    @Test
    public void testRemainsToAllRandom() {
        RTreeSplitter splitter = RTreeSplitter.REMAINS_TO_RANDOM;
        populate(0, 10, 1);
        populate(1, 7, 2);
        populate(2, 20, 3);
        assertEquals("REMAINS_TO_RANDOM", splitter.name());

        Pair<List<Frame>, List<Var>> result = splitter.performSplit(df, w, candidate.getGroupPredicates());

        // groups
        assertEquals(2, result._1.size());
        assertEquals(2, result._2.size());

        int g1count = result._1.get(0).rowCount();
        int g2count = result._1.get(1).rowCount();

        // group 1
        assertEquals(g1count, result._2.get(0).rowCount());
        assertEquals(10 + 3 * (g1count-10), result._2.get(0).stream().mapToDouble().sum(), TOL);

        // group 2
        assertEquals(g2count, result._2.get(1).rowCount());
        assertEquals(14 + 3 * (g2count-7) , result._2.get(1).stream().mapToDouble().sum(), TOL);
    }
}
