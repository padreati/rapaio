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
import rapaio.graphics.Plotter;
import rapaio.graphics.base.Figure;
import rapaio.graphics.plot.GridLayer;
import rapaio.graphics.plot.Plot;
import rapaio.ml.analysis.LDA;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.svm.kernel.*;
import rapaio.ml.eval.CEvaluation;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static rapaio.graphics.Plotter.*;

/**
 * Test for binary smo
 * <p>
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

        Frame df = Datasets.loadSonar();
        df = df.applyFilters(new FFStandardize("all"));
        df.printSummary();

        String target = "Class";

//        LDA lda = new LDA();
//        lda.learn(df, target);
//        lda.printSummary();
//        Frame x = lda.fit(df.removeVars(target), (rv,rm) -> 1);

        BinarySMO smo1 = new BinarySMO()
//                .withInputFilters(new FFStandardize())
                .withKernel(new PolyKernel(3))
                .withC(10)
                .withTol(1e-20)
                .withFirstClassIndex(1)
                .withSecondClassIndex(2);


//        WS.setPrinter(new IdeaPrinter());

//        int from = 0;
//        int to = 4;
//        GridLayer p = new GridLayer(to - from, to - from);
//        for (int i = from; i < to; i++) {
//            for (int j = from; j < to; j++) {
//                p.add(i + 1, j + 1, points(df.var(i), df.var(j), color(df.var(target)), sz(3)));
//            }
//        }
//        WS.draw(p);

//        smo1.train(df, target);
//        smo1.printSummary();

//        smo1.fit(df).printSummary();

            CEvaluation.cv(df, target, smo1, 20);

    }
}
