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
import rapaio.datasets.Datasets;
import rapaio.io.Csv;
import rapaio.math.linear.*;
import rapaio.ml.classifier.boost.AdaBoostSAMME;
import rapaio.ml.eval.CEvaluation;
import rapaio.sys.WS;

import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.graphics.Plotter.*;

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
        RM x = Linear.newRMCopyOf(df.removeVars("y"));

        PCA pca = new PCA();
        pca.learn(df.removeVars("y"));

        Frame fit = pca.fit(df.removeVars("y"), 2);


        WS.draw(points(fit.var(0), fit.var(1), color(df.var("y")), pch(1))
                .abLine(0, true)
                .abLine(0, false));

        pca.printSummary();
    }

    @Test
    public void irisPca() throws IOException, URISyntaxException {
        Frame iris = Datasets.loadIrisDataset();
        Frame x = iris.removeVars("class");

        PCA pca = new PCA();
        pca.learn(x);

        pca.printSummary();

        Frame trans = pca.fit(x, 3).bindVars(iris.var("class"));

//        GridLayer gl = new GridLayer(2, 2);
//        gl.add(1, 1, points(iris.var(0), iris.var(1), color(iris.var("class")), pch(1)));
//        gl.add(1, 2, points(trans.var(0), trans.var(1), color(iris.var("class")), pch(1)));
//
//        gl.add(2, 1, points(trans.var(1), trans.var(2), color(iris.var("class")), pch(1)));
//        gl.add(2, 2, points(trans.var(2), trans.var(3), color(iris.var("class")), pch(1)));

//        WS.draw(gl);

        CEvaluation.cv(iris, "class", new AdaBoostSAMME().withRuns(10), 5);
        CEvaluation.cv(trans, "class", new AdaBoostSAMME().withRuns(10), 5);
    }
}
