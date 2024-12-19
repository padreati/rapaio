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

import static rapaio.graphics.opt.GOpts.color;

import java.io.IOException;

import rapaio.darray.DType;
import rapaio.darray.Shape;
import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.data.VarRange;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plotter;
import rapaio.ml.eval.metric.Confusion;
import rapaio.nn.Autograd;
import rapaio.nn.Loss;
import rapaio.nn.Net;
import rapaio.nn.Optimizer;
import rapaio.nn.TensorManager;
import rapaio.nn.data.ArrayDataset;
import rapaio.nn.layer.ELU;
import rapaio.nn.layer.LayerNorm;
import rapaio.nn.layer.Linear;
import rapaio.nn.layer.LogSoftmax;
import rapaio.nn.layer.Sequential;
import rapaio.nn.loss.NegativeLikelihoodLoss;
import rapaio.printer.Format;
import rapaio.sys.WS;

public class SandboxIris {
    public static void main() throws IOException {

        Frame iris = Datasets.loadIrisDataset();
        var x = iris.mapVars(VarRange.of("0~3")).darray().cast(DType.FLOAT);
        var y = iris.rvar("class").darray().cast(DType.FLOAT);

        ArrayDataset irisDataset = new ArrayDataset(x, y);
        ArrayDataset[] split = irisDataset.trainTestSplit(0.15);
        ArrayDataset train = split[0];
        ArrayDataset test = split[1];

        TensorManager tm = TensorManager.ofFloat();
        tm.seed(42);

        int n = 3;
        int epochs = 1_000;
        double lr = 1e-3;
        int batchSize = 30;

        Sequential nn = new Sequential(tm,
//                new BatchNorm1D(tm, 4),
                new LayerNorm(tm, Shape.of(4)),
                new Linear(tm, 4, n, true),
                new ELU(tm),
//                new BatchNorm1D(tm, n),
                new LayerNorm(tm, Shape.of(n)),
                new Linear(tm, n, 3, true),
                new ELU(tm),
                new LogSoftmax(tm, 1)
        );

        Optimizer optimizer = Optimizer.Adam(tm, nn.parameters())
                .lr.set(lr);

        VarDouble trainLoss = VarDouble.empty().name("trainLoss");
        VarDouble testLoss = VarDouble.empty().name("trainLoss");
        VarDouble accuracy = VarDouble.empty().name("accuracy");

        Loss loss = new NegativeLikelihoodLoss();

        for (int epoch = 0; epoch < epochs; epoch++) {

            optimizer.zeroGrad();
            nn.train();

            Net.BatchOutput batchOut = nn.batchForward(batchSize, tm.var(train.array(0)));
            Net.BatchLoss batchLoss = batchOut.applyLoss(loss, tm.var(train.array(1)));

            double trainLossValue = batchLoss.lossValue();
            Autograd.backward(batchLoss.tensor());

            optimizer.step();

            nn.eval();
            loss.forward(nn.forward11(tm.var(test.array(0))), tm.var(test.array(1)));
            double testLossValue = loss.loss();

            trainLoss.addDouble(trainLossValue);
            testLoss.addDouble(testLossValue);

            var y_pred = nn.forward11(tm.var(x)).value().exp().argmax1d(1, false);
            var levels = iris.rvar("class").levels();

            var cm = Confusion.from(iris.rvar("class"), VarNominal.from(levels, y_pred));
            accuracy.addDouble(cm.accuracy());

            if (epoch % 10 == 0) {

                System.out.println("Epoch: " + epoch + ", train loss:" + trainLossValue + ", test loss:" + testLossValue);
                System.out.println("\t error: " + Format.floatShort(cm.error()) + ", accuracy: " + Format.floatShort(cm.accuracy()));
                cm.frequencyMatrix().printContent();
            }
        }

        WS.draw(Plotter
                .lines(trainLoss, color(1))
                .lines(testLoss, color(2))
                .lines(accuracy, color(3))
        );

        tm.close();
    }
}
