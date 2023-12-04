/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.analysis;


import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.VarRange;
import rapaio.datasets.Datasets;
import rapaio.io.Csv;
import rapaio.linear.DMatrix;
import rapaio.ml.eval.metric.Confusion;
import rapaio.ml.model.ensemble.CForest;

/**
 * Principal component analysis decomposition test
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/2/15.
 */
public class PCATest {

    private static final double TOL = 1e-8;
    private static Frame df;

    private Random random;

    @BeforeAll
    static void beforeAll() throws Exception {
        df = Csv.instance().read(PCATest.class.getResourceAsStream("pca.csv")).removeVars("y");
    }

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    @Test
    void centerOnlyTest() {

        // results computed using sklearn

        PCA pca = PCA.newModel().center.set(true);
        pca.fit(df);

        assertArrayEquals(new double[] {1.67100943, 0.83832597, 0.68195393},
                pca.getValues().valueStream().toArray(), TOL);

        double[][] eigenvectors = new double[][] {
                {-0.49210223, -0.64670286, 0.58276136},
                {-0.47927902, -0.35756937, -0.8015209},
                {-0.72672348, 0.67373552, 0.13399043}
        };
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(Math.abs(eigenvectors[i][j]), Math.abs(pca.getVectors().get(i, j)), 1e-6);
            }
        }

        Frame prediction = pca.transform("pca_", df.removeVars(VarRange.of("y")), 3);
        double[] first_row = new double[] {-0.77536344, -1.00011356, 1.61721809};
        for (int i = 0; i < first_row.length; i++) {
            assertEquals(Math.abs(first_row[i]), Math.abs(prediction.getDouble(0, i)), 1e-6);
        }
    }

    @Test
    void centerAndScaling() {
        PCA pca1 = PCA.newModel().center.set(true).standardize.set(true);
        PCA pca2 = PCA.newModel().center.set(true).standardize.set(false);

        var out1 = pca1.fit(df).transform("pca_", df, 3).mapRows(0);
        var out2 = pca2.fit(df).transform("pca_", df, 3).mapRows(0);

        for (int i = 0; i < out1.varCount(); i++) {
            assertNotEquals(out1.getDouble(0, i), out2.getDouble(0, i));
        }

        DMatrix xx = DMatrix.copy(df);
        assertTrue(xx.mean(0).deepEquals(pca1.getMean()));
        assertTrue(xx.sd(0).deepEquals(pca1.getSd()));
    }

    @Test
    void irisPca() {

        Frame iris = Datasets.loadIrisDataset();
        Frame x = iris.removeVars(VarRange.of("class"));

        PCA pca = PCA.newModel();
        pca.fit(x);

        Frame pca2 = pca.transform("pca_", x, 2).bindVars(iris.rvar("class"));
        Frame pca4 = pca.transform("pca_", x, 4).bindVars(iris.rvar("class"));

        long seed = 2639493980909470186L;

        CForest rf2 = CForest.newModel().poolSize.set(-1).runs.set(20).seed.set(seed);
        CForest rf4 = CForest.newModel().poolSize.set(-1).runs.set(20).seed.set(seed);
        var fit1 = rf2.fit(pca2, "class").predict(pca2);
        var fit2 = rf4.fit(pca4, "class").predict(pca4);

        var c1 = Confusion.from(iris.rvar("class"), fit1.firstClasses());
        var c2 = Confusion.from(iris.rvar("class"), fit2.firstClasses());
        var acc1 = c1.accuracy();
        var acc2 = c2.accuracy();
        assertTrue(acc1 < acc2);
    }

    @Test
    void testPrint() {
        PCA pca = PCA.newModel().fit(df);

        assertEquals("PCA{}", pca.toString());
        assertEquals("""
                PCA decomposition
                =================
                input shape: rows=40, vars=3
                eigen values:
                [0] 1.6710094305325662\s
                [1] 0.8383259734162226\s
                [2] 0.6819539303101686\s

                Eigen vectors
                                   [0]                 [1]                  [2]\s
                [0] 0.4921022293062838 -0.6467028606590822 -0.582761362761075  \s
                [1] 0.4792790249461415 -0.3575693744632702  0.8015209034657932 \s
                [2] 0.726723477093221   0.6737355211515162 -0.13399043018153664\s

                """, pca.toSummary());
        assertEquals(pca.toSummary(), pca.toContent());
        assertEquals(pca.toSummary(), pca.toFullContent());
    }
}
