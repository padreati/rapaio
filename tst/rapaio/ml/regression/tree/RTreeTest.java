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
        Frame t = new FFRefSort(df.var(v).refComparator()).fitApply(df);

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
                "|   |   |TV <= 28.600000  6.7423077 (26) \n" +
                "|   |   |   |TV <= 13.200000  5.2818182 (11)  *\n" +
                "|   |   |   |TV > 13.200000  7.8133333 (15)  *\n" +
                "|   |   |TV > 28.600000  10.775 (48) \n" +
                "|   |   |   |TV <= 66.900000  9.7833333 (18)  *\n" +
                "|   |   |   |TV > 66.900000  11.37 (30) \n" +
                "|   |   |   |   |TV <= 76.300000  11.6909091 (11)  *\n" +
                "|   |   |   |   |TV > 76.300000  11.1842105 (19)  *\n" +
                "|   |TV > 107.400000  16.7619048 (126) \n" +
                "|   |   |TV <= 193.200000  14.6215686 (51) \n" +
                "|   |   |   |TV <= 147.300000  13.76 (25) \n" +
                "|   |   |   |   |TV <= 125.700000  13.6363636 (11)  *\n" +
                "|   |   |   |   |TV > 125.700000  13.8571429 (14)  *\n" +
                "|   |   |   |TV > 147.300000  15.45 (26)  *\n" +
                "|   |   |TV > 193.200000  18.2173333 (75) \n" +
                "|   |   |   |TV <= 265.600000  17.6152542 (59) \n" +
                "|   |   |   |   |TV <= 218.400000  17.95 (24)  *\n" +
                "|   |   |   |   |TV > 218.400000  17.3857143 (35) \n" +
                "|   |   |   |   |   |TV <= 239.300000  16.3947368 (19)  *\n" +
                "|   |   |   |   |   |TV > 239.300000  18.5625 (16)  *\n" +
                "|   |   |   |TV > 265.600000  20.4375 (16)  *\n", tree.summary());

        RFit fit = tree.fit(t);
        fit.printSummary();
    }

}
