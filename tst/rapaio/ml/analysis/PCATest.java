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

package rapaio.ml.analysis;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.NumericVar;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.datasets.Datasets;
import rapaio.io.Csv;
import rapaio.math.linear.RM;
import rapaio.math.linear.dense.SolidRM;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.ensemble.CForest;
import rapaio.ml.eval.Confusion;
import rapaio.sys.WS;

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
        df = new Csv().read(PCATest.class.getResourceAsStream("pca.csv"));
    }

    @Test
    public void pcaTest() {
        RM x = SolidRM.copy(df.removeVars("y"));

        PCA pca = new PCA();
        pca.train(df.removeVars("y"));

        Frame fit = pca.fit(df.removeVars("y"), 2);
        pca.printSummary();
    }

    @Test
    public void irisPca() throws IOException, URISyntaxException {
        RandomSource.setSeed(123);
        Frame iris = Datasets.loadIrisDataset();
        Frame x = iris.removeVars("class");

        PCA pca = new PCA();
        pca.train(x);

        pca.printSummary();

        Frame fit = pca.fit(x, 4).bindVars(iris.getVar("class"));

        CForest rf1 = CForest.newRF().withRunPoolSize(0).withRuns(10);
        CForest rf2 = CForest.newRF().withRunPoolSize(0).withRuns(10);

        rf1.train(iris, "class");
        CFit fit1 = rf1.fit(iris);

        rf2.train(fit.mapVars("0,1,class"), "class");
        CFit fit2 = rf2.fit(fit.mapVars("0~1,class"));

        double acc1 = new Confusion(iris.getVar("class"), fit1.firstClasses()).accuracy();
        double acc2 = new Confusion(iris.getVar("class"), fit2.firstClasses()).accuracy();

        WS.println(acc1);
        WS.println(acc2);

        Assert.assertTrue(acc1<acc2);
    }

    @Test
    public void testColinear() {
        Var x = NumericVar.copy(1, 2, 3, 4).withName("x");
        Var y = NumericVar.copy(2, 3, 4, 5).withName("y");
        Var z = NumericVar.copy(4, 2, 6, 9).withName("z");

        PCA pca = new PCA();
        pca.train(SolidFrame.byVars(x, y, z));
        pca.printSummary();
    }
}
