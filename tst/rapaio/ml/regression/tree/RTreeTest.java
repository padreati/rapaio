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
import rapaio.data.Frame;
import rapaio.data.filter.frame.FFRefSort;
import rapaio.datasets.Datasets;
import rapaio.ml.regression.RFit;

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
        Frame df = Datasets.loadISLAdvertising().removeVars("ID", "Radio", "Newspaper");
        df.printSummary();

        String v = "TV";
        Frame t = new FFRefSort(df.rvar(v).refComparator()).fitApply(df);

        RTree tree = RTree.buildCART()
                .withMaxDepth(10)
                .withMinCount(10)
                .withFunction(RTreeTestFunction.WEIGHTED_SD_GAIN);
        tree.train(t, "Sales");

        Assert.assertEquals("\n" +
                " > TreeClassifier {  varSelector=VarSelector[ALL],\n" +
                "  minCount=10,\n" +
                "  maxDepth=10,\n" +
                "  numericMethod=BINARY,\n" +
                "  nominalMethod=BINARY,\n" +
                "  function=WEIGHTED_SD_GAIN,\n" +
                "  splitter=REMAINS_TO_RANDOM,\n" +
                "  predictor=STANDARD\n" +
                "}\n" +
                "n=200\n" +
                "\n" +
                "description:\n" +
                "split, mean (total weight) [* if is leaf]\n" +
                "\n" +
                "|root  14.0225 (200) \n" +
                "|   |TV <= 107.400000  9.3581081 (74) \n" +
                "|   |   |TV <= 31.500000  6.8444444 (27) \n" +
                "|   |   |   |TV <= 13.200000  5.2818182 (11)  *\n" +
                "|   |   |   |TV > 13.200000  7.91875 (16)  *\n" +
                "|   |   |TV > 31.500000  10.8021277 (47) \n" +
                "|   |   |   |TV <= 66.900000  9.8 (17)  *\n" +
                "|   |   |   |TV > 66.900000  11.37 (30) \n" +
                "|   |   |   |   |TV <= 89.700000  11.6105263 (19)  *\n" +
                "|   |   |   |   |TV > 89.700000  10.9545455 (11)  *\n" +
                "|   |TV > 107.400000  16.7619048 (126) \n" +
                "|   |   |TV <= 180.800000  14.2 (43) \n" +
                "|   |   |   |TV <= 141.300000  13.6695652 (23) \n" +
                "|   |   |   |   |TV <= 125.700000  13.6363636 (11)  *\n" +
                "|   |   |   |   |TV > 125.700000  13.7 (12)  *\n" +
                "|   |   |   |TV > 141.300000  14.81 (20)  *\n" +
                "|   |   |TV > 180.800000  18.0891566 (83) \n" +
                "|   |   |   |TV <= 241.700000  17.2107143 (56) \n" +
                "|   |   |   |   |TV <= 220.500000  17.8472222 (36) \n" +
                "|   |   |   |   |   |TV <= 210.700000  16.8086957 (23) \n" +
                "|   |   |   |   |   |   |TV <= 197.600000  17.0833333 (12)  *\n" +
                "|   |   |   |   |   |   |TV > 197.600000  16.5090909 (11)  *\n" +
                "|   |   |   |   |   |TV > 210.700000  19.6846154 (13)  *\n" +
                "|   |   |   |   |TV > 220.500000  16.065 (20)  *\n" +
                "|   |   |   |TV > 241.700000  19.9111111 (27) \n" +
                "|   |   |   |   |TV <= 265.600000  19.1454545 (11)  *\n" +
                "|   |   |   |   |TV > 265.600000  20.4375 (16)  *\n", tree.summary());

        RFit fit = tree.fit(t);
        fit.printSummary();
    }

}
