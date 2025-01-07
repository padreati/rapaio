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
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Iterator;

import rapaio.darray.Shape;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.datasets.Batch;
import rapaio.datasets.MNISTDatasets;
import rapaio.datasets.TabularDataset;
import rapaio.ml.eval.metric.Confusion;
import rapaio.nn.Autograd;
import rapaio.nn.Loss;
import rapaio.nn.Optimizer;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;
import rapaio.nn.layer.LayerNorm;
import rapaio.nn.layer.Linear;
import rapaio.nn.layer.LogSoftmax;
import rapaio.nn.layer.Sequential;
import rapaio.nn.layer.Tanh;
import rapaio.nn.loss.NegativeLikelihoodLoss;
import rapaio.printer.Format;

public class MNIST {

    public static void main(String[] args) throws IOException {
        TensorManager tm = TensorManager.ofFloat();
        tm.seed(42);

        MNISTDatasets mnist = new MNISTDatasets(tm);
        TabularDataset train1 = mnist.train();
        TabularDataset test1 = mnist.test();
        TabularDataset train = new TabularDataset(tm,
                train1.darray(0).reshape(Shape.of(train1.darray(0).dim(0), 28 * 28)), train1.darray(1));
        TabularDataset test = new TabularDataset(tm,
                test1.darray(0).reshape(Shape.of(test1.darray(0).dim(0), 28 * 28)), test1.darray(1));

        int epochs = 10 ;
        double lr = 1e-3;
        int batchSize = 100;
        int h = 512;

        var nn = new Sequential(tm,
                new LayerNorm(tm, Shape.of(784)),
                new Linear(tm, 784, h, true),
                new Tanh(tm),
                new LayerNorm(tm, Shape.of(h)),
                new Linear(tm, h, 10, true),
                new LogSoftmax(tm, 0)
        );

        var optimizer = Optimizer.Adam(tm, nn.parameters())
                .lr.set(lr);

        var trainLoss = VarDouble.empty().name("trainLoss");
        var testLoss = VarDouble.empty().name("trainLoss");
        var accuracy = VarDouble.empty().name("accuracy");

        var loss = new NegativeLikelihoodLoss(tm);
        long time = 0;

        for (int epoch = 0; epoch < epochs; epoch++) {
            long start = System.currentTimeMillis();

            double trainLossValue = 0;
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

            nn.eval();
            var pred = nn.forward11(tm.var(test.darray(0)));
            var lossOut = loss.forward(pred, tm.var(test.darray(1)));
            double testLossValue = lossOut.lossValue();
            trainLoss.addDouble(trainLossValue);
            testLoss.addDouble(testLossValue);

            var y_pred = nn.forward11(test.tensor(0)).value().exp().argmax1d(1, false);
            var levels = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");

            var cm = Confusion.from(VarNominal.from(levels, test.darray(1)), VarNominal.from(levels, y_pred));
            accuracy.addDouble(cm.accuracy());

            if (epoch % 1 == 0) {

                System.out.println("Epoch: " + epoch + ", train loss:" + trainLossValue + ", test loss:" + testLossValue);
                System.out.println("\t error: " + Format.floatShort(cm.error()) + ", accuracy: " + Format.floatShort(cm.accuracy()));
                cm.frequencyMatrix().printContent();
            }

            long end = System.currentTimeMillis();
            System.out.printf("Epoch time: %s ms\n", Duration.of(end - start, ChronoUnit.MILLIS));
            time += end - start;
        }

//        WS.draw(Plotter
//                .lines(trainLoss, color(1))
//                .lines(testLoss, color(2))
//                .lines(accuracy, color(3))
//        );

        System.out.println("Duration: " + Duration.of(time, ChronoUnit.MILLIS));
        tm.close();

    }
}
