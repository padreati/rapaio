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

package rapaio.ml.classifier.boost;

import org.junit.Test;
import rapaio.core.SamplingTools;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.tree.CTree;
import rapaio.ml.eval.Confusion;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.graphics.Plotter.color;
import static rapaio.graphics.Plotter.plot;

public class AdaBoostSAMMETest {

    @Test
    public void testBuild() throws IOException, URISyntaxException {

        WS.setPrinter(new IdeaPrinter());
        Classifier ab = new AdaBoostSAMME()
                .withClassifier(CTree.newC45().withMinCount(30).withMaxDepth(16).withMCols(10))
                .withRuns(10);
        Frame df = Datasets.loadSpamBase();
        df.printSummary();
        int[] rows = SamplingTools.sampleWOR(df.rowCount(), df.rowCount() / 2);
        Frame tr = df.mapRows(rows);
        Frame te = df.removeRows(rows);

        String target = "spam";

        Numeric runs = Numeric.empty().withName("runs");
        Numeric errTr = Numeric.empty().withName("tr");
        Numeric errTe = Numeric.empty().withName("te");

        ab.withRunningHook((c, run) -> {
            runs.addValue(run);
            errTr.addValue(new Confusion(tr.var(target), ab.fit(tr).classes(target)).error());
            errTe.addValue(new Confusion(te.var(target), ab.fit(te).classes(target)).error());

            WS.draw(
                    plot(color(3))
                            .lines(runs, errTr, color(1))
                            .lines(runs, errTe, color(2))
                            .yLim(0, Double.NaN));
        });
        ab.train(tr, target);
        ab.printSummary();

        new Confusion(tr.var(target), ab.fit(tr).firstClasses()).printSummary();
    }
}
