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
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Nominal;
import rapaio.data.Numeric;
import rapaio.data.SolidFrame;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
        df.applyFilters(new FFStandardize("all")).printSummary();

        String target = "Class";

        BinarySMO smo1 = new BinarySMO()
                .withInputFilters(new FFStandardize())
                .withKernel(new PolyKernel(1))
                .withC(0.1);

        RandomSource.setSeed(1);
        double score = CEvaluation.cv(df, target, smo1, 10);
        assertEquals(0.759762, score, 1e-7);
    }

    @Test
    public void testMultipleKernels() throws IOException {

        Frame df = Datasets.loadSonar();

        List<Kernel> kernels = new ArrayList<>();
        kernels.add(new PolyKernel(1));
        kernels.add(new PolyKernel(2));
        kernels.add(new PolyKernel(3));
        kernels.add(new RBFKernel(1));
        kernels.add(new LogKernel(1));
        kernels.add(new SplineKernel());
        kernels.add(new MinKernel());
        kernels.add(new ChiSquareKernel());
        kernels.add(new CauchyKernel(1));
        kernels.add(new WaveKernel(1));
        kernels.add(new WaveletKernel(1));
        kernels.add(new ExponentialKernel());
        kernels.add(new GeneralizedMinKernel(1, 1));
        kernels.add(new GeneralizedStudentTKernel(1));
        kernels.add(new InverseMultiQuadraticKernel(1));
        kernels.add(new SphericalKernel(1));
        kernels.add(new SigmoidKernel(1, 1));
        kernels.add(new MultiQuadricKernel(1));
        kernels.add(new PowerKernel(2));
        kernels.add(new RationalQuadraticKernel(1));

        Nominal name = Nominal.empty().withName("kernel");
        Numeric score = Numeric.empty().withName("score");

        for (Kernel k : kernels) {

            RandomSource.setSeed(1);

            BinarySMO smo = new BinarySMO();
            smo.withInputFilters(new FFStandardize("all"));
            double s = CEvaluation.cv(df, "Class", smo, 10);

            name.addLabel(k.name());
            score.addValue(s);
        }

        WS.println("\nSummary of the scores for various kernels:\n=====================\n");
        String out = SolidFrame.wrapOf(name, score).lines(name.rowCount());

        assertEquals("" +
                "                  kernel                     score                                 kernel                             score      \n" +
                        " [0] PolyKernel(exp=1,bias=1,slope=1) 0.7066666666666667 [10] Wavelet(invariant=true,dilation=1,translation=0) 0.7307142857142856\n" +
                        " [1] PolyKernel(exp=2,bias=1,slope=1) 0.7261904761904762 [11]            Exponential(sigma=7,factor=0.0102041) 0.7254761904761905\n" +
                        " [2] PolyKernel(exp=3,bias=1,slope=1) 0.6923809523809524 [12]                  GeneralizedMean(alpha=1,beta=1) 0.7114285714285714\n" +
                        " [3]                     RBF(sigma=1) 0.7207142857142858 [13]                     GeneralizedStudent(degree=1) 0.7214285714285714\n" +
                        " [4]                    Log(degree=1) 0.7259523809523809 [14]                       InverseMultiQuadratic(c=1) 0.7209523809523809\n" +
                        " [5]                           Spline 0.6969047619047619 [15]                               Spherical(sigma=1) 0.7259523809523809\n" +
                        " [6]                              Min 0.7161904761904762 [16]                             Sigmoid(alpha=1,c=1) 0.7261904761904762\n" +
                        " [7]                        ChiSquare 0.7264285714285714 [17]                              MultiQuadratic(c=1) 0.7454761904761905\n" +
                        " [8]                        Cauchy(1) 0.7166666666666667 [18]                                  Power(degree=2) 0.7166666666666667\n" +
                        " [9]                    Wave(theta=1) 0.7161904761904762 [19]                           RationalQuadratic(c=1) 0.7259523809523809\n",
                out);
    }
}
