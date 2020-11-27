/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

package rapaio.ml.classifier.bayes.nb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/14/19.
 */
public class PriorMLETest {

    private static final double TOL = 1e-12;

    private static final int N = 10_000;
    private static final String TARGET = "target";

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(1234);
    }

    @Test
    void testNaming() {
        assertEquals("MLE", new PriorMLE().name());
        assertEquals("MLE{}", new PriorMLE().fittedName());

        VarNominal target = VarNominal.copy("a", "a", "b").name(TARGET);
        Prior prior = new PriorMLE();
        prior.fitPriors(SolidFrame.byVars(target), VarDouble.fill(3, 1), TARGET);
        assertEquals("MLE{a:0.6666667,b:0.3333333}", prior.fittedName());
    }

    @Test
    void testPrediction() {
        VarNominal target = VarNominal.copy("a", "b", "a", "c", "a", "b").name(TARGET);
        PriorMLE prior = new PriorMLE();
        prior.fitPriors(SolidFrame.byVars(target), VarDouble.fill(6, 1), TARGET);

        assertEquals(0.5, prior.computePrior("a"), TOL);
        assertEquals(1./3, prior.computePrior("b"), TOL);
        assertEquals(1./6, prior.computePrior("c"), TOL);
        assertEquals(Double.NaN, prior.computePrior("d"), TOL);
    }

    @Test
    void testNewInstance() {
        PriorMLE prior = new PriorMLE();
        VarNominal target = VarNominal.copy("a", "b", "c").name("target");
        prior.fitPriors(SolidFrame.byVars(target), VarDouble.fill(target.size(), 1), "target");

        assertEquals("MLE{a:0.3333333,b:0.3333333,c:0.3333333}", prior.fittedName());

        Prior copy = prior.newInstance();
        assertNotNull(copy);
        assertEquals("MLE{}", copy.fittedName());
    }
}
