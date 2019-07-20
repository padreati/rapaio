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

package rapaio.experiment.ml.regression.ensemble;

import org.junit.Test;
import rapaio.core.SamplingTools;
import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.datasets.Datasets;
import rapaio.ml.common.VarSelector;
import rapaio.ml.regression.Regression;
import rapaio.ml.regression.tree.RTree;

import java.io.IOException;

public class RForestTest {

    @Test
    public void buildTest() throws IOException {

        Regression tree = RTree.newCART().withMaxDepth(7);

        Regression rf = RForest.newRF()
                .withRegression(RTree.newCART().withMaxDepth(7).withVarSelector(VarSelector.fixed(2)))
                .withRuns(1_000);


        Frame df = Datasets.loadISLAdvertising().removeVars(VRange.of("ID"));
        df.printSummary();

        Frame[] dfs = SamplingTools.randomSampleSlices(df, 0.7, 0.3);

        Frame train = dfs[0];
        Frame test = dfs[1];


        tree.fit(train, "Sales");
        tree.predict(test, true).printSummary();

        rf.fit(train, "Sales");
        rf.predict(test, true).printSummary();
    }


}
