/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.ml.model.km;

import static java.lang.StrictMath.abs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Gamma;
import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.data.transform.Jitter;
import rapaio.datasets.Datasets;
import rapaio.narray.NArrays;
import rapaio.ml.eval.RandIndex;
import rapaio.util.collection.DoubleArrays;

public class MWKMeansTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    @Test
    void minimumTest() {
        MWKMeans mwk = MWKMeans.newMWKMeans().seed.set(42L);
        for (int t = 0; t < 10; t++) {

            var dist = Gamma.of(1, 0.5);
            var array = DoubleArrays.newFrom(0, 1_000, i -> dist.sampleNext(random));
            var y = NArrays.stride(array).mul_(10.).sort_(0, true);
            double beta = random.nextDouble() * 10 + 1;

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
        iris = iris.fapply(Jitter.on(new Random(42), 1, VarRange.all()));
        Var target = Datasets.loadIrisDataset().rvar(4);

        int p = 3;
        MWKMeans model = MWKMeans.newMWKMeans()
                .subspace.set(false)
                .k.set(3)
                .p.set(p)
                .nstart.set(3)
                .seed.set(42L);
        model.fit(iris);

        assertTrue(model.hasLearned());
        assertEquals(3, model.getCentroidsMatrix().dim(0));
        assertEquals(4, model.getCentroidsMatrix().dim(1));

        MWKMeansResult result = model.predict(iris);
        var prediction = result.assignment();

        assertTrue(RandIndex.from(target, prediction).getRandIndex() > 0.6);
    }

    @Test
    void irisTestLocalWeights() {
        Frame iris = Datasets.loadIrisDataset().mapVars(VarRange.onlyTypes(VarType.DOUBLE));
        iris = iris.fapply(Jitter.on(random, 1, VarRange.all()));
        Var target = Datasets.loadIrisDataset().rvar(4);

        int p = 3;
        MWKMeans model = MWKMeans.newMWKMeans()
                .subspace.set(true)
                .k.set(3)
                .p.set(p)
                .nstart.set(3);
        model.fit(iris);

        assertTrue(model.hasLearned());
        assertEquals(3, model.getCentroidsMatrix().dim(0));
        assertEquals(4, model.getCentroidsMatrix().dim(1));

        MWKMeansResult result = model.predict(iris);
        var prediction = result.assignment();

        assertTrue(RandIndex.from(target, prediction).getRandIndex() > 0.6);
    }

    @Test
    void testPrinting() {
        Frame iris = Datasets.loadIrisDataset().mapVars(VarRange.onlyTypes(VarType.DOUBLE));
        iris = iris.fapply(Jitter.on(random, 1, VarRange.all()));

        int p = 3;
        MWKMeans model = MWKMeans.newMWKMeans()
                .subspace.set(true)
                .k.set(3)
                .p.set(p)
                .nstart.set(3)
                .seed.set(42L);
        model.fit(iris);

        assertEquals("MWKMeans{k=3,nstart=3,p=3,seed=42,subspace clustering flag=true}, fitted=true", model.toString());

        assertEquals("""
                MWKMeans{k=3,nstart=3,p=3,seed=42,subspace clustering flag=true}
                Model fitted=true
                Inertia:13.439579060524519
                Iterations:10
                Clusters:3
                """, model.toSummary());

        assertEquals("""
                MWKMeans{k=3,nstart=3,p=3,seed=42,subspace clustering flag=true}
                Model fitted=true
                Inertia:13.439579060524519
                Iterations:10
                Clusters:3
                """, model.toContent());

        assertEquals("""
                MWKMeans{k=3,nstart=3,p=3,seed=42,subspace clustering flag=true}
                Model fitted=true
                Inertia:13.439579060524519
                Iterations:10
                Clusters:3
                Centroids:
                [[ 5.705461136317303 3.6231194187349014 1.1785683307386638 0.25564309954691256 ] \s
                 [ 5.986777993511128 3.7180748362391958 5.235773581125203  1.6968020429242028  ] \s
                 [ 5.459718618661379 2.5561069767231737 3.7862023884050187 1.0367951120759455  ]]\s
                Weights:
                [[ 0.1769577085353604 0.1859042556649356  0.3325918758872587  0.3045461599124453  ] \s
                 [ 0.1773231628415696 0.2709335147002507  0.18741222233232233 0.36433110012585734 ] \s
                 [ 0.4256811053679402 0.25814780484782507 0.17781752968402195 0.13835356010021277 ]]\s
                """, model.toFullContent());
    }
}
