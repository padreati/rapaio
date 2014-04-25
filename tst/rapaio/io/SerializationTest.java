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

package rapaio.io;

import org.junit.Test;
import rapaio.classifier.boost.AdaBoostSAMMEClassifier;
import rapaio.classifier.tree.TreeClassifier;
import rapaio.core.stat.ConfusionMatrix;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;

import java.io.File;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class SerializationTest {

    @Test
    public void testAdaboost() throws Exception {

        AdaBoostSAMMEClassifier c = new AdaBoostSAMMEClassifier()
                .withRuns(10)
                .withClassifier(TreeClassifier.buildC45().withMaxDepth(3))
                .withNoSampling();

        Frame df = Datasets.loadSpamBase();
        c.learn(df, "spam");

        File file = File.createTempFile("test", "data");

        JavaSerialization.storeToFile(c, file.getAbsolutePath());

        AdaBoostSAMMEClassifier restored = (AdaBoostSAMMEClassifier) JavaSerialization.restoreFromFile(file.getAbsolutePath());
        restored.predict(df);

        new ConfusionMatrix(df.col("spam"), restored.pred()).summary();
    }
}
