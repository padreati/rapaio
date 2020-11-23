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

package rapaio.ml.regression.tree.rtree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.distributions.Uniform;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.experiment.ml.common.predicate.RowPredicate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SplitterTest {

    private static final double TOL = 1e-20;

    private Frame df;
    private Var w;
    private Candidate candidate;

    @BeforeEach
    void setUp() {
        w = VarDouble.empty();
        df = SolidFrame.byVars(VarDouble.empty().name("x"));

        candidate = new Candidate(0, "");
        candidate.addGroup(RowPredicate.numLess("x", 10));
        candidate.addGroup(RowPredicate.numGreater("x", 20));
    }

    private void populate(int group, int count, double weight) {
        Uniform u;
        switch (group) {
            case 0:
                u = Uniform.of(0, 9);
                break;
            case 1:
                u = Uniform.of(21, 30);
                break;
            default:
                u = Uniform.of(11, 19);
        }
        VarDouble sample = u.sample(count);
        for (int i = 0; i < sample.rowCount(); i++) {
            df.addRows(1);
            df.rvar("x").setDouble(df.rowCount() - 1, sample.getDouble(i));
            w.addDouble(weight);
        }
    }

    @Test
    void testIgnoreMissing() {

        populate(0, 2, 1);
        populate(1, 2, 2);
        populate(2, 2, 3);

        List<Mapping> result = Splitter.Ignore.performSplitMapping(df, w, candidate.getGroupPredicates());
        assertEquals(2, result.size());

        assertEquals(2, df.mapRows(result.get(0)).rowCount());
        assertEquals(2, w.mapRows(result.get(0)).rowCount());

        assertEquals(2, df.mapRows(result.get(1)).rowCount());
        assertEquals(2, w.mapRows(result.get(1)).rowCount());

        assertEquals(2, w.mapRows(result.get(0)).stream().mapToDouble().sum(), TOL);
        assertEquals(4, w.mapRows(result.get(1)).stream().mapToDouble().sum(), TOL);
    }

    @Test
    void testRemainsWithMajority() {

        populate(0, 10, 1);
        populate(1, 7, 2);
        populate(2, 2, 3);

        List<Mapping> result = Splitter.Majority.performSplitMapping(df, w, candidate.getGroupPredicates());

        // groups
        assertEquals(2, result.size());

        // group 1
        assertEquals(10, df.mapRows(result.get(0)).rowCount());
        assertEquals(10, w.mapRows(result.get(0)).rowCount());
        assertEquals(10, w.mapRows(result.get(0)).stream().mapToDouble().sum(), TOL);

        // group 2
        assertEquals(9, df.mapRows(result.get(1)).rowCount());
        assertEquals(9, w.mapRows(result.get(1)).rowCount());
        assertEquals(20, w.mapRows(result.get(1)).stream().mapToDouble().sum(), TOL);
    }

    @Test
    void testRemainsToAllRandom() {
        populate(0, 10, 1);
        populate(1, 7, 2);
        populate(2, 20, 3);

        List<Mapping> result = Splitter.Random.performSplitMapping(df, w, candidate.getGroupPredicates());

        // groups
        assertEquals(2, result.size());

        int g1count = df.mapRows(result.get(0)).rowCount();
        int g2count = df.mapRows(result.get(1)).rowCount();

        // group 1
        assertEquals(g1count, result.get(0).size());
        assertEquals(10 + 3 * (g1count - 10), w.mapRows(result.get(0)).stream().mapToDouble().sum(), TOL);

        // group 2
        assertEquals(g2count, result.get(1).size());
        assertEquals(14 + 3 * (g2count - 7), w.mapRows(result.get(1)).stream().mapToDouble().sum(), TOL);
    }
}
