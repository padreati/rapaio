/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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
            tt.set(0, i + 1, iris.var(i).name(), 1);
        }
        for (int i = 0; i < iris.rowCount(); i++) {
            tt.set(i + 1, 0, String.valueOf(i + "."), 1);
        }
        for (int i = 0; i < iris.rowCount(); i++) {
            for (int j = 0; j < iris.varCount(); j++) {
                tt.set(i + 1, j + 1, iris.label(i, j), iris.var(j).type().isNumeric() ? 1 : 1);
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
            tt.set(0, i + 1, iris.var(i).name(), 0);
        }
        for (int i = 0; i < iris.rowCount(); i++) {
            tt.set(i + 1, 0, String.valueOf(i + "."), 1);
        }
        for (int i = 0; i < iris.rowCount(); i++) {
            for (int j = 0; j < iris.varCount(); j++) {
                tt.set(i + 1, j + 1, iris.label(i, j), iris.var(j).type().isNumeric() ? 1 : 0);
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
                "   centered footer text bla bla bla bla bla bla blblblb bbbuewdjewhd\n", tt.summary());
    }

    @Test
    public void testWithHorizontalMerge() throws IOException, URISyntaxException {
        Frame iris = Datasets.loadIrisDataset().mapRows(Mapping.range(0, 23));
        TextTable tt = TextTable.newEmpty(iris.rowCount() + 3, iris.varCount() + 2);
        for (int i = 0; i < iris.varCount(); i++) {
            tt.set(0, i + 1, iris.var(i).name(), 0);
        }
        for (int i = 0; i < iris.rowCount(); i++) {
            tt.set(i + 1, 0, String.valueOf(i + "."), 1);
        }
        for (int i = 0; i < iris.rowCount(); i++) {
            for (int j = 0; j < iris.varCount(); j++) {
                tt.set(i + 1, j + 1, iris.label(i, j), iris.var(j).type().isNumeric() ? 1 : 0);
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
//        Assert.assertEquals("     sepal-length sepal-width petal-length petal-width    class          sepal-length   sepal-width petal-length petal-width    class  \n" +
//                "  0.          5.1         3.5          1.4         0.2   setosa    76.              6.8         2.8          4.8         1.4 versicolor\n" +
//                "  1.          4.9         3.0          1.4         0.2   setosa    77.              6.7         3.0          5.0         1.7 versicolor\n" +
//                "  2.          4.7         3.2          1.3         0.2   setosa    78.              6.0         2.9          4.5         1.5 versicolor\n" +
//                "  3.          4.6         3.1          1.5         0.2   setosa    79.              5.7         2.6          3.5         1.0 versicolor\n" +
//                "  4.          5.0         3.6          1.4         0.2   setosa    80.              5.5         2.4          3.8         1.1 versicolor\n" +
//                "  5.          5.4         3.9          1.7         0.4   setosa    81.              5.5         2.4          3.7         1.0 versicolor\n" +
//                "  6.          4.6         3.4          1.4         0.3   setosa    82.              5.8         2.7          3.9         1.2 versicolor\n" +
//                "  7.          5.0         3.4          1.5         0.2   setosa    83.              6.0         2.7          5.1         1.6 versicolor\n" +
//                "  8.          4.4         2.9          1.4         0.2   setosa    84.              5.4         3.0          4.5         1.5 versicolor\n" +
//                "  9.          4.9         3.1          1.5         0.1   setosa    85.              6.0         3.4          4.5         1.6 versicolor\n" +
//                " 10.          5.4         3.7          1.5         0.2   setosa    86.              6.7         3.1          4.7         1.5 versicolor\n" +
//                " 11.          4.8         3.4          1.6         0.2   setosa    87.              6.3         2.3          4.4         1.3 versicolor\n" +
//                " 12.          4.8         3.0          1.4         0.1   setosa    88.              5.6         3.0          4.1         1.3 versicolor\n" +
//                " 13.          4.3         3.0          1.1         0.1   setosa    89.              5.5         2.5          4.0         1.3 versicolor\n" +
//                " 14.          5.8         4.0          1.2         0.2   setosa    90.              5.5         2.6          4.4         1.2 versicolor\n" +
//                " 15.          5.7         4.4          1.5         0.4   setosa    91.              6.1         3.0          4.6         1.4 versicolor\n" +
//                " 16.          5.4         3.9          1.3         0.4   setosa    92.              5.8         2.6          4.0         1.2 versicolor\n" +
//                " 17.          5.1         3.5          1.4         0.3   setosa    93.              5.0         2.3          3.3         1.0 versicolor\n" +
//                " 18.          5.7         3.8          1.7         0.3   setosa    94.              5.6         2.7          4.2         1.3 versicolor\n" +
//                " 19.          5.1         3.8          1.5         0.3   setosa    95.              5.7         3.0          4.2         1.2 versicolor\n" +
//                " 20.          5.4         3.4          1.7         0.2   setosa    96.              5.7         2.9          4.2         1.3 versicolor\n" +
//                " 21.          5.1         3.7          1.5         0.4   setosa    97.              6.2         2.9          4.3         1.3 versicolor\n" +
//                " 22.          4.6         3.6          1.0         0.2   setosa    98.              5.1         2.5          3.0         1.1 versicolor\n" +
//                " 23.          5.1         3.3          1.7         0.5   setosa    99.              5.7         2.8          4.1         1.3 versicolor\n" +
//                " 24.          4.8         3.4          1.9         0.2   setosa   100.              6.3         3.3          6.0         2.5  virginica\n" +
//                " 25.          5.0         3.0          1.6         0.2   setosa   101.              5.8         2.7          5.1         1.9  virginica\n" +
//                " 26.          5.0         3.4          1.6         0.4   setosa   102.              7.1         3.0          5.9         2.1  virginica\n" +
//                " 27.          5.2         3.5          1.5         0.2   setosa   103.              6.3         2.9          5.6         1.8  virginica\n" +
//                " 28.          5.2         3.4          1.4         0.2   setosa   104.              6.5         3.0          5.8         2.2  virginica\n" +
//                " 29.          4.7         3.2          1.6         0.2   setosa   105.              7.6         3.0          6.6         2.1  virginica\n" +
//                " 30.          4.8         3.1          1.6         0.2   setosa   106.              4.9         2.5          4.5         1.7  virginica\n" +
//                " 31.          5.4         3.4          1.5         0.4   setosa   107.              7.3         2.9          6.3         1.8  virginica\n" +
//                " 32.          5.2         4.1          1.5         0.1   setosa   108.              6.7         2.5          5.8         1.8  virginica\n" +
//                " 33.          5.5         4.2          1.4         0.2   setosa   109.              7.2         3.6          6.1         2.5  virginica\n" +
//                " 34.          4.9         3.1          1.5         0.2   setosa   110.              6.5         3.2          5.1         2.0  virginica\n" +
//                " 35.          5.0         3.2          1.2         0.2   setosa   111.              6.4         2.7          5.3         1.9  virginica\n" +
//                " 36.          5.5         3.5          1.3         0.2   setosa   112.              6.8         3.0          5.5         2.1  virginica\n" +
//                " 37.          4.9         3.6          1.4         0.1   setosa   113.              5.7         2.5          5.0         2.0  virginica\n" +
//                " 38.          4.4         3.0          1.3         0.2   setosa   114.              5.8         2.8          5.1         2.4  virginica\n" +
//                " 39.          5.1         3.4          1.5         0.2   setosa   115.              6.4         3.2          5.3         2.3  virginica\n" +
//                " 40.          5.0         3.5          1.3         0.3   setosa   116.              6.5         3.0          5.5         1.8  virginica\n" +
//                " 41.          4.5         2.3          1.3         0.3   setosa   117.              7.7         3.8          6.7         2.2  virginica\n" +
//                " 42.          4.4         3.2          1.3         0.2   setosa   118.              7.7         2.6          6.9         2.3  virginica\n" +
//                " 43.          5.0         3.5          1.6         0.6   setosa   119.              6.0         2.2          5.0         1.5  virginica\n" +
//                " 44.          5.1         3.8          1.9         0.4   setosa   120.              6.9         3.2          5.7         2.3  virginica\n" +
//                " 45.          4.8         3.0          1.4         0.3   setosa   121.              5.6         2.8          4.9         2.0  virginica\n" +
//                " 46.          5.1         3.8          1.6         0.2   setosa   122.              7.7         2.8          6.7         2.0  virginica\n" +
//                " 47.          4.6         3.2          1.4         0.2   setosa   123.              6.3         2.7          4.9         1.8  virginica\n" +
//                " 48.          5.3         3.7          1.5         0.2   setosa   124.              6.7         3.3          5.7         2.1  virginica\n" +
//                " 49.          5.0         3.3          1.4         0.2   setosa   125.              7.2         3.2          6.0         1.8  virginica\n" +
//                " 50.          7.0         3.2          4.7         1.4 versicolor 126.              6.2         2.8          4.8         1.8  virginica\n" +
//                " 51.          6.4         3.2          4.5         1.5 versicolor 127.              6.1         3.0          4.9         1.8  virginica\n" +
//                " 52.          6.9         3.1          4.9         1.5 versicolor 128.              6.4         2.8          5.6         2.1  virginica\n" +
//                " 53.          5.5         2.3          4.0         1.3 versicolor 129.              7.2         3.0          5.8         1.6  virginica\n" +
//                " 54.          6.5         2.8          4.6         1.5 versicolor 130.              7.4         2.8          6.1         1.9  virginica\n" +
//                " 55.          5.7         2.8          4.5         1.3 versicolor 131.              7.9         3.8          6.4         2.0  virginica\n" +
//                " 56.          6.3         3.3          4.7         1.6 versicolor 132.              6.4         2.8          5.6         2.2  virginica\n" +
//                " 57.          4.9         2.4          3.3         1.0 versicolor 133.              6.3         2.8          5.1         1.5  virginica\n" +
//                " 58.          6.6         2.9          4.6         1.3 versicolor 134.              6.1         2.6          5.6         1.4  virginica\n" +
//                " 59.          5.2         2.7          3.9         1.4 versicolor 135.              7.7         3.0          6.1         2.3  virginica\n" +
//                " 60.          5.0         2.0          3.5         1.0 versicolor 136.              6.3         3.4          5.6         2.4  virginica\n" +
//                " 61.          5.9         3.0          4.2         1.5 versicolor 137.              6.4         3.1          5.5         1.8  virginica\n" +
//                " 62.          6.0         2.2          4.0         1.0 versicolor 138.              6.0         3.0          4.8         1.8  virginica\n" +
//                " 63.          6.1         2.9          4.7         1.4 versicolor 139.              6.9         3.1          5.4         2.1  virginica\n" +
//                " 64.          5.6         2.9          3.6         1.3 versicolor 140.              6.7         3.1          5.6         2.4  virginica\n" +
//                " 65.          6.7         3.1          4.4         1.4 versicolor 141.              6.9         3.1          5.1         2.3  virginica\n" +
//                " 66.          5.6         3.0          4.5         1.5 versicolor 142.              5.8         2.7          5.1         1.9  virginica\n" +
//                " 67.          5.8         2.7          4.1         1.0 versicolor 143.              6.8         3.2          5.9         2.3  virginica\n" +
//                " 68.          6.2         2.2          4.5         1.5 versicolor 144.              6.7         3.3          5.7         2.5  virginica\n" +
//                " 69.          5.6         2.5          3.9         1.1 versicolor 145.              6.7         3.0          5.2         2.3  virginica\n" +
//                " 70.          5.9         3.2          4.8         1.8 versicolor 146.              6.3         2.5          5.0         1.9  virginica\n" +
//                " 71.          6.1         2.8          4.0         1.3 versicolor 147.              6.5         3.0          5.2         2.0  virginica\n" +
//                " 72.          6.3         2.5          4.9         1.5 versicolor 148.              6.2         3.4          5.4         2.3  virginica\n" +
//                " 73.          6.1         2.8          4.7         1.2 versicolor 149.              5.9         3.0          5.1         1.8  virginica\n" +
//                " 74.          6.4         2.9          4.3         1.3 versicolor                         centered footer text                         \n" +
//                " 75.          6.6         3.0          4.4         1.4 versicolor     centered footer text bla bla bla bla bla bla blblblb bbbuewdjewhd\n", tt.summary());
    }

    @Test
    public void testWithSplit() throws IOException, URISyntaxException {
        Frame iris = Datasets.loadIrisDataset().mapRows(0, 1, 2, 3, 50, 51, 52, 53, 100, 101, 102, 103);
        TextTable tt = TextTable.newEmpty(iris.rowCount() + 1, iris.varCount() + 1);
        for (int i = 0; i < iris.varCount(); i++) {
            tt.set(0, i + 1, iris.var(i).name(), 0);
        }
        for (int i = 0; i < iris.rowCount(); i++) {
            tt.set(i + 1, 0, String.valueOf(i + ")"), 1);
        }
        for (int i = 0; i < iris.rowCount(); i++) {
            for (int j = 0; j < iris.varCount(); j++) {
                tt.set(i + 1, j + 1, iris.label(i, j), iris.var(j).type().isNumeric() ? 1 : 0);
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
                "\n" +
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
                " 11)         1.8  virginica\n" +
                "\n", tt.summary());
    }
}
