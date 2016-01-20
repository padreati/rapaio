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

package rapaio.ml.classifier.svm;

import org.junit.Assert;
import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.filter.FFStandardize;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.svm.kernel.LogKernel;
import rapaio.ml.classifier.svm.kernel.PolyKernel;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

/**
 * Test for binary smo
 *
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/20/16.
 */
public class BinarySMOTest {

    @Test
    public void testDescription() {
        BinarySMO smo = new BinarySMO();
        assertEquals("BinarySMO\n" +
                "{\n" +
                "   sampler=Identity,\n" +
                "   kernel=PolyKernel(exp=1,bias=1,slope=1),\n" +
                "   C=1,\n" +
                "   tol=0.001,\n" +
                "   classIndex1=1,\n" +
                "   classIndex2=2,\n" +
                "   oneVsAll=false,\n" +
                "   maxRuns=2147483647\n" +
                "}\n",
                smo.fullName());

        assertEquals("BinarySMO\n" +
                        "{\n" +
                        "   sampler=Identity,\n" +
                        "   kernel=Log(degree=1),\n" +
                        "   C=17,\n" +
                        "   tol=0.3,\n" +
                        "   classIndex1=4,\n" +
                        "   classIndex2=7,\n" +
                        "   oneVsAll=true,\n" +
                        "   maxRuns=2147483647\n" +
                        "}\n",
                new BinarySMO()
                        .withTol(0.3)
                        .withC(17)
                        .withFirstClassIndex(4)
                        .withSecondClassIndex(7)
                        .withKernel(new LogKernel(1))
                        .withOneVsAll(true)
                        .fullName());

        assertEquals("BinarySMO\n" +
                        "{\n" +
                        "   sampler=Identity,\n" +
                        "   kernel=Log(degree=1),\n" +
                        "   C=17,\n" +
                        "   tol=0.3,\n" +
                        "   classIndex1=4,\n" +
                        "   classIndex2=7,\n" +
                        "   oneVsAll=true,\n" +
                        "   maxRuns=2147483647\n" +
                        "}\n",
                new BinarySMO()
                        .withTol(0.3)
                        .withC(17)
                        .withFirstClassIndex(4)
                        .withSecondClassIndex(7)
                        .withKernel(new LogKernel(1))
                        .withOneVsAll(true)
                        .newInstance()
                        .fullName());
    }

    @Test
    public void testLinear() throws IOException, URISyntaxException {

        Frame iris = Datasets.loadIrisDataset();
        iris.printSummary();

        BinarySMO smo1 = new BinarySMO()
                .withInputFilters(new FFStandardize())
                .withKernel(new PolyKernel(1))
                .withFirstClassIndex(2)
                .withSecondClassIndex(3);

        smo1.train(iris, "class");
        smo1.printSummary();

        smo1.fit(iris).printSummary();

        BinarySMO smo2 = new BinarySMO()
                .withInputFilters(new FFStandardize())
                .withKernel(new PolyKernel(2))
                .withFirstClassIndex(2)
                .withSecondClassIndex(3);

        smo2.train(iris, "class");
        smo2.printSummary();

        smo2.fit(iris).printSummary();

    }
}
