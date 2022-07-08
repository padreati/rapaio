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

package rapaio.ml.model.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import rapaio.datasets.Datasets;

public class ManualClassifierTest {

    @Test
    void smokeTest() {
        var iris = Datasets.loadIrisDataset();

        ManualClassifier c = ManualClassifier.model()
                .rules.add(s -> s.getDouble("sepal-length") <= 6 ? "setosa" : null)
                .rules.add(s -> s.getDouble("petal-length") > 5 ? "virginica" : null)
                .rules.add(s -> "versicolor");

        var predict = c.fit(iris, "class").predict(iris).firstClasses();

        for (int i = 0; i < iris.rowCount(); i++) {
            String label = "versicolor";
            if(iris.getDouble(i, "sepal-length") <= 6) {
                label = "setosa";
            } else {
                if(iris.getDouble(i, "petal-length")>5) {
                    label = "virginica";
                }
            }

            assertEquals(label, predict.getLabel(i));
        }
    }
}
