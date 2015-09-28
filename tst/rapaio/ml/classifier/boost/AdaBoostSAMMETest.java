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
import rapaio.graphics.Plotter;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.tree.CTree;
import rapaio.ml.eval.ConfusionMatrix;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.graphics.Plotter.color;
import static rapaio.graphics.Plotter.lines;

public class AdaBoostSAMMETest {

    @Test
    public void testBuild() throws IOException, URISyntaxException {

        WS.setPrinter(new IdeaPrinter());
        AdaBoostSAMME ab = new AdaBoostSAMME().withRuns(1).withClassifier(CTree.newCART().withMaxDepth(2).withMCols(1));
        Frame df = Datasets.loadSpamBase();
        df.printSummary();
        int[] rows = SamplingTools.sampleWOR(df.rowCount() / 2, df.rowCount());
        Frame tr = df.mapRows(rows);
        Frame te = df.removeRows(rows);

        String target = "spam";

        Numeric errTr = Numeric.newEmpty().withName("tr");
        Numeric errTe = Numeric.newEmpty().withName("te");
        for (int i = 0; i < 50; i++) {
            ab.learnFurther(i + 1, tr, target);
            errTr.addValue(new ConfusionMatrix(tr.var(target), ab.fit(tr).classes(target)).error());
            errTe.addValue(new ConfusionMatrix(te.var(target), ab.fit(te).classes(target)).error());

            WS.draw(lines(errTr, color(1)).lines(errTe, color(2)).yLim(0, Double.NaN));
        }
    }
}
