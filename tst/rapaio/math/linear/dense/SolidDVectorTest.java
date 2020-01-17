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

import org.junit.jupiter.api.Test;
import rapaio.data.VarDouble;
import rapaio.math.linear.DVector;
import rapaio.math.linear.StandardDVectorTest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class SolidDVectorTest extends StandardDVectorTest {

    private static final double TOL = 1e-15;

    @Override
    public DVector generateZeros(int size) {
        return SolidDVector.zeros(size);
    }

    @Override
    public DVector generateFill(int size, double fill) {
        return SolidDVector.fill(size, fill);
    }

    @Override
    public DVector generateWrap(double[] values) {
        return SolidDVector.wrap(values);
    }

    @Override
    public String className() {
        return "SolidDVector";
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
