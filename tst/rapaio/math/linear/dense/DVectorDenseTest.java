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

package rapaio.math.linear.dense;

import org.junit.jupiter.api.Test;
import rapaio.data.VarDouble;
import rapaio.math.linear.DVector;
import rapaio.math.linear.StandardDVectorTest;
import rapaio.math.linear.VType;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class DVectorDenseTest extends StandardDVectorTest {

    private static final double TOL = 1e-15;

    @Override
    public VType type() {
        return VType.DENSE;
    }

    @Override
    public DVector generateFill(int size, double fill) {
        return DVector.fill(size, fill);
    }

    @Override
    public DVector generateWrap(double[] values) {
        return DVectorDense.wrap(values);
    }

    @Override
    public String className() {
        return "DVectorDense";
    }


    @Test
    void testBuilderZeros() {
        var zeros = DVector.zeros(N);
        assertNotNull(zeros);
        for (int i = 0; i < N; i++) {
            assertEquals(0, zeros.get(i), TOL);
        }
    }

    @Test
    void testBuildersOnes() {
        var ones = DVector.ones(N);
        assertNotNull(ones);
        assertEquals(N, ones.size());
        for (int i = 0; i < ones.size(); i++) {
            assertEquals(1., ones.get(i), TOL);
        }
    }

    @Test
    void testBuildersFill() {
        var fill = DVector.fill(N, 13);
        assertNotNull(fill);
        assertEquals(N, fill.size());
        for (int i = 0; i < fill.size(); i++) {
            assertEquals(13, fill.get(i), TOL);
        }
    }

    @Test
    void testBuilders() {
        x = DVector.from(VarDouble.seq(N - 1));
        assertNotNull(x);
        for (int i = 0; i < N; i++) {
            assertEquals(i, x.get(i), TOL);
        }

        DVector y = DVector.from(VarDouble.seq(N - 1));
        x = DVector.copy(y);
        assertNotNull(x);
        for (int i = 0; i < N; i++) {
            assertEquals(i, x.get(i), TOL);
        }

        x = DVector.from(VarDouble.fill(N, 1).bindRows(VarDouble.seq(N - 1)));
        assertNotNull(x);
        for (int i = 0; i < N; i++) {
            assertEquals(1, x.get(i), TOL);
            assertEquals(i, x.get(i + N), TOL);
        }

        x = DVectorDense.wrap(0, 1, 2, 3, 4, 5);
        assertNotNull(x);
        for (int i = 0; i < 6; i++) {
            assertEquals(i, x.get(i), TOL);
        }

        x = DVector.from(10, Math::sqrt);
        assertNotNull(x);
        for (int i = 0; i < 10; i++) {
            assertEquals(Math.sqrt(i), x.get(i), TOL);
        }
    }


    @Test
    void testStream() {
        double[] x = new double[100];
        for (int i = 0; i < x.length; i++) {
            x[i] = i;
        }
        DVector y = DVectorDense.wrap(x);

        double xsum = Arrays.stream(x).sum();
        double ysum = y.valueStream().sum();

        assertEquals(xsum, ysum, TOL);
    }
}
