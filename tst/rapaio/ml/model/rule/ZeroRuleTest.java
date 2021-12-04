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

package rapaio.ml.model.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import rapaio.data.SolidFrame;
import rapaio.data.VarNominal;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/25/20.
 */
public class ZeroRuleTest {

    private static final double TOL = 1e-20;

    @Test
    void testBuilders() {
        ZeroRule zeroRule = ZeroRule.newModel();
        assertNotNull(zeroRule);
        assertFalse(zeroRule.hasLearned());
    }

    @Test
    void testPrintable() {
        VarNominal target = VarNominal.copy("a", "a", "b").name("target");

        ZeroRule notFitted = ZeroRule.newModel();
        ZeroRule fitted = ZeroRule.newModel().fit(SolidFrame.byVars(target), "target");
        ZeroRule copy = fitted.newInstance();

        assertEquals("ZeroRule", notFitted.name());
        assertEquals("ZeroRule{}", notFitted.fullName());
        assertEquals("ZeroRule{}; fitted=false", notFitted.toString());
        assertEquals("ZeroRule{}; fitted=false", notFitted.toContent());
        assertEquals("ZeroRule{}; fitted=false", notFitted.toFullContent());
        assertEquals("ZeroRule{}; fitted=false", notFitted.toSummary());

        assertEquals("ZeroRule", fitted.name());
        assertEquals("ZeroRule{}", fitted.fullName());
        assertEquals("ZeroRule{}; fitted=true, fittedClass=a", fitted.toString());
        assertEquals("ZeroRule{}; fitted=true, fittedClass=a", fitted.toContent());
        assertEquals("ZeroRule{}; fitted=true, fittedClass=a", fitted.toFullContent());
        assertEquals("ZeroRule{}; fitted=true, fittedClass=a", fitted.toSummary());

        assertEquals("ZeroRule", copy.name());
        assertEquals("ZeroRule{}", copy.fullName());
        assertEquals("ZeroRule{}; fitted=false", copy.toString());
        assertEquals("ZeroRule{}; fitted=false", copy.toContent());
        assertEquals("ZeroRule{}; fitted=false", copy.toFullContent());
        assertEquals("ZeroRule{}; fitted=false", copy.toSummary());
    }

    @Test
    void testPredictionNoInputs() {
        VarNominal target = VarNominal.copy("a", "a", "b").name("target");

        var model = ZeroRule.newModel().fit(SolidFrame.byVars(target), "target");
        var result = model.predict(SolidFrame.byVars(target), true, true);

        assertNotNull(result);
        assertEquals(3, result.firstClasses().size());
        for (int i = 0; i < result.firstDensity().rowCount(); i++) {
            assertEquals(1, result.firstDensity().getDouble(i, 1), TOL);
            assertEquals(0, result.firstDensity().getDouble(i, 2), TOL);
            assertEquals("a", result.firstClasses().getLabel(i));
        }
    }

    @Test
    void testNotTrained() {
        var target = VarNominal.copy("a", "a", "b").name("target");
        var ex = assertThrows(IllegalStateException.class, () -> ZeroRule.newModel().predict(SolidFrame.byVars(target)));
        assertEquals("Model was not trained/fitted on data.", ex.getMessage());
    }
}
