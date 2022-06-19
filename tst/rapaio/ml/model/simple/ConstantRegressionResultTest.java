/*
 *
 *  * Apache License
 *  * Version 2.0, January 2004
 *  * http://www.apache.org/licenses/
 *  *
 *  * Copyright 2013 - 2022 Aurelian Tutuianu
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *  http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package rapaio.ml.model.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.data.filter.VToDouble;
import rapaio.datasets.Datasets;

/**
 * Test for simple regression algorithms
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/7/16.
 */
public class ConstantRegressionResultTest {

    private final String father = "Father";
    private final String son = "Son";
    private Frame df;

    @BeforeEach
    void setUp() throws Exception {
        df = Datasets.loadPearsonHeightDataset();
    }

    @Test
    void testConstantRegression() {

        ConstantRegression r1 = ConstantRegression.with(66).newInstance();
        r1.fit(df, father);
        var fit1 = r1.predict(df);
        assertEquals("""
                Regression predict summary
                =======================
                Model class: ConstantRegression
                Model instance: ConstantRegression{constant=66}
                > model is trained.
                > input variables:\s
                1. Son dbl\s
                > target variables:\s
                1. Father dbl\s

                Fitted values:

                Target Estimate\s
                Father    66   \s

                """, r1.toSummary());

        ConstantRegression r2 = ConstantRegression.with(1);
        r2.fit(df, father);
        var fit2 = r2.predict(df, true);

        assertTrue(VarDouble.fill(df.rowCount(), 66).name("Father")
                .deepEquals(fit1.firstPrediction()));
        assertFalse(df.rvar(father).copy().fapply(VToDouble.byValue(x -> x - 66)).name("Father")
                .deepEquals(fit1.firstResidual()));

        assertTrue(VarDouble.fill(df.rowCount(), 1).name("Father")
                .deepEquals(fit2.firstPrediction()));
        assertTrue(df.rvar(father).copy().fapply(VToDouble.byValue(x -> x - 1)).name("Father")
                .deepEquals(fit2.firstResidual()));

        assertEquals("ConstantRegression{constant=66}", r1.toString());
        assertEquals("ConstantRegression{constant=66}", r1.toContent());
        assertEquals("ConstantRegression{constant=66}", r1.toFullContent());
        assertEquals("""
                Regression predict summary
                =======================
                Model class: ConstantRegression
                Model instance: ConstantRegression{constant=66}
                > model is trained.
                > input variables:\s
                1. Son dbl\s
                > target variables:\s
                1. Father dbl\s

                Fitted values:

                Target Estimate\s
                Father    66   \s

                """, r1.toSummary());
    }
}
