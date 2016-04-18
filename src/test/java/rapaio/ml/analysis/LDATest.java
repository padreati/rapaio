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

import org.junit.Test;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.graphics.plot.GridLayer;
import rapaio.ml.classifier.ensemble.CForest;
import rapaio.experiment.ml.eval.CEvaluation;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import static rapaio.graphics.Plotter.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/6/15.
 */
public class LDATest {

    private static final Logger logger = Logger.getLogger(LDATest.class.getName());

    @Test
    public void irisDraft() throws IOException, URISyntaxException {
        final Frame df = Datasets.loadIrisDataset();
        final String targetName = "class";

        LDA lda = new LDA().withMaxRuns(10_000).withTol(1e-30);
        lda.learn(df, "class");
        lda.printSummary();


        Frame fit = lda.fit(df, (rv, rm) -> 4);

        WS.setPrinter(new IdeaPrinter());

        GridLayer gl = new GridLayer(2, 2);
        gl.add(1, 1, points(df.var(0), df.var(1), color(df.var("class")), pch(1)));
        gl.add(1, 2, points(fit.var(0), fit.var(1), color(df.var("class")), pch(1)));

        gl.add(2, 1, points(fit.var(1), fit.var(2), color(df.var("class")), pch(1)));
        gl.add(2, 2, points(fit.var(2), fit.var(3), color(df.var("class")), pch(1)));
//
        WS.draw(gl);

        CEvaluation.cv(df, "class", CForest.newRF().withRuns(100), 10);
        CEvaluation.cv(fit.mapVars("0~1,4"), "class", CForest.newRF().withRuns(100), 10);
    }

}
