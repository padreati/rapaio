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
package rapaio.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static rapaio.core.MathBase.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class NumVectorTest {

	@Test
	public void smokeTest() {
		Vector v = new Numeric(0);
		boolean flag = v.type().isNumeric();
		assertEquals(true, flag);
		assertEquals(false, v.type().isNominal());

		assertEquals(0, v.rowCount());
	}

	@Test
	public void testGetterSetter() {
		Vector v = new Numeric(10);
		for (int i = 0; i < 10; i++) {
			v.setValue(i, log(10 + i));
		}

		for (int i = 0; i < 10; i++) {
			assertEquals(log(10 + i), v.value(i), 1e-10);
			assertEquals((int) Math.rint(log(10 + i)), v.index(i));
		}

		for (int i = 0; i < 10; i++) {
			v.setIndex(i, i * i);
		}

		for (int i = 0; i < 10; i++) {
			assertEquals(i * i, v.index(i));
			assertEquals(i * i, v.value(i), 1e-10);
		}

		for (int i = 0; i < v.rowCount(); i++) {
			assertEquals("", v.label(i));
		}
		boolean exceptional = false;
		try {
			v.setLabel(0, "test");
		} catch (Throwable ex) {
			exceptional = true;
		}
		assertTrue(exceptional);

		exceptional = false;
		try {
			v.dictionary();
		} catch (Throwable ex) {
			exceptional = true;
		}
		assertTrue(exceptional);
	}

	@Test
	public void testOneNumeric() {
		Vector one = Vectors.newNumOne(PI);

		assertEquals(1, one.rowCount());
		assertEquals(PI, one.value(0), 1e-10);

		one = Vectors.newNumOne(E);
		assertEquals(1, one.rowCount());
		assertEquals(E, one.value(0), 1e-10);
	}
}
