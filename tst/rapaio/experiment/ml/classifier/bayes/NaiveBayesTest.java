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

package rapaio.experiment.ml.classifier.bayes;

import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.ClassResult;
import rapaio.ml.classifier.Classifier;
import rapaio.experiment.ml.classifier.bayes.estimator.KernelPdf;
import rapaio.ml.eval.Confusion;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.*;


/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/24/14.
 */
public class NaiveBayesTest {

    @Test
    public void testBasicCvpGaussian() throws IOException, URISyntaxException {

        RandomSource.setSeed(1L);
        Frame df = Datasets.loadIrisDataset();
        NaiveBayes nb = new NaiveBayes();
        nb.fit(df, "class");
        ClassResult pred = nb.predict(df);

        Confusion cm = new Confusion(df.rvar("class"), pred.firstClasses());
        cm.printSummary();

        assertTrue(cm.accuracy() >= 0.9);

        assertEquals(50, cm.matrix()[0][0], 10e-12);
        assertEquals(47, cm.matrix()[1][1], 10e-12);
        assertEquals(47, cm.matrix()[2][2], 10e-12);

        assertEquals(0, cm.matrix()[1][0], 10e-12);
        assertEquals(3, cm.matrix()[1][2], 10e-12);
        assertEquals(3, cm.matrix()[2][1], 10e-12);

    }

    @Test
    public void testBasicCvpEmpirical() throws IOException, URISyntaxException {

        RandomSource.setSeed(1L);
        Frame df = Datasets.loadIrisDataset();
        NaiveBayes nb = new NaiveBayes().withNumEstimator(new KernelPdf());
        nb.fit(df, "class");
        ClassResult pred = nb.predict(df);

        Confusion cm = new Confusion(df.rvar("class"), pred.firstClasses());
        cm.printSummary();

        assertTrue(cm.accuracy() >= 0.9);

        assertEquals(50, cm.matrix()[0][0], 10e-12);
        assertEquals(48, cm.matrix()[1][1], 10e-12);
        assertEquals(47, cm.matrix()[2][2], 10e-12);

        assertEquals(0, cm.matrix()[1][0], 10e-12);
        assertEquals(2, cm.matrix()[1][2], 10e-12);
        assertEquals(3, cm.matrix()[2][1], 10e-12);
    }

    @Test
    public void testBasicDvp() throws IOException, URISyntaxException {

        RandomSource.setSeed(1L);
        Frame df = Datasets.loadMushrooms();

        NaiveBayes nb = new NaiveBayes();
        nb.fit(df, "classes");

        nb.printSummary();

        ClassResult cp = nb.predict(df);

        Confusion cm = new Confusion(df.rvar("classes"), cp.firstClasses());
        cm.printSummary();

        assertTrue(cm.accuracy() >= 0.89);

        assertEquals(3584, cm.matrix()[0][0], 10e-12);
        assertEquals(332, cm.matrix()[0][1], 10e-12);
        assertEquals(20, cm.matrix()[1][0], 10e-12);
        assertEquals(4188, cm.matrix()[1][1], 10e-12);
    }

    @Test
    public void testSummary() throws IOException, URISyntaxException {
        Classifier nb = new NaiveBayes();
        assertEquals("NaiveBayes model\n" +
                "================\n" +
                "\n" +
                "Description:\n" +
                "NaiveBayes(numEstimator=GaussianPdf, nomEstimator=MultinomialPmf)\n" +
                "\n" +
                "Capabilities:\n" +
                "types inputs/targets: BINARY,INT,NOMINAL,DOUBLE/NOMINAL\n" +
                "counts inputs/targets: [0,1000000] / [1,1]\n" +
                "missing inputs/targets: true/false\n" +
                "\n" +
                "Learned model:\n" +
                "Learning phase not called\n" +
                "\n", nb.summary());

        nb.fit(Datasets.loadIrisDataset(), "class");


        nb.printSummary();
    }

}
