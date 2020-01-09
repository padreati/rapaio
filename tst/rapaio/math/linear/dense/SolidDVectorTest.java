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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Maximum;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.math.linear.DVector;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class SolidDVectorTest {

    private static final double TOL = 1e-15;
    private static final int N = 100;

    private Normal normal;
    private Var varx;
    private SolidDVector x;

    @BeforeEach
    void beforeEach() {
        normal = Normal.of(0, 10);
        varx = VarDouble.from(N, normal::sampleNext);
        x = SolidDVector.from(varx);
    }

    @Test
    void testBuilderZeros() {
        var zeros = SolidDVector.zeros(N);
        assertNotNull(zeros);
        for (int i = 0; i < N; i++) {
            assertEquals(0, zeros.get(i), TOL);
        }
    }

    @Test
    void testBuildersOnes() {
        var ones = SolidDVector.ones(N);
        assertNotNull(ones);
        assertEquals(N, ones.size());
        for (int i = 0; i < ones.size(); i++) {
            assertEquals(1., ones.get(i), TOL);
        }
    }

    @Test
    void testBuildersFill() {
        var fill = SolidDVector.fill(N, 13);
        assertNotNull(fill);
        assertEquals(N, fill.size());
        for (int i = 0; i < fill.size(); i++) {
            assertEquals(13, fill.get(i), TOL);
        }
    }

    @Test
    void testBuilders() {


        x = SolidDVector.from(VarDouble.seq(N - 1));
        assertNotNull(x);
        for (int i = 0; i < N; i++) {
            assertEquals(i, x.get(i), TOL);
        }

        DVector y = SolidDVector.from(VarDouble.seq(N - 1));
        x = SolidDVector.copy(y);
        assertNotNull(x);
        for (int i = 0; i < N; i++) {
            assertEquals(i, x.get(i), TOL);
        }

        x = SolidDVector.from(VarDouble.fill(N, 1).bindRows(VarDouble.seq(N - 1)));
        assertNotNull(x);
        for (int i = 0; i < N; i++) {
            assertEquals(1, x.get(i), TOL);
            assertEquals(i, x.get(i + N), TOL);
        }

        x = SolidDVector.wrap(0, 1, 2, 3, 4, 5);
        assertNotNull(x);
        for (int i = 0; i < 6; i++) {
            assertEquals(i, x.get(i), TOL);
        }

        x = SolidDVector.from(10, Math::sqrt);
        assertNotNull(x);
        for (int i = 0; i < 10; i++) {
            assertEquals(Math.sqrt(i), x.get(i), TOL);
        }
    }

    @Test
    void setterTest() {
        DVector y = SolidDVector.zeros(N);
        for (int i = 0; i < y.size(); i++) {
            y.set(i, x.get(i));
        }
        for (int i = 0; i < y.size(); i++) {
            assertEquals(x.get(i), y.get(i), TOL);
        }
    }

    @Test
    void scalarPlusTest() {
        DVector y = x.copy().plus(10);
        for (int i = 0; i < y.size(); i++) {
            assertEquals(x.get(i) + 10, y.get(i), TOL);
        }
    }

    @Test
    void vectorPlusTest() {
        DVector z = SolidDVector.from(VarDouble.fill(N, 10));
        DVector y = x.copy().plus(z);

        for (int i = 0; i < y.size(); i++) {
            assertEquals(x.get(i) + z.get(i), y.get(i), TOL);
        }
    }

    @Test
    void vectorPlusNonconformantTest() {
        DVector y = SolidDVector.zeros(N / 2);
        assertThrows(IllegalArgumentException.class, () -> x.plus(y));
    }

    @Test
    void scalarDotTest() {
        DVector y = x.copy().times(10);
        for (int i = 0; i < y.size(); i++) {
            assertEquals(x.get(i) * 10, y.get(i), TOL);
        }
    }

    @Test
    void scalarMinusTest() {
        DVector y = x.copy().minus(10);
        for (int i = 0; i < y.size(); i++) {
            assertEquals(x.get(i) - 10, y.get(i), TOL);
        }
    }

    @Test
    void vectorMinusTest() {
        DVector z = SolidDVector.from(VarDouble.fill(N, 10));
        DVector y = x.copy().minus(z);

        for (int i = 0; i < y.size(); i++) {
            assertEquals(x.get(i) - z.get(i), y.get(i), TOL);
        }
    }

    @Test
    void vectorMinusNonconformantTest() {
        DVector y = SolidDVector.zeros(N / 2);
        assertThrows(IllegalArgumentException.class, () -> x.minus(y));
    }

    @Test
    public void normTest() {
        double[] pvalues = new double[]{Double.POSITIVE_INFINITY, 0, 0.5, 1, 1.5, 2, 100};
        for (double p : pvalues) {
            double pnorm = 0.0;
            for (int i = 0; i < varx.rowCount(); i++) {
                pnorm += Math.pow(Math.abs(varx.getDouble(i)), p);
            }
            pnorm = Math.pow(pnorm, p > 0 ? 1.0 / p : 1.0);
            if (Double.POSITIVE_INFINITY == p) {
                pnorm = Maximum.of(varx).value();
            }
            assertEquals(pnorm, x.norm(p), TOL, "pnorm failed for p=" + p);
        }
    }

    @Test
    void normalizeTest() {
        DVector y = x.copy().normalize(1.5);
        double norm = x.norm(1.5);
        for (int i = 0; i < y.size(); i++) {
            assertEquals(x.get(i) / norm, y.get(i), TOL);
        }
    }

    @Test
    void meanVarTest() {
        assertEquals(Mean.of(varx).value(), x.mean(), 1e-12);
        assertEquals(Variance.of(varx).value(), x.variance(), 1e-12);
    }

    @Test
    void testStream() {
        double[] x = new double[100];
        for (int i = 0; i < x.length; i++) {
            x[i] = i;
        }
        DVector y = SolidDVector.wrap(x);

        double xsum = Arrays.stream(x).sum();
        double ysum = y.valueStream().sum();

        assertEquals(xsum, ysum, TOL);
    }
}
