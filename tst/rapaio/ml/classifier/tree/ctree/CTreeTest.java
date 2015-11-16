/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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
import rapaio.data.Frame;
import rapaio.data.VarType;
import rapaio.data.filter.FFRetainTypes;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.tree.*;
import rapaio.ws.Summary;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public class CTreeTest {

    @Test
    public void testBuilderDecisionStump() throws IOException, URISyntaxException {
        Frame df = Datasets.loadIrisDataset();

        CTree tree = CTree.newDecisionStump();
        assertEquals(1, tree.maxDepth());

        tree.train(df, "class");

        tree.printSummary();
        CTree.Node root = tree.getRoot();
        assertEquals("root", root.getGroupName());

        String testName = root.getBestCandidate().getTestName();
        if ("petal-width".equals(testName)) {
            assertEquals("petal-width", root.getBestCandidate().getTestName());
            assertEquals("petal-width <= 2.35", root.getBestCandidate().getGroupNames().get(0));
            assertEquals("petal-width > 2.35", root.getBestCandidate().getGroupNames().get(1));
        } else {
            assertEquals("petal-length", root.getBestCandidate().getTestName());
            assertEquals("petal-length <= 2.45", root.getBestCandidate().getGroupNames().get(0));
            assertEquals("petal-length > 2.45", root.getBestCandidate().getGroupNames().get(1));
        }
    }

    @Test
    public void testBuilderID3() throws IOException, URISyntaxException {
        Frame df = Datasets.loadMushrooms();
        Summary.printNames(df);
        df = new FFRetainTypes(VarType.NOMINAL).filter(df);

        Summary.printSummary(df);
    }

    @Test
    public void testCandidate() {
        CTree.Candidate candidate = new CTree.Candidate(1, "test");
        candidate.addGroup("test <= 0", s -> s.value("test") <= 0);
        candidate.addGroup("test > 0", s -> s.value("test") > 0);

        assertEquals(1, candidate.compareTo(new CTree.Candidate(2, "test")));
        assertEquals(-1, candidate.compareTo(new CTree.Candidate(-2, "test")));
        assertEquals(-1, candidate.compareTo(new CTree.Candidate(0.5, "test")));

        try {
            candidate.addGroup("test <= 0", s -> true);
            assertTrue("should raise an exception", false);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testPredictorStandard() throws IOException, URISyntaxException {
        Frame df = Datasets.loadIrisDataset();
        CTree tree = CTree.newCART().withMaxDepth(10000).withMinCount(1);
        tree.train(df, "class");
        tree.printSummary();

        CFit pred = tree.fit(df, true, true);
        df = df.bindVars(pred.firstClasses().solidCopy().withName("fit"));

        Frame match = df.stream().filter(spot -> spot.index("class") == spot.index("fit")).toMappedFrame();
        assertEquals(150, match.rowCount());

        df.setMissing(0, 0);
        df.setMissing(0, 1);
        df.setMissing(0, 2);
        df.setMissing(0, 3);

        tree.fit(df, true, false);
        match = df.stream().filter(spot -> spot.index("class") == spot.index("fit")).toMappedFrame();
        assertEquals(150, match.rowCount());
    }


}
