
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
import rapaio.core.tools.DTable;
import rapaio.data.Frame;
import rapaio.data.VarType;
import rapaio.data.filter.FFRetainTypes;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.tree.CTree;
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
        df = new FFRetainTypes(VarType.NOMINAL).filter(df);
        final String className = "class";

        CTree id3 = CTree.newID3();
        id3.train(df, className);
        id3.fit(df);
        id3.printSummary();

        DTable dtWindy = DTable.newFromCounts(df.var("windy"), df.var("class"), true);
        DTable dtOutlook = DTable.newFromCounts(df.var("outlook"), df.var("class"), true);
        String splitCol = (dtWindy.splitByRowAverageEntropy() < dtOutlook.splitByRowAverageEntropy()) ? "windy" : "outlook";
        Assert.assertTrue(id3.getRoot().getChildren().get(0).getGroupName().startsWith(splitCol));

        id3.printSummary();
    }

    @Test
    public void testBasicID3InfoGain() throws IOException {
        Frame df = new FFRetainTypes(VarType.NOMINAL).filter(Datasets.loadPlay());
        final String className = "class";

        df.printSummary();

        CTree id3 = new CTree()
                .withTest(VarType.NOMINAL, CTreeTest.Nominal_Full)
                .withFunction(CTree.PurityFunction.InfoGain);
        id3.train(df, className);
        id3.fit(df);
        id3.printSummary();

        DTable dtWindy = DTable.newFromCounts(df.var("windy"), df.var("class"), true);
        DTable dtOutlook = DTable.newFromCounts(df.var("outlook"), df.var("class"), true);
        String splitCol = (dtWindy.splitByRowInfoGain() > dtOutlook.splitByRowInfoGain()) ? "windy" : "outlook";
        Assert.assertTrue(id3.getRoot().getChildren().get(0).getGroupName().startsWith(splitCol));

        id3.printSummary();
    }
}
