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

package rapaio.data.group;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static rapaio.data.Group.count;
import static rapaio.data.Group.kurtosis;
import static rapaio.data.Group.max;
import static rapaio.data.Group.mean;
import static rapaio.data.Group.min;
import static rapaio.data.Group.nunique;
import static rapaio.data.Group.skewness;
import static rapaio.data.Group.std;
import static rapaio.data.Group.sum;
import static rapaio.sys.With.textWidth;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Group;
import rapaio.data.SolidFrame;
import rapaio.data.VarNominal;
import rapaio.data.VarRange;
import rapaio.datasets.Datasets;
import rapaio.sys.WS;
import rapaio.util.StringBag;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/21/19.
 */
public class GroupTest {

    private Frame iris;
    private Frame play;
    private int textWidth = 0;

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(1234);
        play = Datasets.loadPlay();
        iris = Datasets.loadIrisDataset();
        textWidth = WS.getPrinter().getOptions().textWidth();
        WS.getPrinter().withOptions(textWidth(100));
    }

    @AfterEach
    void afterEach() {
        WS.getPrinter().withOptions(textWidth(textWidth));
    }

    @Test
    void testGroupStrings() {

        Group group1 = Group.from(play, "class", "outlook");
        assertEquals("GroupBy{keys:[class,outlook], group count:5, row count:14}", group1.toString());
        assertEquals("""
                group by: class, outlook
                group count: 5

                     class  outlook  row     temp humidity windy      class  outlook  row     temp humidity windy\s
                 [0] noplay rain     9  ->    71     80     true  [7] play   overcast 7  ->    64     65     true\s
                 [1] noplay rain     10  ->   65     70     true  [8] play   overcast 8  ->    81     75    false\s
                 [2] noplay sunny    1  ->    80     90     true  [9] play   rain     11  ->   75     80    false\s
                 [3] noplay sunny    2  ->    85     85    false [10] play   rain     12  ->   68     80    false\s
                 [4] noplay sunny    3  ->    72     95    false [11] play   rain     13  ->   70     96    false\s
                 [5] play   overcast 5  ->    72     90     true [12] play   sunny    0  ->    75     70     true\s
                 [6] play   overcast 6  ->    83     78    false [13] play   sunny    4  ->    69     70    false\s
                """, group1.toContent());
        assertEquals("""
                group by: class, outlook
                group count: 5

                     class  outlook  row     temp humidity windy      class  outlook  row     temp humidity windy\s
                 [0] noplay rain     9  ->    71     80     true  [7] play   overcast 7  ->    64     65     true\s
                 [1] noplay rain     10  ->   65     70     true  [8] play   overcast 8  ->    81     75    false\s
                 [2] noplay sunny    1  ->    80     90     true  [9] play   rain     11  ->   75     80    false\s
                 [3] noplay sunny    2  ->    85     85    false [10] play   rain     12  ->   68     80    false\s
                 [4] noplay sunny    3  ->    72     95    false [11] play   rain     13  ->   70     96    false\s
                 [5] play   overcast 5  ->    72     90     true [12] play   sunny    0  ->    75     70     true\s
                 [6] play   overcast 6  ->    83     78    false [13] play   sunny    4  ->    69     70    false\s
                """, group1.toFullContent());
        assertEquals("""
                group by: class, outlook
                group count: 5

                     class  outlook  row     temp humidity windy      class  outlook  row     temp humidity windy\s
                 [0] noplay rain     9  ->    71     80     true  [7] play   overcast 7  ->    64     65     true\s
                 [1] noplay rain     10  ->   65     70     true  [8] play   overcast 8  ->    81     75    false\s
                 [2] noplay sunny    1  ->    80     90     true  [9] play   rain     11  ->   75     80    false\s
                 [3] noplay sunny    2  ->    85     85    false [10] play   rain     12  ->   68     80    false\s
                 [4] noplay sunny    3  ->    72     95    false [11] play   rain     13  ->   70     96    false\s
                 [5] play   overcast 5  ->    72     90     true [12] play   sunny    0  ->    75     70     true\s
                 [6] play   overcast 6  ->    83     78    false [13] play   sunny    4  ->    69     70    false\s
                """, group1.toSummary());

        assertEquals("temp,humidity,windy", String.join(",", group1.getFeatureNameList()));

        Group group2 = Group.from(iris, "class");
        assertEquals("""
                group by: class
                group count: 3

                      class     row      sepal-length sepal-width petal-length petal-width\s
                  [0] setosa    0  ->        5.1          3.5         1.4          0.2    \s
                  [1] setosa    1  ->        4.9          3           1.4          0.2    \s
                  [2] setosa    2  ->        4.7          3.2         1.3          0.2    \s
                  [3] setosa    3  ->        4.6          3.1         1.5          0.2    \s
                  [4] setosa    4  ->        5            3.6         1.4          0.2    \s
                  [5] setosa    5  ->        5.4          3.9         1.7          0.4    \s
                  [6] setosa    6  ->        4.6          3.4         1.4          0.3    \s
                  [7] setosa    7  ->        5            3.4         1.5          0.2    \s
                  [8] setosa    8  ->        4.4          2.9         1.4          0.2    \s
                  [9] setosa    9  ->        4.9          3.1         1.5          0.1    \s
                 [10] setosa    10  ->       5.4          3.7         1.5          0.2    \s
                 [11] setosa    11  ->       4.8          3.4         1.6          0.2    \s
                 [12] setosa    12  ->       4.8          3           1.4          0.1    \s
                 [13] setosa    13  ->       4.3          3           1.1          0.1    \s
                 [14] setosa    14  ->       5.8          4           1.2          0.2    \s
                 [15] setosa    15  ->       5.7          4.4         1.5          0.4    \s
                 [16] setosa    16  ->       5.4          3.9         1.3          0.4    \s
                 [17] setosa    17  ->       5.1          3.5         1.4          0.3    \s
                 [18] setosa    18  ->       5.7          3.8         1.7          0.3    \s
                 [19] setosa    19  ->       5.1          3.8         1.5          0.3    \s
                 [20] setosa    20  ->       5.4          3.4         1.7          0.2    \s
                 [21] setosa    21  ->       5.1          3.7         1.5          0.4    \s
                 [22] setosa    22  ->       4.6          3.6         1            0.2    \s
                 [23] setosa    23  ->       5.1          3.3         1.7          0.5    \s
                 [24] setosa    24  ->       4.8          3.4         1.9          0.2    \s
                 [25] setosa    25  ->       5            3           1.6          0.2    \s
                 [26] setosa    26  ->       5            3.4         1.6          0.4    \s
                 [27] setosa    27  ->       5.2          3.5         1.5          0.2    \s
                 [28] setosa    28  ->       5.2          3.4         1.4          0.2    \s
                 [29] setosa    29  ->       4.7          3.2         1.6          0.2    \s
                 ...  ...                ...          ...         ...          ...        \s
                [141] virginica 141  ->      6.9          3.1         5.1          2.3    \s
                [142] virginica 142  ->      5.8          2.7         5.1          1.9    \s
                [143] virginica 143  ->      6.8          3.2         5.9          2.3    \s
                [144] virginica 144  ->      6.7          3.3         5.7          2.5    \s
                [145] virginica 145  ->      6.7          3           5.2          2.3    \s
                [146] virginica 146  ->      6.3          2.5         5            1.9    \s
                [147] virginica 147  ->      6.5          3           5.2          2      \s
                [148] virginica 148  ->      6.2          3.4         5.4          2.3    \s
                [149] virginica 149  ->      5.9          3           5.1          1.8    \s
                """, group2.toContent());
    }

    @Test
    void testAggregate() {
        Group group1 = Group.from(iris, "class");
        Group.Aggregate agg1 = group1.aggregate(count("petal-width"));

        assertEquals("Group.Aggregate{group=GroupBy{keys:[class], group count:3, row count:150}, funs=[GroupByFunction{name=count,varNames=[petal-width]}]}", agg1.toString());
        assertEquals("""
                group by: class
                group count: 3
                group by functions: GroupByFunction{name=count,varNames=[petal-width]}

                      class    petal-width_count\s
                [0]     setosa                50\s
                [1] versicolor                50\s
                [2]  virginica                50\s

                """, agg1.toContent());
        assertEquals("""
                group by: class
                group count: 3
                group by functions: GroupByFunction{name=count,varNames=[petal-width]}

                      class    petal-width_count\s
                [0]     setosa                50\s
                [1] versicolor                50\s
                [2]  virginica                50\s

                """, agg1.toFullContent());
        assertEquals("""
                group by: class
                group count: 3
                group by functions: GroupByFunction{name=count,varNames=[petal-width]}

                """, agg1.toSummary());

        assertEquals("""
                      class    petal-width_count\s
                [0]     setosa                50\s
                [1] versicolor                50\s
                [2]  virginica                50\s
                """, agg1.toFrame().toContent());

    }

    @Test
    void testGroupFunctions() {

        Group group = Group.from(play, "class");
        assertEquals("""
                    class  outlook_count\s
                [0] noplay             5\s
                [1]   play             9\s
                """, group.aggregate(count("outlook")).toFrame().toContent());
        assertEquals("""
                    class  outlook_count_N1\s
                [0] noplay    0.3571429    \s
                [1]   play    0.6428571    \s
                """, group.aggregate(count(1, "outlook")).toFrame().toContent());

        assertEquals("""
                    class  temp_sum temp_sum_N1 temp_mean temp_mean_N1 windy_nunique windy_nunique_N1 temp_min temp_min_N1\s
                [0] noplay   373     0.3621359    74.6     0.5054201               2       0.5           65     0.503876  \s
                [1]   play   657     0.6378641    73       0.4945799               2       0.5           64     0.496124  \s

                    temp_max temp_max_N1 temp_skewness temp_skewness_N1 temp_std  temp_std_N1 temp_kurtosis temp_kurtosis_N1\s
                [0]    85     0.5059524    0.1894857      0.3651726     7.059745   0.5484741   -1.2885832      0.5910967    \s
                [1]    83     0.4940476    0.3294078      0.6348274     5.8118653  0.4515259   -0.8914041      0.4089033    \s

                """, group.aggregate(
                sum("temp"), sum(1, "temp"), mean("temp"), mean(1, "temp"), nunique("windy"), nunique(1, "windy"),
                min("temp"), min(1, "temp"), max("temp"), max(1, "temp"), skewness("temp"), skewness(1, "temp"),
                std("temp"), std(1, "temp"), kurtosis("temp"), kurtosis(1, "temp")
        ).toFrame().toContent());
    }

    @Test
    void testNominalAggregate() {

        final int N = 100;
        String[] groupLevels = new String[]{"alpha", "beta", "gamma", "delta", "iota", "niu", "miu"};
        String[] fieldLevels = new String[]{"x", "y", "z", "t", "a", "b", "d", "c", "f", "m", "n", "p", "q", "w", "e", "j", "k"};

        VarNominal varGroup = VarNominal.from(N, row -> groupLevels[RandomSource.nextInt(groupLevels.length)], groupLevels).name("group");
        VarNominal field = VarNominal.from(N, row -> fieldLevels[RandomSource.nextInt(fieldLevels.length)], fieldLevels).name("field");

        Frame df = SolidFrame.byVars(varGroup, field);

        Group group = Group.from(df, "group");
        Frame grouped = group.aggregate(count("field")).toFrame();

        Map<StringBag, Integer> counts = new HashMap<>();
        df.stream().forEach(s -> {
            StringBag sb = StringBag.of(s, VarRange.of("group"));
            if (!counts.containsKey(sb)) {
                counts.put(sb, 0);
            }
            counts.put(sb, counts.get(sb) + 1);
        });

        for (int i = 0; i < grouped.rowCount(); i++) {
            String gr = grouped.getLabel(i, "group");
            int count = grouped.getInt(i, "field_count");

            StringBag sb = StringBag.of(Map.of("group", gr));
            assertTrue(counts.containsKey(sb));
            assertEquals((int) counts.get(sb), count);
        }
    }
}
