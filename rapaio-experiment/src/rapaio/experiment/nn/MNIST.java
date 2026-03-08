/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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
import rapaio.nn.Network;
import rapaio.nn.Optimizer;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;
import rapaio.nn.layer.AbstractNetwork;
import rapaio.nn.layer.Conv2D;
import rapaio.nn.layer.Dropout;
import rapaio.nn.layer.Flatten;
import rapaio.nn.layer.LayerNorm;
import rapaio.nn.layer.Linear;
import rapaio.nn.layer.LogSoftmax;
import rapaio.nn.layer.Sequential;
import rapaio.nn.layer.Sigmoid;
import rapaio.nn.loss.NegativeLikelihoodLoss;
import rapaio.printer.Format;

public class MNIST {

    static class ConvNetwork extends AbstractNetwork {

        private LayerNorm norm1;
        private Conv2D conv2d1;
        private Dropout dropout1;
        private Conv2D conv2d2;
        private LayerNorm norm2;
        private Linear linear1;
        private Linear linear2;
        private LogSoftmax softmax;

        private static final int linSize = 32 * 3 * 3;

        public ConvNetwork(TensorManager tm) {
            super(tm);

            this.norm1 = new LayerNorm(tm, Shape.of(28, 28));
            this.conv2d1 = new Conv2D(tm, 1, 16, 4, 4, 4, 0, 1, 1, true);
            this.dropout1 = new Dropout(tm, 0.25);
            this.conv2d2 = new Conv2D(tm, 16, 32, 3, 3, 2, 0, 1, 2, true);
            this.norm2 = new LayerNorm(tm, Shape.of(32, 3, 3));
            this.linear1 = new Linear(tm, linSize, 32, true);
            this.linear2 = new Linear(tm, 32, 10, true);
            this.softmax = new LogSoftmax(tm, 0);
        }

        @Override
        public Tensor forward11(Tensor x) {

            x = norm1.forward11(x);
            x = conv2d1.forward11(x.stretch(1));
            x = dropout1.forward11(x);
            x = conv2d2.forward11(x);
            x = norm2.forward11(x);
            x = x.reshape(Shape.of(x.dim(0), linSize));
            x = linear1.forward11(x);
            x = linear2.forward11(x);
            x = softmax.forward11(x);
            return x;
        }
    }

    public static Network createLeNet1(TensorManager tm) {
        return new Sequential(tm,
                new Flatten(tm),
                new Linear(tm, 28 * 28, 10, true),
                new LogSoftmax(tm, 0)
        );
    }

    public static Network createLeNet2(TensorManager tm) {
        return new Sequential(tm,
                new Flatten(tm),
                new Linear(tm, 28 * 28, 16, true),
                new Linear(tm, 16, 10, true),
                new LogSoftmax(tm, 0)
        );
    }

    public static Network createLeNet3(TensorManager tm) {
        return new Sequential(tm,
                new Conv2D(tm, 1, 1, 3, 3, 2, 0, 1, 1, true),
                new Sigmoid(tm),
                new Conv2D(tm, 1, 1, 3, 3, 2, 0, 2, 1, true),
                new Sigmoid(tm),
                new Flatten(tm),
                new Linear(tm, 25, 10, true),
                new LogSoftmax(tm, 0)
        );
    }

    static void main() throws IOException {
        TensorManager tm = TensorManager.ofFloat();
        tm.seed(42);

        MNISTDatasets mnist = new MNISTDatasets(tm);
        TabularDataset train = new TabularDataset(tm,
                mnist.train().darray(0).stretch(1),
                mnist.train().darray(1));
        TabularDataset test = new TabularDataset(tm,
                mnist.test().darray(0).stretch(1),
                mnist.test().darray(1));

        int epochs = 100;
        double lr = 1e-3;
        int batchSize = 1000;

//        var nn = new ConvNetwork(tm);
//        var nn = new SplitNetwork(tm, 7, 2);
        var nn = createLeNet3(tm);

        var optimizer = Optimizer.Adam(tm, nn.parameters()).lr.set(lr);

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
                System.out.print(".");
            }
            System.out.println();
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
