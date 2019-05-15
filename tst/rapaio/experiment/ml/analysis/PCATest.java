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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VRange;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.datasets.Datasets;
import rapaio.io.Csv;
import rapaio.math.linear.RM;
import rapaio.math.linear.dense.SolidRM;
import rapaio.ml.classifier.CPrediction;
import rapaio.experiment.ml.classifier.ensemble.CForest;
import rapaio.ml.eval.Confusion;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Principal component analysis decomposition test
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/2/15.
 */
public class PCATest {

    Frame df;

    @Before
    public void setUp() throws Exception {
        df = Csv.instance().read(PCATest.class.getResourceAsStream("pca.csv"));
    }

    @Test
    public void pcaTest() {
        RM x = SolidRM.copy(df.removeVars(VRange.of("y")));

        PCA pca = new PCA();
        pca.fit(df.removeVars(VRange.of("y")));

        Frame fit = pca.predict(df.removeVars(VRange.of("y")), 2);
        pca.printSummary();
    }

    @Test
    public void irisPca() throws IOException, URISyntaxException {
        RandomSource.setSeed(123);
        Frame iris = Datasets.loadIrisDataset();
        Frame x = iris.removeVars(VRange.of("class"));

        PCA pca = new PCA();
        pca.fit(x);

        pca.printSummary();

        Frame fit = pca.predict(x, 4).bindVars(iris.rvar("class"));

        CForest rf1 = CForest.newRF().withRunPoolSize(0).withRuns(10);
        CForest rf2 = CForest.newRF().withRunPoolSize(0).withRuns(10);

        rf1.fit(iris, "class");
        CPrediction fit1 = rf1.predict(iris);

        rf2.fit(fit.mapVars("0~2,class"), "class");
        CPrediction fit2 = rf2.predict(fit.mapVars("0~2,class"));

        double acc1 = new Confusion(iris.rvar("class"), fit1.firstClasses()).accuracy();
        double acc2 = new Confusion(iris.rvar("class"), fit2.firstClasses()).accuracy();

        Assert.assertTrue(acc1<acc2);
    }

    @Test
    public void testColinear() {
        Var x = VarDouble.copy(1, 2, 3, 4).withName("x");
        Var y = VarDouble.copy(2, 3, 4, 5).withName("y");
        Var z = VarDouble.copy(4, 2, 6, 9).withName("z");

        PCA pca = new PCA();
        pca.fit(SolidFrame.byVars(x, y, z));
        pca.printSummary();
    }
}
