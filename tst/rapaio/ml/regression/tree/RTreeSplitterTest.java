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
import rapaio.data.Numeric;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.util.Pair;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class RTreeSplitterTest {

    private Frame df;
    private Var w;
    private RTree.RTreeCandidate candidate;

    @Before
    public void setUp() throws Exception {
        w = Numeric.empty();
        df = SolidFrame.byVars(Numeric.empty().withName("x"));

        candidate = new RTree.RTreeCandidate(0, "");
        candidate.addGroup("x < 10", s -> s.value("x") < 10);
        candidate.addGroup("x > 20", s -> s.value("x") > 20);
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
        Numeric sample = u.sample(count);
        for (int i = 0; i < sample.rowCount(); i++) {
            df.addRows(1);
            df.var("x").setValue(df.rowCount()-1, sample.value(i));
            w.addValue(weight);
        }
    }

    @Test
    public void testIgnoreMissing() {

        RTreeSplitter splitter = RTreeSplitter.REMAINS_IGNORED;
        populate(0, 2, 1);
        populate(1, 2, 1);
        populate(2, 2, 1);
        assertEquals("REMAINS_IGNORED", splitter.name());

        Pair<List<Frame>, List<Var>> result = splitter.performSplit(df, w, candidate);
        assertEquals(2, result._1.size());
        assertEquals(2, result._2.size());

        assertEquals(2, result._1.get(0).rowCount());
        assertEquals(2, result._2.get(0).rowCount());

        assertEquals(2, result._1.get(1).rowCount());
        assertEquals(2, result._2.get(1).rowCount());
    }
}
