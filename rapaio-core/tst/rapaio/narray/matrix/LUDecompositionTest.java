/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.narray.matrix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.narray.NArray;
import rapaio.narray.NArrayManager;
import rapaio.narray.NArrays;
import rapaio.narray.Order;
import rapaio.narray.Shape;

public class LUDecompositionTest {

    private static final double TOL = 1e-12;

    private Random random;

    @BeforeEach
    void setUpEach() {
        random = new Random(14);
    }

    @Test
    void testSuite() {
        testPlan(NArrayManager.base().ofDouble());
    }

    <N extends Number> void testPlan(NArrayManager.OfType<N> tmt) {
        testBasicGaussian(tmt);
        testBasicCrout(tmt);
        testIsSingular(tmt);
        testDeterminant(tmt);
        testInv(tmt);
        testSolve(tmt);
        testInvalidMatrixForDeterminant(tmt);
        testInvalidSolver(tmt);
        testPrintable(tmt);
    }

    <N extends Number> void testBasicGaussian(NArrayManager.OfType<N> tmt) {
        NArray<N> a = tmt.random(Shape.of(100, 100), random);
        LUDecomposition<N> lu = a.lu(LUDecomposition.Method.GAUSSIAN_ELIMINATION);
        NArray<N> a1 = a.take(0, lu.pivots());
        NArray<N> a2 = lu.l().mm(lu.u());
        assertTrue(a1.deepEquals(a2, TOL));
    }

    <N extends Number> void testBasicCrout(NArrayManager.OfType<N> tmt) {
        NArray<N> a = tmt.random(Shape.of(100, 100), random);
        LUDecomposition<N> lu = a.lu(LUDecomposition.Method.CROUT);
        NArray<N> a1 = a.take(0, lu.pivots());
        NArray<N> a2 = lu.l().mm(lu.u());
        assertTrue(a1.deepEquals(a2, TOL));
    }

    <N extends Number> void testIsSingular(NArrayManager.OfType<N> tmt) {
        assertFalse(tmt.zeros(Shape.of(10, 10)).lu().isNonSingular());
        assertTrue(tmt.random(Shape.of(10, 10), random).lu().isNonSingular());
    }

    <N extends Number> void testDeterminant(NArrayManager.OfType<N> tmt) {
        NArray<N> a = tmt.stride(Shape.of(2, 2), Order.C, 1, 2, 3, 4);
        assertEquals(-2, a.lu().det(), TOL);
    }

    <N extends Number> void testInv(NArrayManager.OfType<N> tmt) {
        NArray<N> m = tmt.random(Shape.of(4, 4), random);
        NArray<N> inv = m.lu().inv();

        assertTrue(m.lu().solve(tmt.eye(4)).deepEquals(inv));
        assertTrue(m.mm(inv).deepEquals(NArrays.eye(4), 1e-14));
    }

    <N extends Number> void testSolve(NArrayManager.OfType<N> tmt) {
        NArray<N> a1 = tmt.stride(Shape.of(3, 3), Order.C, 3, 2, -1, 2, -2, 4, -1, 0.5, -1);
        NArray<N> b1 = tmt.stride(Shape.of(3, 1), Order.C, 1, -2, 0);
        NArray<N> x1 = tmt.stride(Shape.of(3, 1), Order.C, 1, -2, -2);
        assertTrue(x1.deepEquals(a1.lu().solve(b1), TOL));

        NArray<N> a2 = tmt.stride(Shape.of(2, 2), Order.C, 2, 3, 4, 9);
        NArray<N> b2 = tmt.stride(Shape.of(2, 1), Order.C, 6, 15);
        NArray<N> x2 = tmt.stride(Shape.of(2, 1), Order.C, 1.5, 1);
        assertTrue(x2.deepEquals(a2.lu().solve(b2), TOL));
    }

    <N extends Number> void testInvalidMatrixForDeterminant(NArrayManager.OfType<N> tmt) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> tmt.random(Shape.of(4, 3), random).lu().det());
        assertEquals("The determinant can be computed only for squared matrices.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> tmt.random(Shape.of(2, 3), random).lu().det());
        assertEquals("For LU decomposition, number of rows must be greater or equal with number of columns.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class,
                () -> tmt.random(Shape.of(2, 3), random).lu(LUDecomposition.Method.GAUSSIAN_ELIMINATION).det());
        assertEquals("For LU decomposition, number of rows must be greater or equal with number of columns.", ex.getMessage());
    }

    <N extends Number> void testInvalidSolver(NArrayManager.OfType<N> tmt) {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> tmt.random(Shape.of(4, 3), random).lu().solve(tmt.random(Shape.of(6, 6), random)));
        assertEquals("Matrix row dimensions must agree.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> tmt.zeros(Shape.of(3, 3)).lu().solve(tmt.eye(3)));
        assertEquals("Matrix is singular.", ex.getMessage());
    }

    <N extends Number> void testPrintable(NArrayManager.OfType<N> tmt) {
        var m = tmt.random(Shape.of(4, 3), random);
        assertEquals("""
                LU decomposition summary
                ========================
                
                L matrix
                [[  1                    0                   0                  ] \s
                 [ -0.1838712748793559   1                   0                  ] \s
                 [ -0.6027617986101053  -0.3231857242768964  1                  ] \s
                 [ -0.15916365457312046  0.9351243157826898 -0.6526637742033669 ]]\s
                
                U matrix:
                [[ -1.512111583036765 0.5677776492080698  0.6994509926021243 ] \s
                 [  0                 1.1798553983165567 -0.5599942578652789 ] \s
                 [  0                 0                  -1.4770359066029968 ]]\s
                
                pivots: [2,1,3,0,]""", m.lu().toSummary());

        m = tmt.random(Shape.of(20, 4), random);
        assertEquals("""
                LU decomposition summary
                ========================
                
                L matrix
                [[  1                     0                     0                     0                    ] \s
                 [ -0.08732004475553648   1                     0                     0                    ] \s
                 [ -0.3488495967024431    0.546421686739584     1                     0                    ] \s
                 [  0.04559800679705344   0.6390517429637834   -0.890565134297079     1                    ] \s
                 [ -0.36597827961886237  -0.3522373194977662    0.49207831957838094   0.1956830261462967   ] \s
                 [  0.12859989430850494   0.44129307778432836  -0.7933528921795173    0.08074441933044042  ] \s
                 [  0.17512284462813654  -0.3505062194634798    0.7880746561068751   -0.08982162254250817  ] \s
                 [  0.6798235456343875    0.24370702838919428   0.04370534143116507   0.3969049008962668   ] \s
                 [ -0.23420836771206602  -0.7843252713381054   -0.029176061000715965 -0.4918153253022454   ] \s
                 [  0.565842971852496    -0.003090811011397119  0.4368475261352267    0.4343067152681312   ] \s
                 [  0.795360465901589     0.29886126844609157  -0.027152322714711463  0.049610352983444635 ] \s
                 [ -0.29940242446038745  -0.1509407040436643    0.04159296954129074   0.47018538987212216  ] \s
                 [  0.6613778112108548    0.3424363155790381   -0.9312915702885887   -0.5375231940993214   ] \s
                 [  0.42525210757152265   0.5507969116739547   -0.648259440161315     0.9439856176875396   ] \s
                 [ -0.7933673063760093   -0.17648537137622275  -0.13000621917383795   0.5828481073627316   ] \s
                 [ -0.003702717478399649  0.06794709798342909  -0.01670098796460777  -0.11947462582914686  ] \s
                 [  0.40910109425442853   0.15199901370223654  -0.995026543146163     0.18645837359861392  ] \s
                 [ -0.01628522714514495   0.06403522237197133   0.2957692707216886   -0.4864672658453687   ] \s
                 [ -0.6473812435115904   -0.8256725597444828    0.044396988623446695 -0.20185023231556715  ] \s
                 [  0.06323568191342709  -0.1478015361114375    0.12480197173797451   0.019319297194222762 ]]\s
                
                U matrix:
                [[ 2.382126449812679 -0.7744707978588579  1.2798088827640226 -0.42035831943677054 ] \s
                 [ 0                  2.4880221761344457 -0.9128478305538341  1.2593633690827426  ] \s
                 [ 0                  0                   1.2378749198022572 -0.616858425990862   ] \s
                 [ 0                  0                   0                  -2.166964093636305   ]]\s
                
                pivots: [4,14,13,2,0,5,6,7,8,9,10,11,...]""", m.lu().toSummary());
    }
}
