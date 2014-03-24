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

package rapaio.sandbox.classifier;

import rapaio.classifier.boost.AdaBoostSAMMEClassifier;
import rapaio.classifier.tools.CTreeTest;
import rapaio.classifier.tree.DecisionStumpClassifier;
import rapaio.core.sample.StatSampling;
import rapaio.core.stat.ConfusionMatrix;
import rapaio.data.Frame;
import rapaio.data.Index;
import rapaio.data.Numeric;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Lines;
import rapaio.printer.LocalPrinter;
import rapaio.workspace.Workspace;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class AdaBoostSAMMEEval {

    public static void main(String[] args) throws IOException, URISyntaxException {

        Workspace.setPrinter(new LocalPrinter());

//        evalWith(Datasets.loadIrisDataset(), "class");
//        evalWith(Datasets.loadSpamBase(), "spam");
//        evalWith(Datasets.loadProstateCancer(), "train");
        evalWith(Datasets.loadMushrooms(), "classes");
    }

    private static void evalWith(Frame df, String targetName) {
        List<Frame> samples = StatSampling.randomSample(df, new int[]{(int) (df.rowCount() * 0.7)});
        Frame tr = samples.get(0);
        Frame te = samples.get(1);

        AdaBoostSAMMEClassifier c = new AdaBoostSAMMEClassifier()
                .setWeak(new DecisionStumpClassifier()
                        .withMethod(CTreeTest.Method.INFO_GAIN)
                        .withMinCount(1))
                .setT(0);

        AdaBoostSAMMEClassifier prev = null;

        Index index = new Index();
        Numeric trainAcc = new Numeric();
        Numeric testAcc = new Numeric();

        for (int i = 1; i <= 500; i++) {
            c.setT(i);
            index.addIndex(i);

            c.learnFurther(tr, targetName, prev);
//            c.learn(tr, targetName);

            c.predict(tr);
            trainAcc.addValue(new ConfusionMatrix(tr.col(targetName), c.pred()).getAccuracy());

            c.predict(te);
            testAcc.addValue(new ConfusionMatrix(te.col(targetName), c.pred()).getAccuracy());

            Workspace.draw(new Plot()
                    .add(new Lines(index, trainAcc).setColorIndex(1))
                    .add(new Lines(index, testAcc).setColorIndex(2)));

            prev = c;
            c = prev.newInstance();
        }

        prev.predict(tr);
        prev.summary();
        new ConfusionMatrix(tr.col(targetName), prev.pred()).summary();


        prev.predict(te);
        prev.summary();
        new ConfusionMatrix(te.col(targetName), prev.pred()).summary();
    }
}
