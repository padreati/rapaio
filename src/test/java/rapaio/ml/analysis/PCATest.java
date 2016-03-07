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

package rapaio.ml.analysis;

import org.junit.Before;
import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.datasets.Datasets;
import rapaio.io.Csv;
import rapaio.math.linear.RM;
import rapaio.math.linear.dense.SolidRM;
import rapaio.ml.classifier.ensemble.CForest;
import rapaio.experiment.ml.eval.CEvaluation;

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
        RM x = SolidRM.copyOf(df.removeVars("y"));

        PCA pca = new PCA();
        pca.train(df.removeVars("y"));

        Frame fit = pca.fit(df.removeVars("y"), 2);
        pca.printSummary();
    }

    @Test
    public void irisPca() throws IOException, URISyntaxException {
        Frame iris = Datasets.loadIrisDataset();
        Frame x = iris.removeVars("class");

        PCA pca = new PCA();
        pca.train(x);

        pca.printSummary();

        Frame trans = pca.fit(x, 4).bindVars(iris.var("class"));

        CEvaluation.cv(iris, "class", CForest.newRF().withRuns(100), 5);
        CEvaluation.cv(trans, "class", CForest.newRF().withRuns(100), 5);
    }

    @Test
    public void testColinear() {
        Var x = Numeric.copy(1, 2, 3, 4).withName("x");
        Var y = Numeric.copy(2, 3, 4, 5).withName("y");
        Var z = Numeric.copy(4, 2, 6, 9).withName("z");

        PCA pca = new PCA();
        pca.train(SolidFrame.wrapOf(x, y, z));
        pca.printSummary();
    }
}
