/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.data.matrix;

import org.junit.Before;
import org.junit.Test;

import java.text.DecimalFormat;

import static junit.framework.Assert.assertEquals;
import static rapaio.data.matrix.MathMatrix.*;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class MatrixMathTest {

	Matrix A, B, At, Bt, C;

	@Before
	public void setUp() throws Exception {
		A = new Matrix(3, new double[]{
				1, 3, 1,
				1, 0, 0
		});
		B = new Matrix(3, new double[]{
				0, 0, 5,
				7, 5, 0
		});

		At = new Matrix(2, new double[]{
				1, 1,
				3, 0,
				1, 0
		});
		Bt = new Matrix(2, new double[]{
				0, 7,
				0, 5,
				5, 0
		});

		C = new Matrix(3, new double[]{
				1, 2, 3,
				4, 3, 0,
				5, 2, 9
		});
	}

	@Test
	public void basicOperations() {
		assertEqualsM(
				plus(A, B),
				new Matrix(3, new double[]{
						1, 3, 6,
						8, 5, 0
				}));

		assertEqualsM(
				minus(A, B),
				new Matrix(3, new double[]{
						1, 3, -4,
						-6, -5, 0
				})
		);

		assertEqualsM(
				minus(plus(minus(A, B), B), A),
				new Matrix(3, new double[]{
						0, 0, 0,
						0, 0, 0
				})
		);

		assertEqualsM(t(A), At);
		assertEqualsM(t(B), Bt);
	}

	@Test
	public void testInverse() {

		Matrix I = new Matrix(3, new double[]{
				1, 0, 0,
				0, 1, 0,
				0, 0, 1
		});

		Matrix invC = new QRDecomposition(C).solve(I);
		print(invC);
		print(times(C, invC));
		invC = new LUDecomposition(C).solve(I);
		print(invC);
		print(times(C, invC));
	}

	private void print(Matrix m) {
		m.print(new DecimalFormat("0.000000000000000000"), 30);
	}

	private void assertEqualsM(Matrix a, Matrix b) {
		assertEquals(a.n, b.n);
		assertEquals(a.m, b.m);

		for (int i = 0; i < a.m; i++) {
			for (int j = 0; j < a.n; j++) {
				assertEquals(a.get(i, j), b.get(i, j));
			}
		}
	}
}
