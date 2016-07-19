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

package rapaio.ml.classifier;

import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.sample.RowSampler;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.boost.AdaBoostSAMME;
import rapaio.ml.classifier.boost.GBTClassifier;
import rapaio.ml.classifier.ensemble.CForest;
import rapaio.ml.classifier.tree.CTree;
import rapaio.experiment.ml.eval.CEvaluation;
import rapaio.ml.regression.tree.RTree;
import rapaio.ml.regression.tree.RTreeTestFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * This test is not intended as a benchmark. It's sole purpose
 * is to get a smoke test for various classifiers.
 *
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ClassifiersPerformanceTest {

    @Test
    public void mushroomsTest() throws Exception {
        Frame df = Datasets.loadMushrooms();

        List<Classifier> classifiers = new ArrayList<>();
        classifiers.add(
                CForest.newRF()
                        .withRuns(5)
                        .withSampler(RowSampler.bootstrap(0.5))
        );
        classifiers.add(
                new AdaBoostSAMME()
                        .withClassifier(CTree.newCART().withMaxDepth(4))
                        .withRuns(5)
                        .withSampler(RowSampler.bootstrap(0.5))
        );
        classifiers.add(new GBTClassifier()
                .withTree(RTree.buildCART().withMaxDepth(3).withFunction(RTreeTestFunction.WeightedSdGain))
                .withRuns(5));

        CEvaluation.multiCv(df, "classes", classifiers, 3);
    }
}
