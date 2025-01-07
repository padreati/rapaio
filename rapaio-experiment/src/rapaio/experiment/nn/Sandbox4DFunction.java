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

import rapaio.darray.DArray;
import rapaio.darray.Shape;
import rapaio.data.VarDouble;
import rapaio.nn.Autograd;
import rapaio.nn.Loss;
import rapaio.nn.Network;
import rapaio.nn.Optimizer;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;
import rapaio.datasets.TabularDataset;
import rapaio.nn.layer.AbstractNetwork;
import rapaio.nn.layer.BatchNorm1D;
import rapaio.nn.layer.Linear;
import rapaio.nn.layer.ReLU;
import rapaio.nn.loss.MSELoss;
import rapaio.sys.WS;

public class Sandbox4DFunction {

    static class Fun4dNetwork extends AbstractNetwork {

        final BatchNorm1D norm1;
        final Linear linear1;
        final ReLU relu1;
        final BatchNorm1D norm2;
        final Linear linear2;

        public Fun4dNetwork(TensorManager tm) {
            super(tm);
            norm1 = new BatchNorm1D(tm, 4);
            linear1 = new Linear(tm, 4, 1_000, true);
            relu1 = new ReLU(tm);
            norm2 = new BatchNorm1D(tm, 1_000);
            linear2 = new Linear(tm, 1_000, 1, true);
        }

        @Override
        public Tensor forward11(Tensor x) {
            x = norm1.forward11(x);
            x = linear1.forward11(x);
            x = relu1.forward11(x);
            x = norm2.forward11(x);
            x = linear2.forward11(x);
            return x;
        }
    }

    public static double fun4d(double x1, double x2, double x3, double x4) {
        return Math.sin(x1) * Math.cos(x2) + x3 + x4 * x4 + Math.sqrt(Math.abs(x1 + x3));
    }

    public static void main() {
        TensorManager tm = TensorManager.ofFloat();
        tm.seed(42);


        final int N = 1_000;
        Tensor x = tm.randomTensor(Shape.of(N, 4));
        Tensor y = tm.zerosTensor(Shape.of(N));
        for (int i = 0; i < N; i++) {
            DArray<?> row = x.value().selsq(0, i);
            y.value().setDouble(tm.random().nextDouble() / 100 + fun4d(row.getDouble(0), row.getDouble(1), row.getDouble(2), row.getDouble(3)), i);
        }


        TabularDataset[] split = new TabularDataset(tm, x, y).trainTestSplit(0.2);
        TabularDataset train = split[0];
        TabularDataset test = split[1];

        Network nn = new Fun4dNetwork(tm);

        int EPOCHS = 400;
        int BATCH_SIZE = 100;

        Optimizer optimizer = Optimizer.Adam(tm, nn.parameters()).lr.set(1e-3);

        VarDouble trainLoss = VarDouble.empty().name("trainLoss");
        VarDouble testLoss = VarDouble.empty().name("testLoss");

        Loss loss = new MSELoss(tm);

        for (int epoch = 1; epoch <= EPOCHS; epoch++) {

            nn.train();
            optimizer.zeroGrad();

            var batchOut = nn.batchForward(BATCH_SIZE, true, false, train.tensor(0));
            var lossOut = loss.batchForward(batchOut, train.tensor(1));

            double trainLossValue = lossOut.lossValue();
            trainLoss.addDouble(trainLossValue);

            Autograd.backward(lossOut.tensor());
            optimizer.step();

            nn.eval();
            Tensor outputs = nn.forward11(tm.var(test.darray(0)));
            lossOut = loss.forward(outputs, tm.var(test.darray(1)));
            double teLoss = lossOut.lossValue();
            testLoss.addDouble(teLoss);

            if (epoch == 1 || epoch % 25 == 0) {
                System.out.printf("Epoch: %d, Train loss: %.6f, test loss: %.6f\n", epoch, trainLossValue, teLoss);
            }
        }
        WS.draw(lines(trainLoss, color(1), lwd(1)).lines(testLoss, color(2), lwd(1)));

        tm.close();
    }

}
