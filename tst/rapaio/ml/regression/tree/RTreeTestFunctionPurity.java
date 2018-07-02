/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.ml.regression.tree;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RTreeTestFunctionPurity {

    private static final double TOL = 1e-20;

    @Test
    public void testWeightedVarGain() {

        RTreeTestPayload payload = new RTreeTestPayload(2);

        payload.totalVar = 100.;
        payload.totalWeight = 1;

        payload.splitVar[0] = 40;
        payload.splitWeight[0] = 0.5;
        payload.splitVar[1] = 40;
        payload.splitWeight[1] = 0.5;

        RTreePurityFunction test = RTreePurityFunction.WEIGHTED_VAR_GAIN;
        assertEquals("WEIGHTED_VAR_GAIN", test.name());
        double score = test.computeTestValue(payload);
        assertEquals(60.0, score, TOL);
    }

    @Test
    public void testWeightedSdGain() {

        RTreeTestPayload payload = new RTreeTestPayload(2);

        payload.totalVar = 10000.;
        payload.splitVar[0] = 1600;
        payload.splitWeight[0] = 0.5;
        payload.splitVar[1] = 1600;
        payload.splitWeight[1] = 0.5;

        RTreePurityFunction test = RTreePurityFunction.WEIGHTED_SD_GAIN;
        assertEquals("WEIGHTED_SD_GAIN", test.name());
        double score = test.computeTestValue(payload);
        assertEquals(60.0, score, TOL);
    }

    @Test
    public void testWeightedSSGain() {
        RTreeTestPayload payload = new RTreeTestPayload(2);

        payload.totalVar = 100.;
        payload.totalWeight = 1;

        payload.splitVar[0] = 40;
        payload.splitWeight[0] = 0.5;
        payload.splitVar[1] = 40;
        payload.splitWeight[1] = 0.5;

        RTreePurityFunction test = RTreePurityFunction.WEIGHTED_SS_GAIN;
        assertEquals("WEIGHTED_SS_GAIN", test.name());
        double score = test.computeTestValue(payload);
        assertEquals(80.0, score, TOL);
    }
}
