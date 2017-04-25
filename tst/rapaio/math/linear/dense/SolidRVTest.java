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

package rapaio.math.linear.dense;

import org.junit.Before;
import org.junit.Test;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Maximum;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.math.linear.RV;
import rapaio.math.linear.dense.SolidRV;

import java.util.Arrays;

import static org.junit.Assert.*;

public class SolidRVTest {

    private static final double TOL = 1e-15;
    private static final int N = 100;

    private Normal normal;
    private Var varx;
    private RV x;

    @Before
    public void setUp() throws Exception {
        normal = new Normal(0, 10);
        varx = Numeric.from(N, normal::sampleNext);
        x = SolidRV.from(varx);
    }

    @Test
    public void testBuilders() {

        RV x = null;

        x = SolidRV.empty(N);
        assertNotNull(x);
        for (int i = 0; i < N; i++) {
            assertEquals(0, x.get(i), TOL);
        }

        x = SolidRV.fill(N, Double.NaN);
        assertNotNull(x);
        for (int i = 0; i < N; i++) {
            assertTrue(Double.isNaN(x.get(i)));
        }

        x = SolidRV.from(Numeric.seq(N - 1));
        assertNotNull(x);
        for (int i = 0; i < N; i++) {
            assertEquals(i, x.get(i), TOL);
        }

        RV y = SolidRV.from(Numeric.seq(N - 1));
        x = SolidRV.copy(y);
        assertNotNull(x);
        for (int i = 0; i < N; i++) {
            assertEquals(i, x.get(i), TOL);
        }

        x = SolidRV.from(Numeric.fill(N, 1).bindRows(Numeric.seq(N - 1)));
        assertNotNull(x);
        for (int i = 0; i < N; i++) {
            assertEquals(1, x.get(i), TOL);
            assertEquals(i, x.get(i + N), TOL);
        }

        x = SolidRV.wrap(0, 1, 2, 3, 4, 5);
        assertNotNull(x);
        for (int i = 0; i < 6; i++) {
            assertEquals(i, x.get(i), TOL);
        }

        x = SolidRV.from(10, Math::sqrt);
        assertNotNull(x);
        for (int i = 0; i < 10; i++) {
            assertEquals(Math.sqrt(i), x.get(i), TOL);
        }
    }

    @Test
    public void incrementTest() {
        RV x = SolidRV.from(Numeric.seq(0, 1, 0.01));
        RV y = x.solidCopy();
        for (int i = 0; i < y.count(); i++) {
            int sign = i % 2 == 0 ? 1 : -1;
            y.increment(i, sign * 10);
        }
        for (int i = 0; i < y.count(); i++) {
            if (i % 2 == 0) {
                assertEquals(x.get(i) + 10, y.get(i), TOL);
            } else {
                assertEquals(x.get(i) - 10, y.get(i), TOL);
            }
        }
    }

    @Test
    public void setterTest() {
        RV y = SolidRV.empty(N);
        for (int i = 0; i < y.count(); i++) {
            y.set(i, x.get(i));
        }
        for (int i = 0; i < y.count(); i++) {
            assertEquals(x.get(i), y.get(i), TOL);
        }
    }

    @Test
    public void scalarPlusTest() {
        RV y = x.solidCopy().plus(10);
        for (int i = 0; i < y.count(); i++) {
            assertEquals(x.get(i) + 10, y.get(i), TOL);
        }
    }

    @Test
    public void vectorPlusTest() {
        RV z = SolidRV.from(Numeric.fill(N, 10));
        RV y = x.solidCopy().plus(z);

        for (int i = 0; i < y.count(); i++) {
            assertEquals(x.get(i) + z.get(i), y.get(i), TOL);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void vectorPlusNonconformantTest() {
        RV y = SolidRV.empty(N / 2);
        x.plus(y);
    }

    @Test
    public void scalarDotTest() {
        RV y = x.solidCopy().dot(10);
        for (int i = 0; i < y.count(); i++) {
            assertEquals(x.get(i) * 10, y.get(i), TOL);
        }
    }

    @Test
    public void scalarMinusTest() {
        RV y = x.solidCopy().minus(10);
        for (int i = 0; i < y.count(); i++) {
            assertEquals(x.get(i) - 10, y.get(i), TOL);
        }
    }

    @Test
    public void vectorMinusTest() {
        RV z = SolidRV.from(Numeric.fill(N, 10));
        RV y = x.solidCopy().minus(z);

        for (int i = 0; i < y.count(); i++) {
            assertEquals(x.get(i) - z.get(i), y.get(i), TOL);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void vectorMinusNonconformantTest() {
        RV y = SolidRV.empty(N / 2);
        x.minus(y);
    }

    @Test
    public void normTest() {
        double[] pvalues = new double[]{Double.POSITIVE_INFINITY, 0, 0.5, 1, 1.5, 2, 100};
        for (double p : pvalues) {
            double pnorm = 0.0;
            for (int i = 0; i < varx.rowCount(); i++) {
                pnorm += Math.pow(Math.abs(varx.value(i)), p);
            }
            pnorm = Math.pow(pnorm, p > 0 ? 1.0 / p : 1.0);
            if (Double.POSITIVE_INFINITY == p) {
                pnorm = Maximum.from(varx).value();
            }
            assertEquals("pnorm failed for p=" + p, pnorm, x.norm(p), TOL);
        }
    }

    @Test
    public void meanVarTest() {
        assertEquals(Mean.from(varx).value(), x.mean().value(), TOL);
        assertEquals(Variance.from(varx).value(), x.var().value(), TOL);
    }

    @Test
    public void normalizeTest() {
        RV y = x.solidCopy().normalize(1.5);
        double norm = x.norm(1.5);
        for (int i = 0; i < y.count(); i++) {
            assertEquals(x.get(i) / norm, y.get(i), TOL);
        }
    }

    @Test
    public void testStream() {
        double[] x = new double[100];
        for (int i = 0; i < x.length; i++) {
            x[i] = i;
        }
        RV y = SolidRV.wrap(x);

        double xsum = Arrays.stream(x).sum();
        double ysum = y.valueStream().sum();

        assertEquals(xsum, ysum, TOL);
    }
}
