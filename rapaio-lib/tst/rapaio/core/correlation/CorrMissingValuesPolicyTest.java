/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.core.correlation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Random;

import org.junit.jupiter.api.Test;

import rapaio.data.VarDouble;

/**
 * The missing value policy of the correlations is pairwise deletion, for both the Pearson and the Spearman
 * coefficients: the value for a pair of variables depends only on the rows where both are present.
 * Spearman used to delete the rows where any variable was missing, so adding a variable changed the coefficient
 * of two others.
 */
public class CorrMissingValuesPolicyTest {

    private static final double NAN = Double.NaN;

    /** Average ranks, the ties share the mean of the positions they occupy. */
    private static double[] averageRanks(double[] v) {
        int n = v.length;
        double[] ranks = new double[n];
        for (int i = 0; i < n; i++) {
            double less = 0, equal = 0;
            for (int j = 0; j < n; j++) {
                if (v[j] < v[i]) less++;
                else if (v[j] == v[i]) equal++;
            }
            ranks[i] = less + (equal + 1) / 2;
        }
        return ranks;
    }

    private static double textbookPearson(double[] a, double[] b) {
        int n = a.length;
        double ma = 0, mb = 0;
        for (int i = 0; i < n; i++) {
            ma += a[i];
            mb += b[i];
        }
        ma /= n;
        mb /= n;
        double sab = 0, saa = 0, sbb = 0;
        for (int i = 0; i < n; i++) {
            sab += (a[i] - ma) * (b[i] - mb);
            saa += (a[i] - ma) * (a[i] - ma);
            sbb += (b[i] - mb) * (b[i] - mb);
        }
        return sab / Math.sqrt(saa * sbb);
    }

    private static double[][] completePairs(double[] x, double[] y) {
        var a = new ArrayList<Double>();
        var b = new ArrayList<Double>();
        for (int i = 0; i < x.length; i++) {
            if (!Double.isNaN(x[i]) && !Double.isNaN(y[i])) {
                a.add(x[i]);
                b.add(y[i]);
            }
        }
        return new double[][] {a.stream().mapToDouble(d -> d).toArray(), b.stream().mapToDouble(d -> d).toArray()};
    }

    private static double referenceSpearman(double[] x, double[] y) {
        double[][] p = completePairs(x, y);
        return textbookPearson(averageRanks(p[0]), averageRanks(p[1]));
    }

    private static double referencePearson(double[] x, double[] y) {
        double[][] p = completePairs(x, y);
        return textbookPearson(p[0], p[1]);
    }

    @Test
    void spearmanOfAPairDoesNotDependOnAThirdVariable() {
        // x and y are complete; z is missing a row. The ranks of x and y are (1,2,3,4,5) and (1,2,3,5,4), R:
        // cor(x, y, method="spearman") == 0.9. Deleting the row for z made the pair (1,2,3,4) and (1,2,3,5): 1.0
        VarDouble x = VarDouble.wrap(1, 2, 3, 4, 5).name("x");
        VarDouble y = VarDouble.wrap(1, 2, 3, 5, 4).name("y");
        VarDouble z = VarDouble.copy(1, 2, 3, 4, NAN).name("z");

        assertEquals(0.9, CorrSpearman.of(x, y).singleValue(), 1e-12);
        assertEquals(0.9, CorrSpearman.of(x, y, z).matrix().get(0, 1), 1e-12);
        assertEquals(0.9, CorrSpearman.of(x, z, y).matrix().get(0, 2), 1e-12);
    }

    @Test
    void pearsonOfAPairDoesNotDependOnAThirdVariable() {
        VarDouble x = VarDouble.wrap(1, 2, 3, 4, 5).name("x");
        VarDouble y = VarDouble.wrap(1, 2, 3, 5, 4).name("y");
        VarDouble z = VarDouble.copy(1, 2, 3, 4, NAN).name("z");

        assertEquals(0.9, CorrPearson.of(x, y).singleValue(), 1e-12);
        assertEquals(0.9, CorrPearson.of(x, y, z).matrix().get(0, 1), 1e-12);
    }

    @Test
    void ranksAreTakenAfterTheIncompleteRowsAreRemoved() {
        // the rows where y is missing hold the extreme values of x; the ranks of the remaining rows are
        // relative to each other: x = (10, 20, 30) against y = (3, 2, 1) is a perfect negative association
        VarDouble x = VarDouble.copy(1000, 10, 20, 30, -1000);
        VarDouble y = VarDouble.copy(NAN, 3, 2, 1, NAN);
        assertEquals(-1.0, CorrSpearman.of(x, y).singleValue(), 1e-12);
        assertEquals(-1.0, CorrPearson.of(x, y).singleValue(), 1e-12);
    }

    @Test
    void bothCoefficientsMatchTheirDefinitionOnPairwiseCompleteRows() {
        Random random = new Random(7);
        for (int trial = 0; trial < 200; trial++) {
            int n = 8 + random.nextInt(20);
            int vars = 3 + random.nextInt(3);
            double[][] data = new double[vars][n];
            for (int v = 0; v < vars; v++) {
                for (int i = 0; i < n; i++) {
                    // integers make ties frequent, and a quarter of the values are missing
                    data[v][i] = random.nextInt(4) == 0 ? NAN : random.nextInt(6);
                }
            }
            VarDouble[] columns = new VarDouble[vars];
            for (int v = 0; v < vars; v++) {
                columns[v] = VarDouble.wrap(data[v].clone()).name("v" + v);
            }
            var spearman = CorrSpearman.of(columns).matrix();
            var pearson = CorrPearson.of(columns).matrix();
            for (int i = 0; i < vars; i++) {
                for (int j = i + 1; j < vars; j++) {
                    double expectedSpearman = referenceSpearman(data[i], data[j]);
                    double expectedPearson = referencePearson(data[i], data[j]);
                    String where = "trial " + trial + " (" + i + "," + j + ")";
                    if (Double.isNaN(expectedSpearman)) {
                        assertTrue(Double.isNaN(spearman.get(i, j)), where);
                    } else {
                        assertEquals(expectedSpearman, spearman.get(i, j), 1e-10, where);
                    }
                    if (Double.isNaN(expectedPearson)) {
                        assertTrue(Double.isNaN(pearson.get(i, j)), where);
                    } else {
                        assertEquals(expectedPearson, pearson.get(i, j), 1e-10, where);
                    }
                }
            }
        }
    }

    @Test
    void coefficientOfAPairIsTheSameWithOrWithoutTheOtherVariables() {
        Random random = new Random(11);
        for (int trial = 0; trial < 100; trial++) {
            int n = 10 + random.nextInt(20);
            VarDouble[] columns = new VarDouble[4];
            for (int v = 0; v < 4; v++) {
                double[] values = new double[n];
                for (int i = 0; i < n; i++) {
                    values[i] = random.nextInt(5) == 0 ? NAN : random.nextGaussian();
                }
                columns[v] = VarDouble.wrap(values).name("v" + v);
            }
            var spearman = CorrSpearman.of(columns).matrix();
            var pearson = CorrPearson.of(columns).matrix();
            for (int i = 0; i < 4; i++) {
                for (int j = i + 1; j < 4; j++) {
                    assertEquals(CorrSpearman.of(columns[i], columns[j]).singleValue(), spearman.get(i, j), 1e-12);
                    assertEquals(CorrPearson.of(columns[i], columns[j]).singleValue(), pearson.get(i, j), 1e-12);
                }
            }
        }
    }

    @Test
    void matricesAreSymmetricWithAUnitDiagonal() {
        VarDouble a = VarDouble.copy(1, 2, NAN, 4, 5, 6);
        VarDouble b = VarDouble.copy(2, NAN, 3, 5, 4, 7);
        VarDouble c = VarDouble.copy(9, 7, 8, NAN, 1, 2);
        for (var m : new rapaio.core.tools.DistanceMatrix[] {CorrSpearman.of(a, b, c).matrix(), CorrPearson.of(a, b, c).matrix()}) {
            for (int i = 0; i < 3; i++) {
                assertEquals(1.0, m.get(i, i), 0.0);
                for (int j = 0; j < 3; j++) {
                    assertEquals(m.get(i, j), m.get(j, i), 0.0);
                }
            }
        }
    }

    @Test
    void aCorrelationNeverLeavesTheUnitInterval() {
        // rounding used to take the self-correlation of some samples to 1.0000000000000004
        Random random = new Random(42);
        for (int trial = 0; trial < 20_000; trial++) {
            int n = 2 + random.nextInt(6);
            double[] a = new double[n];
            for (int i = 0; i < n; i++) {
                a[i] = random.nextInt(3) == 0 ? random.nextGaussian() * 1e8 : random.nextGaussian();
            }
            double r = CorrPearson.of(VarDouble.wrap(a.clone()), VarDouble.wrap(a.clone())).singleValue();
            assertTrue(Double.isNaN(r) || (r <= 1.0 && r >= -1.0), "trial " + trial + " r=" + r);
            double rn = CorrPearson.of(VarDouble.wrap(a.clone()), VarDouble.wrap(java.util.Arrays.stream(a).map(v -> -v).toArray())).singleValue();
            assertTrue(Double.isNaN(rn) || (rn <= 1.0 && rn >= -1.0), "trial " + trial + " rn=" + rn);
        }
    }
}
