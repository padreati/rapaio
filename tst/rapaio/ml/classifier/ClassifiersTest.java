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

package rapaio.ml.classifier;

import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.sample.FrameSampler;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.boost.AdaBoostSAMME;
import rapaio.ml.classifier.boost.GBTClassifier;
import rapaio.ml.classifier.ensemble.CForest;
import rapaio.ml.classifier.tree.CTree;
import rapaio.ml.eval.CEvaluation;
import rapaio.ml.regression.tree.RTree;
import rapaio.ml.regression.tree.RTreeTestFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class ClassifiersTest {

    @Test
    public void mushroomsTest() throws Exception {
//        Frame df = Datasets.loadMushrooms().stream().complete().toMappedFrame();
        Frame df = Datasets.loadMushrooms();
//        Frame df = Datasets.loadIrisDataset();
        df.printSummary();

        List<Classifier> classifiers = new ArrayList<>();
        classifiers.add(CForest.newRF().withRuns(20).withBootstrap(0.5));
        classifiers.add(new AdaBoostSAMME().withClassifier(CTree.newCART().withMaxDepth(4)).withRuns(20).withSampler(new FrameSampler.Bootstrap(0.5)));
        classifiers.add(new GBTClassifier()
                .withTree(RTree.buildCART().withMaxDepth(4).withFunction(RTreeTestFunction.WeightedSdGain))
                .withRuns(20));

        CEvaluation.multiCv(df, "classes", classifiers, 3);
    }
}
