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

package rapaio.core.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.distributions.StudentT;
import rapaio.core.distributions.Uniform;
import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.datasets.Datasets;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class KSTestTest {

    @Test
    void testPearson() throws IOException {
        RandomSource.setSeed(1);
        Frame df = Datasets.loadPearsonHeightDataset();
        KSTestTwoSamples test = KSTestTwoSamples.from(df.rvar("Son"), df.rvar("Father"));

        assertEquals(0.150278, test.d(), 10e-5);
        assertEquals(0.0000000000411316, test.pValue(), 10e-10);
    }

    @Test
    void testNormal() {
        RandomSource.setSeed(1);
        Normal d = Normal.std();
        VarDouble sample = d.sample(1000);
        KSTestOneSample test = KSTestOneSample.from(sample, d);
        assertTrue(test.d() < 0.4);
        assertTrue(test.pValue() > 0.08);
    }

    @Test
    void testUniform() {
        RandomSource.setSeed(1);
        VarDouble sample = Uniform.of(0, 1).sample(1_000);
        KSTestOneSample test = KSTestOneSample.from(sample, Normal.std());
        assertTrue(test.d() > 0.4);
        assertTrue(test.pValue() < 0.001);
    }

    @Test
    void testStudentT() {
        RandomSource.setSeed(1);
        StudentT d = StudentT.of(3, 0, 1);
        VarDouble sample = d.sample(1000);
        KSTestOneSample test = KSTestOneSample.from(sample, Normal.std());
        assertTrue(test.d() > 0.04);
        assertTrue(test.pValue() < 0.05);
    }
}
