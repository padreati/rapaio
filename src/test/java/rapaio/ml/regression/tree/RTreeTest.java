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

package rapaio.ml.regression.tree;

import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.filter.frame.FFRefSort;
import rapaio.data.sample.RowSampler;
import rapaio.datasets.Datasets;
import rapaio.ml.regression.RFit;
import rapaio.ml.regression.Regression;
import rapaio.ml.regression.ensemble.RForest;

import java.io.IOException;

/**
 * Test for regression decision trees
 * <p>
 * Created by <a href="mailto:tutuianu@amazon.com">Aurelian Tutuianu</a> on 11/5/15.
 */
public class RTreeTest {

    public static final String Sales = "Sales";

    @Test
    public void testSimple() throws IOException {
        Frame df = Datasets.loadISLAdvertising().removeVars("ID", "Radio", "Newspaper");
//        df = Datasets.loadISLAdvertising().removeVars("ID");
        df.printSummary();

        String v = "TV";
        Frame t = new FFRefSort(df.var(v).refComparator()).filter(df);

//        WS.setPrinter(new IdeaPrinter());

        RTree tree = RTree.buildCART().withMaxDepth(10).withMinCount(1).withFunction(RTreeTestFunction.WeightedSdGain);
        Regression model = RForest.newRF()
                .withRegression(tree)
                .withRunningHook((r, run) -> {
                    RFit fit = r.fit(t);
//                    WS.draw(plot()
//                            .lines(t.var(v), fit.firstFit(), color(1))
//                            .points(t.var(v), t.var("Sales"), pch(3))
//
//                    );
                }).withSampler(RowSampler.bootstrap(1))
                .withRuns(10);
        model.train(t, "Sales");
//        model.printSummary();
    }
}
