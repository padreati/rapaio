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
import rapaio.data.VarDouble;
import rapaio.datasets.Datasets;
import rapaio.experiment.ml.classifier.boost.*;
import rapaio.ml.classifier.ClassifierModel;
import rapaio.experiment.ml.classifier.tree.CTree;
import rapaio.ml.common.VarSelector;
import rapaio.ml.eval.Confusion;
import rapaio.printer.idea.IdeaPrinter;
import rapaio.sys.WS;

import java.io.IOException;
import java.net.URISyntaxException;

public class AdaBoostSAMMETest {

    @Test
    public void testBuild() throws IOException, URISyntaxException {

        WS.setPrinter(new IdeaPrinter());
        AdaBoostSAMME ab = new AdaBoostSAMME()
                .withClassifier(CTree.newC45().withMinCount(5).withMaxDepth(3).withVarSelector(VarSelector.fixed(5)))
                .withRuns(10);
        Frame df = Datasets.loadSpamBase();
        int[] rows = SamplingTools.sampleWOR(df.rowCount(), df.rowCount() / 2);
        Frame tr = df.mapRows(rows);
        Frame te = df.removeRows(rows);

        String target = "spam";

        VarDouble runs = VarDouble.empty().withName("runs");
        VarDouble errTr = VarDouble.empty().withName("tr");
        VarDouble errTe = VarDouble.empty().withName("te");

        ab.withRunningHook((c, run) -> {
            runs.addDouble(run);
            errTr.addDouble(Confusion.from(tr.rvar(target), ab.predict(tr).classes(target)).error());
            errTe.addDouble(Confusion.from(te.rvar(target), ab.predict(te).classes(target)).error());
        });
        ab.fit(tr, target);
        ab.printSummary();

        Confusion.from(tr.rvar(target), ab.predict(tr).firstClasses()).printSummary();
    }
}
