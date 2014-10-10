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
import rapaio.data.VarType;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CsvTest {

    private Csv persistence;

    @Before
    public void setUp() {
        persistence = new Csv().withTrimSpaces(true).withEscapeChar('\"');
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testHeader() {
        try {
            Frame f = persistence.read(getClass(), "csv-test.csv");
            assertNotNull(f);
            assertEquals(5, f.varCount());
            assertArrayEquals(new String[]{"Year", "Make", "Model", "Description", "Price"}, f.varNames());
        } catch (IOException ex) {
            assertTrue("this should not happen.", false);
        }
    }

    @Test
    public void testLineWithoutQuotas() {
        checkLine(new Csv().withSeparatorChar(',').withQuotas(false).withTrimSpaces(false),
                "  , ,,,", new String[]{"  ", " ", "", ""});
        checkLine(new Csv().withSeparatorChar(',').withQuotas(false).withTrimSpaces(true),
                "  , ,,,", new String[]{"", "", "", ""});

        checkLine(new Csv().withSeparatorChar(',').withQuotas(false).withTrimSpaces(true),
                " ana , are , mere ", new String[]{"ana", "are", "mere"});

        checkLine(new Csv().withSeparatorChar(',').withQuotas(false).withTrimSpaces(false),
                " ana , are , mere ", new String[]{" ana ", " are ", " mere "});

        checkLine(new Csv().withSeparatorChar(',').withQuotas(false).withTrimSpaces(false),
                "ana,are,mere", new String[]{"ana", "are", "mere"});
    }

    @Test
    public void testLineWithQuotas() {
        checkLine(new Csv().withSeparatorChar(',').withQuotas(true).withTrimSpaces(true).withEscapeChar('\\'),
                " \"ana", new String[]{"ana"});
        checkLine(new Csv().withSeparatorChar(',').withQuotas(true).withTrimSpaces(true).withEscapeChar('\\'),
                " \"ana\", \"ana again\"", new String[]{"ana", "ana again"});
        checkLine(new Csv().withSeparatorChar(',').withQuotas(true).withTrimSpaces(true).withEscapeChar('\\'),
                " \"ana\", \"ana,again\"", new String[]{"ana", "ana,again"});
        checkLine(new Csv().withSeparatorChar(',').withQuotas(true).withTrimSpaces(true).withEscapeChar('\\'),
                " \"ana\", \"ana\\\"again\"", new String[]{"ana", "ana\"again"});
        checkLine(new Csv().withSeparatorChar(',').withQuotas(true).withTrimSpaces(true).withEscapeChar('\"'),
                " \"ana\", \"ana\"\"again\"", new String[]{"ana", "ana\"again"});
    }

    @Test
    public void testFullFrame() {
        try {
            persistence.withQuotas(true);
            Frame df = persistence.read(getClass(), "csv-test.csv");
            assertNotNull(df);
            assertEquals(5, df.varCount());
            assertArrayEquals(new String[]{"Year", "Make", "Model", "Description", "Price"}, df.varNames());
        } catch (IOException ex) {
            assertTrue("this should not happen.", false);
        }

    }

    private void checkLine(Csv csv, String line, String[] matches) {
        List<String> tokens = csv.parseLine(line);
        assertEqualTokens(tokens, matches);
    }

    private void assertEqualTokens(List<String> tokens, String[] matches) {
        assertEquals(tokens.size(), matches.length);
        for (int i = 0; i < tokens.size(); i++) {
            assertEquals(matches[i], tokens.get(i));
        }
    }

    @Test
    public void testDefaults() throws IOException {
        Frame df = new Csv().read(this.getClass().getResourceAsStream("defaults-test.csv"));

        // x1 is binary

        assertEquals(df.var("x1").type(), VarType.BINARY);
        assertEquals(false, df.binary(0, "x1"));
        assertEquals(true, df.binary(1, "x1"));
        assertEquals(false, df.binary(2, "x1"));
        assertEquals(true, df.binary(3, "x1"));
        assertEquals(true, df.missing(4, "x1"));
    }
}
