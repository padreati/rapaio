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

package rapaio.ml.classifier.rule.onerule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.ml.classifier.rule.OneRule;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/31/20.
 */
public class HolteBinningTest {

    private static final double TOLERANCE = 1e-12;

    @BeforeEach
    void beforeEach() {

    }

    @Test
    void testName() {
        assertEquals("HolteBinning(minCount=2)", new HolteBinning(2).name());
    }

    @Test
    void testMinimumOne() {

        VarDouble x = VarDouble.copy(1, 1, 2, 2, 3, 3, 4, 5, 6).name("x");
        VarDouble w = VarDouble.fill(x.size(), 1);
        VarNominal y = VarNominal.copy("a", "a", "b", "b", "c", "c", "a", "b", "c").name("y");
        Frame df = SolidFrame.byVars(x, y);

        OneRule model = OneRule.newModel();
        model.fit(df, "y");
        HolteBinning binning = new HolteBinning(1);
        RuleSet set = binning.compute("x", model, df, w);

        assertNotNull(set);
        assertEquals(1, set.getAccuracy(), TOLERANCE);
        assertEquals("x", set.getVarName());

        assertEquals(6, set.getRules().size());
        for (int i = 0; i < 6; i++) {
            NumericRule rule = (NumericRule) set.getRules().get(i);
            assertEquals(i == 0 ? Double.NEGATIVE_INFINITY : i + 0.5, rule.getMinValue(), TOLERANCE);
            assertEquals(i == 5 ? Double.POSITIVE_INFINITY : i + 1.5, rule.getMaxValue(), TOLERANCE);
            assertFalse(rule.isMissingValue());
            if (i < 3) {
                assertEquals(0, rule.getErrorCount(), TOLERANCE);
                assertEquals(2, rule.getTotalCount(), TOLERANCE);
            } else {
                assertEquals(0, rule.getErrorCount(), TOLERANCE);
                assertEquals(1, rule.getTotalCount(), TOLERANCE);
            }
        }
    }

    @Test
    void testMinimumTwo() {

        VarDouble x = VarDouble.copy(1, 1, 2, 2, 3, 3, 4, 5, 6).name("x");
        VarDouble w = VarDouble.fill(x.size(), 1);
        VarNominal y = VarNominal.copy("a", "a", "b", "b", "c", "c", "a", "b", "c").name("y");
        Frame df = SolidFrame.byVars(x, y);

        OneRule model = OneRule.newModel();
        model.fit(df, "y");
        HolteBinning binning = new HolteBinning(2);
        RuleSet set = binning.compute("x", model, df, w);

        assertNotNull(set);
        assertEquals(0.7777777777777778, set.getAccuracy(), TOLERANCE);
        assertEquals("x", set.getVarName());

        assertEquals(4, set.getRules().size());
        for (int i = 0; i < 4; i++) {
            NumericRule rule = (NumericRule) set.getRules().get(i);
            assertEquals(i == 0 ? Double.NEGATIVE_INFINITY : i + 0.5, rule.getMinValue(), TOLERANCE);
            assertEquals(i == 3 ? Double.POSITIVE_INFINITY : i + 1.5, rule.getMaxValue(), TOLERANCE);
            assertFalse(rule.isMissingValue());
            if (i < 3) {
                assertEquals(0, rule.getErrorCount(), TOLERANCE);
                assertEquals(2, rule.getTotalCount(), TOLERANCE);
            } else {
                assertEquals(2, rule.getErrorCount(), TOLERANCE);
                assertEquals(3, rule.getTotalCount(), TOLERANCE);
            }
        }
    }


    @Test
    void testMinimumThree() {

        VarDouble x = VarDouble.copy(1, 1, 2, 2, 3, 3, 4, 5, 6).name("x");
        VarDouble w = VarDouble.fill(x.size(), 1);
        VarNominal y = VarNominal.copy("a", "a", "b", "b", "c", "c", "a", "b", "c").name("y");
        Frame df = SolidFrame.byVars(x, y);

        OneRule model = OneRule.newModel();
        model.fit(df, "y");
        HolteBinning binning = new HolteBinning(3);
        RuleSet set = binning.compute("x", model, df, w);

        assertNotNull(set);
        assertEquals(0.444444444444444444, set.getAccuracy(), TOLERANCE);
        assertEquals("x", set.getVarName());

        assertEquals(2, set.getRules().size());

        NumericRule rule = (NumericRule) set.getRules().get(0);
        assertEquals(Double.NEGATIVE_INFINITY, rule.getMinValue(), TOLERANCE);
        assertEquals(4.5, rule.getMaxValue(), TOLERANCE);
        assertFalse(rule.isMissingValue());
        assertEquals(4, rule.getErrorCount(), TOLERANCE);
        assertEquals(7, rule.getTotalCount(), TOLERANCE);

        rule = (NumericRule) set.getRules().get(1);
        assertEquals(4.5, rule.getMinValue(), TOLERANCE);
        assertEquals(Double.POSITIVE_INFINITY, rule.getMaxValue(), TOLERANCE);
        assertFalse(rule.isMissingValue());
        assertEquals(1, rule.getErrorCount(), TOLERANCE);
        assertEquals(2, rule.getTotalCount(), TOLERANCE);
    }

    @Test
    void testWithMissing() {
        VarDouble x = VarDouble.copy(1., 1, 1, 1, 1, 1, Double.NaN, Double.NaN, 1, 1, 1, Double.NaN).name("x");
        VarNominal y = VarNominal.copy("a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a").name("y");
        VarDouble w = VarDouble.fill(x.size(), 1);
        Frame df = SolidFrame.byVars(x, y);

        OneRule model = OneRule.newModel();
        model.fit(df, "y");
        HolteBinning binning = new HolteBinning(2);
        RuleSet set = binning.compute("x", model, df, w);

        assertEquals(2, set.getRules().size());

        NumericRule rule = (NumericRule)set.getRules().get(0);
        assertEquals(Double.NEGATIVE_INFINITY, rule.getMinValue(), TOLERANCE);
        assertEquals(Double.POSITIVE_INFINITY, rule.getMaxValue(), TOLERANCE);
        assertFalse(rule.isMissingValue());
        rule = (NumericRule) set.rules.get(1);
        assertTrue(rule.isMissingValue());
        assertEquals(Double.NaN, rule.getMinValue(), TOLERANCE);
        assertEquals(Double.NaN, rule.getMinValue(), TOLERANCE);
        assertEquals(0, rule.getErrorCount());
        assertEquals(3, rule.getTotalCount());
    }
}
