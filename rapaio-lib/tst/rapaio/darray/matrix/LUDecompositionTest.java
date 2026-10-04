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

package rapaio.darray.matrix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.DType;
import rapaio.darray.DArrays;
import rapaio.darray.Order;
import rapaio.darray.Shape;

public class LUDecompositionTest {

    private static final double TOL = 1e-12;

    private Random random;

    @BeforeEach
    void setUpEach() {
        random = new Random(14);
    }

    @Test
    void testSuite() {
        testPlan(DArrayManager.base(), DType.DOUBLE);
    }

    <N extends Number> void testPlan(DArrayManager tm, DType<N> dt) {
        testBasicGaussian(tm, dt);
        testBasicCrout(tm, dt);
        testIsSingular(tm, dt);
        testDeterminant(tm, dt);
        testInv(tm, dt);
        testSolve(tm, dt);
        testInvalidMatrixForDeterminant(tm, dt);
        testInvalidSolver(tm, dt);
        testPrintable(tm, dt);
    }

    <N extends Number> void testBasicGaussian(DArrayManager tm, DType<N> dt) {
        DArray<N> a = tm.random(dt, Shape.of(100, 100), random);
        LUDecomposition<N> lu = a.lu(LUDecomposition.Method.GAUSSIAN_ELIMINATION);
        DArray<N> a1 = a.sel(0, lu.pivots());
        DArray<N> a2 = lu.l().mm(lu.u());
        assertTrue(a1.deepEquals(a2, TOL));
    }

    <N extends Number> void testBasicCrout(DArrayManager tm, DType<N> dt) {
        DArray<N> a = tm.random(dt, Shape.of(100, 100), random);
        LUDecomposition<N> lu = a.lu(LUDecomposition.Method.CROUT);
        DArray<N> a1 = a.sel(0, lu.pivots());
        DArray<N> a2 = lu.l().mm(lu.u());
        assertTrue(a1.deepEquals(a2, TOL));
    }

    <N extends Number> void testIsSingular(DArrayManager tm, DType<N> dt) {
        assertFalse(tm.zeros(dt, Shape.of(10, 10)).lu().isNonSingular());
        assertTrue(tm.random(dt, Shape.of(10, 10), random).lu().isNonSingular());
    }

    <N extends Number> void testDeterminant(DArrayManager tm, DType<N> dt) {
        DArray<N> a = tm.stride(dt, Shape.of(2, 2), Order.C, 1, 2, 3, 4);
        assertEquals(-2, a.lu().det(), TOL);
    }

    <N extends Number> void testInv(DArrayManager tm, DType<N> dt) {
        DArray<N> m = tm.random(dt, Shape.of(4, 4), random);
        DArray<N> inv = m.lu().inv();

        assertTrue(m.lu().solve(tm.eye(dt, 4)).deepEquals(inv));
        assertTrue(m.mm(inv).deepEquals(DArrays.eye(4), 1e-14));
    }

    <N extends Number> void testSolve(DArrayManager tm, DType<N> dt) {
        DArray<N> a1 = tm.stride(dt, Shape.of(3, 3), Order.C, 3, 2, -1, 2, -2, 4, -1, 0.5, -1);
        DArray<N> b1 = tm.stride(dt, Shape.of(3, 1), Order.C, 1, -2, 0);
        DArray<N> x1 = tm.stride(dt, Shape.of(3, 1), Order.C, 1, -2, -2);
        assertTrue(x1.deepEquals(a1.lu().solve(b1), TOL));

        DArray<N> a2 = tm.stride(dt, Shape.of(2, 2), Order.C, 2, 3, 4, 9);
        DArray<N> b2 = tm.stride(dt, Shape.of(2, 1), Order.C, 6, 15);
        DArray<N> x2 = tm.stride(dt, Shape.of(2, 1), Order.C, 1.5, 1);
        assertTrue(x2.deepEquals(a2.lu().solve(b2), TOL));
    }

    <N extends Number> void testInvalidMatrixForDeterminant(DArrayManager tm, DType<N> dt) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> tm.random(dt, Shape.of(4, 3), random).lu().det());
        assertEquals("The determinant can be computed only for squared matrices.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> tm.random(dt, Shape.of(2, 3), random).lu().det());
        assertEquals("For LU decomposition, number of rows must be greater or equal with number of columns.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class,
                () -> tm.random(dt, Shape.of(2, 3), random).lu(LUDecomposition.Method.GAUSSIAN_ELIMINATION).det());
        assertEquals("For LU decomposition, number of rows must be greater or equal with number of columns.", ex.getMessage());
    }

    <N extends Number> void testInvalidSolver(DArrayManager tm, DType<N> dt) {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> tm.random(dt, Shape.of(4, 3), random).lu().solve(tm.random(dt, Shape.of(6, 6), random)));
        assertEquals("A system can be solved only for squared matrices.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class,
                () -> tm.eye(dt, 4).lu().solve(tm.eye(dt, 6)));
        assertEquals("Matrix row dimensions must agree.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> tm.zeros(dt, Shape.of(3, 3)).lu().solve(tm.eye(dt, 3)));
        assertEquals("Matrix is singular.", ex.getMessage());
    }

    <N extends Number> void testPrintable(DArrayManager tm, DType<N> dt) {
        var m = tm.random(dt, Shape.of(4, 3), random);
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

        m = tm.random(dt, Shape.of(20, 4), random);
        assertEquals("""
                LU decomposition summary
                ========================
                
                L matrix
                [[  1                      0                    0                     0                    ] \s
                 [ -0.08732004475553666    1                    0                     0                    ] \s
                 [ -0.3488495967024438     0.5464216867395838   1                     0                    ] \s
                 [  0.04559800679705353    0.6390517429637836  -0.8905651342970786    1                    ] \s
                 [ -0.36597827961886314   -0.3522373194977665   0.4920783195783815    0.19568302614629665  ] \s
                 [  0.1285998943085052     0.4412930777843284  -0.7933528921795174    0.08074441933044033  ] \s
                 [  0.1751228446281369    -0.3505062194634797   0.7880746561068745   -0.08982162254250786  ] \s
                 [  0.679823545634389      0.24370702838919478  0.043705341431163884  0.39690490089626707  ] \s
                 [ -0.2342083677120665    -0.7843252713381056  -0.029176061000715462 -0.49181532530224553  ] \s
                 [  0.5658429718524972    -0.00309081101139674  0.43684752613522554   0.4343067152681316   ] \s
                 [  0.7953604659015907     0.2988612684460921  -0.02715232271471289   0.04961035298344504  ] \s
                 [ -0.29940242446038806   -0.15094070404366453  0.04159296954129122   0.47018538987212205  ] \s
                 [  0.6613778112108561     0.3424363155790385  -0.9312915702885893   -0.5375231940993214   ] \s
                 [  0.42525210757152354    0.5507969116739551  -0.6482594401613155    0.9439856176875399   ] \s
                 [ -0.7933673063760109    -0.17648537137622328 -0.13000621917383665   0.5828481073627312   ] \s
                 [ -0.0037027174783996566  0.0679470979834291  -0.016700987964607762 -0.11947462582914686  ] \s
                 [  0.4091010942544294     0.15199901370223684 -0.9950265431461633    0.18645837359861392  ] \s
                 [ -0.016285227145144986   0.06403522237197133  0.29576927072168857  -0.4864672658453687   ] \s
                 [ -0.6473812435115918    -0.8256725597444834   0.04439698862344785  -0.20185023231556745  ] \s
                 [  0.06323568191342721   -0.14780153611143754  0.12480197173797433   0.019319297194222814 ]]\s
                
                U matrix:
                [[ 2.382126449812674 -0.7744707978588579  1.2798088827640226 -0.42035831943677054 ] \s
                 [ 0                  2.488022176134445  -0.9128478305538339  1.2593633690827424  ] \s
                 [ 0                  0                   1.2378749198022576 -0.6168584259908618  ] \s
                 [ 0                  0                   0                  -2.166964093636305   ]]\s
                
                pivots: [4,14,13,2,0,5,6,7,8,9,10,11,...]""", m.lu().toSummary());
    }
}
