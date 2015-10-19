///*
// * Apache License
// * Version 2.0, January 2004
// * http://www.apache.org/licenses/
// *
// *    Copyright 2013 Aurelian Tutuianu
// *    Copyright 2014 Aurelian Tutuianu
// *    Copyright 2015 Aurelian Tutuianu
// *
// *    Licensed under the Apache License, Version 2.0 (the "License");
// *    you may not use this file except in compliance with the License.
// *    You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// *    Unless required by applicable law or agreed to in writing, software
// *    distributed under the License is distributed on an "AS IS" BASIS,
// *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *    See the License for the specific language governing permissions and
// *    limitations under the License.
// *
// */
//
//package rapaio.ml.classifier.svm;
//
//import org.junit.Assert;
//import org.junit.Test;
//import rapaio.core.RandomSource;
//import rapaio.data.Frame;
//import rapaio.datasets.Datasets;
//import rapaio.ml.classifier.CFit;
//import rapaio.ml.classifier.svm.kernel.RBFKernel;
//
//import java.io.IOException;
//import java.net.URISyntaxException;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
///**
// * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/11/15.
// */
//@Deprecated
//public class BinarySmoTest {
//
//    @Test
//    public void testIrisLinear() throws Exception {
//
//        RandomSource.setSeed(1234L);
//
//        Frame iris = Datasets.loadIrisDataset();
//        BinarySMO smo = new BinarySMO();
//        String fullName = smo.fullName();
//        assertEquals("BinarySMO{sampler=Identity, kernel=PolyKernel(exp=1,bias=1,slope=1), C=1, tol=0.001, classIndex1=1, classIndex2=2, oneVsAll=false, maxRuns=2147483647}",
//                fullName);
//
//        smo.learn(iris, "class");
//        StringBuilder sb = new StringBuilder();
//        smo.buildPrintSummary(sb);
//
//        assertEquals("BinarySMO model\n" +
//                        "===============\n" +
//                        "**Parameters**\n" +
//                        "BinarySMO{sampler=Identity, kernel=PolyKernel(exp=1,bias=1,slope=1), C=1, tol=0.001, classIndex1=1, classIndex2=2, oneVsAll=false, maxRuns=2147483647}\n" +
//                        "\n" +
//                        "**Decision function**\n" +
//                        "Linear support vector: use attribute weights folding instead of kernel dot products.\n" +
//                        "   0.0460395 * [sepal-length]\n" +
//                        " - 0.5217806 * [sepal-width]\n" +
//                        " + 1.0032767 * [petal-length]\n" +
//                        " + 0.4642313 * [petal-width]\n" +
//                        " - 1.4507227",
//                sb.toString());
//    }
//
//    @Test
//    public void testIrisRBF() throws Exception {
//
//        RandomSource.setSeed(1234L);
//
//        Frame iris = Datasets.loadIrisDataset();
//        BinarySMO smo = new BinarySMO()
//                .withKernel(new RBFKernel(2)).withC(0.01)
//                .withFirstClassIndex(2)
//                .withSecondClassIndex(3);
//        String fullName = smo.fullName();
//        assertEquals("BinarySMO{sampler=Identity, kernel=RBF(sigma=2), C=0.01, tol=0.001, classIndex1=2, classIndex2=3, oneVsAll=false, maxRuns=2147483647}",
//                fullName);
//
//        smo.learn(iris, "class");
//        StringBuilder sb = new StringBuilder();
//        smo.buildPrintSummary(sb);
//
//        assertEquals("BinarySMO model\n" +
//                        "===============\n" +
//                        "**Parameters**\n" +
//                        "BinarySMO{sampler=Identity, kernel=RBF(sigma=2), C=0.01, tol=0.001, classIndex1=2, classIndex2=3, oneVsAll=false, maxRuns=2147483647}\n" +
//                        "\n" +
//                        "**Decision function**\n" +
//                        " - 0.01 * <[7,3.2,4.7,1.4], X>\n" +
//                        " - 0.01 * <[6.4,3.2,4.5,1.5], X>\n" +
//                        " - 0.01 * <[6.9,3.1,4.9,1.5], X>\n" +
//                        " - 0.01 * <[5.5,2.3,4,1.3], X>\n" +
//                        " - 0.01 * <[6.5,2.8,4.6,1.5], X>\n" +
//                        " - 0.01 * <[6.3,3.3,4.7,1.6], X>\n" +
//                        " - 0.01 * <[4.9,2.4,3.3,1], X>\n" +
//                        " - 0.01 * <[5.9,3.2,4.8,1.8], X>\n" +
//                        " - 0.01 * <[6.3,2.5,4.9,1.5], X>\n" +
//                        " - 0.01 * <[6.8,2.8,4.8,1.4], X>\n" +
//                        " - 0.01 * <[6.7,3,5,1.7], X>\n" +
//                        " - 0.01 * <[6,2.7,5.1,1.6], X>\n" +
//                        " - 0.0040729 * <[5.4,3,4.5,1.5], X>\n" +
//                        " - 0.01 * <[6.7,3.1,4.7,1.5], X>\n" +
//                        " + 0.01 * <[6.3,3.3,6,2.5], X>\n" +
//                        " + 0.01 * <[5.8,2.7,5.1,1.9], X>\n" +
//                        " + 0.01 * <[7.1,3,5.9,2.1], X>\n" +
//                        " + 0.01 * <[6.3,2.9,5.6,1.8], X>\n" +
//                        " + 0.01 * <[6.5,3,5.8,2.2], X>\n" +
//                        " + 0.0017919 * <[7.6,3,6.6,2.1], X>\n" +
//                        " + 0.0040729 * <[4.9,2.5,4.5,1.7], X>\n" +
//                        " + 0.01 * <[6,2.2,5,1.5], X>\n" +
//                        " + 0.0052842 * <[6.3,2.7,4.9,1.8], X>\n" +
//                        " + 0.01 * <[6.2,2.8,4.8,1.8], X>\n" +
//                        " + 0.01 * <[6.1,3,4.9,1.8], X>\n" +
//                        " + 0.01 * <[7.7,3,6.1,2.3], X>\n" +
//                        " + 0.01 * <[6,3,4.8,1.8], X>\n" +
//                        " + 0.01 * <[6.8,3.2,5.9,2.3], X>\n" +
//                        " + 0.0019297 * <[6.7,3.3,5.7,2.5], X>\n" +
//                        " + 0.0009942 * <[6.7,3,5.2,2.3], X>\n" +
//                        " + 0.01 * <[5.9,3,5.1,1.8], X>\n" +
//                        " + 0.0153502\n" +
//                        "\n" +
//                        "Number of support vectors: 31",
//                sb.toString());
//
//        CFit cr = smo.fit(iris);
//
//        cr.printSummary();
//
//        System.out.println(sb.toString());
//    }
//
//    @Test
//    public void testEqualIndexes() throws IOException, URISyntaxException {
//        try {
//            Frame iris = Datasets.loadIrisDataset();
//            new BinarySMO().withFirstClassIndex(1).withSecondClassIndex(1).learn(iris, "class");
//            assertTrue(false);
//        } catch (IllegalArgumentException ex) {
//            Assert.assertTrue(true);
//            return;
//        }
//        assertTrue(false);
//    }
//}
