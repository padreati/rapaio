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

package rapaio.ml.regression.tree;

import org.junit.Assert;
import org.junit.Test;
import rapaio.core.SamplingTools;
import rapaio.data.BoundFrame;
import rapaio.data.Frame;
import rapaio.data.NumVar;
import rapaio.data.Var;
import rapaio.data.filter.frame.FFRefSort;
import rapaio.data.sample.RowSampler;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plotter;
import rapaio.ml.regression.RFit;
import rapaio.ml.regression.ensemble.RForest;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import java.io.IOException;
import java.util.List;

import static rapaio.graphics.Plotter.*;

/**
 * Test for regression decision trees
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/5/15.
 */
public class RTreeTest {

    public static final String Sales = "Sales";

    @Test
    public void testSimple() throws IOException {
        Frame df = Datasets.loadISLAdvertising().removeVars("ID", "Radio", "Newspaper");
        df.printSummary();

        String v = "TV";
        Frame t = new FFRefSort(df.rvar(v).refComparator()).fitApply(df);

        RTree tree = RTree.buildCART()
                .withMaxDepth(10)
                .withMinCount(4)
                .withFunction(RTreeTestFunction.WEIGHTED_SD_GAIN);
        tree.train(t, "Sales");

        Assert.assertEquals("\n" +
                " > TreeClassifier {  varSelector=VarSelector[ALL],\n" +
                "  minCount=4,\n" +
                "  maxDepth=10,\n" +
                "  numericMethod=BINARY,\n" +
                "  nominalMethod=BINARY,\n" +
                "  function=WEIGHTED_SD_GAIN,\n" +
                "  splitter=REMAINS_TO_ALL_WEIGHTED,\n" +
                "  predictor=STANDARD\n" +
                "}\n" +
                "n=200\n" +
                "\n" +
                "description:\n" +
                "split, mean (total weight) [* if is leaf]\n" +
                "\n" +
                "|root  14.0225 (200) \n" +
                "|   |TV <= 107.4  9.3581081 (74) \n" +
                "|   |   |TV <= 31.5  6.8444444 (27) \n" +
                "|   |   |   |TV <= 13.2  5.2818182 (11) \n" +
                "|   |   |   |   |TV <= 7.8  4.44 (5)  *\n" +
                "|   |   |   |   |TV > 7.8  5.9833333 (6)  *\n" +
                "|   |   |   |TV > 13.2  7.91875 (16) \n" +
                "|   |   |   |   |TV <= 19.6  7.475 (8)  *\n" +
                "|   |   |   |   |TV > 19.6  8.3625 (8)  *\n" +
                "|   |   |TV > 31.5  10.8021277 (47) \n" +
                "|   |   |   |TV <= 66.9  9.8 (17) \n" +
                "|   |   |   |   |TV <= 44.7  10.0375 (8)  *\n" +
                "|   |   |   |   |TV > 44.7  9.5888889 (9)  *\n" +
                "|   |   |   |TV > 66.9  11.37 (30) \n" +
                "|   |   |   |   |TV <= 89.7  11.6105263 (19) \n" +
                "|   |   |   |   |   |TV <= 75.3  11.8555556 (9)  *\n" +
                "|   |   |   |   |   |TV > 75.3  11.39 (10) \n" +
                "|   |   |   |   |   |   |TV <= 78.2  11.54 (5)  *\n" +
                "|   |   |   |   |   |   |TV > 78.2  11.24 (5)  *\n" +
                "|   |   |   |   |TV > 89.7  10.9545455 (11) \n" +
                "|   |   |   |   |   |TV <= 96.2  10.92 (5)  *\n" +
                "|   |   |   |   |   |TV > 96.2  10.9833333 (6)  *\n" +
                "|   |TV > 107.4  16.7619048 (126) \n" +
                "|   |   |TV <= 180.8  14.2 (43) \n" +
                "|   |   |   |TV <= 141.3  13.6695652 (23) \n" +
                "|   |   |   |   |TV <= 136.2  13.7352941 (17) \n" +
                "|   |   |   |   |   |TV <= 123.1  13.41 (10) \n" +
                "|   |   |   |   |   |   |TV <= 116  13.6 (5)  *\n" +
                "|   |   |   |   |   |   |TV > 116  13.22 (5)  *\n" +
                "|   |   |   |   |   |TV > 123.1  14.2 (7)  *\n" +
                "|   |   |   |   |TV > 136.2  13.4833333 (6)  *\n" +
                "|   |   |   |TV > 141.3  14.81 (20) \n" +
                "|   |   |   |   |TV <= 172.5  14.96 (15) \n" +
                "|   |   |   |   |   |TV <= 164.5  15.0444444 (9)  *\n" +
                "|   |   |   |   |   |TV > 164.5  14.8333333 (6)  *\n" +
                "|   |   |   |   |TV > 172.5  14.36 (5)  *\n" +
                "|   |   |TV > 180.8  18.0891566 (83) \n" +
                "|   |   |   |TV <= 241.7  17.2107143 (56) \n" +
                "|   |   |   |   |TV <= 220.5  17.8472222 (36) \n" +
                "|   |   |   |   |   |TV <= 210.7  16.8086957 (23) \n" +
                "|   |   |   |   |   |   |TV <= 199.1  17.6428571 (14) \n" +
                "|   |   |   |   |   |   |   |TV <= 193.7  17.1444444 (9)  *\n" +
                "|   |   |   |   |   |   |   |TV > 193.7  18.54 (5)  *\n" +
                "|   |   |   |   |   |   |TV > 199.1  15.5111111 (9)  *\n" +
                "|   |   |   |   |   |TV > 210.7  19.6846154 (13) \n" +
                "|   |   |   |   |   |   |TV <= 217.7  20.1625 (8)  *\n" +
                "|   |   |   |   |   |   |TV > 217.7  18.92 (5)  *\n" +
                "|   |   |   |   |TV > 220.5  16.065 (20) \n" +
                "|   |   |   |   |   |TV <= 227.2  12.6 (5)  *\n" +
                "|   |   |   |   |   |TV > 227.2  17.22 (15) \n" +
                "|   |   |   |   |   |   |TV <= 232.1  18.44 (5)  *\n" +
                "|   |   |   |   |   |   |TV > 232.1  16.61 (10) \n" +
                "|   |   |   |   |   |   |   |TV <= 239.3  15.94 (5)  *\n" +
                "|   |   |   |   |   |   |   |TV > 239.3  17.28 (5)  *\n" +
                "|   |   |   |TV > 241.7  19.9111111 (27) \n" +
                "|   |   |   |   |TV <= 262.7  21.0625 (8)  *\n" +
                "|   |   |   |   |TV > 262.7  19.4263158 (19) \n" +
                "|   |   |   |   |   |TV <= 280.7  17.5555556 (9)  *\n" +
                "|   |   |   |   |   |TV > 280.7  21.11 (10) \n" +
                "|   |   |   |   |   |   |TV <= 287.6  21.4 (5)  *\n" +
                "|   |   |   |   |   |   |TV > 287.6  20.82 (5)  *\n", tree.summary());

        RFit fit = tree.fit(t, true);
        fit.printSummary();

    }

    @Test
    public void testISLR() throws IOException {
        Frame df = Datasets.loadISLAdvertising().removeVars("ID");

        List<Frame> frames = SamplingTools.randomSampleSlices(df, 0.7);

        Frame train = frames.get(0);
        Frame test = frames.get(1);

        WS.setPrinter(new IdeaPrinter());

        RTree tree = RTree.buildCART()
                .withMaxDepth(10)
                .withMinCount(2)
                .withFunction(RTreeTestFunction.WEIGHTED_VAR_GAIN);
        tree.train(train, "Sales");
        tree.fit(test, true).printSummary();

        RForest rf = (RForest) RForest.newRF()
                .withRegression(tree)
                .withSampler(RowSampler.subsampler(0.8))
                .withRuns(1000);

        rf.train(train, "Sales");
        rf.fit(test, true).printSummary();
    }
}
