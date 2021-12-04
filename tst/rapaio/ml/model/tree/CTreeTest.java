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

package rapaio.ml.model.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.datasets.Datasets;
import rapaio.ml.model.tree.ctree.Candidate;
import rapaio.ml.model.tree.ctree.Node;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/11/20.
 */
public class CTreeTest {

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
    }

    @Test
    void candidateTest() {
        Candidate c = new Candidate(0.1, "t1");
        c.addGroup(RowPredicate.numLessEqual("x", 1));
        c.addGroup(RowPredicate.numGreater("x", 1));

        Frame df = SolidFrame.byVars(VarDouble.wrap(0).name("x"));

        assertTrue(c.groupPredicates().get(0).test(0, df));
        assertFalse(c.groupPredicates().get(1).test(0, df));

        assertEquals(0.1, c.score(), 1e-10);
        assertEquals("t1", c.testName());

        Candidate b = new Candidate(0.2, "t2");

        assertTrue(c.score() < b.score());
    }

    @Test
    void testBuilderDecisionStump() {
        Frame df = Datasets.loadIrisDataset();

        CTree tree = CTree.newDecisionStump();
        assertEquals(1, tree.maxDepth.get());

        tree.fit(df, "class");

        Node root = tree.getRoot();
        assertEquals("root", root.groupName);

        String testName = root.bestCandidate.testName();
        if ("petal-width".equals(testName)) {
            assertEquals("petal-width", root.bestCandidate.testName());
            assertEquals("petal-width<=0.8", root.bestCandidate.groupPredicates().get(0).toString());
            assertEquals("petal-width>0.8", root.bestCandidate.groupPredicates().get(1).toString());
        } else {
            assertEquals("petal-length", root.bestCandidate.testName());
            assertEquals("petal-length<=2.45", root.bestCandidate.groupPredicates().get(0).toString());
            assertEquals("petal-length>2.45", root.bestCandidate.groupPredicates().get(1).toString());
        }
    }

    @Test
    void testPredictorStandard() {
        Frame df = Datasets.loadIrisDataset();
        CTree tree = CTree.newCART().maxDepth.set(10000).minCount.set(1);
        tree.fit(df, "class");

        var pred = tree.predict(df, true, true);
        df = df.bindVars(pred.firstClasses().copy().name("predict"));

        Frame match = df.stream().filter(spot -> spot.getInt("class") == spot.getInt("predict")).toMappedFrame();
        assertEquals(150, match.rowCount());

        df.setMissing(0, 0);
        df.setMissing(0, 1);
        df.setMissing(0, 2);
        df.setMissing(0, 3);

        tree.predict(df, true, false);
        match = df.stream().filter(spot -> spot.getInt("class") == spot.getInt("predict")).toMappedFrame();
        assertEquals(150, match.rowCount());
    }

    @Test
    void printingTest() {

        var iris = Datasets.loadIrisDataset();
        var model = CTree.newCART().fit(iris, "class");

        assertEquals("""
                CTree model
                ================
                                
                Description:
                CTree{purity=GiniGain,splitter=Random,varSelector=VarSelector[ALL]}
                                
                Capabilities:
                types inputs/targets: NOMINAL,INT,DOUBLE,BINARY/NOMINAL
                counts inputs/targets: [1,1000000] / [1,1]
                missing inputs/targets: true/false
                                
                Learned model:
                                
                total number of nodes: 17
                total number of leaves: 9
                description:
                split, n/err, classes (densities) [* if is leaf / purity if not]
                                
                |- 0. root    150/100 virginica (0.333 0.333 0.333 ) [0.3333333]
                |   |- 1. petal-length<=2.45    50/0 setosa (1 0 0 ) *
                |   |- 2. petal-length>2.45    100/50 versicolor (0 0.5 0.5 ) [0.389694]
                |   |   |- 3. petal-width<=1.75    54/5 versicolor (0 0.907 0.093 ) [0.0823903]
                |   |   |   |- 5. petal-length<=4.95    48/1 versicolor (0 0.979 0.021 ) [0.0407986]
                |   |   |   |   |- 9. petal-width<=1.65    47/0 versicolor (0 1 0 ) *
                |   |   |   |   |- 10. petal-width>1.65    1/0 virginica (0 0 1 ) *
                |   |   |   |- 6. petal-length>4.95    6/2 virginica (0 0.333 0.667 ) [0.2222222]
                |   |   |   |   |- 11. petal-width<=1.55    3/0 virginica (0 0 1 ) *
                |   |   |   |   |- 12. petal-width>1.55    3/1 versicolor (0 0.667 0.333 ) [0.4444444]
                |   |   |   |   |   |- 15. sepal-length<=6.95    2/0 versicolor (0 1 0 ) *
                |   |   |   |   |   |- 16. sepal-length>6.95    1/0 virginica (0 0 1 ) *
                |   |   |- 4. petal-width>1.75    46/1 virginica (0 0.022 0.978 ) [0.0135476]
                |   |   |   |- 7. petal-length<=4.85    3/1 virginica (0 0.333 0.667 ) [0.4444444]
                |   |   |   |   |- 13. sepal-width<=3.1    2/0 virginica (0 0 1 ) *
                |   |   |   |   |- 14. sepal-width>3.1    1/0 versicolor (0 1 0 ) *
                |   |   |   |- 8. petal-length>4.85    43/0 virginica (0 0 1 ) *
                """, model.toSummary());

        assertEquals(model.toContent(), model.toSummary());
        assertEquals(model.toFullContent(), model.toSummary());

        assertEquals("CTree{purity=GiniGain,splitter=Random,varSelector=VarSelector[ALL]}", model.toString());
    }
}
