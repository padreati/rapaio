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

import org.junit.Before;
import org.junit.Test;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.data.Var;
import rapaio.data.filter.var.VFRefSort;
import rapaio.datasets.Datasets;
import rapaio.ml.regression.tree.RTree;

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
        w = VarDouble.fill(df.rowCount(), 1);
        tree = RTree.newDecisionStump();
    }

    @Test
    public void ignoreTest() {
        RTreeNumericTest m = RTreeNumericTest.ignore();
        Optional<RTreeCandidate> c = m.computeCandidate(tree, df, w, NUM_TEST, TARGET,
                RTreePurityFunction.WEIGHTED_VAR_GAIN);

        assertEquals("IGNORE", m.name());
        assertTrue(!c.isPresent());
    }

    @Test
    public void binaryTest() {
        RTreeNumericTest m = RTreeNumericTest.binary();

        assertEquals("BINARY", m.name());

        Var target = df.rvar(TARGET).fitApply(new VFRefSort(df.rvar(NUM_TEST).refComparator()));
        Var test = df.rvar(NUM_TEST).fitApply(new VFRefSort(df.rvar(NUM_TEST).refComparator()));
        Var weights = w.fitApply(new VFRefSort(df.rvar(NUM_TEST).refComparator()));

        double variance = Variance.from(target).value();
        for(int i=1; i<test.rowCount()-2; i++) {
            double value = test.getDouble(i);

            Var left = target.stream().filter(s -> test.getDouble(s.row()) <= value).toMappedVar();
            Var right = target.stream().filter(s -> test.getDouble(s.row()) > value).toMappedVar();

            double varLeft = Variance.from(left).value();
            double varRight = Variance.from(right).value();

            System.out.println(value + "  => " + varLeft + " | " + varRight + "    -> "
                    + (variance - varLeft*left.rowCount()/test.rowCount() - varRight*right.rowCount()/test.rowCount()));
        }

        Optional<RTreeCandidate> c = m.computeCandidate(tree, df, w, NUM_TEST, TARGET,
                RTreePurityFunction.WEIGHTED_VAR_GAIN);

        assertTrue(c.isPresent());
        assertEquals("Candidate{score=23.029262133969894, testName='temp', groupNames=[temp <= 69.5, temp > 69.5]}",
                c.get().toString());
    }
}
