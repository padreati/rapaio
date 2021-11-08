/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.clustering.km;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static rapaio.math.MathTools.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.distributions.Gamma;
import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.data.filter.FJitter;
import rapaio.datasets.Datasets;
import rapaio.math.linear.DVector;
import rapaio.ml.eval.RandIndex;

public class MWKMeansTest {

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(42);
    }

    @Test
    void minimumTest() {
        MWKMeans mwk = MWKMeans.newMWKMeans();
        for (int t = 0; t < 10; t++) {

            DVector y = DVector.random(1_000, Gamma.of(1, 0.5)).mul(10).sortValues(true);
            double beta = RandomSource.nextDouble() * 10 + 1;

            double c = mwk.findMinimum(y, beta);
            double minError = mwk.error(y, beta, c);

            for (int i = 0; i < 1000; i++) {
                double delta = Normal.of(0, 1e-4).sampleNext();
                double currentError = mwk.error(y, beta, c + delta);
                if (currentError < minError) {
                    assertTrue(abs(currentError - minError) / minError < 1e-12,
                            "currentError: %.20f, minError: %.20f".formatted(currentError, minError));
                }
            }
        }
    }

    @Test
    void irisTestGlobalWeights() {
        Frame iris = Datasets.loadIrisDataset().mapVars(VarRange.onlyTypes(VarType.DOUBLE));
        iris = iris.fapply(FJitter.on(1, VarRange.all()));
        Var target = Datasets.loadIrisDataset().rvar(4);

        double p = 3;
        MWKMeans model = MWKMeans.newMWKMeans()
                .subspace.set(false)
                .k.set(3)
                .p.set(p)
                .nstart.set(3);
        model.fit(iris);

        assertTrue(model.hasLearned());
        assertEquals(3, model.getCentroidsMatrix().rowCount());
        assertEquals(4, model.getCentroidsMatrix().colCount());

        MWKMeansResult result = model.predict(iris);
        var prediction = result.getAssignment();

        assertTrue(RandIndex.from(target, prediction).getRandIndex()>0.6);
    }

    @Test
    void irisTestLocalWeights() {
        Frame iris = Datasets.loadIrisDataset().mapVars(VarRange.onlyTypes(VarType.DOUBLE));
        iris = iris.fapply(FJitter.on(1, VarRange.all()));
        Var target = Datasets.loadIrisDataset().rvar(4);

        double p = 3;
        MWKMeans model = MWKMeans.newMWKMeans()
                .subspace.set(true)
                .k.set(3)
                .p.set(p)
                .nstart.set(3);
        model.fit(iris);

        assertTrue(model.hasLearned());
        assertEquals(3, model.getCentroidsMatrix().rowCount());
        assertEquals(4, model.getCentroidsMatrix().colCount());

        MWKMeansResult result = model.predict(iris);
        var prediction = result.getAssignment();

        assertTrue(RandIndex.from(target, prediction).getRandIndex()>0.6);
    }

    @Test
    void testPrinting() {
        Frame iris = Datasets.loadIrisDataset().mapVars(VarRange.onlyTypes(VarType.DOUBLE));
        iris = iris.fapply(FJitter.on(1, VarRange.all()));

        double p = 3;
        MWKMeans model = MWKMeans.newMWKMeans()
                .subspace.set(true)
                .k.set(3)
                .p.set(p)
                .nstart.set(3);
        model.fit(iris);

        assertEquals("MWKMeans{k=3,nstart=3,p=3,subspace clustering flag=true}, fitted=true", model.toString());

        assertEquals("""
                MWKMeans{k=3,nstart=3,p=3,subspace clustering flag=true}
                Model fitted=true
                Inertia:9.60130221317551
                Iterations:11
                Clusters:3
                """, model.toSummary());

        assertEquals("""
                MWKMeans{k=3,nstart=3,p=3,subspace clustering flag=true}
                Model fitted=true
                Inertia:9.60130221317551
                Iterations:11
                Clusters:3
                """, model.toContent());

        assertEquals("""
                MWKMeans{k=3,nstart=3,p=3,subspace clustering flag=true}
                Model fitted=true
                Inertia:9.60130221317551
                Iterations:11
                Clusters:3
                Centroids:
                                  [0]                [1]               [2]                 [3]\s
                [0] 5.986777993515609 2.9423085358642593 4.191995839461496  2.1017568006493   \s
                [1] 6.800591757353497 3.1893952588470564 5.740304620672073  1.9229173573873266\s
                [2] 5.705461136317303 3.6231194187349014 1.986967451710812 -0.0665784631607151\s
                Weights:
                                    [0]                 [1]                 [2]                 [3]\s
                [0] 0.1255641752675887  0.31483725762707626 0.10186068763814621 0.4577378794671888 \s
                [1] 0.49737953182084893 0.15477617835581178 0.18077398413522214 0.1670703056881172 \s
                [2] 0.22830331555968278 0.1957300412590679  0.15386102479123878 0.42210561839001054\s
                """, model.toFullContent());
    }
}
