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

package rapaio.ml.classifier.tree.ctree;

import org.junit.Test;
import rapaio.data.*;
import rapaio.data.filter.frame.*;
import rapaio.datasets.*;
import rapaio.ml.classifier.*;
import rapaio.ml.classifier.tree.*;
import rapaio.ml.common.predicate.*;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public class CTreeTest {

    @Test
    public void testBuilderDecisionStump() throws IOException, URISyntaxException {
        Frame df = Datasets.loadIrisDataset();

        CTree tree = CTree.newDecisionStump();
        assertEquals(1, tree.maxDepth());

        tree.fit(df, "class");

        tree.printSummary();
        CTreeNode root = tree.getRoot();
        assertEquals("root", root.getGroupName());

        String testName = root.getBestCandidate().getTestName();
        if ("petal-width".equals(testName)) {
            assertEquals("petal-width", root.getBestCandidate().getTestName());
            assertEquals("petal-width <= 0.8", root.getBestCandidate().getGroupPredicates().get(0).toString());
            assertEquals("petal-width > 0.8", root.getBestCandidate().getGroupPredicates().get(1).toString());
        } else {
            assertEquals("petal-length", root.getBestCandidate().getTestName());
            assertEquals("petal-length <= 2.45", root.getBestCandidate().getGroupPredicates().get(0).toString());
            assertEquals("petal-length > 2.45", root.getBestCandidate().getGroupPredicates().get(1).toString());
        }
    }

    @Test
    public void testBuilderID3() throws IOException, URISyntaxException {
        Frame df = Datasets.loadMushrooms();
        df = FRetainTypes.on(VType.NOMINAL).fapply(df);
        df.printSummary();
    }

    @Test
    public void testCandidate() {
        CTreeCandidate candidate = new CTreeCandidate(1, "test");
        candidate.addGroup(RowPredicate.numLessEqual("test", 0));
        candidate.addGroup(RowPredicate.numGreater("test", 0));

        assertEquals(1, candidate.compareTo(new CTreeCandidate(2, "test")));
        assertEquals(-1, candidate.compareTo(new CTreeCandidate(-2, "test")));
        assertEquals(-1, candidate.compareTo(new CTreeCandidate(0.5, "test")));
    }

    @Test
    public void testPredictorStandard() throws IOException, URISyntaxException {
        Frame df = Datasets.loadIrisDataset();
        CTree tree = CTree.newCART().withMaxDepth(10000).withMinCount(1);
        tree.fit(df, "class");
        tree.printSummary();

        CPrediction pred = tree.predict(df, true, true);
        df = df.bindVars(pred.firstClasses().solidCopy().withName("predict"));

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


}
