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

package rapaio.io;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rapaio.data.*;
import rapaio.datasets.Datasets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

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
        checkLine(new Csv().withSeparatorChar(',').withQuotes(false).withTrimSpaces(false),
                "  , ,,,", new String[]{"  ", " ", "", ""});
        checkLine(new Csv().withSeparatorChar(',').withQuotes(false).withTrimSpaces(true),
                "  , ,,,", new String[]{"", "", "", ""});

        checkLine(new Csv().withSeparatorChar(',').withQuotes(false).withTrimSpaces(true),
                " ana , are , mere ", new String[]{"ana", "are", "mere"});

        checkLine(new Csv().withSeparatorChar(',').withQuotes(false).withTrimSpaces(false),
                " ana , are , mere ", new String[]{" ana ", " are ", " mere "});

        checkLine(new Csv().withSeparatorChar(',').withQuotes(false).withTrimSpaces(false),
                "ana,are,mere", new String[]{"ana", "are", "mere"});
    }

    @Test
    public void testLineWithQuotas() {
        checkLine(new Csv().withSeparatorChar(',').withQuotes(true).withTrimSpaces(true).withEscapeChar('\\'),
                " \"ana", new String[]{"ana"});
        checkLine(new Csv().withSeparatorChar(',').withQuotes(true).withTrimSpaces(true).withEscapeChar('\\'),
                " \"ana\", \"ana again\"", new String[]{"ana", "ana again"});
        checkLine(new Csv().withSeparatorChar(',').withQuotes(true).withTrimSpaces(true).withEscapeChar('\\'),
                " \"ana\", \"ana,again\"", new String[]{"ana", "ana,again"});
        checkLine(new Csv().withSeparatorChar(',').withQuotes(true).withTrimSpaces(true).withEscapeChar('\\'),
                " \"ana\", \"ana\\\"again\"", new String[]{"ana", "ana\"again"});
        checkLine(new Csv().withSeparatorChar(',').withQuotes(true).withTrimSpaces(true).withEscapeChar('\"'),
                " \"ana\", \"ana\"\"again\"", new String[]{"ana", "ana\"again"});
    }

    @Test
    public void testFullFrame() {
        try {
            persistence.withQuotes(true);
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
        Frame df = new Csv()
                .withQuotes(true)
                .withHeader(true)
                .withDefaultTypes(VarType.BINARY, VarType.INT, VarType.DOUBLE, VarType.NOMINAL)
                .read(this.getClass().getResourceAsStream("defaults-test.csv"));

        assertEquals(7, df.rowCount());

        // x1 is binary

        assertEquals(VarType.BINARY, df.rvar("x1").type());
        assertEquals(false, df.getBoolean(0, "x1"));
        assertEquals(true, df.getBoolean(1, "x1"));
        assertEquals(false, df.getBoolean(2, "x1"));
        assertEquals(true, df.getBoolean(3, "x1"));
        assertEquals(true, df.isMissing(4, "x1"));
        assertEquals(false, df.getBoolean(5, "x1"));
        assertEquals(true, df.getBoolean(6, "x1"));

        // x2 is index

        assertEquals(VarType.INT, df.rvar("x2").type());
        assertEquals(0, df.getInt(0, "x2"));
        assertEquals(1, df.getInt(1, "x2"));
        assertEquals(0, df.getInt(2, "x2"));
        assertEquals(1, df.getInt(3, "x2"));
        assertEquals(true, df.isMissing(4, "x2"));
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
    public void testSkipRows() throws IOException {

        List<String> allVarNames = new ArrayList<>();
        allVarNames.add("sepal-length");
        allVarNames.add("sepal-width");
        allVarNames.add("petal-length");
        allVarNames.add("petal-width");
        allVarNames.add("class");

        // test no skip
        Frame full = new Csv().read(Datasets.class, "iris-r.csv");
        Assert.assertEquals(5, full.varCount());
        Assert.assertArrayEquals(allVarNames.toArray(), full.varNames());

        // test skip first 10 rows

        Frame r1 = new Csv().withSkipRows(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).read(Datasets.class, "iris-r.csv");
        Frame r2 = new Csv().withSkipRows(row -> row < 10).read(Datasets.class, "iris-r.csv");
        Frame r3 = new Csv().withRows(IntStream.range(10, 150).toArray()).read(Datasets.class, "iris-r.csv");
        Frame r4 = new Csv().withRows(row -> row >= 10).read(Datasets.class, "iris-r.csv");

        Assert.assertTrue(r1.deepEquals(r2));
        Assert.assertTrue(r1.deepEquals(r3));
        Assert.assertTrue(r1.deepEquals(r4));

        // test skip row % 2 == 0 and between 50 and 100

        Frame r5 = new Csv().withStartRow(50).withEndRow(100).withSkipRows(row -> row % 2 == 0).read(Datasets.class, "iris-r.csv");
        Assert.assertEquals(25, r5.rowCount());
        assertEquals("?", r5.rvar("class").levels().get(0));
        assertEquals("virginica", r5.rvar("class").levels().get(1));

        // test skip vars 0 and 2

        Frame v1 = new Csv().withSkipCols(0, 2).read(Datasets.class, "iris-r.csv");
        Frame v2 = new Csv().withSkipCols(row -> row == 0 || row == 2).read(Datasets.class, "iris-r.csv");
        Frame v3 = new Csv().withCols(1, 3, 4).read(Datasets.class, "iris-r.csv");
        Frame v4 = new Csv().withCols(row -> (row != 0) && (row != 2)).read(Datasets.class, "iris-r.csv");

        Assert.assertEquals(3, v1.varCount());
        Assert.assertTrue(v1.deepEquals(v2));
        Assert.assertTrue(v1.deepEquals(v3));
        Assert.assertTrue(v1.deepEquals(v4));

        // test mixed

        Frame m1 = new Csv().withRows(row -> row >= 20 && row < 30).withCols(col -> col >= 2).read(Datasets.class, "iris-r.csv");
        Assert.assertEquals(10, m1.rowCount());
        Assert.assertEquals(3, m1.varCount());
    }

    @Test
    public void testTypes() throws IOException {
        Frame t1 = new Csv()
                .withTypes(VarType.DOUBLE, "sepal-length")
                .withTypes(VarType.NOMINAL, "petal-width", "sepal-length")
                .read(Datasets.class, "iris-r.csv");
        t1.printSummary();

        VarType[] types = new VarType[]{VarType.NOMINAL, VarType.DOUBLE, VarType.DOUBLE, VarType.NOMINAL, VarType.NOMINAL};
        Assert.assertArrayEquals(types, t1.varStream().map(Var::type).toArray());

        Frame t2 = new Csv().withTemplate(t1).read(Datasets.class, "iris-r.csv");
        Assert.assertTrue(t1.deepEquals(t2));
    }

    @Test
    public void testNAValues() throws IOException {
        // no NA values
        Frame na1 = new Csv().read(Datasets.class, "iris-r.csv");
        Assert.assertEquals(150, na1.stream().complete().count());

        // non existent NA values
        Frame na2 = new Csv().withNAValues("", "xxxx").read(Datasets.class, "iris-r.csv");
        Assert.assertEquals(150, na2.stream().complete().count());

        Frame na3 = new Csv().withNAValues("virginica").withTypes(VarType.NOMINAL, "sepal-length").read(Datasets.class, "iris-r.csv");
        Assert.assertEquals(100, na3.stream().complete().count());

        Frame na4 = new Csv().withNAValues("virginica", "5").withTypes(VarType.NOMINAL, "sepal-length").read(Datasets.class, "iris-r.csv");
        Assert.assertEquals(89, na4.stream().complete().count());
    }
}
