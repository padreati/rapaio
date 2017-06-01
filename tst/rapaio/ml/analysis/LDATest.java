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
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.graphics.plot.GridLayer;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.ensemble.CForest;
import rapaio.experiment.ml.eval.CEvaluation;
import rapaio.ml.eval.Confusion;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import static rapaio.graphics.Plotter.*;

public class LDATest {

    private static final Logger logger = Logger.getLogger(LDATest.class.getName());
    private RandomSource randomSource = RandomSource.createRandom();

    @Test
    public void irisDraft() throws IOException, URISyntaxException {
        randomSource.setSeed(123);
        final Frame df = Datasets.loadIrisDataset();
        final String targetName = "class";

        LDA lda = new LDA().withMaxRuns(1_000).withTol(1e-20);
        lda.learn(df, "class");
        lda.printSummary();

        Frame fit = lda.fit(df, (rv, rm) -> 4);

        CForest rf1 = CForest.newRF().withRunPoolSize(0).withRuns(10);
        CForest rf2 = CForest.newRF().withRunPoolSize(0).withRuns(10);

        rf1.train(df, "class");
        CFit fit1 = rf1.fit(df);

        rf2.train(fit.mapVars("0,1,class"), "class");
        CFit fit2 = rf2.fit(fit.mapVars("0~1,class"));

        double acc1 = new Confusion(df.getVar("class"), fit1.firstClasses()).accuracy();
        double acc2 = new Confusion(df.getVar("class"), fit2.firstClasses()).accuracy();

        WS.println(acc1);
        WS.println(acc2);

        Assert.assertTrue(acc1<acc2);
    }

}
