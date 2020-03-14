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

import org.junit.jupiter.api.Test;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.ClassifierResult;
import rapaio.ml.classifier.bayes.nb.Estimator;
import rapaio.ml.classifier.bayes.nb.GaussianEstimator;
import rapaio.ml.classifier.bayes.nb.KernelEstimator;
import rapaio.ml.classifier.bayes.nb.PriorUniform;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/24/14.
 */
public class NaiveBayesTest {

    private static final double TOLERANCE = 1e-12;

    @Test
    void testBuilders() {

        var iris = Datasets.loadIrisDataset();

        NaiveBayes nb = NaiveBayes.newModel();
        assertEquals("NaiveBayes", nb.name());
        assertEquals("NaiveBayes{prior=MLE{},estimators=[]}", nb.fullName());
        assertEquals("MLE", nb.getPrior().name());

        // change default parameters

        nb.withEstimators(GaussianEstimator.forType(iris, VType.DOUBLE));
        nb.withPriorSupplier(new PriorUniform());

        assertEquals("NaiveBayes", nb.name());
        assertEquals("NaiveBayes{prior=Uniform{value=?,targetLevels=[]}," +
                "estimators=[Gaussian{test=sepal-length, values=[]},Gaussian{test=sepal-width, values=[]}," +
                "Gaussian{test=petal-length, values=[]},Gaussian{test=petal-width, values=[]}]}", nb.fullName());
        assertEquals("Uniform", nb.getPrior().name());

        // fit it to see changes

        nb.fit(iris, VarDouble.fill(iris.rowCount(), 1), "class");

        assertEquals("NaiveBayes{prior=Uniform{value=0.3333333,targetLevels=[virginica,setosa,versicolor]}," +
                "estimators=[Gaussian{test=sepal-length, values=[virginica:Normal(mu=6.588, sd=0.6294887), " +
                "setosa:Normal(mu=5.006, sd=0.348947), versicolor:Normal(mu=5.936, sd=0.5109834)]}," +
                "Gaussian{test=sepal-width, values=[virginica:Normal(mu=2.974, sd=0.3192554), " +
                "setosa:Normal(mu=3.428, sd=0.3752546), versicolor:Normal(mu=2.77, sd=0.3106445)]}," +
                "Gaussian{test=petal-length, values=[virginica:Normal(mu=5.552, sd=0.5463479), " +
                "setosa:Normal(mu=1.462, sd=0.1719186), versicolor:Normal(mu=4.26, sd=0.4651881)]}," +
                "Gaussian{test=petal-width, values=[virginica:Normal(mu=2.026, sd=0.2718897), " +
                "setosa:Normal(mu=0.246, sd=0.1043264), versicolor:Normal(mu=1.326, sd=0.1957652)]}]}", nb.fullName());

        // test now if new instance produce a default with settings

        var copy = nb.newInstance();

        assertEquals("NaiveBayes{prior=Uniform{value=?,targetLevels=[]}," +
                "estimators=[Gaussian{test=sepal-length, values=[]},Gaussian{test=sepal-width, values=[]}," +
                "Gaussian{test=petal-length, values=[]},Gaussian{test=petal-width, values=[]}]}", copy.fullName());
    }

    @Test
    void testEstimatorsHandling() {

        assertEquals(0, NaiveBayes.newModel().getEstimators().size());

        assertEquals(Arrays.asList("a", "b", "x", "y"), NaiveBayes.newModel()
                .withEstimators(GaussianEstimator.forNames("a", "b"))
                .withEstimators(GaussianEstimator.forName("x"))
                .withEstimators(KernelEstimator.forName("y"))
                .getEstimators().stream().flatMap(e -> e.getTestNames().stream()).collect(Collectors.toList())
        );

        var ex = assertThrows(IllegalArgumentException.class, () -> NaiveBayes.newModel()
                .withEstimators(GaussianEstimator.forName("a")).withEstimators(GaussianEstimator.forName("a")));
        assertEquals("Cannot add estimator since it contains variable: a which is already handled by Gaussian{test=a}", ex.getMessage());
    }

    @Test
    void testInvalidFit() {
        var ex = assertThrows(IllegalStateException.class, () -> NaiveBayes.newModel()
                .withEstimators(GaussianEstimator.forName("a"))
                .fit(SolidFrame.byVars(VarNominal.copy("a", "b").withName("y")), "y"));
        assertEquals("Input variable: a is not contained in training data frame.", ex.getMessage());
    }

    @Test
    void testPrediction() {

        Estimator estimator = new Estimator() {
            private static final long serialVersionUID = 5459709521908513314L;

            @Override
            public Estimator newInstance() {
                return null;
            }

            @Override
            public String name() {
                return null;
            }

            @Override
            public String fittedName() {
                return null;
            }

            @Override
            public List<String> getTestNames() {
                return Collections.singletonList("a");
            }

            @Override
            public boolean fit(Frame df, Var weights, String targetName) {
                return true;
            }

            @Override
            public double predict(Frame df, int row, String targetLevel) {
                if (targetLevel.equals("a")) {
                    return 1 / (1 + Math.exp(row / 10.));
                } else {
                    return 1 / (1 + Math.exp(-row / 10.));
                }
            }
        };
        NaiveBayes model = NaiveBayes.newModel()
                .withEstimators(estimator);

        Frame df = SolidFrame.byVars(
                VarNominal.from(100, row -> row > 0 ? "a" : "b").withName("a"),
                VarNominal.from(100, row -> row > 0 ? "a" : "b").withName("t")
        );
        model.fit(df, "t");
        ClassifierResult<NaiveBayes> result = model.predict(df, true, true);


        Frame densities = result.firstDensity();
        for (int i = 0; i < densities.rowCount(); i++) {
            assertEquals(0, densities.getDouble(i, 0), TOLERANCE);

            double r1 = 1 / (1 + Math.exp(i / 10.)) * model.getPrior().computePrior("a");
            double r2 = 1 / (1 + Math.exp(-i / 10.)) * model.getPrior().computePrior("b");
            double sum = r1 + r2;
            r1 /= sum;
            r2 /= sum;

            assertEquals(r1, densities.getDouble(i, "a"), TOLERANCE);
            assertEquals(r2, densities.getDouble(i, "b"), TOLERANCE);
        }
    }

    @Test
    void testPrinter() {
        Frame iris = Datasets.loadIrisDataset();
        NaiveBayes model = NaiveBayes.newModel().withEstimators(GaussianEstimator.forType(iris, VType.DOUBLE));

        assertEquals("NaiveBayes{prior=MLE{},estimators=[" +
                "Gaussian{test=sepal-length, values=[]}," +
                "Gaussian{test=sepal-width, values=[]}," +
                "Gaussian{test=petal-length, values=[]}," +
                "Gaussian{test=petal-width, values=[]}]}", model.toString());

        assertEquals("NaiveBayes model\n" +
                "================\n" +
                "\n" +
                "Capabilities:\n" +
                "types inputs/targets: NOMINAL,DOUBLE,INT,BINARY/NOMINAL,BINARY\n" +
                "counts inputs/targets: [0,1000000] / [1,1]\n" +
                "missing inputs/targets: true/false\n" +
                "\n" +
                "Model not fitted.\n" +
                "\n" +
                "Prior: MLE\n" +
                "Estimators: \n" +
                "\t- Gaussian{test=sepal-length, values=[]}\n" +
                "\t- Gaussian{test=sepal-width, values=[]}\n" +
                "\t- Gaussian{test=petal-length, values=[]}\n" +
                "\t- Gaussian{test=petal-width, values=[]}\n", model.toContent());
        assertEquals(model.toSummary(), model.toContent());
        assertEquals(model.toFullContent(), model.toContent());

        model.fit(iris, "class");

        assertEquals("NaiveBayes{prior=MLE{virginica:0.3333333,setosa:0.3333333,versicolor:0.3333333},estimators=[" +
                "Gaussian{test=sepal-length, values=[virginica:Normal(mu=6.588, sd=0.6294887), " +
                "setosa:Normal(mu=5.006, sd=0.348947), versicolor:Normal(mu=5.936, sd=0.5109834)]}," +
                "Gaussian{test=sepal-width, values=[virginica:Normal(mu=2.974, sd=0.3192554), " +
                "setosa:Normal(mu=3.428, sd=0.3752546), versicolor:Normal(mu=2.77, sd=0.3106445)]}," +
                "Gaussian{test=petal-length, values=[virginica:Normal(mu=5.552, sd=0.5463479), " +
                "setosa:Normal(mu=1.462, sd=0.1719186), versicolor:Normal(mu=4.26, sd=0.4651881)]}," +
                "Gaussian{test=petal-width, values=[virginica:Normal(mu=2.026, sd=0.2718897), " +
                "setosa:Normal(mu=0.246, sd=0.1043264), versicolor:Normal(mu=1.326, sd=0.1957652)]}]}", model.toString());

        assertEquals("NaiveBayes model\n" +
                "================\n" +
                "\n" +
                "Capabilities:\n" +
                "types inputs/targets: NOMINAL,DOUBLE,INT,BINARY/NOMINAL,BINARY\n" +
                "counts inputs/targets: [0,1000000] / [1,1]\n" +
                "missing inputs/targets: true/false\n" +
                "\n" +
                "Model is fitted.\n" +
                "\n" +
                "input vars: \n" +
                "0. sepal-length : DOUBLE  | \n" +
                "1.  sepal-width : DOUBLE  | \n" +
                "2. petal-length : DOUBLE  | \n" +
                "3.  petal-width : DOUBLE  | \n" +
                "\n" +
                "target vars:\n" +
                "> class : NOMINAL [?,setosa,versicolor,virginica]\n" +
                "\n" +
                "Prior: MLE{virginica:0.3333333,setosa:0.3333333,versicolor:0.3333333}\n" +
                "Estimators: \n" +
                "\t- Gaussian{test=sepal-length, values=[virginica:Normal(mu=6.588, sd=0.6294887), setosa:Normal(mu=5.006, sd=0.348947), versicolor:Normal(mu=5.936, sd=0.5109834)]}\n" +
                "\t- Gaussian{test=sepal-width, values=[virginica:Normal(mu=2.974, sd=0.3192554), setosa:Normal(mu=3.428, sd=0.3752546), versicolor:Normal(mu=2.77, sd=0.3106445)]}\n" +
                "\t- Gaussian{test=petal-length, values=[virginica:Normal(mu=5.552, sd=0.5463479), setosa:Normal(mu=1.462, sd=0.1719186), versicolor:Normal(mu=4.26, sd=0.4651881)]}\n" +
                "\t- Gaussian{test=petal-width, values=[virginica:Normal(mu=2.026, sd=0.2718897), setosa:Normal(mu=0.246, sd=0.1043264), versicolor:Normal(mu=1.326, sd=0.1957652)]}\n", model.toContent());
        assertEquals(model.toSummary(), model.toContent());
        assertEquals(model.toFullContent(), model.toContent());

    }
}


