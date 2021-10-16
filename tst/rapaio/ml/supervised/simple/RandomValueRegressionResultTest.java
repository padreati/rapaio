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

package rapaio.ml.supervised.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.distributions.Uniform;
import rapaio.core.tests.KSTestOneSample;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/9/19.
 */
public class RandomValueRegressionResultTest {

    private final String father = "Father";
    private final String son = "Son";
    private Frame df;

    @BeforeEach
    void setUp() throws Exception {
        RandomSource.setSeed(123);
        df = Datasets.loadPearsonHeightDataset();
    }

    @Test
    void testRandomValueRegression() {
        var fit1 = RandomValueRegression.newRVR().fit(df, father).predict(df);
        var fit2 = RandomValueRegression.from(Normal.of(10, 0.1)).fit(df, father).predict(df);

        // unsignificant if test on true distribution
        assertTrue(KSTestOneSample.from(fit1.firstPrediction(), Uniform.of(0, 1)).pValue() > 0.01);
        assertTrue(KSTestOneSample.from(fit2.firstPrediction(), Normal.of(10, 0.1)).pValue() > 0.01);

        // significant if test on a different distribution
        assertTrue(KSTestOneSample.from(fit1.firstPrediction(), Normal.of(10, 0.1)).pValue() < 0.01);
        assertTrue(KSTestOneSample.from(fit2.firstPrediction(), Uniform.of(0, 1)).pValue() < 0.01);
    }

    @Test
    void testNaming() {
        RandomValueRegression model = RandomValueRegression.newRVR();
        assertEquals("RandomValueRegression", model.name());
        assertEquals("RandomValueRegression{}", model.newInstance().fullName());

        assertEquals("Normal(mu=10, sd=20)", RandomValueRegression.from(Normal.of(10, 20)).newInstance().distribution.get().name());

        assertEquals("""
                Regression predict summary
                =======================
                Model class: RandomValueRegression
                Model instance: RandomValueRegression{}
                > model not trained.
                """, model.toContent());

        model = model.fit(df, "Son");
        assertEquals("RandomValueRegression{}", model.toString());
        assertEquals("""
                Regression predict summary
                =======================
                Model class: RandomValueRegression
                Model instance: RandomValueRegression{}
                > model is trained.
                > input variables:\s
                1. Father dbl\s
                > target variables:\s
                1. Son dbl\s
                Model is trained.
                """, model.toContent());
        assertEquals("""
                Regression predict summary
                =======================
                Model class: RandomValueRegression
                Model instance: RandomValueRegression{}
                > model is trained.
                > input variables:\s
                1. Father dbl\s
                > target variables:\s
                1. Son dbl\s
                Model is trained.
                """, model.toFullContent());
        assertEquals("""
                Regression predict summary
                =======================
                Model class: RandomValueRegression
                Model instance: RandomValueRegression{}
                > model is trained.
                > input variables:\s
                1. Father dbl\s
                > target variables:\s
                1. Son dbl\s
                Model is trained.
                """, model.toSummary());
    }
}
