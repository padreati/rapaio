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
import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.filter.frame.FRefSort;
import rapaio.data.sample.RowSampler;
import rapaio.datasets.Datasets;
import rapaio.ml.regression.RegResult;
import rapaio.experiment.ml.regression.ensemble.RForest;
import rapaio.ml.regression.tree.rtree.RTreePurityFunction;
import rapaio.printer.idea.IdeaPrinter;
import rapaio.sys.WS;

import java.io.IOException;

/**
 * Test for regression decision trees
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/5/15.
 */
public class RTreeTest {

    public static final String Sales = "Sales";

    @Test
    public void testSimple() throws IOException {
        Frame df = Datasets.loadISLAdvertising().removeVars(VRange.of("ID", "Radio", "Newspaper"));
        df.printSummary();

        String v = "TV";
        Frame t = FRefSort.by(df.rvar(v).refComparator()).fapply(df);

        RTree tree = RTree.newCART()
                .withMaxDepth(10)
                .withMinCount(4)
                .withPurityFunction(RTreePurityFunction.WEIGHTED_SD_GAIN);
        tree.fit(t, "Sales");

        Assert.assertEquals("\n" +
                " > TreeClassifier {  minCount=4,\n" +
                "  maxDepth=10,\n" +
                "  maxSize=2147483647,\n" +
                "  test[binary]=NumericBinary,\n" +
                "  test[int]=NumericBinary,\n" +
                "  test[nominal]=NominalBinary,\n" +
                "  test[double]=NumericBinary,\n" +
                "  regressionLoss=L2\n" +
                "  purityFunction=WEIGHTED_SD_GAIN,\n" +
                "  splitter=REMAINS_TO_RANDOM,\n" +
                "  predictor=STANDARD\n" +
                "  varSelector=VarSelector[ALL],\n" +
                "  runs=1,\n" +
                "  poolSize=-1,\n" +
                "  sampler=Identity,\n" +
                "}\n" +
                "\n" +
                "description:\n" +
                "split, mean (total weight) [* if is leaf]\n" +
                "\n" +
                "|root  14.0225 (200) \n" +
                "|   |TV <= 108.6  9.3581081 (74) \n" +
                "|   |   |TV <= 34.2  6.8444444 (27) \n" +
                "|   |   |   |TV <= 15.05  5.2818182 (11) \n" +
                "|   |   |   |   |TV <= 8.1  4.44 (5)  *\n" +
                "|   |   |   |   |TV > 8.1  5.9833333 (6)  *\n" +
                "|   |   |   |TV > 15.05  7.91875 (16) \n" +
                "|   |   |   |   |TV <= 21.7  7.475 (8)  *\n" +
                "|   |   |   |   |TV > 21.7  8.3625 (8)  *\n" +
                "|   |   |TV > 34.2  10.8021277 (47) \n" +
                "|   |   |   |TV <= 67.35  9.8 (17) \n" +
                "|   |   |   |   |TV <= 46.5  10.0375 (8)  *\n" +
                "|   |   |   |   |TV > 46.5  9.5888889 (9)  *\n" +
                "|   |   |   |TV > 67.35  11.37 (30) \n" +
                "|   |   |   |   |TV <= 90.05  11.6105263 (19) \n" +
                "|   |   |   |   |   |TV <= 75.4  11.8555556 (9)  *\n" +
                "|   |   |   |   |   |TV > 75.4  11.39 (10) \n" +
                "|   |   |   |   |   |   |TV <= 79.2  11.54 (5)  *\n" +
                "|   |   |   |   |   |   |TV > 79.2  11.24 (5)  *\n" +
                "|   |   |   |   |TV > 90.05  10.9545455 (11) \n" +
                "|   |   |   |   |   |TV <= 96.7  10.92 (5)  *\n" +
                "|   |   |   |   |   |TV > 96.7  10.9833333 (6)  *\n" +
                "|   |TV > 108.6  16.7619048 (126) \n" +
                "|   |   |TV <= 181.7  14.2 (43) \n" +
                "|   |   |   |TV <= 142.1  13.6695652 (23) \n" +
                "|   |   |   |   |TV <= 137.05  13.7352941 (17) \n" +
                "|   |   |   |   |   |TV <= 124.4  13.41 (10) \n" +
                "|   |   |   |   |   |   |TV <= 116.6  13.6 (5)  *\n" +
                "|   |   |   |   |   |   |TV > 116.6  13.22 (5)  *\n" +
                "|   |   |   |   |   |TV > 124.4  14.2 (7)  *\n" +
                "|   |   |   |   |TV > 137.05  13.4833333 (6)  *\n" +
                "|   |   |   |TV > 142.1  14.81 (20) \n" +
                "|   |   |   |   |TV <= 173.8  14.96 (15) \n" +
                "|   |   |   |   |   |TV <= 165.05  15.0444444 (9)  *\n" +
                "|   |   |   |   |   |TV > 165.05  14.8333333 (6)  *\n" +
                "|   |   |   |   |TV > 173.8  14.36 (5)  *\n" +
                "|   |   |TV > 181.7  18.0891566 (83) \n" +
                "|   |   |   |TV <= 242.45  17.2107143 (56) \n" +
                "|   |   |   |   |TV <= 221.45  17.8472222 (36) \n" +
                "|   |   |   |   |   |TV <= 210.75  16.8086957 (23) \n" +
                "|   |   |   |   |   |   |TV <= 199.45  17.6428571 (14) \n" +
                "|   |   |   |   |   |   |   |TV <= 194.55  17.1444444 (9)  *\n" +
                "|   |   |   |   |   |   |   |TV > 194.55  18.54 (5)  *\n" +
                "|   |   |   |   |   |   |TV > 199.45  15.5111111 (9)  *\n" +
                "|   |   |   |   |   |TV > 210.75  19.6846154 (13) \n" +
                "|   |   |   |   |   |   |TV <= 218.05  20.1625 (8)  *\n" +
                "|   |   |   |   |   |   |TV > 218.05  18.92 (5)  *\n" +
                "|   |   |   |   |TV > 221.45  16.065 (20) \n" +
                "|   |   |   |   |   |TV <= 227.6  12.6 (5)  *\n" +
                "|   |   |   |   |   |TV > 227.6  17.22 (15) \n" +
                "|   |   |   |   |   |   |TV <= 233.3  18.44 (5)  *\n" +
                "|   |   |   |   |   |   |TV > 233.3  16.61 (10) \n" +
                "|   |   |   |   |   |   |   |TV <= 239.55  15.94 (5)  *\n" +
                "|   |   |   |   |   |   |   |TV > 239.55  17.28 (5)  *\n" +
                "|   |   |   |TV > 242.45  19.9111111 (27) \n" +
                "|   |   |   |   |TV <= 262.8  21.0625 (8)  *\n" +
                "|   |   |   |   |TV > 262.8  19.4263158 (19) \n" +
                "|   |   |   |   |   |TV <= 281.05  17.5555556 (9)  *\n" +
                "|   |   |   |   |   |TV > 281.05  21.11 (10) \n" +
                "|   |   |   |   |   |   |TV <= 288.65  21.4 (5)  *\n" +
                "|   |   |   |   |   |   |TV > 288.65  20.82 (5)  *\n", tree.summary());

        RegResult fit = tree.predict(t, true);
        fit.printSummary();

    }

    @Test
    public void testISLR() throws IOException {
        Frame df = Datasets.loadISLAdvertising().removeVars(VRange.of("ID"));

        Frame[] frames = SamplingTools.randomSampleSlices(df, 0.7, 0.3);

        Frame train = frames[0];
        Frame test = frames[1];

        WS.setPrinter(new IdeaPrinter());

        RTree tree = RTree.newCART()
                .withMaxDepth(10)
                .withMinCount(2)
                .withPurityFunction(RTreePurityFunction.WEIGHTED_VAR_GAIN);
        tree.fit(train, "Sales");
        tree.predict(test, true).printSummary();

        RForest rf = (RForest) RForest.newRF()
                .withRegression(tree)
                .withSampler(RowSampler.subsampler(0.8))
                .withRuns(1000);

        rf.fit(train, "Sales");
        rf.predict(test, true).printSummary();

        tree = RTree.newDecisionStump();
        tree.fit(train, "Sales");
        tree.printSummary();

        tree.predict(test, true).printSummary();
    }
}
