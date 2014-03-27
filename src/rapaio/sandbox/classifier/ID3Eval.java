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

import rapaio.classifier.Classifier;
import rapaio.classifier.tools.CTreeTest;
import rapaio.classifier.tree.ID3Classifier;
import rapaio.core.sample.StatSampling;
import rapaio.core.stat.ConfusionMatrix;
import rapaio.data.Frame;
import rapaio.data.filters.BaseFilters;
import rapaio.datasets.Datasets;
import rapaio.io.ArffPersistence;
import rapaio.workspace.Summary;

import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class ID3Eval {

    public static void main(String[] args) throws Exception {
//        withData(loadArff("mushroom"), "class", 10);
        withData(BaseFilters.retainNominal(Datasets.loadPlay()), "class", 1);
    }

    private static Frame loadArff(String name) throws Exception {
        final String path = "/home/ati/rapaio/rapaio-data/datasets-UCI/UCI/" + name + ".arff";
        ArffPersistence arff = new ArffPersistence();
        return arff.read(name, path);
    }

    private static void withData(Frame df, String targetName, int minCount) {

        Summary.summary(df);

        List<Frame> samples = StatSampling.randomSample(df, new int[]{(int) (df.rowCount() * 0.7)});
        Frame tr = samples.get(0);
        Frame te = samples.get(1);

        Classifier c = new ID3Classifier()
                .withMethod(CTreeTest.Method.ENTROPY)
                .withMinCount(minCount);

        c.learn(tr, targetName);

        c.predict(tr);
        c.summary();
        new ConfusionMatrix(tr.col(targetName), c.pred()).summary();


        c.predict(te);
        c.summary();
        new ConfusionMatrix(te.col(targetName), c.pred()).summary();
    }

}
