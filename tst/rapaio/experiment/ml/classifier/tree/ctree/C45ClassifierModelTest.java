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

package rapaio.experiment.ml.classifier.tree.ctree;

import org.junit.Assert;
import org.junit.Test;
import rapaio.experiment.core.tools.DTable;
import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.data.filter.frame.FRetainTypes;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.ClassifierResult;
import rapaio.experiment.ml.classifier.tree.CTree;
import rapaio.ml.eval.Confusion;

import java.io.IOException;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class C45ClassifierModelTest {

    @Test
    public void testNominalInfoGain() throws IOException {
        Frame df = Datasets.loadPlay();
        df = FRetainTypes.on(VType.NOMINAL).fapply(df);
        final String className = "class";

        CTree classifier = CTree.newC45();
        classifier.fit(df, className);
        ClassifierResult pred = classifier.predict(df);

        DTable dtWindy = DTable.fromCounts(df.rvar("windy"), df.rvar("class"), false);
        DTable dtOutlook = DTable.fromCounts(df.rvar("outlook"), df.rvar("class"), false);
        String splitCol = (dtWindy.splitByRowInfoGain() > dtOutlook.splitByRowInfoGain()) ? "windy" : "outlook";
        Assert.assertTrue(classifier.getRoot().getBestCandidate().getGroupPredicates().get(0).toString().contains(splitCol));

        classifier.printSummary();

        Confusion cm = Confusion.from(df.rvar("class"), pred.firstClasses());
        cm.printSummary();
    }

    @Test
    public void testNumericInfoGain() throws IOException {
        Frame df = Datasets.loadPlay();
        df = df.mapVars("temp,humidity,class");
        final String className = "class";

        CTree classifier = CTree.newC45();
        classifier.fit(df, className);
        classifier.printSummary();

        ClassifierResult pred = classifier.predict(df);

        Confusion cm = Confusion.from(df.rvar("class"), pred.firstClasses());
        cm.printSummary();
    }

    @Test
    public void testAllInfoGain() throws IOException {
        Frame df = Datasets.loadPlay();
        final String className = "class";

        CTree classifier = CTree.newC45().withMinCount(1);
        classifier.fit(df, className);
        classifier.printSummary();

        ClassifierResult pred = classifier.predict(df);

        Confusion cm = Confusion.from(df.rvar("class"), pred.firstClasses());
        cm.printSummary();
    }
}
