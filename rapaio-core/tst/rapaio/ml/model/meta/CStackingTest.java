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

package rapaio.ml.model.meta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

import rapaio.core.SamplingTools;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.ml.eval.metric.Accuracy;
import rapaio.ml.model.linear.BinaryLogistic;
import rapaio.ml.model.svm.SvmClassifier;
import rapaio.ml.model.tree.CTree;

public class CStackingTest {

    private final Frame iris = Datasets.loadIrisDataset();
    private static final String target = "class";

    @Test
    void buildTest() {
        CStacking s = CStacking.newModel();
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> s.fit(iris, target));
        assertEquals("Stack model is not configured.", ex.getMessage());
        ex = assertThrows(IllegalStateException.class, () -> s.stackModel.set(BinaryLogistic.newModel()).fit(iris, target));
        assertEquals("At least a weak learner is needed.", ex.getMessage());
    }

    @Test
    void smokeTest() {
        CStacking s = CStacking.newModel()
                .seed.set(1L)
                .learners.add(new CTree())
                .learners.add(new CTree())
                .learners.add(new CTree())
                .learners.add(new CTree())
                .stackModel.set(new SvmClassifier());
        var tts = SamplingTools.trainTestSplit(new Random(1223), iris, 0.8);
        var pred = s.fit(tts.trainDf(), target).predict(tts.testDf());
        double accuracy = Accuracy.newMetric(true).compute(tts.testDf().rvar(target), pred).getScore().value();
        assertTrue(accuracy>0.9);
    }
}
