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

package rapaio.experiment.ml.analysis;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.datasets.Datasets;
import rapaio.experiment.ml.classifier.ensemble.CForest;
import rapaio.io.Csv;
import rapaio.math.linear.RM;
import rapaio.math.linear.dense.SolidRM;
import rapaio.ml.eval.metric.Confusion;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Principal component analysis decomposition test
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/2/15.
 */
public class PCATest {

    private static Frame df;

    @BeforeAll
    static void beforeAll() throws Exception {
        df = Csv.instance().read(PCATest.class.getResourceAsStream("pca.csv"));
    }

    @Test
    void pcaTest() {
        RM x = SolidRM.copy(df.removeVars(VRange.of("y")));

        PCA pca = new PCA();
        pca.fit(df.removeVars(VRange.of("y")));

        Frame fit = pca.predict(df.removeVars(VRange.of("y")), 2);
        pca.printSummary();
    }

    @Test
    void irisPca() {
        RandomSource.setSeed(123);
        Frame iris = Datasets.loadIrisDataset();
        Frame x = iris.removeVars(VRange.of("class"));

        PCA pca = new PCA();
        pca.fit(x);

        pca.printSummary();

        Frame fit = pca.predict(x, 4).bindVars(iris.rvar("class"));

        CForest rf1 = CForest.newRF().withPoolSize(0).withRuns(2);
        CForest rf2 = CForest.newRF().withPoolSize(0).withRuns(2);

        rf1.fit(iris, "class");
        var fit1 = rf1.predict(iris);

        rf2.fit(fit.mapVars("0~2,class"), "class");
        var fit2 = rf2.predict(fit.mapVars("0~2,class"));

        double acc1 = Confusion.from(iris.rvar("class"), fit1.firstClasses()).accuracy();
        double acc2 = Confusion.from(iris.rvar("class"), fit2.firstClasses()).accuracy();

        assertTrue(acc1 < acc2);
    }
}
