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

package rapaio.ml.supervised.bayes.nb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/4/20.
 */
public class PriorUniformTest {

    private static final String TARGET = "target";
    private static final double TOLERANCE = 1e-20;

    @Test
    void testNaming() {
        PriorUniform prior = new PriorUniform();
        assertEquals("Uniform", prior.name());
        assertEquals("Uniform{value=?,targetLevels=[]}", prior.fittedName());

        VarNominal target = VarNominal.copy("a", "b", "a", "c", "a", "b").name(TARGET);
        prior.fitPriors(SolidFrame.byVars(target), VarDouble.fill(6, 1), TARGET);

        assertEquals("Uniform", prior.name());
        assertEquals("Uniform{value=0.3333333,targetLevels=[a,b,c]}", prior.fittedName());
    }

    @Test
    void testPrediction() {
        Prior prior = new PriorUniform();
        VarNominal target = VarNominal.copy("a", "b", "a", "c", "a", "b").name(TARGET);
        prior.fitPriors(SolidFrame.byVars(target), VarDouble.fill(6, 1), TARGET);

        assertEquals(1./3, prior.computePrior("a"), TOLERANCE);
        assertEquals(1./3, prior.computePrior("b"), TOLERANCE);
        assertEquals(1./3, prior.computePrior("c"), TOLERANCE);
        assertEquals(Double.NaN, prior.computePrior("d"), TOLERANCE);
    }

    @Test
    void testNewInstance() {
        PriorUniform prior = new PriorUniform();
        VarNominal target = VarNominal.copy("a", "b", "c").name("target");
        prior.fitPriors(SolidFrame.byVars(target), VarDouble.fill(target.size(), 1), "target");

        assertEquals("Uniform{value=0.3333333,targetLevels=[a,b,c]}", prior.fittedName());

        Prior copy = prior.newInstance();
        assertNotNull(copy);
        assertEquals("Uniform{value=?,targetLevels=[]}", copy.fittedName());
    }
}
