/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.model.knn;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Normal;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.ml.common.distance.EuclideanDistance;
import rapaio.ml.model.RegressionResult;

public class KnnRegressionTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    @Test
    void builderTest() {
        KnnRegression m1 = KnnRegression.newModel()
                .k.set(10)
                .distance.set(new EuclideanDistance())
                .wdistance.set(new EuclideanDistance())
                .eps.set(1e-10)
                .kernel.set(KnnRegression.Kernel.COS)
                .seed.set(42L);

        KnnRegression m2 = m1.newInstance();
        assertEquals(10, m2.k.get());
        assertEquals(new EuclideanDistance().name(), m2.distance.get().name());
        assertEquals(new EuclideanDistance().name(), m2.wdistance.get().name());
        assertEquals(1e-10, m2.eps.get());
        assertEquals(KnnRegression.Kernel.COS.name(), m2.kernel.get().name());
    }

    @Test
    void overfitTest() {
        VarDouble x = Normal.std().sample(100).name("x");
        x.dt().sort_(0, true);
        VarDouble y = VarDouble.from(x, v -> v * 10 + Normal.std().sampleNext()).name("y");

        for (KnnRegression.Kernel kernel : KnnRegression.Kernel.values()) {
            KnnRegression model = KnnRegression.newModel()
                    .k.set(1)
                    .eps.set(1e-100)
                    .kernel.set(kernel);

            model.fit(SolidFrame.byVars(x, y), "y");
            RegressionResult prediction = model.predict(SolidFrame.byVars(x));

            for (int i = 0; i < x.size(); i++) {
                assertEquals(y.getDouble(i), prediction.firstPrediction().getDouble(i), 1e-9);
            }
        }
    }
}
