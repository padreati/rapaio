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

package rapaio.ml.classifier.svm;

import org.junit.Before;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.data.*;
import rapaio.data.filter.frame.FFStandardize;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.svm.kernel.*;
import rapaio.experiment.ml.eval.CEvaluation;
import rapaio.sys.WS;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for binary smo
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/20/16.
 */
public class BinarySMOTest {

    @Before
    public void setUp() throws Exception {
        RandomSource.setSeed(123);
    }

    @Test
    public void testDescription() {
        BinarySMO smo = new BinarySMO().withMaxRuns(200);
        assertEquals("BinarySMO\n" +
                        "{\n" +
                        "   sampler=Identity,\n" +
                        "   kernel=PolyKernel(exp=1,bias=1,slope=1),\n" +
                        "   C=1,\n" +
                        "   tol=0.001,\n" +
                        "   classIndex1=1,\n" +
                        "   classIndex2=2,\n" +
                        "   oneVsAll=false,\n" +
                        "   maxRuns=200\n" +
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
                        "   maxRuns=200\n" +
                        "}\n",
                new BinarySMO()
                        .withTol(0.3)
                        .withC(17)
                        .withFirstClassIndex(4)
                        .withSecondClassIndex(7)
                        .withKernel(new LogKernel(1))
                        .withOneVsAll(true)
                        .withMaxRuns(200)
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
                        "   maxRuns=200\n" +
                        "}\n",
                new BinarySMO()
                        .withTol(0.3)
                        .withC(17)
                        .withFirstClassIndex(4)
                        .withSecondClassIndex(7)
                        .withKernel(new LogKernel(1))
                        .withOneVsAll(true)
                        .withMaxRuns(200)
                        .newInstance()
                        .fullName());
    }

    @Test
    public void testLinear() throws IOException, URISyntaxException {

        Frame df = Datasets.loadSonar();
        df.solidCopy().fitApply(new FFStandardize(VRange.all())).printSummary();

        String target = "Class";

        BinarySMO smo1 = new BinarySMO()
                .withInputFilters(new FFStandardize(VRange.all()))
                .withKernel(new PolyKernel(1))
                .withC(0.1);

        RandomSource.setSeed(1);
        double score = CEvaluation.cv(df, target, smo1, 10);
        assertEquals(0.75, score, 1e-7);
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

            BinarySMO smo = new BinarySMO().withMaxRuns(200);
            smo.withInputFilters(new FFStandardize(VRange.all()));
            double s = CEvaluation.cv(df, "Class", smo, 3);

            name.addLabel(k.name());
            score.addValue(s);
        }

        WS.println("\nSummary of the scores for various kernels:\n=====================\n");
        String out = SolidFrame.byVars(name, score).lines(name.rowCount());

        assertEquals("                  kernel                     score                                 kernel                             score      \n" +
                        " [0] PolyKernel(exp=1,bias=1,slope=1) 0.7356797791580401 [10] Wavelet(invariant=true,dilation=1,translation=0)  0.754727398205659\n" +
                        " [1] PolyKernel(exp=2,bias=1,slope=1) 0.7356107660455486 [11]            Exponential(sigma=7,factor=0.0102041) 0.7691511387163561\n" +
                        " [2] PolyKernel(exp=3,bias=1,slope=1) 0.7546583850931677 [12]                  GeneralizedMean(alpha=1,beta=1) 0.7498274672187716\n" +
                        " [3]                     RBF(sigma=1) 0.7498274672187716 [13]                     GeneralizedStudent(degree=1)  0.749896480331263\n" +
                        " [4]                    Log(degree=1) 0.7546583850931677 [14]                       InverseMultiQuadratic(c=1)  0.749896480331263\n" +
                        " [5]                           Spline 0.7355417529330572 [15]                               Spherical(sigma=1) 0.7643202208419599\n" +
                        " [6]                              Min 0.7356797791580401 [16]                             Sigmoid(alpha=1,c=1) 0.7643202208419599\n" +
                        " [7]                        ChiSquare 0.7643202208419599 [17]                              MultiQuadratic(c=1) 0.7259489302967563\n" +
                        " [8]                        Cauchy(1) 0.7546583850931677 [18]                                  Power(degree=2)  0.749896480331263\n" +
                        " [9]                    Wave(theta=1) 0.7452035886818495 [19]                           RationalQuadratic(c=1) 0.7546583850931677\n",
                out);
    }
}
