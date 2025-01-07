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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import rapaio.darray.DType;
import rapaio.darray.Shape;
import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.data.VarRange;
import rapaio.datasets.Batch;
import rapaio.datasets.Datasets;
import rapaio.datasets.TabularDataset;
import rapaio.graphics.Plotter;
import rapaio.graphics.opt.GOpts;
import rapaio.ml.eval.metric.Confusion;
import rapaio.nn.Autograd;
import rapaio.nn.Loss;
import rapaio.nn.Optimizer;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;
import rapaio.nn.layer.AbstractNetwork;
import rapaio.nn.layer.ELU;
import rapaio.nn.layer.LayerNorm;
import rapaio.nn.layer.Linear;
import rapaio.nn.layer.LogSoftmax;
import rapaio.nn.loss.NegativeLikelihoodLoss;
import rapaio.printer.Format;
import rapaio.sys.WS;

public class SandboxIrisSplit {

    static class IrisNet extends AbstractNetwork {

        final LayerNorm norm1;
        final Linear linear1;
        final ELU elu1;
        final Linear linear2;
        final ELU elu2;
        final LogSoftmax ls;

        public IrisNet(TensorManager tm) {
            super(tm);

            this.norm1 = new LayerNorm(tm, Shape.of(4));
            this.linear1 = new Linear(tm, 12, 6, true);
            this.elu1 = new ELU(tm);
            this.linear2 = new Linear(tm, 6, 3, true);
            this.elu2 = new ELU(tm);
            this.ls = new LogSoftmax(tm, 0);
        }

        @Override
        public Tensor forward11(Tensor x) {
            x = norm1.forward11(x);
            x = tm.cat(1, x, x.sqr(), x.sqr().add(1).log());
            x = linear1.forward11(x);
            x = elu1.forward11(x);
            x = linear2.forward11(x);
            x = elu2.forward11(x);
            x = ls.forward11(x);
            return x;
        }
    }

    public static void main() throws IOException {

        TensorManager tm = TensorManager.ofFloat();
//        tm.seed(-671352);

        Frame iris = Datasets.loadIrisDataset();
        var x = iris.mapVars(VarRange.of("0~3")).darray().cast(DType.FLOAT);
        var y = iris.rvar("class").darray().cast(DType.FLOAT);

        TabularDataset irisDataset = new TabularDataset(tm, x, y);
        TabularDataset[] split = irisDataset.trainTestSplit(0.15);
        TabularDataset train = split[0];
        TabularDataset test = split[1];

        int epochs = 5_000;
        double lr = 1e-5;
        int batchSize = 5;

        boolean separateBatches = true;

        var nn = new IrisNet(tm);

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
                    Tensor pred = nn.forward11(batch.tensor(0).name("x-batch"));
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

            if (epoch % 100 == 0) {

                System.out.println("Epoch: " + epoch + ", train loss:" + trainLossValue + ", test loss:" + testLossValue);
                System.out.println("\t error: " + Format.floatShort(cm.error()) + ", accuracy: " + Format.floatShort(cm.accuracy()));
                cm.frequencyMatrix().printContent();
            }
        }

        WS.draw(Plotter
                .lines(trainLoss, GOpts.color(1))
                .lines(testLoss, GOpts.color(2))
                .lines(accuracy, GOpts.color(3))
        );
        tm.close();
    }
}
