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
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.bayes.NaiveBayesClassifier;
import rapaio.ml.classifier.ensemble.CBagging;
import rapaio.ml.eval.ModelEvaluation;

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
//        Frame df = Datasets.loadMushrooms();
        Frame df = Datasets.loadIrisDataset();

        List<Classifier> classifiers = new ArrayList<>();
//        classifiers.add(new CForest().withRuns(100).withBootstrap(0.5));
        classifiers.add(new CBagging().withRuns(100).withBootstrap(1));
        classifiers.add(new CBagging().withRuns(100).withBootstrap(1).withClassifier(new NaiveBayesClassifier()));
//        classifiers.add(new NaiveBayesClassifier());
//        classifiers.add(new BinaryLogistic());
//        classifiers.add(new BinarySMO().withKernel(new MinKernel()));
//        classifiers.add(new AdaBoostSAMMEClassifier());
//        classifiers.add(new GBTClassifier());

        new ModelEvaluation().multiCv(df, "class", classifiers, 10);
    }
}
