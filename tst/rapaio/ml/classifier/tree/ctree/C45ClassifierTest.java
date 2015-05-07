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

package rapaio.ml.classifier.tree.ctree;

import org.junit.Assert;
import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.VarType;
import rapaio.data.filter.frame.FFRetainTypes;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.ClassifierFit;
import rapaio.ml.classifier.tools.DensityTable;
import rapaio.ml.classifier.tree.CTree;
import rapaio.ml.eval.ConfusionMatrix;
import rapaio.ws.Summary;

import java.io.IOException;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
@Deprecated
public class C45ClassifierTest {

    @Test
    public void testNominalInfoGain() throws IOException {
        Frame df = Datasets.loadPlay();
        df = new FFRetainTypes(VarType.NOMINAL).fitApply(df);
        final String className = "class";

        CTree classifier = CTree.newC45();
        classifier.learn(df, className);
        ClassifierFit pred = classifier.predict(df);

        DensityTable dtWindy = new DensityTable(df.var("windy"), df.var("class"));
        DensityTable dtOutlook = new DensityTable(df.var("outlook"), df.var("class"));
        String splitCol = (dtWindy.getInfoGain() > dtOutlook.getInfoGain()) ? "windy" : "outlook";
        Assert.assertTrue(classifier.getRoot().getBestCandidate().getGroupNames().get(0).contains(splitCol));

        Summary.printSummary(classifier);

        ConfusionMatrix cm = new ConfusionMatrix(df.var("class"), pred.firstClasses());
        Summary.printSummary(cm);
    }

    @Test
    public void testNumericInfoGain() throws IOException {
        Frame df = Datasets.loadPlay();
        df = df.mapVars("temp,humidity,class");
        final String className = "class";

        CTree classifier = CTree.newC45();
        classifier.learn(df, className);
        Summary.printSummary(classifier);

        ClassifierFit pred = classifier.predict(df);

        ConfusionMatrix cm = new ConfusionMatrix(df.var("class"), pred.firstClasses());
        Summary.printSummary(cm);
    }

    @Test
    public void testAllInfoGain() throws IOException {
        Frame df = Datasets.loadPlay();
        final String className = "class";

        CTree classifier = CTree.newC45().withMinCount(1);
        classifier.learn(df, className);
        Summary.printSummary(classifier);

        ClassifierFit pred = classifier.predict(df);

        ConfusionMatrix cm = new ConfusionMatrix(df.var("class"), pred.firstClasses());
        Summary.printSummary(cm);
    }

}
