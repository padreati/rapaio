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

package rapaio.experiment.ml.regression.tree.rtree;

import org.junit.Before;
import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.datasets.Datasets;
import rapaio.experiment.ml.regression.tree.RTree;

import java.util.Optional;

import static org.junit.Assert.*;

public class RTreeNominalMethodTest {

    private Frame df;
    private Var w;
    private RTree tree;
    private static final String TARGET = "humidity";
    private static final String NOM_TEST = "outlook";

    @Before
    public void setUp() throws Exception {
        df = Datasets.loadPlay();
        w = VarDouble.fill(df.rowCount(), 1);
        tree = RTree.newDecisionStump();
    }

    @Test
    public void ignoreTest() {

        RTreeNominalTest m = RTreeNominalTest.ignore();
        Optional<RTreeCandidate> cs = m.computeCandidate(tree, df, w, NOM_TEST, TARGET,
                RTreePurityFunction.WEIGHTED_VAR_GAIN);

        assertEquals("IGNORE", m.name());
        assertFalse(cs.isPresent());
    }

    @Test
    public void fullTest() {

        RTreeNominalTest m = RTreeNominalTest.full();
        Optional<RTreeCandidate> cs = m.computeCandidate(tree, df, w, NOM_TEST, TARGET,
                RTreePurityFunction.WEIGHTED_VAR_GAIN);

        assertEquals("FULL", m.name());
        assertTrue(cs.isPresent());

        RTreeCandidate c = cs.get();
        assertEquals(NOM_TEST, c.getTestName());

        assertEquals(3, c.getGroupNames().size());
        assertEquals(3, c.getGroupPredicates().size());

        assertEquals("outlook = 'sunny'", c.getGroupNames().get(0));
        assertEquals("outlook = 'overcast'", c.getGroupNames().get(1));
        assertEquals("outlook = 'rain'", c.getGroupNames().get(2));

        assertEquals(4.432653061224499, c.getScore(), 1e-20);
    }

    @Test
    public void fullTestFailed() {

        RTreeNominalTest m = RTreeNominalTest.full();
        Optional<RTreeCandidate> cs = m.computeCandidate(tree, df.mapRows(1), w.mapRows(1),
                NOM_TEST, TARGET, RTreePurityFunction.WEIGHTED_VAR_GAIN);

        assertEquals("FULL", m.name());
        assertTrue(!cs.isPresent());
    }

    @Test
    public void binaryTest() {
        RTreeNominalTest m = RTreeNominalTest.binary();

        assertEquals("BINARY", m.name());

        Optional<RTreeCandidate> cs = m.computeCandidate(tree, df, w,
                NOM_TEST, TARGET, RTreePurityFunction.WEIGHTED_VAR_GAIN);

        assertTrue(cs.isPresent());

        assertEquals("Candidate{score=4.318367346938771, testName='outlook', groupNames=[outlook = 'overcast', outlook != 'overcast']}",
                cs.get().toString());
    }
}
