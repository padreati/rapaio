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
 */

package rapaio.ml.classifier.svm;

import org.junit.Assert;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.CResult;
import rapaio.ml.classifier.svm.kernel.RBFKernel;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/11/15.
 */
public class BinarySmoTest {

    @Test
    public void testIrisLinear() throws Exception {

        RandomSource.setSeed(1234L);

        Frame iris = Datasets.loadIrisDataset();
        BinarySMO smo = new BinarySMO();
        String fullName = smo.fullName();
        assertEquals("BinarySMO{" +
                        "sampler=Identity, " +
                        "kernel=PolyKernel(exp=1.000,bias=1.000,slope=1.000), " +
                        "C=1.000, " +
                        "tol=0.001, " +
                        "classIndex1=1, " +
                        "classIndex2=2, " +
                        "oneVsAll=false, " +
                        "maxRuns=2147483647}",
                fullName);

        smo.learn(iris, "class");
        StringBuilder sb = new StringBuilder();
        smo.buildSummary(sb);

        assertEquals("BinarySMO model\n" +
                        "===============\n" +
                        "**Parameters**\n" +
                        "BinarySMO{sampler=Identity, kernel=PolyKernel(exp=1.000,bias=1.000,slope=1.000), C=1.000, tol=0.001, classIndex1=1, classIndex2=2, oneVsAll=false, maxRuns=2147483647}\n" +
                        "\n" +
                        "**Decision function**\n" +
                        "Linear support vector: use attribute weights folding instead of kernel dot products.\n" +
                        "   0.046039464762139870000000000000 * [sepal-length]\n" +
                        " - 0.521780600637575600000000000000 * [sepal-width]\n" +
                        " + 1.003276669608281500000000000000 * [petal-length]\n" +
                        " + 0.464231269684902970000000000000 * [petal-width]\n" +
                        " - 1.450722717765327200000000000000",
                sb.toString());
    }

    @Test
    public void testIrisRBF() throws Exception {

        RandomSource.setSeed(1234L);

        Frame iris = Datasets.loadIrisDataset();
        BinarySMO smo = new BinarySMO()
                .withKernel(new RBFKernel(2)).withC(0.01)
                .withFirstClassIndex(2)
                .withSecondClassIndex(3);
        String fullName = smo.fullName();
        assertEquals("BinarySMO{" +
                        "sampler=Identity, " +
                        "kernel=RBF(sigma=2.000), " +
                        "C=0.010, " +
                        "tol=0.001, " +
                        "classIndex1=2, " +
                        "classIndex2=3, " +
                        "oneVsAll=false, " +
                        "maxRuns=2147483647}",
                fullName);

        smo.learn(iris, "class");
        StringBuilder sb = new StringBuilder();
        smo.buildSummary(sb);

        assertEquals("BinarySMO model\n" +
                        "===============\n" +
                        "**Parameters**\n" +
                        "BinarySMO{sampler=Identity, kernel=RBF(sigma=2.000), C=0.010, tol=0.001, classIndex1=2, classIndex2=3, oneVsAll=false, maxRuns=2147483647}\n" +
                        "\n" +
                        "**Decision function**\n" +
                        " - 0.010000000000000000000000000000 * <[7.000,3.200,4.700,1.400], X>\n" +
                        " - 0.010000000000000000000000000000 * <[6.400,3.200,4.500,1.500], X>\n" +
                        " - 0.010000000000000000000000000000 * <[6.900,3.100,4.900,1.500], X>\n" +
                        " - 0.009999999999999998000000000000 * <[5.500,2.300,4.000,1.300], X>\n" +
                        " - 0.010000000000000000000000000000 * <[6.500,2.800,4.600,1.500], X>\n" +
                        " - 0.010000000000000000000000000000 * <[6.300,3.300,4.700,1.600], X>\n" +
                        " - 0.009999999999999998000000000000 * <[4.900,2.400,3.300,1.000], X>\n" +
                        " - 0.010000000000000000000000000000 * <[5.900,3.200,4.800,1.800], X>\n" +
                        " - 0.010000000000000000000000000000 * <[6.300,2.500,4.900,1.500], X>\n" +
                        " - 0.010000000000000000000000000000 * <[6.800,2.800,4.800,1.400], X>\n" +
                        " - 0.010000000000000000000000000000 * <[6.700,3.000,5.000,1.700], X>\n" +
                        " - 0.010000000000000000000000000000 * <[6.000,2.700,5.100,1.600], X>\n" +
                        " - 0.004072873895024384000000000000 * <[5.400,3.000,4.500,1.500], X>\n" +
                        " - 0.010000000000000000000000000000 * <[6.700,3.100,4.700,1.500], X>\n" +
                        " + 0.010000000000000000000000000000 * <[6.300,3.300,6.000,2.500], X>\n" +
                        " + 0.010000000000000000000000000000 * <[5.800,2.700,5.100,1.900], X>\n" +
                        " + 0.010000000000000000000000000000 * <[7.100,3.000,5.900,2.100], X>\n" +
                        " + 0.010000000000000000000000000000 * <[6.300,2.900,5.600,1.800], X>\n" +
                        " + 0.010000000000000000000000000000 * <[6.500,3.000,5.800,2.200], X>\n" +
                        " + 0.001791863391359203200000000000 * <[7.600,3.000,6.600,2.100], X>\n" +
                        " + 0.004072873895024383000000000000 * <[4.900,2.500,4.500,1.700], X>\n" +
                        " + 0.010000000000000000000000000000 * <[6.000,2.200,5.000,1.500], X>\n" +
                        " + 0.005284184310569656000000000000 * <[6.300,2.700,4.900,1.800], X>\n" +
                        " + 0.009999999999999998000000000000 * <[6.200,2.800,4.800,1.800], X>\n" +
                        " + 0.010000000000000000000000000000 * <[6.100,3.000,4.900,1.800], X>\n" +
                        " + 0.010000000000000000000000000000 * <[7.700,3.000,6.100,2.300], X>\n" +
                        " + 0.010000000000000000000000000000 * <[6.000,3.000,4.800,1.800], X>\n" +
                        " + 0.010000000000000000000000000000 * <[6.800,3.200,5.900,2.300], X>\n" +
                        " + 0.001929747821591998300000000000 * <[6.700,3.300,5.700,2.500], X>\n" +
                        " + 0.000994204476479142900000000000 * <[6.700,3.000,5.200,2.300], X>\n" +
                        " + 0.010000000000000000000000000000 * <[5.900,3.000,5.100,1.800], X>\n" +
                        " + 0.015350231039748530000000000000\n" +
                        "\n" +
                        "Number of support vectors: 31",
                sb.toString());

        CResult cr = smo.predict(iris);

        cr.summary();

        System.out.println(sb.toString());
    }

    @Test
    public void testEqualIndexes() throws IOException, URISyntaxException {
        try {
            Frame iris = Datasets.loadIrisDataset();
            new BinarySMO().withFirstClassIndex(1).withSecondClassIndex(1).learn(iris, "class");
            assertTrue(false);
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(true);
            return;
        }
        assertTrue(false);
    }
}
