
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

import org.junit.jupiter.api.Test;
import rapaio.core.tools.DensityTable;
import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.data.filter.FRetainTypes;
import rapaio.datasets.Datasets;
import rapaio.experiment.ml.classifier.tree.CTree;
import rapaio.experiment.ml.classifier.tree.CTreePurityFunction;
import rapaio.experiment.ml.classifier.tree.CTreeTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ID3ClassifierModelTest {

    @Test
    void testBasicID3Entropy() {
        Frame df = Datasets.loadPlay();
        df.printSummary();
        df = FRetainTypes.on(VType.NOMINAL).fapply(df);
        final String className = "class";

        CTree id3 = CTree.newID3();
        id3.fit(df, className);
        id3.predict(df);
        id3.printSummary();

        var dtWindy = DensityTable.fromLevelCounts(true, df.rvar("windy"), df.rvar("class"));
        var dtOutlook = DensityTable.fromLevelCounts(true, df.rvar("outlook"), df.rvar("class"));
        String splitCol = (dtWindy.getTools().splitByRowAverageEntropy() < dtOutlook.getTools().splitByRowAverageEntropy()) ? "windy" : "outlook";
        assertTrue(id3.getRoot().getChildren().get(0).getGroupName().startsWith(splitCol));

        id3.printSummary();
    }

    @Test
    void testBasicID3InfoGain() {
        Frame df = FRetainTypes.on(VType.NOMINAL).fapply(Datasets.loadPlay());
        final String className = "class";

        df.printSummary();

        CTree id3 = new CTree()
                .testMap.add(VType.NOMINAL, CTreeTest.NominalFull)
                .purity.set(CTreePurityFunction.InfoGain);
        id3.fit(df, className);
        id3.predict(df);
        id3.printSummary();

        var dtWindy = DensityTable.fromLevelCounts(true, df.rvar("windy"), df.rvar("class"));
        var dtOutlook = DensityTable.fromLevelCounts(true, df.rvar("outlook"), df.rvar("class"));
        String splitCol = (dtWindy.getTools().splitByRowInfoGain() > dtOutlook.getTools().splitByRowInfoGain()) ? "windy" : "outlook";
        assertTrue(id3.getRoot().getChildren().get(0).getGroupName().startsWith(splitCol));

        id3.printSummary();
    }
}
