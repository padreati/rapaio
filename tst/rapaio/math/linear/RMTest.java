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

package rapaio.math.linear;

import org.junit.Test;
import rapaio.math.linear.dense.SolidRM;

import static org.junit.Assert.*;

public class RMTest {

    private static final double TOL = 1e-20;

    @Test
    public void plusMinusTest() {
        RM A1 = SolidRM.identity(3);
        A1.plus(2);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(i == j ? 3 : 2, A1.get(i, j), TOL);
            }
        }

        RM A2 = SolidRM.identity(3);
        RM A3 = A2.solidCopy().plus(A2);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(i == j ? 2 : 0, A3.get(i, j), TOL);
            }
        }

        RM A4 = A3.minus(A2);
        assertTrue(A4.isEqual(A2));


        RM A5 = SolidRM.identity(10);
        RM A6 = A5.solidCopy().plus(10).minus(10);
        assertTrue(A5.isEqual(A6));
    }

    @Test(expected = IllegalArgumentException.class)
    public void plusNonConformantTest() {
        RM A = SolidRM.identity(3);
        RM B = SolidRM.identity(4);
        A.plus(B);
    }

    @Test(expected = IllegalArgumentException.class)
    public void minusNonConformantTest() {
        RM A = SolidRM.identity(3);
        RM B = SolidRM.identity(4);
        A.minus(B);
    }


    @Test
    public void rowMappingTest() {
        RM a1 = SolidRM.random(5, 5);

        RM a2 = a1.mapRows(1, 3);
        for (int i = 0; i < a1.getColCount(); i++) {
            assertEquals(a1.get(1, i), a2.get(0, i), TOL);
            assertEquals(a1.get(3, i), a2.get(1, i), TOL);
        }

        RM a3 = a1.removeRows(0, 2, 4);
        assertTrue(a3.isEqual(a2));

        RM a4 = a1.rangeRows(1, 4).removeRows(1);
        assertTrue(a4.isEqual(a2));
    }

    @Test
    public void colMappingTest() {
        RM a1 = SolidRM.random(5, 5);

        RM a2 = a1.mapCols(1, 3);
        for (int i = 0; i < a1.getRowCount(); i++) {
            assertEquals(a1.get(i, 1), a2.get(i, 0), TOL);
            assertEquals(a1.get(i, 3), a2.get(i, 1), TOL);
        }

        RM a3 = a1.removeCols(0, 2, 4);
        assertTrue(a3.isEqual(a2));

        RM a4 = a1.rangeCols(1, 4).removeCols(1);
        assertTrue(a4.isEqual(a2));
    }

    @Test
    public void dotTest() {

        RM a1 = SolidRM.random(10, 10);
        RM a2 = a1.solidCopy().dot(2);
        RM a3 = a1.solidCopy().plus(a1);

        assertTrue(a2.isEqual(a3, TOL));

        RM i10 = SolidRM.identity(10);
        assertTrue(a1.isEqual(a1.dot(i10), TOL));
    }

    @Test
    public void meanVarTest() {
        RM a1 = SolidRM.wrap(new double[][]{
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        });

        assertEquals(5, a1.mean().getValue(), TOL);
        assertEquals(7.5, a1.var().getValue(), TOL);
    }

    @Test
    public void testDiagonal() {
        RM a1 = SolidRM.identity(10);
        assertEquals(10, a1.diag().valueStream().sum(), TOL);

        RM a2 = SolidRM.random(10, 10);
        RV d2 = a2.diag();
        for (int i = 0; i < 10; i++) {
            assertEquals(a2.get(i, i), d2.get(i), TOL);
        }
    }

    @Test
    public void scatter() {
        RM a1 = SolidRM.identity(4);
        RM s1 = a1.scatter();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(i == j ? 0.75 : -0.25, s1.get(i, j), TOL);
            }
        }

        // reference for this test is found at: http://www.itl.nist.gov/div898/handbook/pmc/section5/pmc541.htm

        RM a2 = SolidRM.wrap(new double[][]{
                {4, 2, 0.6},
                {4.2, 2.1, 0.59},
                {3.9, 2.0, 0.58},
                {4.3, 2.1, 0.62},
                {4.1, 2.2, 0.63}
        });


        // reference for this test is found at: https://gist.github.com/nok/73d07cc644a390fad9e9

        RM a3 = SolidRM.wrap(new double[][]{
                {90, 60, 90},
                {90, 90, 30},
                {60, 60, 60},
                {60, 60, 90},
                {30, 30, 30}
        });
        RM s3 = SolidRM.wrap(new double[][]{
                {504.0, 360.0, 180.0},
                {360.0, 360.0, 0.0},
                {180.0, 0.0, 720.0}
        });
        assertTrue(s3.isEqual(a3.scatter().dot(1.0 / a3.getRowCount())));
    }
}
