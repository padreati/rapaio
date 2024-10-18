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

package rapaio.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarBinary;
import rapaio.data.VarInt;
import rapaio.data.VarType;
import rapaio.datasets.Datasets;
import rapaio.text.TextParserException;
import rapaio.util.IntRule;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CsvTest {

    private static Csv persistence;

    @BeforeAll
    static void setUp() {
        persistence = Csv.instance().stripSpaces.set(true).escapeChar.set('\"');
    }

    @Test
    void testHeader() throws IOException {
        Frame f = persistence.read(getClass(), "csv-test.csv");
        assertNotNull(f);
        assertEquals(5, f.varCount());
        assertArrayEquals(new String[] {"Year", "Make", "Model", "Description", "Price"}, f.varNames());
    }

    @Test
    void testLineWithoutQuotas() {
        checkLine(Csv.instance().separatorChar.set(',').quotes.set(false).stripSpaces.set(false),
                "  , ,,,", new String[] {"  ", " ", "", ""});
        checkLine(Csv.instance().separatorChar.set(',').quotes.set(false).stripSpaces.set(true),
                "  , ,,,", new String[] {"", "", "", ""});

        checkLine(Csv.instance().separatorChar.set(',').quotes.set(false).stripSpaces.set(true),
                " ana , are , mere ", new String[] {"ana", "are", "mere"});

        checkLine(Csv.instance().separatorChar.set(',').quotes.set(false).stripSpaces.set(false),
                " ana , are , mere ", new String[] {" ana ", " are ", " mere "});

        checkLine(Csv.instance().separatorChar.set(',').quotes.set(false).stripSpaces.set(false),
                "ana,are,mere", new String[] {"ana", "are", "mere"});
    }

    @Test
    void testLineWithQuotas() {
        checkLine(Csv.instance().separatorChar.set(',').quotes.set(true).stripSpaces.set(true).escapeChar.set('\\'),
                " \"ana", new String[] {"ana"});
        checkLine(Csv.instance().separatorChar.set(',').quotes.set(true).stripSpaces.set(true).escapeChar.set('\\'),
                " \"ana\", \"ana again\"", new String[] {"ana", "ana again"});
        checkLine(Csv.instance().separatorChar.set(',').quotes.set(true).stripSpaces.set(true).escapeChar.set('\\'),
                " \"ana\", \"ana,again\"", new String[] {"ana", "ana,again"});
        checkLine(Csv.instance().separatorChar.set(',').quotes.set(true).stripSpaces.set(true).escapeChar.set('\\'),
                " \"ana\", \"ana\\\"again\"", new String[] {"ana", "ana\"again"});
        checkLine(Csv.instance().separatorChar.set(',').quotes.set(true).stripSpaces.set(true).escapeChar.set('\"'),
                " \"ana\", \"ana\"\"again\"", new String[] {"ana", "ana\"again"});
    }

    @Test
    void testFullFrame() throws IOException {
        persistence.quotes.set(true);
        Frame df = persistence.read(getClass(), "csv-test.csv");
        assertNotNull(df);
        assertEquals(5, df.varCount());
        assertArrayEquals(new String[] {"Year", "Make", "Model", "Description", "Price"}, df.varNames());
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
    void testDefaults() throws IOException {
        Frame df = Csv.instance()
                .quotes.set(true)
                .header.set(true)
                .defaultTypes.set(VarType.BINARY, VarType.INT, VarType.DOUBLE, VarType.NOMINAL)
                .read(this.getClass().getResourceAsStream("defaults-test.csv"));

        assertEquals(7, df.rowCount());

        // x1 is binary

        assertEquals(VarType.BINARY, df.rvar("x1").type());
        assertEquals(0, df.getInt(0, "x1"));
        assertEquals(1, df.getInt(1, "x1"));
        assertEquals(0, df.getInt(2, "x1"));
        assertEquals(1, df.getInt(3, "x1"));
        assertTrue(df.isMissing(4, "x1"));
        assertEquals(0, df.getInt(5, "x1"));
        assertEquals(1, df.getInt(6, "x1"));

        // x2 is index

        assertEquals(VarType.INT, df.rvar("x2").type());
        assertEquals(0, df.getInt(0, "x2"));
        assertEquals(1, df.getInt(1, "x2"));
        assertEquals(0, df.getInt(2, "x2"));
        assertEquals(1, df.getInt(3, "x2"));
        assertTrue(df.isMissing(4, "x2"));
        assertEquals(2, df.getInt(5, "x2"));
        assertEquals(3, df.getInt(6, "x2"));

        // x3 is numeric

        assertEquals(VarType.DOUBLE, df.rvar("x3").type());
        assertEquals(0.0, df.getDouble(0, "x3"), 10e-12);
        assertEquals(1.0, df.getDouble(1, "x3"), 10e-12);
        assertEquals(0.0, df.getDouble(2, "x3"), 10e-12);
        assertEquals(1.0, df.getDouble(3, "x3"), 10e-12);
        assertEquals(Double.NaN, df.getDouble(4, "x3"), 10e-12);
        assertEquals(2.0, df.getDouble(5, "x3"), 10e-12);
        assertEquals(3.0, df.getDouble(6, "x3"), 10e-12);

        // x4 nominal

        assertEquals(VarType.NOMINAL, df.rvar("x4").type());
        assertEquals("0", df.getLabel(0, "x4"));
        assertEquals("1", df.getLabel(1, "x4"));
        assertEquals("false", df.getLabel(2, "x4"));
        assertEquals("other", df.getLabel(3, "x4"));
        assertEquals("?", df.getLabel(4, "x4"));
        assertEquals("2", df.getLabel(5, "x4"));
        assertEquals("3", df.getLabel(6, "x4"));
    }

    @Test
    void testSkipRows() throws IOException {

        List<String> allVarNames = new ArrayList<>();
        allVarNames.add("sepal-length");
        allVarNames.add("sepal-width");
        allVarNames.add("petal-length");
        allVarNames.add("petal-width");
        allVarNames.add("class");

        // test no skip
        Frame full = Csv.instance().read(Datasets.class, "iris-r.csv");
        assertEquals(5, full.varCount());
        assertArrayEquals(allVarNames.toArray(), full.varNames());

        // test skip first 10 rows

        Frame r1 = Csv.instance().keepRows.set(IntRule.from(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).negate()).read(Datasets.class, "iris-r.csv");
        Frame r2 = Csv.instance().keepRows.set(row -> row >= 10).read(Datasets.class, "iris-r.csv");
        Frame r3 = Csv.instance().keepRows.set(IntRule.range(10, 150)).read(Datasets.class, "iris-r.csv");

        assertTrue(r1.deepEquals(r2));
        assertTrue(r1.deepEquals(r3));

        // test skip row % 2 == 0 and between 50 and 100

        Frame r5 = Csv.instance().startRow.set(50).endRow.set(100).keepRows.set(row -> row % 2 != 0).read(Datasets.class, "iris-r.csv");
        assertEquals(25, r5.rowCount());
        assertEquals("?", r5.rvar("class").levels().get(0));
        assertEquals("virginica", r5.rvar("class").levels().get(1));

        // test skip vars 0 and 2

        Frame v1 = Csv.instance().keepCols.set(IntRule.from(0, 2).negate()).read(Datasets.class, "iris-r.csv");
        Frame v2 = Csv.instance().keepCols.set(i -> i != 0 && i != 2).read(Datasets.class, "iris-r.csv");
        Frame v3 = Csv.instance().keepCols.set(IntRule.from(1, 3, 4)).read(Datasets.class, "iris-r.csv");

        assertEquals(3, v1.varCount());
        assertTrue(v1.deepEquals(v2));
        assertTrue(v1.deepEquals(v3));

        // test mixed

        Frame m1 = Csv.instance()
                .keepRows.set(IntRule.range(20, 30))
                .keepCols.set(IntRule.geq(2))
                .read(Datasets.class, "iris-r.csv");
        assertEquals(10, m1.rowCount());
        assertEquals(3, m1.varCount());
    }

    @Test
    void testTypes() throws IOException {
        Frame t1 = Csv.instance()
                .varTypes.add(VarType.DOUBLE, "sepal-length")
                .varTypes.add(VarType.NOMINAL, "petal-width", "sepal-length")
                .read(Datasets.class, "iris-r.csv");

        VarType[] types = new VarType[] {VarType.NOMINAL, VarType.DOUBLE, VarType.DOUBLE, VarType.NOMINAL, VarType.NOMINAL};
        assertArrayEquals(types, t1.varStream().map(Var::type).toArray());

        Frame t2 = Csv.instance().template.set(t1).read(Datasets.class, "iris-r.csv");
        assertTrue(t1.deepEquals(t2));
    }

    @Test
    void testNAValues() throws IOException {
        // no NA values
        Frame na1 = Csv.instance().read(Datasets.class, "iris-r.csv");
        assertEquals(150, na1.stream().complete().count());

        // non existent NA values
        Frame na2 = Csv.instance().naValues.set("", "xxxx").read(Datasets.class, "iris-r.csv");
        assertEquals(150, na2.stream().complete().count());

        Frame na3 =
                Csv.instance().naValues.set("virginica").varTypes.add(VarType.NOMINAL, "sepal-length").read(Datasets.class, "iris-r.csv");
        assertEquals(100, na3.stream().complete().count());

        Frame na4 = Csv.instance().naValues.set("virginica", "5").varTypes.add(VarType.NOMINAL, "sepal-length")
                .read(Datasets.class, "iris-r.csv");
        assertEquals(89, na4.stream().complete().count());
    }

    @Test
    void testParsers() {
        String content = """
                a,b,c
                T,xx,-1
                B,yy,2
                T,aa,-3
                ?,cc,4
                """;
        try {
            Frame df = Csv.instance()
                    .typeParsers.add(VarType.BINARY, value -> {
                        if ("?".equals(value)) {
                            return null;
                        }
                        return "T".equalsIgnoreCase(value);
                    })
                    .varTypes.add(VarType.INT, "c")
                    .varParsers.add("c", value -> -Integer.parseInt(value))
                    .read(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
            assertEquals(3, df.varCount());
            assertEquals(VarType.BINARY, df.rvar(0).type());
            assertTrue(VarBinary.copy(1, 0, 1, -1).name("a").deepEquals(df.rvar("a")));
            assertTrue(VarInt.copy(1, -2, 3, -4).name("c").deepEquals(df.rvar("c")));

            df = Csv.instance()
                    .varTypes.add(VarType.BINARY, "a")
                    .varParsers.add("a", value -> {
                        if ("?".equals(value)) {
                            return null;
                        }
                        return "T".equalsIgnoreCase(value);
                    })
                    .typeParsers.add(VarType.INT, value -> {
                        try {
                            return -Integer.parseInt(value);
                        } catch (NumberFormatException ex) {
                            throw new TextParserException(ex.getMessage());
                        }
                    })
                    .read(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
            assertEquals(3, df.varCount());
            assertEquals(VarType.BINARY, df.rvar(0).type());
            assertTrue(VarBinary.copy(1, 0, 1, -1).name("a").deepEquals(df.rvar("a")));
            assertTrue(VarInt.copy(1, -2, 3, -4).name("c").deepEquals(df.rvar("c")));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
