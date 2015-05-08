/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.core.distributions;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 11/4/14.
 */
public class UniformTest {

    @Test
    public void testUniformPdf() {
        Distribution u = new Uniform(0, 2);

        assertEquals("Uniform(a=0,b=2)", u.name());

        assertEquals(0, u.pdf(-1), 1e-12);
        assertEquals(0.5, u.pdf(0), 1e-12);
        assertEquals(0.5, u.pdf(1), 1e-12);
        assertEquals(0.5, u.pdf(2), 1e-12);
        assertEquals(0, u.pdf(3), 1e-12);
    }

    @Test
    public void testUniformCdf() {
        Distribution u = new Uniform(0, 2);

        assertEquals(0, u.cdf(-1), 1e-12);
        assertEquals(0.0, u.cdf(0), 1e-12);
        assertEquals(0.25, u.cdf(0.5), 1e-12);
        assertEquals(0.5, u.cdf(1), 1e-12);
        assertEquals(1.0, u.cdf(2), 1e-12);
        assertEquals(1.0, u.cdf(3), 1e-12);
    }

    @Test
    public void testUniformQuantile() {
        Distribution u = new Uniform(0, 2);

        assertEquals(0, u.quantile(0), 1e-12);
        assertEquals(0.5, u.quantile(0.25), 1e-12);
        assertEquals(1, u.quantile(0.5), 1e-12);
        assertEquals(1.5, u.quantile(0.75), 1e-12);
        assertEquals(2.0, u.quantile(1), 1e-12);

        try {
            u.quantile(-1);
            assertTrue("this should not happen", true);
        } catch (IllegalArgumentException ignored) {
        }
    }
}
