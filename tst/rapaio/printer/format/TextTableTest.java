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

package rapaio.printer.format;

import org.junit.Assert;
import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.datasets.Datasets;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Tests for text table utility.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/28/15.
 */
public class TextTableTest {

    @Test
    public void testSimple() throws IOException, URISyntaxException {
        Frame iris = Datasets.loadIrisDataset().mapRows(0, 1, 2, 50, 51, 52, 100, 101, 102);
        TextTable tt = TextTable.newEmpty(iris.rowCount() + 1, iris.varCount() + 1);
        for (int i = 0; i < iris.varCount(); i++) {
            tt.set(0, i + 1, iris.rvar(i).name(), 1);
        }
        for (int i = 0; i < iris.rowCount(); i++) {
            tt.set(i + 1, 0, String.valueOf(i + "."), 1);
        }
        for (int i = 0; i < iris.rowCount(); i++) {
            for (int j = 0; j < iris.varCount(); j++) {
                tt.set(i + 1, j + 1, iris.getLabel(i, j), iris.rvar(j).type().isNumeric() ? 1 : 1);
            }
        }
        tt.printSummary();
        Assert.assertEquals("    sepal-length sepal-width petal-length petal-width      class\n" +
                " 0.          5.1         3.5          1.4         0.2     setosa\n" +
                " 1.          4.9         3.0          1.4         0.2     setosa\n" +
                " 2.          4.7         3.2          1.3         0.2     setosa\n" +
                " 3.          7.0         3.2          4.7         1.4 versicolor\n" +
                " 4.          6.4         3.2          4.5         1.5 versicolor\n" +
                " 5.          6.9         3.1          4.9         1.5 versicolor\n" +
                " 6.          6.3         3.3          6.0         2.5  virginica\n" +
                " 7.          5.8         2.7          5.1         1.9  virginica\n" +
                " 8.          7.1         3.0          5.9         2.1  virginica\n", tt.summary());
    }

    @Test
    public void testSimpleWithMerge() throws IOException, URISyntaxException {
        Frame iris = Datasets.loadIrisDataset().mapRows(0, 1, 2, 50, 51, 52, 100, 101, 102);
        TextTable tt = TextTable.newEmpty(iris.rowCount() + 3, iris.varCount() + 1);
        for (int i = 0; i < iris.varCount(); i++) {
            tt.set(0, i + 1, iris.rvar(i).name(), 0);
        }
        for (int i = 0; i < iris.rowCount(); i++) {
            tt.set(i + 1, 0, String.valueOf(i + "."), 1);
        }
        for (int i = 0; i < iris.rowCount(); i++) {
            for (int j = 0; j < iris.varCount(); j++) {
                tt.set(i + 1, j + 1, iris.getLabel(i, j), iris.rvar(j).type().isNumeric() ? 1 : 0);
            }
        }

        tt.mergeCols(iris.rowCount() + 1, 0, iris.varCount() + 1);
        tt.set(iris.rowCount() + 1, 0, "centered footer text", 0);

        tt.mergeCols(iris.rowCount() + 2, 1, iris.varCount());
        tt.set(iris.rowCount() + 2, 1, "centered footer text bla bla bla bla bla bla blblblb bbbuewdjewhd", 0);

        tt.printSummary();
        Assert.assertEquals("      sepal-length   sepal-width petal-length petal-width    class  \n" +
                " 0.              5.1         3.5          1.4         0.2   setosa  \n" +
                " 1.              4.9         3.0          1.4         0.2   setosa  \n" +
                " 2.              4.7         3.2          1.3         0.2   setosa  \n" +
                " 3.              7.0         3.2          4.7         1.4 versicolor\n" +
                " 4.              6.4         3.2          4.5         1.5 versicolor\n" +
                " 5.              6.9         3.1          4.9         1.5 versicolor\n" +
                " 6.              6.3         3.3          6.0         2.5  virginica\n" +
                " 7.              5.8         2.7          5.1         1.9  virginica\n" +
                " 8.              7.1         3.0          5.9         2.1  virginica\n" +
                "                        centered footer text                        \n" +
                "   centered footer text bla bla bla bla bla bla blblblb bbbuewdjewhd\n",
                tt.summary());
    }

    @Test
    public void testWithHorizontalMerge() throws IOException, URISyntaxException {
        Frame iris = Datasets.loadIrisDataset().mapRows(Mapping.range(0, 23));
        TextTable tt = TextTable.newEmpty(iris.rowCount() + 3, iris.varCount() + 2);
        for (int i = 0; i < iris.varCount(); i++) {
            tt.set(0, i + 1, iris.rvar(i).name(), 0);
        }
        for (int i = 0; i < iris.rowCount(); i++) {
            tt.set(i + 1, 0, String.valueOf(i + "."), 1);
        }
        for (int i = 0; i < iris.rowCount(); i++) {
            for (int j = 0; j < iris.varCount(); j++) {
                tt.set(i + 1, j + 1, iris.getLabel(i, j), iris.rvar(j).type().isNumeric() ? 1 : 0);
            }
        }
        for (int i = 0; i < iris.rowCount(); i++) {
            tt.set(i + 1, iris.varCount() + 1, " || ", 1);
        }

        tt.mergeCols(iris.rowCount() + 1, 0, iris.varCount() + 1);
        tt.set(iris.rowCount() + 1, 0, "centered footer text", 0);

        tt.mergeCols(iris.rowCount() + 2, 1, iris.varCount());
        tt.set(iris.rowCount() + 2, 1, "centered footer text bla bla bla bla bla bla blblblb bbbuewdjewhd", 0);

        tt.withHeaderRows(1);
        tt.withMerge(150);

        tt.printSummary();
    }

    @Test
    public void testWithSplit() throws IOException, URISyntaxException {
        Frame iris = Datasets.loadIrisDataset().mapRows(0, 1, 2, 3, 50, 51, 52, 53, 100, 101, 102, 103);
        TextTable tt = TextTable.newEmpty(iris.rowCount() + 1, iris.varCount() + 1);
        for (int i = 0; i < iris.varCount(); i++) {
            tt.set(0, i + 1, iris.rvar(i).name(), 0);
        }
        for (int i = 0; i < iris.rowCount(); i++) {
            tt.set(i + 1, 0, String.valueOf(i + ")"), 1);
        }
        for (int i = 0; i < iris.rowCount(); i++) {
            for (int j = 0; j < iris.varCount(); j++) {
                tt.set(i + 1, j + 1, iris.getLabel(i, j), iris.rvar(j).type().isNumeric() ? 1 : 0);
            }
        }

        tt.withHeaderCols(1);
        tt.withSplit(50);

        tt.printSummary();

        Assert.assertEquals("     sepal-length sepal-width petal-length\n" +
                "  0)          5.1         3.5          1.4\n" +
                "  1)          4.9         3.0          1.4\n" +
                "  2)          4.7         3.2          1.3\n" +
                "  3)          4.6         3.1          1.5\n" +
                "  4)          7.0         3.2          4.7\n" +
                "  5)          6.4         3.2          4.5\n" +
                "  6)          6.9         3.1          4.9\n" +
                "  7)          5.5         2.3          4.0\n" +
                "  8)          6.3         3.3          6.0\n" +
                "  9)          5.8         2.7          5.1\n" +
                " 10)          7.1         3.0          5.9\n" +
                " 11)          6.3         2.9          5.6\n" +
                "     petal-width    class  \n" +
                "  0)         0.2   setosa  \n" +
                "  1)         0.2   setosa  \n" +
                "  2)         0.2   setosa  \n" +
                "  3)         0.2   setosa  \n" +
                "  4)         1.4 versicolor\n" +
                "  5)         1.5 versicolor\n" +
                "  6)         1.5 versicolor\n" +
                "  7)         1.3 versicolor\n" +
                "  8)         2.5  virginica\n" +
                "  9)         1.9  virginica\n" +
                " 10)         2.1  virginica\n" +
                " 11)         1.8  virginica\n" ,
                tt.summary());
    }
}
