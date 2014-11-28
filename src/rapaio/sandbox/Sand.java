/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.sandbox;

import rapaio.core.eval.ConfusionMatrix;
import rapaio.data.Frame;
import rapaio.data.Index;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Points;
import rapaio.ml.classifier.CPrediction;
import rapaio.ml.classifier.boost.AdaBoostSAMMEClassifier;
import rapaio.ml.classifier.meta.SplitClassifier;
import rapaio.ml.classifier.tree.ctree.CTree;
import rapaio.printer.LocalPrinter;
import rapaio.ws.Summary;

import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.WS.draw;
import static rapaio.WS.setPrinter;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class Sand {
    public static void main(String[] args) throws IOException, URISyntaxException {

        setPrinter(new LocalPrinter());

        Frame df = Datasets.loadIrisDataset();
        Summary.summary(df);

        SplitClassifier sc = new SplitClassifier();
        sc.withSplit(s -> s.value("sepal-width") >= 3.1, CTree.newID3());
        sc.withSplit(s -> s.value("sepal-width") < 3.1, new AdaBoostSAMMEClassifier().withClassifier(CTree.newC45()).withRuns(100));

        sc.learn(df, "class");

        CPrediction pred = sc.predict(df, true, true);
        new ConfusionMatrix(df.var("class"), pred.firstClasses()).summary();

        Index hit = Index.newEmpty();
        for (int i = 0; i < df.rowCount(); i++) {
            hit.addBinary(pred.firstClasses().index(i) == df.var("class").index(i));
        }
        draw(new Plot().add(new Points(df.var(0), df.var(1)).color(df.var("class")).pch(hit)));

    }
}
