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
import rapaio.data.Frame;
import rapaio.data.sample.FrameSampler;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.boost.AdaBoostSAMME;
import rapaio.ml.classifier.tree.CTree;
import rapaio.ml.eval.Confusion;

import java.io.File;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
@Deprecated public class SerializationTest {

    @Test
    public void testAdaboost() throws Exception {

        AdaBoostSAMME c = new AdaBoostSAMME()
                .withRuns(10)
                .withClassifier(CTree.newC45().withMaxDepth(3))
                .withSampler(new FrameSampler.Identity());

        Frame df = Datasets.loadSpamBase();
        c.train(df, "spam");

        File file = File.createTempFile("test", "data");

        JavaIO.storeToFile(c, file.getAbsolutePath());

        AdaBoostSAMME restored = (AdaBoostSAMME) JavaIO.restoreFromFile(file.getAbsolutePath());
        CFit fit = restored.fit(df);

        new Confusion(df.var("spam"), fit.firstClasses()).printSummary();
    }
}
