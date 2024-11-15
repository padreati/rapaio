/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.experiment.math.nn;

import static rapaio.graphics.Plotter.lines;
import static rapaio.graphics.opt.GOpts.color;
import static rapaio.graphics.opt.GOpts.lwd;

import java.util.Arrays;
import java.util.Random;

import rapaio.core.SamplingTools;
import rapaio.data.VarDouble;
import rapaio.math.nn.Autograd;
import rapaio.math.nn.Loss;
import rapaio.math.nn.Net;
import rapaio.math.nn.Tensor;
import rapaio.math.nn.Optimizer;
import rapaio.math.nn.layer.BatchNorm1D;
import rapaio.math.nn.layer.Linear;
import rapaio.math.nn.layer.ReLU;
import rapaio.math.nn.layer.Sequential;
import rapaio.math.nn.loss.MSELoss;
import rapaio.math.narrays.DType;
import rapaio.math.narrays.NArrayManager;
import rapaio.math.narrays.NArrays;
import rapaio.math.narrays.Shape;
import rapaio.math.narrays.NArray;
import rapaio.sys.WS;

public class Sandbox2DFunctionRegression {

    private static final NArrayManager.OfType<?> tmd = NArrayManager.base().ofDouble();

    public static void main(String[] args) {
        DType<?> dtype = DType.FLOAT;
        Random random = new Random(42);

        NArray<?> xtrain = NArrays.ofType(dtype).random(Shape.of(10_000, 4), random);
        NArray<?> ytrain = NArrays.ofType(dtype).zeros(Shape.of(10_000));
        for (int i = 0; i < ytrain.dim(0); i++) {
            NArray<?> row = xtrain.takesq(0, i);
            ytrain.setDouble(random.nextDouble() / 100 + fun(row.getDouble(0), row.getDouble(1), row.getDouble(2), row.getDouble(3)), i);
        }
        NArray<?> xtest = NArrays.ofType(dtype).random(Shape.of(200, 4), random);
        NArray<?> ytest = NArrays.ofType(dtype).zeros(Shape.of(200));
        for (int i = 0; i < ytest.dim(0); i++) {
            NArray<?> row = xtest.takesq(0, i);
            ytest.setDouble(random.nextDouble() / 100 + fun(row.getDouble(0), row.getDouble(1), row.getDouble(2), row.getDouble(3)), i);
        }

        Net nn = new Sequential(
                new Linear(dtype, 4, 100, true),
//                new Sigmoid(),
                new ReLU(),
                new BatchNorm1D(dtype, 100, 1e-5),
                new Linear(dtype, 100, 1, true),
                new ReLU()
        );
        nn.seed(42);


        int EPOCHS = 1_000;
        int BATCH_SIZE = 100;
        double LR = 1e-1;

        Optimizer c = Optimizer.Adam(nn.parameters())
                .lr.set(LR)
//                .weightDecay.set(0.1)
                .amsgrad.set(true);
        Loss loss = new MSELoss();

        VarDouble trainLoss = VarDouble.empty().name("trainLoss");
        VarDouble testLoss = VarDouble.empty().name("testLoss");

        for (int i = 0; i < EPOCHS; i++) {
            int[] sample = SamplingTools.sampleWOR(random, 10_000, BATCH_SIZE);

            double trLoss = 0;
            double teLoss = 0;

            nn.train();
            c.zeroGrad();

            for (int j = 0; j < sample.length; j += BATCH_SIZE) {
                int[] batchIndexes = Arrays.copyOfRange(sample, j, Math.min(sample.length, j + BATCH_SIZE));
                NArray<?> xx = xtrain.take(0, batchIndexes);
                NArray<?> yy = ytrain.take(0, batchIndexes);
                Tensor[] outputs = nn.forward(Autograd.var(xx));

                loss.forward(outputs[0], Autograd.var(yy));
                loss.backward();

                trainLoss.addDouble(loss.loss());
                trLoss += loss.loss() * batchIndexes.length / sample.length;

                c.step();
            }

            nn.eval();
            Tensor[] outputs = nn.forward(Autograd.var(xtest));
            loss.forward(outputs[0], Autograd.var(ytest));
            testLoss.addDouble(loss.loss());
            teLoss += loss.loss();

            System.out.printf("Epoch: %d, Train loss: %.6f, test loss: %.6f\n", i + 1, trLoss, teLoss);


        }

        WS.draw(lines(trainLoss, color(1), lwd(1)).lines(testLoss, color(2), lwd(1)));

        nn.forward(Autograd.var(NArrays.ofType(dtype).random(Shape.of(2, 4), random)).name("x"))[0].value().printString();
    }

    public static double fun(double x1, double x2, double x3, double x4) {
        return Math.sin(x1) * Math.cos(x2) + x3 + x4 * x4 + Math.sqrt(Math.abs(x1 + x3));
    }

}
