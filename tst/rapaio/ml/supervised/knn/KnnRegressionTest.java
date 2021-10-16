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

package rapaio.ml.supervised.knn;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.ml.common.distance.EuclideanDistance;
import rapaio.ml.supervised.RegressionResult;

public class KnnRegressionTest {

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(42);
    }

    @Test
    void builderTest() {
        KnnRegression m1 = KnnRegression.newModel()
                .k.set(10)
                .distance.set(new EuclideanDistance())
                .wdistance.set(new EuclideanDistance())
                .tol.set(1e-10)
                .kernel.set(KnnRegression.Kernel.COS);

        KnnRegression m2 = m1.newInstance();
        assertEquals(10, m2.k.get());
        assertEquals(new EuclideanDistance().name(), m2.distance.get().name());
        assertEquals(new EuclideanDistance().name(), m2.wdistance.get().name());
        assertEquals(1e-10, m2.tol.get());
        assertEquals(KnnRegression.Kernel.COS.name(), m2.kernel.get().name());
    }

    @Test
    void overfitTest() {
        VarDouble x = Normal.std().sample(100).name("x").op().sort();
        VarDouble y = VarDouble.from(x, v -> v * 10 + Normal.std().sampleNext()).name("y");

        for (KnnRegression.Kernel kernel : KnnRegression.Kernel.values()) {
            KnnRegression model = KnnRegression.newModel()
                    .k.set(1)
                    .tol.set(1e-100)
                    .kernel.set(kernel);

            model.fit(SolidFrame.byVars(x, y), "y");
            RegressionResult prediction = model.predict(SolidFrame.byVars(x));

            for (int i = 0; i < x.size(); i++) {
                assertEquals(y.getDouble(i), prediction.firstPrediction().getDouble(i), 1e-9);
            }
        }
    }
}
