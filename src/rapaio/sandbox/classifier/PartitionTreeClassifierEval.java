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
import rapaio.classifier.tree.PartitionTreeClassifier;
import rapaio.core.sample.StatSampling;
import rapaio.core.stat.ConfusionMatrix;
import rapaio.data.Frame;
import rapaio.data.Index;
import rapaio.data.Numeric;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Lines;
import rapaio.io.ArffPersistence;
import rapaio.printer.LocalPrinter;
import rapaio.workspace.Summary;
import rapaio.workspace.Workspace;

import java.util.List;

import static rapaio.workspace.Workspace.draw;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class PartitionTreeClassifierEval {

    public static void main(String[] args) throws Exception {


//        testCart(Datasets.loadIrisDataset(), "class");

//        evalWith(Datasets.loadIrisDataset(), "class", 100, 1, 1, true, 4);
        evalWith(Datasets.loadSpamBase(), "spam", 100, 1, 1, true, 20);

//        evalWith(loadArff("breast-cancer"), "Class", 200, 1, 1, true, 40);
//        evalWith(loadArff("letter"), "class", 50, 1, 1, true, 50);
//        evalWith(loadArff("mushroom"), "class", 200, 1, 1, true, 2);
//        evalWith(loadArff("vote"), "Class", 200, 1, 0.6, true, 1);
    }

    private static Frame loadArff(String name) throws Exception {
        final String path = "/home/ati/rapaio/rapaio-data/datasets-UCI/UCI/" + name + ".arff";
        ArffPersistence arff = new ArffPersistence();
        return arff.read(name, path);
    }

    private static void evalWith(Frame df, String targetName, int rounds, int step,
                                 double sampling, boolean bootstrap, int minCount) {

        Workspace.setPrinter(new LocalPrinter());

        Summary.summary(df);

        List<Frame> samples = StatSampling.randomSample(df, new int[]{(int) (df.rowCount() * 0.7)});
        Frame tr = samples.get(0);
        Frame te = samples.get(1);

        AdaBoostSAMMEClassifier c = new AdaBoostSAMMEClassifier()
                .withClassifier(new PartitionTreeClassifier()
                        .withSplitter(PartitionTreeClassifier.Splitters.REMAINS_TO_ALL_WEIGHTED)
                        .withMaxDepth(10)
                        .withMinCount(minCount))
                .withSampling(sampling, bootstrap);

        Index index = new Index();
        Numeric trainAcc = new Numeric();
        Numeric testAcc = new Numeric();

        for (int i = 1; i <= rounds; i++) {
            c.withRuns(i * step);
            index.addIndex(i * step);

            c.learnFurther(tr, targetName, step);

            c.predict(tr);
            trainAcc.addValue(1 - new ConfusionMatrix(tr.col(targetName), c.pred()).getAccuracy());

            c.predict(te);
            testAcc.addValue(1 - new ConfusionMatrix(te.col(targetName), c.pred()).getAccuracy());

            draw(new Plot()
                    .add(new Lines(index, trainAcc).setCol(1))
                    .add(new Lines(index, testAcc).setCol(2)));
        }

        c.predict(tr);
        c.summary();
        new ConfusionMatrix(tr.col(targetName), c.pred()).summary();


        c.predict(te);
        c.summary();
        new ConfusionMatrix(te.col(targetName), c.pred()).summary();
    }

    public static void testCart(Frame df, String targetCol) {

        PartitionTreeClassifier c = new PartitionTreeClassifier().withMaxDepth(100).withMinCount(2);
        List<Frame> sets = StatSampling.randomSample(df, new int[]{(int) (df.rowCount() * 0.7)});

        Frame tr = sets.get(0);
        Frame te = sets.get(1);

        c.learn(tr, targetCol);
        c.predict(tr);
        new ConfusionMatrix(tr.col(targetCol), c.pred()).summary();
        c.summary();

        c.predict(te);
        new ConfusionMatrix(te.col(targetCol), c.pred()).summary();
        c.summary();
    }
}
