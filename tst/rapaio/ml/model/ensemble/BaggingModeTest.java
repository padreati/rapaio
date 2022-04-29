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

package rapaio.ml.model.ensemble;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.data.VarType;
import rapaio.ml.model.ClassifierModel;
import rapaio.ml.model.ClassifierResult;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/14/20.
 */
@ExtendWith(MockitoExtension.class)
public class BaggingModeTest {

    private static final List<String> dictionary = List.of("?", "a", "b", "c");

    private List<ClassifierResult> results;
    private VarNominal classes;
    private Frame densities;

    @BeforeEach
    void beforeEach() {
        results = new ArrayList<>();
        results.add(buildResult(0, 0.6, 0, 0.4));
        results.add(buildResult(0, 0.5, 0.1, 0.4));
        results.add(buildResult(0, 0, 0.1, 0.9));

        classes = VarNominal.empty(1, "a", "b", "c");
        densities = SolidFrame.byVars(
                VarDouble.empty(1).name("?"),
                VarDouble.empty(1).name("a"),
                VarDouble.empty(1).name("b"),
                VarDouble.empty(1).name("c")
        );
    }

    @Test
    void votingTest() {
        BaggingMode.VOTING.computeDensity(dictionary, results, classes, densities);
        assertEquals("a", classes.getLabel(0));
    }

    @Test
    void distributionTest() {
        BaggingMode.DISTRIBUTION.computeDensity(dictionary, results, classes, densities);
        assertEquals(1.1 / 3, densities.getDouble(0, "a"));
        assertEquals(0.2 / 3, densities.getDouble(0, "b"));
        assertEquals(1.7 / 3, densities.getDouble(0, "c"), 1e-12);

        assertEquals("c", classes.getLabel(0));
    }

    private ClassifierResult buildResult(double... probabilities) {

        ClassifierModel<?, ?, ?> model = mock(ClassifierModel.class);
        Frame df = mock(Frame.class);

        when(model.targetNames()).thenReturn(new String[] {"y"});
        when(model.targetTypes()).thenReturn(new VarType[] {VarType.NOMINAL});
        when(model.targetLevels(anyString())).thenReturn(dictionary);
        when(df.rowCount()).thenReturn(1);

        ClassifierResult result = ClassifierResult.build(model, df, true, true);
        Var firstClasses = result.firstClasses();
        Frame firstDensity = result.firstDensity();

        int max = 0;
        double pMax = probabilities[0];
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > pMax) {
                max = i;
                pMax = probabilities[i];
            }
        }

        for (int i = 0; i < firstClasses.size(); i++) {
            firstClasses.setLabel(i, dictionary.get(max));
            for (int j = 0; j < dictionary.size(); j++) {
                String label = dictionary.get(j);
                firstDensity.setDouble(i, label, probabilities[j]);
            }
        }

        return result;
    }
}
