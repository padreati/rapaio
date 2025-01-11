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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
import rapaio.nn.layer.AbstractNetwork;
import rapaio.nn.layer.LayerNorm;
import rapaio.nn.layer.Linear;
import rapaio.nn.layer.LogSoftmax;
import rapaio.nn.layer.Tanh;
import rapaio.nn.loss.NegativeLikelihoodLoss;
import rapaio.printer.Format;

public class MNIST {

    static class DenseNetwork extends AbstractNetwork {

        private LayerNorm norm1;
        private Linear linear1;
        private Tanh tanh;
        private LayerNorm norm2;
        private Linear linear2;
        private LogSoftmax softmax;

        private final int h;

        public DenseNetwork(TensorManager tm, int h) {
            super(tm);
            this.h = h;
            this.norm1 = new LayerNorm(tm, Shape.of(784));
            this.linear1 = new Linear(tm, 784, h, true);
            this.tanh = new Tanh(tm);
            this.norm2 = new LayerNorm(tm, Shape.of(h));
            this.linear2 = new Linear(tm, h, 10, true);
            this.softmax = new LogSoftmax(tm, 0);
        }

        @Override
        public Tensor forward11(Tensor x) {
            x = norm1.forward11(x);
            x = linear1.forward11(x);
            x = tanh.forward11(x);
            x = norm2.forward11(x);
            x = linear2.forward11(x);
            x = softmax.forward11(x);
            return x;
        }
    }

    static class SplitNetwork extends AbstractNetwork {

        private LayerNorm norm1;
        private Linear[] linears1;
        private Tanh tanh;
        private LayerNorm norm2;
        private Linear linear2;
        private LogSoftmax softmax;

        private final int h;
        private final int split;

        int[] indices;

        public SplitNetwork(TensorManager tm, int split, int h) {
            super(tm);
            this.h = h;
            this.split = split;

            if (784 % split != 0) {
                throw new IllegalArgumentException("Number of hidden units must be divisible by split");
            }
            this.indices = new int[784 / split];
            for (int i = 1; i < indices.length; i++) {
                indices[i] = indices[i - 1] + split;
            }

            this.norm1 = new LayerNorm(tm, Shape.of(784));

            this.linears1 = new Linear[indices.length];
            for (int i = 0; i < indices.length; i++) {
                linears1[i] = new Linear(tm, split, h, true);
            }
            this.tanh = new Tanh(tm);
            this.norm2 = new LayerNorm(tm, Shape.of(h * indices.length));
            this.linear2 = new Linear(tm, h * indices.length, 10, true);
            this.softmax = new LogSoftmax(tm, 0);
        }

        @Override
        public List<Tensor> parameters() {
            ArrayList<Tensor> params = new ArrayList<>();
            params.addAll(norm1.parameters());
            for (var lin : linears1) {
                params.addAll(lin.parameters());
            }
            params.addAll(tanh.parameters());
            params.addAll(norm2.parameters());
            params.addAll(linear2.parameters());
            params.addAll(softmax.parameters());
            return params;
        }


        @Override
        public Tensor forward11(Tensor x) {
            x = norm1.forward11(x);

            List<Tensor> splits = x.split(1, indices);
            List<Tensor> after = new ArrayList<>();
            for (int i = 0; i < splits.size(); i++) {
                var a = linears1[i].forward11(splits.get(i));
                a = tanh.forward11(a);
                after.add(a);
            }

            x = tm.cat(1, after.toArray(Tensor[]::new));

            x = norm2.forward11(x);
            x = linear2.forward11(x);
            x = softmax.forward11(x);
            return x;
        }
    }


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

        int epochs = 10;
        double lr = 1e-3;
        int batchSize = 100;

        var nn = new SplitNetwork(tm, 7, 2);

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
