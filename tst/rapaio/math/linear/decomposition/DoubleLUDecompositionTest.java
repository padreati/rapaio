/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.math.linear.decomposition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.dense.DMatrixDenseR;

public class DoubleLUDecompositionTest {

    private static final double TOL = 1e-14;

    @BeforeEach
    void setUpEach() {
        RandomSource.setSeed(14);
    }

    @Test
    void testBasicGaussian() {

        DMatrix a = DMatrix.random(100, 100);
        DoubleLUDecomposition lu = a.lu(DoubleLUDecomposition.Method.GAUSSIAN_ELIMINATION);
        DMatrix a1 = a.mapRows(lu.pivots());
        DMatrix a2 = lu.l().dot(lu.u());
        assertTrue(a1.deepEquals(a2, TOL));
    }

    @Test
    void testBasicCrout() {
        DMatrix a = DMatrix.random(100, 100);
        DoubleLUDecomposition lu = a.lu(DoubleLUDecomposition.Method.CROUT);
        DMatrix a1 = a.mapRows(lu.pivots());
        DMatrix a2 = lu.l().dot(lu.u());
        assertTrue(a1.deepEquals(a2, TOL));
    }

    @Test
    void testIsSingular() {
        assertFalse(DMatrix.empty(10, 10).lu().isNonSingular());
        assertTrue(DMatrix.random(10, 10).lu().isNonSingular());
    }

    @Test
    void solveTest() {

        DMatrix a1 = DMatrixDenseR.wrap(3, 3,
                3, 2, -1,
                2, -2, 4,
                -1, 0.5, -1
        );
        DMatrix b1 = DMatrixDenseR.wrap(3, 1,
                1,
                -2,
                0
        );
        DMatrix x1 = DMatrixDenseR.wrap(3, 1,
                1,
                -2,
                -2
        );
        assertTrue(x1.deepEquals(a1.lu().solve(b1), TOL));


        DMatrix a2 = DMatrixDenseR.wrap(2, 2,
                2, 3,
                4, 9
        );
        DMatrix b2 = DMatrixDenseR.wrap(2, 1,
                6,
                15
        );
        DMatrix x2 = DMatrixDenseR.wrap(2, 1, 1.5, 1);
        assertTrue(x2.deepEquals(a2.lu().solve(b2), TOL));
    }

    @Test
    void determinantTest() {
        DMatrix a = DMatrixDenseR.wrap(2, 2,
                1, 2,
                3, 4
        );
        assertEquals(-2, a.lu().det(), TOL);
    }

    @Test
    void testInvalidMatrixForDeterminant() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> DMatrix.random(4, 3).lu().det());
        assertEquals("The determinant can be computed only for squared matrices.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> DMatrix.random(2, 3).lu().det());
        assertEquals("For LU decomposition, number of rows must be greater or equal with number of columns.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class,
                () -> DMatrix.random(2, 3).lu(DoubleLUDecomposition.Method.GAUSSIAN_ELIMINATION).det());
        assertEquals("For LU decomposition, number of rows must be greater or equal with number of columns.", ex.getMessage());
    }

    @Test
    void testInvalidSolver() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> DMatrix.random(4,3).lu().solve(DMatrix.random(6,6)));
        assertEquals("Matrix row dimensions must agree.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> DMatrix.fill(3, 3, 0).lu().solve(DMatrix.identity(3)));
        assertEquals("Matrix is singular.", ex.getMessage());
    }

    @Test
    void testPrintable() {
        DMatrix m = DMatrix.random(4,3);
        assertEquals("""
                LU decomposition summary
                ========================
                                
                L matrix
                                     [0]                  [1]                 [2]\s
                [0]  1                    0                   0                  \s
                [1]  0.4792464745827134   1                   0                  \s
                [2] -0.25273793283419704 -0.3456407173323528  1                  \s
                [3] -0.7448674167867424   0.05424481555962996 0.11599700479646144\s
                                
                U matrix:
                                  [0]                    [1]                  [2]\s
                [0] 1.279155303448773 -0.0071248796373319335 -0.4814283288057913 \s
                [1] 0                  1.5252942897617914    -0.18067166669528356\s
                [2] 0                  0                     -1.7206360832026606 \s
                                
                pivots: [3,0,1,2,]""", m.lu().toSummary());

        m = DMatrix.random(20,4);
        assertEquals("""
                LU decomposition summary
                ========================
                                
                L matrix
                                      [0]                  [1]                    [2]                    [3]\s
                 [0]  1                    0                    0                      0                    \s
                 [1] -0.22210681636435078  1                    0                      0                    \s
                 [2]  0.4024138071366819  -0.7171555234325474   1                      0                    \s
                 [3]  0.6333704195191314  -0.0621478356555948  -0.5331002409382986     1                    \s
                 [4] -0.08975618243622163 -0.2897274727992778   0.32249285802271604    0.10221315263773842  \s
                 [5] -0.2415960540474802  -0.2163474319747699   0.06540022873473987    0.1152386687111418   \s
                 [6]  0.7132150227582116  -0.46983310457989397 -0.43536231475239806    0.30444768848976944  \s
                 [7] -0.104441322080974    0.08742989748896811  0.10258179163039656   -0.37213766383890273  \s
                 [8] -0.9900679111853908   0.9119354697744949   0.15142881536315558   -0.0062538984146961706\s
                 [9] -0.7013589532266957   0.4197731758458362  -0.661865482045089     -0.06867203021827288  \s
                [10] -0.5148537635292276  -0.1749151786007003   0.24525987408609945    0.09871363010452527  \s
                [11] -0.16778472277012516  0.6827325616348383  -0.10981328798123077    0.8135322729402065   \s
                [12] -0.17935514709289269  0.14106886976596839  0.6432474361938612    -0.06081021092503014  \s
                [13]  0.7491710148400931   0.4839831056928707   0.19560898099296503    0.1996115782830129   \s
                [14] -0.8981014695902978  -0.3445293105193671   0.0025735122119108136 -0.24609222696625507  \s
                [15] -0.7110849988923935   0.21102706259950132 -0.6311830916733985     0.6351019719549358   \s
                [16]  0.6020724889397904  -0.22298674482462605  0.6218967404753459     0.5845291484381557   \s
                [17]  0.27711968774618745  0.20512725116560515 -0.3486690417291394    -0.133726323681601    \s
                [18] -0.6549566018699366   0.09945382760917398  0.1392322499838197    -0.5680478642711828   \s
                [19] -0.38661246708013824 -0.17890790476891066 -0.7249242470237195    -0.15675827639716733  \s
                                
                U matrix:
                                    [0]                 [1]                 [2]                  [3]\s
                [0] -1.8387662903545976 0.43214553959225205 -0.4286769827440557  0.339764545556105  \s
                [1]  0                  1.6604960804050428  -1.4683512384062385 -0.14998175376284756\s
                [2]  0                  0                   -2.0132813781301073 -0.5448112451068791 \s
                [3]  0                  0                    0                  -2.2872823239397264 \s
                                
                pivots: [2,1,11,13,4,5,6,7,8,9,10,0,...]""", m.lu().toSummary());
    }

    @Test
    void testInv() {
        DMatrix m = DMatrix.random(4,4);
        DMatrix inv = m.lu().inv();

        assertTrue(m.lu().solve(DMatrix.identity(4)).deepEquals(inv));
        assertTrue(m.dot(inv).roundValues(14).deepEquals(DMatrix.identity(4)));
    }

}
