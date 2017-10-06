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

package rapaio.ml.classifier.boost;

import org.junit.Test;
import rapaio.core.SamplingTools;
import rapaio.data.Frame;
import rapaio.data.NumVar;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.tree.CTree;
import rapaio.ml.eval.Confusion;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.graphics.Plotter.color;

public class AdaBoostSAMMETest {

    @Test
    public void testBuild() throws IOException, URISyntaxException {

        WS.setPrinter(new IdeaPrinter());
        Classifier ab = new AdaBoostSAMME()
                .withClassifier(CTree.newC45().withMinCount(5).withMaxDepth(3).withMCols(5))
                .withRuns(10);
        Frame df = Datasets.loadSpamBase();
        int[] rows = SamplingTools.sampleWOR(df.rowCount(), df.rowCount() / 2);
        Frame tr = df.mapRows(rows);
        Frame te = df.removeRows(rows);

        String target = "spam";

        NumVar runs = NumVar.empty().withName("runs");
        NumVar errTr = NumVar.empty().withName("tr");
        NumVar errTe = NumVar.empty().withName("te");

        ab.withRunningHook((c, run) -> {
            runs.addValue(run);
            errTr.addValue(new Confusion(tr.var(target), ab.fit(tr).classes(target)).error());
            errTe.addValue(new Confusion(te.var(target), ab.fit(te).classes(target)).error());
        });
        ab.train(tr, target);
        ab.printSummary();

        new Confusion(tr.var(target), ab.fit(tr).firstClasses()).printSummary();
    }
}
