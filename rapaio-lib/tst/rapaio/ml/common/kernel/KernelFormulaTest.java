/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

package rapaio.ml.common.kernel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import rapaio.darray.DArray;
import rapaio.darray.DArrays;
import rapaio.ml.common.distance.MinkowskiDistance;

/**
 * Each kernel is checked against its documented formula on a pair of vectors whose difference has a
 * known norm: {@code x = (1, 2, 2)}, {@code y = (0, 0, 0)}, so {@code ||x - y|| = 3}, {@code ||x - y||^2 = 9}
 * and {@code x'y = 0}; and {@code x'z = 3} for {@code z = (1, 1, 0)}. The values are written out by hand from
 * the formulas, not copied from the implementation.
 */
public class KernelFormulaTest {

    private static final double TOL = 1e-12;
    private static final DArray<Double> x = DArrays.stride(1, 2, 2);
    private static final DArray<Double> y = DArrays.stride(0, 0, 0);
    private static final DArray<Double> z = DArrays.stride(1, 1, 0);

    @Test
    void exponentialUsesTheNormNotItsSquare() {
        // exp(-||x-y|| / (2 sigma^2)) with sigma = 1.5: exp(-3 / 4.5)
        assertEquals(Math.exp(-3 / 4.5), new ExponentialKernel(1.5).compute(x, y), TOL);
        assertEquals(1, new ExponentialKernel(1.5).compute(x, x), TOL);
    }

    @Test
    void cauchyDividesTheSquaredNormBySigmaSquared() {
        // 1 / (1 + ||x-y||^2 / sigma^2) with sigma = 3: 1 / (1 + 9/9)
        assertEquals(0.5, new CauchyKernel(3).compute(x, y), TOL);
        // sigma = 2: 1 / (1 + 9/4)
        assertEquals(1 / (1 + 9.0 / 4), new CauchyKernel(2).compute(x, y), TOL);
    }

    @Test
    void sigmoidIsHyperbolicTangent() {
        assertEquals(Math.tanh(0.5 * 3 + 0.25), new SigmoidKernel(0.5, 0.25).compute(x, z), TOL);
        // tanh is bounded by one; atan is not
        assertTrue(new SigmoidKernel(100, 0).compute(x, x) <= 1);
    }

    @Test
    void generalizedStudentTRaisesTheNormToTheDegree() {
        // 1 / (1 + ||x-y||^d) with d = 1: 1/4; d = 3: 1/28
        assertEquals(0.25, new GeneralizedStudentTKernel(1).compute(x, y), TOL);
        assertEquals(1.0 / 28, new GeneralizedStudentTKernel(3).compute(x, y), TOL);
    }

    @Test
    void rationalQuadraticUsesTheSquaredNormOnce() {
        // 1 - ||x-y||^2 / (||x-y||^2 + c) with c = 9: 1 - 9/18
        assertEquals(0.5, new RationalQuadraticKernel(9).compute(x, y), TOL);
        assertEquals(1, new RationalQuadraticKernel(9).compute(x, x), TOL);
    }

    @Test
    void multiQuadricKernelsUseTheSquaredNormOnce() {
        // sqrt(||x-y||^2 + c^2) with c = 4: sqrt(9 + 16) = 5
        assertEquals(5, new MultiQuadricKernel(4).compute(x, y), TOL);
        assertEquals(0.2, new InverseMultiQuadricKernel(4).compute(x, y), TOL);
        // and the same with c = 1: sqrt(10)
        assertEquals(Math.sqrt(10), new MultiQuadricKernel(1).compute(x, y), TOL);
        assertEquals(1 / Math.sqrt(10), new InverseMultiQuadricKernel(1).compute(x, y), TOL);
    }

    @Test
    void logAndPowerRaiseTheNormToTheDegree() {
        // -log(||x-y||^d + 1), d = 1: -log(4); d = 2: -log(10)
        assertEquals(-Math.log(4), new LogKernel(1).compute(x, y), TOL);
        assertEquals(-Math.log(10), new LogKernel(2).compute(x, y), TOL);
        // -||x-y||^d, d = 1: -3; d = 2: -9
        assertEquals(-3, new PowerKernel(1).compute(x, y), TOL);
        assertEquals(-9, new PowerKernel(2).compute(x, y), TOL);
    }

    @Test
    void circularIsOneAtZeroDistanceAndVanishesContinuouslyAtSigma() {
        CircularKernel k = new CircularKernel(6);
        assertEquals(1, k.compute(x, x), TOL);
        // f = 3/6 = 0.5: 2/pi (acos(0.5) - 0.5 sqrt(0.75))
        assertEquals(2 / Math.PI * (Math.acos(0.5) - 0.5 * Math.sqrt(0.75)), k.compute(x, y), TOL);
        // just inside the support the value is close to zero, outside it is exactly zero
        assertEquals(0, new CircularKernel(3.0000001).compute(x, y), 1e-3);
        assertEquals(0, new CircularKernel(3).compute(x, y), TOL);
        assertEquals(0, new CircularKernel(1).compute(x, y), TOL);
        assertFalse(Double.isNaN(new CircularKernel(1).compute(x, y)));
    }

    @Test
    void sphericalIsOneAtZeroDistanceAndVanishesContinuouslyAtSigma() {
        SphericalKernel k = new SphericalKernel(6);
        assertEquals(1, k.compute(x, x), TOL);
        // f = 0.5: 1 - 0.75 + 0.0625
        assertEquals(0.3125, k.compute(x, y), TOL);
        assertEquals(0, new SphericalKernel(3.0000001).compute(x, y), 1e-6);
        assertEquals(0, new SphericalKernel(3).compute(x, y), TOL);
        assertEquals(0, new SphericalKernel(1).compute(x, y), TOL);
    }

    @Test
    void waveIsOneAtZeroDistanceAndUsesTheNorm() {
        WaveKernel k = new WaveKernel(2);
        assertEquals(1, k.compute(x, x), TOL);
        // theta / ||x-y|| sin(||x-y|| / theta) = 2/3 sin(1.5)
        assertEquals(2.0 / 3 * Math.sin(1.5), k.compute(x, y), TOL);
    }

    @Test
    void chiSquareIgnoresCoordinatesWhereBothVectorsAreZero() {
        DArray<Double> h1 = DArrays.stride(0, 1, 3);
        DArray<Double> h2 = DArrays.stride(0, 3, 1);
        // 1 - 2 * (4/4 + 4/4)
        assertEquals(-3, new ChiSquareKernel().compute(h1, h2), TOL);
        assertEquals(1, new ChiSquareKernel().compute(h1, h1), TOL);
        assertEquals(1, new ChiSquareKernel().compute(DArrays.stride(0, 0), DArrays.stride(0, 0)), TOL);
    }

    @Test
    void isLinearMeansInnerProductPlusAConstant() {
        assertTrue(new LinearKernel().isLinear());
        assertTrue(new LinearKernel(3).isLinear());
        assertTrue(new PolyKernel(1).isLinear());
        assertTrue(new PolyKernel(1, 5).isLinear());
        assertFalse(new PolyKernel(1, 0, 2).isLinear(), "a scaled inner product is not usable through a plain weight vector");
        assertFalse(new PolyKernel(2).isLinear());
        for (Kernel k : List.of(new RBFKernel(1), new SigmoidKernel(1, 0), new ExponentialKernel(), new MinKernel())) {
            assertFalse(k.isLinear(), k.name());
        }
    }

    @Test
    void minkowskiTakesAbsoluteDifferences() {
        DArray<Double> a = DArrays.stride(0, 0);
        DArray<Double> b = DArrays.stride(1, -1);
        assertEquals(Math.pow(2, 1.0 / 3), new MinkowskiDistance(3).compute(a, b), TOL);
        assertEquals(2, new MinkowskiDistance(1).compute(a, b), TOL);
        assertEquals(Math.sqrt(2), new MinkowskiDistance(2).compute(a, b), TOL);
        assertFalse(Double.isNaN(new MinkowskiDistance(1.5).compute(a, b)));
        assertEquals("Minkowski(p=3.0)", new MinkowskiDistance(3).name());
    }
}
