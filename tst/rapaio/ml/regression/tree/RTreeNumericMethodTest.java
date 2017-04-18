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
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.datasets.Datasets;
import rapaio.sys.WS;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RTreeNumericMethodTest {

    private Frame df;
    private Var w;
    private RTree tree;
    private static final String TARGET = "humidity";
    private static final String NUM_TEST = "temp";

    @Before
    public void setUp() throws Exception {
        df = Datasets.loadPlay();
        w = Numeric.fill(df.rowCount(), 1);
        tree = RTree.buildDecisionStump();
    }

    @Test
    public void ignoreTest() {
        RTreeNumericMethod m = RTreeNumericMethod.IGNORE;
        Optional<RTree.Candidate> c = m.computeCandidate(tree, df, w, NUM_TEST, TARGET,
                RTreeTestFunction.WEIGHTED_VAR_GAIN);

        assertEquals("IGNORE", m.name());
        assertTrue(!c.isPresent());
    }

    @Test
    public void binaryTest() {
        RTreeNumericMethod m = RTreeNumericMethod.BINARY;

        assertEquals("BINARY", m.name());
        Optional<RTree.Candidate> c = m.computeCandidate(tree, df, w, NUM_TEST, TARGET,
                RTreeTestFunction.WEIGHTED_VAR_GAIN);

        assertTrue(c.isPresent());
        assertEquals("Candidate{score=20.54116483516485, testName='temp', groupNames=[temp <= 69.000000, temp > 69.000000]}",
                c.get().toString());
    }
}
