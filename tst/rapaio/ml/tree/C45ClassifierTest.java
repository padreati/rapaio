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

package rapaio.ml.tree;

import junit.framework.Assert;
import org.junit.Test;
import rapaio.core.stat.ConfusionMatrix;
import rapaio.data.Frame;
import rapaio.data.filters.BaseFilters;
import rapaio.datasets.Datasets;
import rapaio.ml.tools.DensityTable;
import rapaio.workspace.Summary;

import java.io.IOException;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class C45ClassifierTest {

    //	@Test
    public void testNominalInfoGain() throws IOException {
        Frame df = Datasets.loadPlay();
        df = BaseFilters.retainNominal(df);
        final String className = "class";

        C45Classifier classifier = new C45Classifier().setSelection(C45Classifier.SELECTION_GAINRATIO);
        classifier.learn(df, className);
        classifier.predict(df);

        DensityTable dtWindy = new DensityTable(df.col("windy"), df.col("class"));
        DensityTable dtOutlook = new DensityTable(df.col("outlook"), df.col("class"));
        String splitCol = (dtWindy.getInfoGain() > dtOutlook.getInfoGain()) ? "windy" : "outlook";
        Assert.assertEquals(splitCol, classifier.root.testName);

        Summary.summary(classifier);

        ConfusionMatrix cm = new ConfusionMatrix(df.col("class"), classifier.prediction());
        Summary.summary(cm);
    }

    //	@Test
    public void testNumericInfoGain() throws IOException {
        Frame df = Datasets.loadPlay();
        df = BaseFilters.retainCols(df, "temp,humidity,class");
        final String className = "class";

        C45Classifier classifier = new C45Classifier().setSelection(C45Classifier.SELECTION_INFOGAIN);
        classifier.learn(df, className);
        Summary.summary(classifier);

        classifier.predict(df);

        ConfusionMatrix cm = new ConfusionMatrix(df.col("class"), classifier.prediction());
        Summary.summary(cm);
    }

    @Test
    public void testAllInfoGain() throws IOException {
        Frame df = Datasets.loadPlay();
        final String className = "class";

        C45Classifier classifier = new C45Classifier().setSelection(C45Classifier.SELECTION_INFOGAIN);
        classifier.learn(df, className);
        Summary.summary(classifier);

        classifier.predict(df);

        ConfusionMatrix cm = new ConfusionMatrix(df.col("class"), classifier.prediction());
        Summary.summary(cm);
    }

}
