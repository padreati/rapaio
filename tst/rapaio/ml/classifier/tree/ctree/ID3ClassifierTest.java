
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

package rapaio.ml.classifier.tree.ctree;

import org.junit.Assert;
import org.junit.Test;
import rapaio.experiment.core.tools.DTable;
import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.data.filter.frame.FRetainTypes;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.tree.CTree;
import rapaio.ml.classifier.tree.CTreePurityFunction;
import rapaio.ml.classifier.tree.CTreeTest;

import java.io.IOException;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ID3ClassifierTest {

    @Test
    public void testBasicID3Entropy() throws IOException {
        Frame df = Datasets.loadPlay();
        df.printSummary();
        df = FRetainTypes.on(VType.NOMINAL).fapply(df);
        final String className = "class";

        CTree id3 = CTree.newID3();
        id3.fit(df, className);
        id3.predict(df);
        id3.printSummary();

        DTable dtWindy = DTable.fromCounts(df.rvar("windy"), df.rvar("class"), true);
        DTable dtOutlook = DTable.fromCounts(df.rvar("outlook"), df.rvar("class"), true);
        String splitCol = (dtWindy.splitByRowAverageEntropy() < dtOutlook.splitByRowAverageEntropy()) ? "windy" : "outlook";
        Assert.assertTrue(id3.getRoot().getChildren().get(0).getGroupName().startsWith(splitCol));

        id3.printSummary();
    }

    @Test
    public void testBasicID3InfoGain() throws IOException {
        Frame df = FRetainTypes.on(VType.NOMINAL).fapply(Datasets.loadPlay());
        final String className = "class";

        df.printSummary();

        CTree id3 = new CTree()
                .withTest(VType.NOMINAL, CTreeTest.NominalFull)
                .withFunction(CTreePurityFunction.InfoGain);
        id3.fit(df, className);
        id3.predict(df);
        id3.printSummary();

        DTable dtWindy = DTable.fromCounts(df.rvar("windy"), df.rvar("class"), true);
        DTable dtOutlook = DTable.fromCounts(df.rvar("outlook"), df.rvar("class"), true);
        String splitCol = (dtWindy.splitByRowInfoGain() > dtOutlook.splitByRowInfoGain()) ? "windy" : "outlook";
        Assert.assertTrue(id3.getRoot().getChildren().get(0).getGroupName().startsWith(splitCol));

        id3.printSummary();
    }
}
