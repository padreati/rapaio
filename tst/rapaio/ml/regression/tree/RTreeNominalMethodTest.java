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
import rapaio.data.Frame;
import rapaio.data.NumVar;
import rapaio.data.Var;
import rapaio.datasets.Datasets;

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
        w = NumVar.fill(df.rowCount(), 1);
        tree = RTree.buildDecisionStump();
    }

    @Test
    public void ignoreTest() {

        RTreeNominalMethod m = RTreeNominalMethod.IGNORE;
        Optional<RTreeCandidate> cs = m.computeCandidate(tree, df, w, NOM_TEST, TARGET,
                RTreeTestFunction.WEIGHTED_VAR_GAIN);

        assertEquals("IGNORE", m.name());
        assertTrue(!cs.isPresent());
    }

    @Test
    public void fullTest() {

        RTreeNominalMethod m = RTreeNominalMethod.FULL;
        Optional<RTreeCandidate> cs = m.computeCandidate(tree, df, w, NOM_TEST, TARGET,
                RTreeTestFunction.WEIGHTED_VAR_GAIN);

        assertEquals("FULL", m.name());
        assertTrue(cs.isPresent());

        RTreeCandidate c = cs.get();
        assertEquals(NOM_TEST, c.getTestName());

        assertEquals(3, c.getGroupNames().size());
        assertEquals(3, c.getGroupPredicates().size());

        assertEquals("outlook == sunny", c.getGroupNames().get(0));
        assertEquals("outlook == overcast", c.getGroupNames().get(1));
        assertEquals("outlook == rain", c.getGroupNames().get(2));

        assertEquals(-0.8092796092796135, c.getScore(), 1e-20);
    }

    @Test
    public void fullTestFailed() {

        RTreeNominalMethod m = RTreeNominalMethod.FULL;
        Optional<RTreeCandidate> cs = m.computeCandidate(tree, df.mapRows(1), w.mapRows(1),
                NOM_TEST, TARGET, RTreeTestFunction.WEIGHTED_VAR_GAIN);

        assertEquals("FULL", m.name());
        assertTrue(!cs.isPresent());
    }

    @Test
    public void binaryTest() {
        RTreeNominalMethod m = RTreeNominalMethod.BINARY;

        assertEquals("BINARY", m.name());

        Optional<RTreeCandidate> cs = m.computeCandidate(tree, df, w,
                NOM_TEST, TARGET, RTreeTestFunction.WEIGHTED_VAR_GAIN);

        assertTrue(cs.isPresent());

        assertEquals("Candidate{score=11.235164835164838, testName='outlook', groupNames=[outlook == overcast, outlook != overcast]}",
                cs.get().toString());
    }
}
