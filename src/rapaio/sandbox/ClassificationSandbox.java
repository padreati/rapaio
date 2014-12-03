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

import rapaio.core.eval.ROC;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.CResult;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.tree.CForest;
import rapaio.printer.LocalPrinter;

import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.WS.setPrinter;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/3/14.
 */
public class ClassificationSandbox {

    public static void main(String[] args) throws IOException, URISyntaxException {

        setPrinter(new LocalPrinter());

        Frame df = Datasets.loadIrisDataset();

        // make it binary by removing one target class
        df = df.stream().filter(s -> s.index("class") != 2).toMappedFrame();

        Classifier c = CForest.buildRandomForest(1, 2, 0.8);

        c.learn(df, "class");
        CResult cr = c.predict(df, true, true);

        new ROC(cr.firstDensity().var(1), df.var("class"), 1).summary();
    }
}
