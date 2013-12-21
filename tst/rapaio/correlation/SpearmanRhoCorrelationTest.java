/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */
package rapaio.correlation;

import static org.junit.Assert.*;
import org.junit.Test;
import rapaio.data.NumericVector;
import rapaio.data.Vector;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */

public class SpearmanRhoCorrelationTest {

    private final Vector iq = new NumericVector(new double[]{106, 86, 100, 101, 99, 103, 97, 113, 112, 110});
    private final Vector tvHours = new NumericVector(new double[]{7, 0, 27, 50, 28, 29, 20, 12, 6, 17});

    @Test
    public void testFromWikipedia() {
        SpearmanRhoCorrelation sc = new SpearmanRhoCorrelation(iq, tvHours);
        // according with wikipedia article rho must be −0.175757575
        assertEquals(-0.175757575, sc.getValues()[0][1], 1e-8);
    }

    @Test
    public void testSameVector() {
        SpearmanRhoCorrelation same = new SpearmanRhoCorrelation(iq, iq);
        assertEquals(1., same.getValues()[0][1], 1e-10);

        same = new SpearmanRhoCorrelation(tvHours, tvHours);
        assertEquals(1., same.getValues()[0][1], 1e-10);
    }
}
