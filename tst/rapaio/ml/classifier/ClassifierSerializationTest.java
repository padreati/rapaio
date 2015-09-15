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

import org.junit.Assert;
import org.junit.Test;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.io.JavaIO;
import rapaio.ml.classifier.bayes.NaiveBayes;
import rapaio.ml.classifier.bayes.estimator.KernelPdf;
import rapaio.ml.classifier.rule.OneRule;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by <a href="mailto:tutuianu@amazon.com">Aurelian Tutuianu</a> on 9/15/15.
 */
public class ClassifierSerializationTest {

    @Test
    public void testOneRuleIris() throws IOException, URISyntaxException, ClassNotFoundException {

        Frame iris = Datasets.loadIrisDataset();
        testModel(new OneRule(), iris, "class");
        testModel(new NaiveBayes().withNumEstimator(new KernelPdf()), iris, "class");

        Frame mushrooms = Datasets.loadMushrooms();
        testModel(new OneRule(), mushrooms, "classes");
        testModel(new NaiveBayes().withNumEstimator(new KernelPdf()), mushrooms, "classes");
    }

    private <T extends Classifier> void testModel(T model, Frame df, String... targets) throws IOException, ClassNotFoundException {
        model.learn(df, targets);
        File tmp = File.createTempFile("model-", "ser");
        JavaIO.storeToFile(model, tmp);

        T shaddow = (T) JavaIO.restoreFromFile(tmp);

        CFit modelFit = model.fit(df);
        CFit shaddowFit = shaddow.fit(df);

        modelFit.printSummary();
        assertEquals(modelFit.summary(), shaddowFit.summary());
    }
}
