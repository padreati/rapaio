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
package rapaio.io;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rapaio.data.Frame;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CsvPersistenceTest {

	private CsvPersistence persistence;

	@Before
	public void setUp() {
		persistence = new CsvPersistence();
		persistence.setTrimSpaces(true);
		persistence.setEscapeQuotas('\"');
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testHeader() {
		try {
			Frame f = persistence.read(getClass(), "csv-test.csv");
			assertNotNull(f);
			assertEquals(5, f.getColCount());
			assertArrayEquals(new String[]{"Year", "Make", "Model", "Description", "Price"}, f.getColNames());
		} catch (IOException ex) {
			assertTrue("this should not happen.", false);
		}
	}

	@Test
	public void testLineWithoutQuotas() {
		CsvPersistence csv = new CsvPersistence();

		csv.setColSeparator(',');
		csv.setHasQuotas(false);
		csv.setTrimSpaces(false);
		checkLine(csv, "  , ,,,", new String[]{"  ", " ", "", ""});

		csv.setColSeparator(',');
		csv.setHasQuotas(false);
		csv.setTrimSpaces(true);
		checkLine(csv, "  , ,,,", new String[]{"", "", "", ""});

		csv.setColSeparator(',');
		csv.setHasQuotas(false);
		csv.setTrimSpaces(true);
		checkLine(csv, " ana , are , mere ", new String[]{"ana", "are", "mere"});

		csv.setColSeparator(',');
		csv.setHasQuotas(false);
		csv.setTrimSpaces(false);
		checkLine(csv, " ana , are , mere ", new String[]{" ana ", " are ", " mere "});

		csv.setColSeparator(',');
		csv.setHasQuotas(false);
		csv.setTrimSpaces(false);
		checkLine(csv, "ana,are,mere", new String[]{"ana", "are", "mere"});
	}

	@Test
	public void testLineWithQuotas() {
		CsvPersistence csv = new CsvPersistence();

		csv.setColSeparator(',');
		csv.setHasQuotas(true);
		csv.setTrimSpaces(true);
		csv.setEscapeQuotas('\\');
		checkLine(csv, " \"ana", new String[]{"ana"});

		csv.setColSeparator(',');
		csv.setHasQuotas(true);
		csv.setTrimSpaces(true);
		csv.setEscapeQuotas('\\');
		checkLine(csv, " \"ana\", \"ana again\"", new String[]{"ana", "ana again"});

		csv.setColSeparator(',');
		csv.setHasQuotas(true);
		csv.setTrimSpaces(true);
		csv.setEscapeQuotas('\\');
		checkLine(csv, " \"ana\", \"ana,again\"", new String[]{"ana", "ana,again"});

		csv.setColSeparator(',');
		csv.setHasQuotas(true);
		csv.setTrimSpaces(true);
		csv.setEscapeQuotas('\\');
		checkLine(csv, " \"ana\", \"ana\\\"again\"", new String[]{"ana", "ana\"again"});

		csv.setColSeparator(',');
		csv.setHasQuotas(true);
		csv.setTrimSpaces(true);
		csv.setEscapeQuotas('\"');
		checkLine(csv, " \"ana\", \"ana\"\"again\"", new String[]{"ana", "ana\"again"});
	}

	@Test
	public void testFullFrame() {
		try {
			persistence.setHasQuotas(true);
			Frame df = persistence.read(getClass(), "csv-test.csv");
			assertNotNull(df);
			assertEquals(5, df.getColCount());
			assertArrayEquals(new String[]{"Year", "Make", "Model", "Description", "Price"}, df.getColNames());
		} catch (IOException ex) {
			assertTrue("this should not happen.", false);
		}

	}

	private void checkLine(CsvPersistence csv, String line, String[] matches) {
		List<String> tokens = csv.parseLine(line);
		assertEqualTokens(tokens, matches);
	}

	private void assertEqualTokens(List<String> tokens, String[] matches) {
		assertEquals(tokens.size(), matches.length);
		for (int i = 0; i < tokens.size(); i++) {
			assertEquals(matches[i], tokens.get(i));
		}
	}
}
