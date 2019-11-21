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

package rapaio.ml.classifier.bayes;

import org.junit.Before;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.ml.classifier.bayes.estimator.KernelPdf;
import rapaio.ml.classifier.bayes.estimator.MultinomialPmf;
import rapaio.ml.eval.metric.Confusion;

import static org.junit.Assert.assertEquals;


/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/24/14.
 */
public class NaiveBayesTest {

    private static final int N = 100;
    private static final double TOL = 1e-20;
    private Var target;
    private Frame dfGood;
    private Frame dfBad;
    private Frame dfMixt;

    @Before
    public void setUp() {
        RandomSource.setSeed(1L);
        Normal normal = Normal.std();
        target = VarNominal.from(N, row -> row >= 50 ? "A" : "B").withName("target");
        Var nom1 = VarNominal.from(N, row -> row >= 50 ? "a" : "b").withName("nom1");
        Var nom2 = VarNominal.from(N, row -> row % 2 == 0 ? "a" : "b").withName("nom2");
        Var num1 = VarDouble.from(N, row -> row >= 50 ? normal.sampleNext() : normal.sampleNext() + 10).withName("num1");
        Var num2 = VarDouble.from(N, normal::sampleNext).withName("num2");
        dfGood = SolidFrame.byVars(nom1, num1, target);
        dfBad = SolidFrame.byVars(nom2, num2, target);
        dfMixt = SolidFrame.byVars(nom1, nom2, num1, num2, target);
    }

    @Test
    public void testBasicCvpGaussian() {
        Var goodPrediction = new NaiveBayes().fit(dfGood, "target").predict(dfGood).firstClasses();
        assertEquals(Confusion.from(target, goodPrediction).accuracy(), 1, TOL);

        Var badPrediction = new NaiveBayes().fit(dfBad, "target").predict(dfBad).firstClasses();
        assertEquals(0.5, Confusion.from(target, badPrediction).accuracy(), 0.1);

        Var mixtPrediction = new NaiveBayes().fit(dfMixt, "target").predict(dfMixt).firstClasses();
        Var mixtProb = new NaiveBayes().fit(dfMixt, "target").predict(dfMixt, true, true).firstDensity().rvar(1);
        assertEquals(1, Confusion.from(target, mixtPrediction).accuracy(), 0.1);
        assertEquals(0.99, mixtProb.mapRows(Mapping.range(0, 50)).op().avg(), 0.01);
        assertEquals(0.01, mixtProb.mapRows(Mapping.range(50, 100)).op().avg(), 0.01);
    }

    @Test
    public void testBasicCvpEmpirical() {
        Var goodPrediction = new NaiveBayes().withNumEstimator(new KernelPdf()).fit(dfGood, "target").predict(dfGood).firstClasses();
        assertEquals(Confusion.from(target, goodPrediction).accuracy(), 1, TOL);

        Var badPrediction = new NaiveBayes().withNumEstimator(new KernelPdf()).fit(dfBad, "target").predict(dfBad).firstClasses();
        assertEquals(0.5, Confusion.from(target, badPrediction).accuracy(), 0.1);

        Var mixtPrediction = new NaiveBayes().withNumEstimator(new KernelPdf()).fit(dfMixt, "target").predict(dfMixt).firstClasses();
        Var mixtProb = new NaiveBayes().fit(dfMixt, "target").predict(dfMixt).firstDensity().rvar(1);
        assertEquals(1, Confusion.from(target, mixtPrediction).accuracy(), 0.1);
        assertEquals(0.99, mixtProb.mapRows(Mapping.range(0, 50)).op().avg(), 0.01);
        assertEquals(0.01, mixtProb.mapRows(Mapping.range(50, 100)).op().avg(), 0.01);
    }

    @Test
    public void testGaussianNoVariance() {
        Frame df = SolidFrame.byVars(VarDouble.from(N, row -> row >= 50 ? 1.0 : 1.1).withName("constant"), target);
        NaiveBayes nb = new NaiveBayes().withLaplaceSmoother(1);
        nb.fit(df, "target");
        Var prediction = nb.predict(df).firstDensity().rvar(1);
        assertEquals(1.0, prediction.mapRows(Mapping.range(0, 50)).op().avg(), TOL);
        assertEquals(0.0, prediction.mapRows(Mapping.range(50, 100)).op().avg(), TOL);
    }

    @Test
    public void testBuilder() {
        NaiveBayes nb = new NaiveBayes()
                .withPriorSupplier(PriorSupplier.PRIOR_UNIFORM)
                .withLaplaceSmoother(2)
                .withNumEstimator(new KernelPdf())
                .withNomEstimator(new MultinomialPmf());
        NaiveBayes nbCopy = nb.newInstance();

        assertEquals(nb.getLaplaceSmoother(), nbCopy.getLaplaceSmoother(), TOL);
        assertEquals(nb.getPriorSupplier(), nbCopy.getPriorSupplier());
        assertEquals(nb.getNomEstimator(), nbCopy.getNomEstimator());
        assertEquals(nb.getNumEstimator(), nbCopy.getNumEstimator());
    }

    @Test
    public void testSummary() {
        NaiveBayes nb = new NaiveBayes();
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
                "\n", nb.toSummary());

        nb.fit(dfGood, "target");

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
                "input vars: \n" +
                "0. nom1 : NOMINAL  | \n" +
                "1. num1 : DOUBLE   | \n" +
                "\n" +
                "target vars:\n" +
                "> target : NOMINAL [?,B,A]\n" +
                "\n" +
                "prior probabilities:\n" +
                "> P(target='B')=0.5\n" +
                "> P(target='A')=0.5\n" +
                "numerical estimators:\n" +
                "> num1 : GaussianPdf {A~Normal(mu=0.0614354, sd=1.0404937), B~Normal(mu=9.8896993, sd=1.1051043)}\n" +
                "nominal estimators:\n" +
                "> nom1 : MultinomialPmf\n", nb.toSummary());

        nb.withNumEstimator(new KernelPdf()).fit(dfGood, "target");
        assertEquals("NaiveBayes model\n" +
                "================\n" +
                "\n" +
                "Description:\n" +
                "NaiveBayes(numEstimator=EmpiricKDE, nomEstimator=MultinomialPmf)\n" +
                "\n" +
                "Capabilities:\n" +
                "types inputs/targets: BINARY,INT,NOMINAL,DOUBLE/NOMINAL\n" +
                "counts inputs/targets: [0,1000000] / [1,1]\n" +
                "missing inputs/targets: true/false\n" +
                "\n" +
                "Learned model:\n" +
                "input vars: \n" +
                "0. nom1 : NOMINAL  | \n" +
                "1. num1 : DOUBLE   | \n" +
                "\n" +
                "target vars:\n" +
                "> target : NOMINAL [?,B,A]\n" +
                "\n" +
                "prior probabilities:\n" +
                "> P(target='B')=0.5\n" +
                "> P(target='A')=0.5\n" +
                "numerical estimators:\n" +
                "> num1 : EmpiricKDE{ KFuncGaussian }\n" +
                "nominal estimators:\n" +
                "> nom1 : MultinomialPmf\n", nb.toSummary());

        assertEquals("NaiveBayes(numEstimator=EmpiricKDE, nomEstimator=MultinomialPmf)", nb.fullName());
        assertEquals(nb.toContent(), nb.toSummary());
        assertEquals(nb.toFullContent(), nb.toSummary());
    }

}
