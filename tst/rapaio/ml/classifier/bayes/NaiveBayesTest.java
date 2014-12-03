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

package rapaio.ml.classifier.bayes;

import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.CResult;
import rapaio.ml.eval.ConfusionMatrix;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/24/14.
 */
public class NaiveBayesTest {

    @Test
    public void testBasicCvpGaussian() throws IOException, URISyntaxException {

        RandomSource.setSeed(1L);
        Frame df = Datasets.loadIrisDataset();
        NaiveBayesClassifier nb = new NaiveBayesClassifier();
        nb.learn(df, "class");
        CResult pred = nb.predict(df);

        ConfusionMatrix cm = new ConfusionMatrix(df.var("class"), pred.firstClasses());
        cm.summary();

        assertTrue(cm.accuracy() >= 0.9);

        assertEquals(50, cm.matrix()[0][0], 10e-12);
        assertEquals(47, cm.matrix()[1][1], 10e-12);
        assertEquals(46, cm.matrix()[2][2], 10e-12);

        assertEquals(1, cm.matrix()[1][0], 10e-12);
        assertEquals(2, cm.matrix()[1][2], 10e-12);
        assertEquals(4, cm.matrix()[2][1], 10e-12);

    }

    @Test
    public void testBasicCvpEmpirical() throws IOException, URISyntaxException {

        RandomSource.setSeed(1L);
        Frame df = Datasets.loadIrisDataset();
        NaiveBayesClassifier nb = new NaiveBayesClassifier().withCvpEstimator(new NaiveBayesClassifier.CvpEstimatorKDE());
        nb.learn(df, "class");
        CResult pred = nb.predict(df);

        ConfusionMatrix cm = new ConfusionMatrix(df.var("class"), pred.firstClasses());
        cm.summary();

        assertTrue(cm.accuracy() >= 0.9);

        assertEquals(50, cm.matrix()[0][0], 10e-12);
        assertEquals(48, cm.matrix()[1][1], 10e-12);
        assertEquals(46, cm.matrix()[2][2], 10e-12);

        assertEquals(0, cm.matrix()[1][0], 10e-12);
        assertEquals(2, cm.matrix()[1][2], 10e-12);
        assertEquals(4, cm.matrix()[2][1], 10e-12);
    }

    @Test
    public void testBasicDvp() throws IOException, URISyntaxException {

        RandomSource.setSeed(1L);
        Frame df = Datasets.loadMushrooms();

        NaiveBayesClassifier nb = new NaiveBayesClassifier();
        nb.learn(df, "classes");
        CResult cp = nb.predict(df);

        ConfusionMatrix cm = new ConfusionMatrix(df.var("classes"), cp.firstClasses());
        cm.summary();

        assertTrue(cm.accuracy() >= 0.89);

        assertEquals(3100, cm.matrix()[0][0], 10e-12);
        assertEquals(816, cm.matrix()[0][1], 10e-12);
        assertEquals(71, cm.matrix()[1][0], 10e-12);
        assertEquals(4137, cm.matrix()[1][1], 10e-12);
    }
}
