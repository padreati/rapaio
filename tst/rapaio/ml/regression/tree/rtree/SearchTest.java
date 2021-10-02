/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.regression.tree.rtree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.filter.VRefSort;
import rapaio.datasets.Datasets;
import rapaio.ml.regression.tree.RTree;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/20/19.
 */
public class SearchTest {

    private static final String TARGET = "humidity";
    private static final String NUM_TEST = "temp";
    private static final String NOM_TEST = "outlook";
    private Frame df;
    private Var w;
    private RTree tree;

    @BeforeEach
    void setUp() {
        df = Datasets.loadPlay();
        w = VarDouble.fill(df.rowCount(), 1);
        tree = RTree.newDecisionStump();
    }

    @Test
    void ignoreTest() {

        Search m = Search.Ignore;
        Optional<Candidate> cs = m.computeCandidate(tree, df, w, NOM_TEST, TARGET);
        assertFalse(cs.isPresent());
    }

    @Test
    void nominalFullTest() {

        Search m = Search.NominalFull;
        Optional<Candidate> cs = m.computeCandidate(tree, df, w, NOM_TEST, TARGET);

        assertTrue(cs.isPresent());

        Candidate c = cs.get();
        assertEquals(NOM_TEST, c.getTestName());

        assertEquals(3, c.getGroupPredicates().size());

        assertEquals("outlook='sunny'", c.getGroupPredicates().get(0).toString());
        assertEquals("outlook='overcast'", c.getGroupPredicates().get(1).toString());
        assertEquals("outlook='rain'", c.getGroupPredicates().get(2).toString());

        assertEquals(4.432653061224499, c.getScore(), 1e-20);
    }

    @Test
    void nominalFullTestFailed() {
        Optional<Candidate> cs = Search.NominalFull.computeCandidate(tree, df.mapRows(1), w.mapRows(1), NOM_TEST, TARGET);
        assertFalse(cs.isPresent());
    }

    @Test
    void nominalBinaryTest() {
        Optional<Candidate> cs = Search.NominalBinary.computeCandidate(tree, df, w, NOM_TEST, TARGET);
        assertTrue(cs.isPresent());
        assertEquals("Candidate{score=4.318367346938771, testName='outlook', predicates=[outlook='overcast', outlook!='overcast']}",
                cs.get().toString());
    }

    @Test
    void numericBinaryTest() {
        Var target = df.rvar(TARGET).fapply(new VRefSort(df.rvar(NUM_TEST).refComparator()));
        Var test = df.rvar(NUM_TEST).fapply(new VRefSort(df.rvar(NUM_TEST).refComparator()));
        Var weights = w.fapply(new VRefSort(df.rvar(NUM_TEST).refComparator()));

        Optional<Candidate> c = Search.NumericBinary.computeCandidate(tree, df, w, NUM_TEST, TARGET);

        assertTrue(c.isPresent());
        assertEquals(32.657653061224515, c.get().getScore(), 1e-12);
        assertEquals("temp", c.get().getTestName());
        assertEquals(2, c.get().getGroupPredicates().size());
        assertEquals("temp<=69.5", c.get().getGroupPredicates().get(0).toString());
        assertEquals("temp>69.5", c.get().getGroupPredicates().get(1).toString());
    }

}
