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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import rapaio.darray.DArray;
import rapaio.darray.DType;
import rapaio.darray.Shape;
import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.data.VarRange;
import rapaio.datasets.Datasets;
import rapaio.ml.eval.metric.Confusion;
import rapaio.nn.Autograd;
import rapaio.nn.Loss;
import rapaio.nn.Network;
import rapaio.nn.Optimizer;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;
import rapaio.nn.data.Batch;
import rapaio.nn.data.TabularDataset;
import rapaio.nn.layer.ELU;
import rapaio.nn.layer.LayerNorm;
import rapaio.nn.layer.Linear;
import rapaio.nn.layer.LogSoftmax;
import rapaio.nn.layer.Sequential;
import rapaio.nn.loss.NegativeLikelihoodLoss;
import rapaio.printer.Format;

public class SandboxIris {
    public static void main() throws IOException {

        TensorManager tm = TensorManager.ofFloat();
        tm.seed(42);

        Frame iris = Datasets.loadIrisDataset();
        var x = iris.mapVars(VarRange.of("0~3")).darray().cast(DType.FLOAT);
        var y = iris.rvar("class").darray().cast(DType.FLOAT);

        TabularDataset irisDataset = new TabularDataset(tm, x, y);
        TabularDataset[] split = irisDataset.trainTestSplit(0.2);
        TabularDataset train = split[0];
        TabularDataset test = split[1];

        int n = 3;
        int epochs = 1_000;
        double lr = 1e-3;
        int batchSize = 30;

        boolean separateBatches = true;

        var nn = new Sequential(tm,
                new LayerNorm(tm, Shape.of(4)),
                new Linear(tm, 4, n, true),
                new ELU(tm),
                new LayerNorm(tm, Shape.of(n)),
                new Linear(tm, n, 3, true),
                new ELU(tm),
                new LogSoftmax(tm, 1)
        );

        var optimizer = Optimizer.Adam(tm, nn.parameters())
                .lr.set(lr);

        var trainLoss = VarDouble.empty().name("trainLoss");
        var testLoss = VarDouble.empty().name("trainLoss");
        var accuracy = VarDouble.empty().name("accuracy");

        var loss = new NegativeLikelihoodLoss(tm);

        for (int epoch = 0; epoch < epochs; epoch++) {

            double trainLossValue = 0;
            if (separateBatches) {
                Iterator<Batch> batchIterator = train.batchIterator(batchSize, true, false);
                double batchCount = 0;
                while (batchIterator.hasNext()) {
                    optimizer.zeroGrad();
                    nn.train();
                    Batch batch = batchIterator.next();
                    Tensor pred = nn.forward11(batch.tensor(0));
                    Loss.Output lossOut = loss.forward(pred, batch.tensor(1));

                    trainLossValue += lossOut.lossValue();
                    Autograd.backward(lossOut.tensor());
                    optimizer.step();
                    batchCount++;
                }
                trainLossValue /= batchCount;
            } else {

                optimizer.zeroGrad();
                nn.train();

                List<Batch> batches = nn.batchForward(batchSize, tm.var(train.darray(0)));
                Loss.Output batchLoss = loss.batchForward(batches, tm.var(train.darray(1)));

                trainLossValue = batchLoss.lossValue();
                Autograd.backward(batchLoss.tensor());

                optimizer.step();
            }

            nn.eval();
            var lossOut = loss.forward(nn.forward11(tm.var(test.darray(0))), tm.var(test.darray(1)));
            double testLossValue = lossOut.lossValue();
            trainLoss.addDouble(trainLossValue);
            testLoss.addDouble(testLossValue);

            var y_pred = nn.forward11(test.tensor(0)).value().exp().argmax1d(1, false);
            var levels = iris.rvar("class").levels();

            var cm = Confusion.from(VarNominal.from(levels, test.darray(1)), VarNominal.from(levels, y_pred));
            accuracy.addDouble(cm.accuracy());

            if (epoch % 10 == 0) {

                System.out.println("Epoch: " + epoch + ", train loss:" + trainLossValue + ", test loss:" + testLossValue);
                System.out.println("\t error: " + Format.floatShort(cm.error()) + ", accuracy: " + Format.floatShort(cm.accuracy()));
                cm.frequencyMatrix().printContent();
            }
        }

//        WS.draw(Plotter
//                .lines(trainLoss, color(1))
//                .lines(testLoss, color(2))
//                .lines(accuracy, color(3))
//        );

        final String path = "/home/ati/work/rapaio/rapaio-experiment/src/rapaio/experiment/nn/net.rbin";
        nn.saveState(new File(path));

        Network nn2 = new Sequential(tm,
                new LayerNorm(tm, Shape.of(4)),
                new Linear(tm, 4, n, true),
                new ELU(tm),
                new LayerNorm(tm, Shape.of(n)),
                new Linear(tm, n, 3, true),
                new ELU(tm),
                new LogSoftmax(tm, 1)
        );

        nn2.loadState(new File(path));

        DArray<?> pred1 = nn.forward11(test.tensor(0)).value().sel(1, 0).t();
        DArray<?> pred2 = nn2.forward11(test.tensor(0)).value().sel(1, 0).t();

        System.out.println(pred1.sub(pred2).abs_().sum());


        tm.close();
    }
}
