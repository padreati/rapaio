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

package rapaio.experiment.nn;

import static rapaio.graphics.Plotter.lines;
import static rapaio.graphics.opt.GOpts.color;
import static rapaio.graphics.opt.GOpts.lwd;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Random;

import rapaio.darray.DArray;
import rapaio.darray.Shape;
import rapaio.data.VarDouble;
import rapaio.nn.Autograd;
import rapaio.nn.Loss;
import rapaio.nn.Network;
import rapaio.nn.Optimizer;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;
import rapaio.nn.data.TabularDataset;
import rapaio.nn.layer.LayerNorm;
import rapaio.nn.layer.Linear;
import rapaio.nn.layer.ReLU;
import rapaio.nn.layer.Sequential;
import rapaio.nn.loss.MSELoss;
import rapaio.sys.WS;

public class Sandbox2DFunctionRegression {

    public static void main() {
        TensorManager tm = TensorManager.ofFloat();
        tm.seed(42);

        Random random = new Random(42);

        final int N = 1_000;
        Tensor x = tm.randomTensor(Shape.of(N, 4), random);
        Tensor y = tm.zerosTensor(Shape.of(N));
        for (int i = 0; i < N; i++) {
            DArray<?> row = x.value().selsq(0, i);
            y.value().setDouble(random.nextDouble() / 100 + fun(row.getDouble(0), row.getDouble(1), row.getDouble(2), row.getDouble(3)), i);
        }


        TabularDataset[] split = new TabularDataset(tm, x, y).trainTestSplit(0.2);
        TabularDataset train = split[0];
        TabularDataset test = split[1];

        Network nn = new Sequential(tm,
//                new BatchNorm1D(tm, 4),
                new LayerNorm(tm, Shape.of(4)),
                new Linear(tm, 4, 1_000, true),
                new ReLU(tm),
//                new BatchNorm1D(tm, 1_000),
                new LayerNorm(tm, Shape.of(1_000)),
                new Linear(tm, 1_000, 1, true),
                new ReLU(tm)
        );


        int EPOCHS = 40;
        int BATCH_SIZE = 100;

        Optimizer c = Optimizer.Adam(tm, nn.parameters())
                .lr.set(4e-4)
                .weightDecay.set(0.1)
                .amsgrad.set(true);

        VarDouble trainLoss = VarDouble.empty().name("trainLoss");
        VarDouble testLoss = VarDouble.empty().name("testLoss");

        Loss loss = new MSELoss(tm);

        long start = System.currentTimeMillis();
        for (int epoch = 1; epoch <= EPOCHS; epoch++) {

            nn.train();
            c.zeroGrad();

            var batchOut = nn.batchForward(BATCH_SIZE, true, false, train.tensor(0));
            var lossOut = loss.batchForward(batchOut, train.tensor(1));

            double trainLossValue = lossOut.lossValue();
            trainLoss.addDouble(trainLossValue);

            Autograd.backward(lossOut.tensor()).covered();
            c.step();

            nn.eval();
            Tensor outputs = nn.forward11(tm.var(test.darray(0)));
            lossOut = loss.forward(outputs, tm.var(test.darray(1)));
            double teLoss = lossOut.lossValue();
            testLoss.addDouble(teLoss);

            if (epoch == 1 || epoch % 1 == 0) {
                System.out.printf("Epoch: %d, Train loss: %.6f, test loss: %.6f\n", epoch, trainLossValue, teLoss);
            }
        }
        long end = System.currentTimeMillis();
        System.out.println(Duration.of(end - start, ChronoUnit.MILLIS));


        var input = tm.randomTensor(Shape.of(2, 4), random).name("x");
        System.out.println("input:" + input.value());
        System.out.println("output:" + nn.forward11(input).value());
        System.out.println("true:" +
                fun(input.value().getDouble(0, 0), input.value().getDouble(0, 1),
                        input.value().getDouble(0, 2), input.value().getDouble(0, 3)) +
                ", " +
                fun(input.value().getDouble(1, 0), input.value().getDouble(1, 1),
                        input.value().getDouble(1, 2), input.value().getDouble(1, 3))
        );

        WS.draw(lines(trainLoss, color(1), lwd(1)).lines(testLoss, color(2), lwd(1)));

        tm.close();
    }

    public static double fun(double x1, double x2, double x3, double x4) {
        return Math.sin(x1) * Math.cos(x2) + x3 + x4 * x4 + Math.sqrt(Math.abs(x1 + x3));
    }

}
