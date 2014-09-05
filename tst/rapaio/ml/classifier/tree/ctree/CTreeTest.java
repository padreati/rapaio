/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.ml.classifier.tree.ctree;

import junit.framework.Assert;
import org.junit.Test;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.ws.Summary;

import java.io.IOException;
import java.net.URISyntaxException;

import static junit.framework.Assert.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public class CTreeTest {

    @Test
    public void testBuilders() throws IOException, URISyntaxException {
        Frame df = Datasets.loadIrisDataset();

        CTree tree = CTree.newDecisionStump();
        assertEquals(1, tree.getMaxDepth());

        tree.learn(df, "class");


    }

    @Test
    public void testStandard() throws IOException, URISyntaxException {
        Frame df = Datasets.loadIrisDataset();
        CTree tree = CTree.newCART().withMaxDepth(10000).withMinCount(1).withTestCounter(CTreeTestCounter.M_NOMINAL_M_NUMERIC);
        tree.learn(df, "class");
        tree.summary();
        CTreePredictor predictor = CTreePredictor.STANDARD;
        assertEquals("STANDARD", predictor.name());

        tree.predict(df, true, true);
        df = df.bindVars(tree.firstClasses().solidCopy().withName("predict"));

        Frame match = df.stream().filter(spot -> spot.index("class")== spot.index("predict")).toMappedFrame();
        assertEquals(150, match.rowCount());

        df.setMissing(0, 0);
        df.setMissing(0, 1);
        df.setMissing(0, 2);
        df.setMissing(0, 3);

        tree.predict(df, true, false);
        match = df.stream().filter(spot -> spot.index("class")== spot.index("predict")).toMappedFrame();
        assertEquals(150, match.rowCount());
    }

}
