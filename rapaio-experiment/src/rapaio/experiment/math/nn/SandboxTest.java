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
import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.data.VarRange;
import rapaio.data.transform.OneHotEncoding;
import rapaio.datasets.Datasets;
import rapaio.math.nn.Autograd;
import rapaio.math.nn.Loss;
import rapaio.math.nn.Net;
import rapaio.math.nn.Node;
import rapaio.math.nn.Optimizer;
import rapaio.math.nn.layer.Linear;
import rapaio.math.nn.layer.LogSoftmax;
import rapaio.math.nn.layer.ReLU;
import rapaio.math.nn.layer.Sequential;
import rapaio.math.nn.loss.MSELoss;
import rapaio.math.nn.loss.NegativeLikelihoodLoss;
import rapaio.math.tensor.DType;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.TensorManager;
import rapaio.math.tensor.Tensors;
import rapaio.sys.WS;
import rapaio.util.collection.IntArrays;

public class SandboxTest {

    private static final TensorManager.OfType<?> tmd = TensorManager.base().ofDouble();

    public static void main(String[] args) {
        sandboxTest();
    }

    static void sandboxTest() {
        DType<?> dtype = DType.FLOAT;
        Random random = new Random(42);

        Tensor<?> xtrain = Tensors.ofType(dtype).random(Shape.of(10_000, 4), random);
        Tensor<?> ytrain = Tensors.ofType(dtype).zeros(Shape.of(10_000));
        for (int i = 0; i < ytrain.dim(0); i++) {
            Tensor<?> row = xtrain.takesq(0, i);
            ytrain.setDouble(random.nextDouble() / 100 + fun(row.getDouble(0), row.getDouble(1), row.getDouble(2), row.getDouble(3)), i);
        }
        Tensor<?> xtest = Tensors.ofType(dtype).random(Shape.of(200, 4), random);
        Tensor<?> ytest = Tensors.ofType(dtype).zeros(Shape.of(200));
        for (int i = 0; i < ytest.dim(0); i++) {
            Tensor<?> row = xtest.takesq(0, i);
            ytest.setDouble(random.nextDouble() / 100 + fun(row.getDouble(0), row.getDouble(1), row.getDouble(2), row.getDouble(3)), i);
        }

        Net nn = new Sequential(
                new Linear(dtype, 4, 1000, true),
                new ReLU(),
                new Linear(dtype, 1000, 1, true),
                new ReLU()
        );
        nn.seed(42);


        int EPOCHS = 1_000;
        int BATCH_SIZE = 100;
        double LR = 1e-3;

        Optimizer c = Optimizer.Adam(nn.parameters())
                .lr.set(LR);
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
                Tensor<?> xx = xtrain.take(0, batchIndexes);
                Tensor<?> yy = ytrain.take(0, batchIndexes);
                Node[] outputs = nn.forward(Autograd.var(xx));

                loss.forward(outputs[0], Autograd.var(yy));
                loss.backward();

                trainLoss.addDouble(loss.loss());
                trLoss += loss.loss() * batchIndexes.length / sample.length;

                c.step();
            }

            nn.eval();
            Node[] outputs = nn.forward(Autograd.var(xtest));
            loss.forward(outputs[0], Autograd.var(ytest));
            testLoss.addDouble(loss.loss());
            teLoss += loss.loss();

            System.out.printf("Epoch: %d, Train loss: %.6f, test loss: %.6f\n", i + 1, trLoss, teLoss);


        }

        WS.draw(lines(trainLoss, color(1), lwd(1)).lines(testLoss, color(2), lwd(1)));

        nn.forward(Autograd.var(Tensors.ofType(dtype).random(Shape.of(2, 4), random)).name("x"))[0].value().printString();
    }

    public static double fun(double x1, double x2, double x3, double x4) {
        return Math.sin(x1) * Math.cos(x2) + x3 + x4 * x4 + Math.sqrt(Math.abs(x1 + x3));
    }

    static void testIris() {
        Frame iris = Datasets.loadIrisDataset();

        DType<?> dtype = DType.FLOAT;

        var x = iris.mapVars(VarRange.of("0~3")).tensor().cast(dtype);
        var y = iris.fapply(OneHotEncoding.on(false, false, VarRange.of("class"))).mapVars("4~6").tensor().cast(dtype);

        Random random = new Random(42);

        int[] indexes = IntArrays.newSeq(x.dim(0));
        IntArrays.shuffle(indexes, random);
        int[] train_sample = Arrays.copyOfRange(indexes, 0, (int) (x.shape().dim(0) * 0.1));
        int[] test_sample = Arrays.copyOfRange(indexes, (int) (x.shape().dim(1) * 0.1), indexes.length);

        var x_train = x.take(0, train_sample);
        var x_test = x.take(0, test_sample);
        var y_train = y.take(0, train_sample);
        var y_test = y.take(0, test_sample);

        Net nn = new Sequential(
                new Linear(dtype, 4, 10_000, true),
                new ReLU(),
                new Linear(dtype, 10_000, 64, true),
                new ReLU(),
                new Linear(dtype, 64, 3, true),
                new LogSoftmax(1)
        );
        nn.seed(423);

        Optimizer optimizer = Optimizer.Adam(nn.parameters())
                .lr.set(1e-6)
                .amsgrad.set(false);
        VarDouble trainLoss = VarDouble.empty().name("trainLoss");
        VarDouble testLoss = VarDouble.empty().name("trainLoss");

        int batch = 8;

        for (int i = 0; i < 1000; i++) {
            optimizer.zeroGrad();

            int[] idx = IntArrays.newSeq(x_train.dim(0));
            IntArrays.shuffle(idx, random);

            double trainLossValue = 0;
            Loss loss = new NegativeLikelihoodLoss();

            for (int j = 0; j < idx.length; j += batch) {
                int[] idx_batch = Arrays.copyOfRange(idx, j, Math.min(idx.length, j + batch));
                var x_batch = x_train.take(0, idx_batch);
                var y_batch = y_train.take(0, idx_batch);

                loss.forward(nn.forward(Autograd.var(x_batch))[0], Autograd.var(y_batch));

                trainLossValue += loss.loss();

                loss.backward();
                optimizer.step();
            }

            loss.forward(nn.forward(Autograd.var(x_test))[0], Autograd.var(y_test));
            double testLossValue = loss.loss();

            trainLoss.addDouble(trainLossValue * batch / (idx.length));
            testLoss.addDouble(testLossValue);

            System.out.println("Epoch: " + (i + 1) + ", train loss:" + trainLossValue + ", test loss:" + testLossValue);


        }

        WS.draw(lines(trainLoss, color(1), lwd(1)).lines(testLoss, color(2), lwd(1)));
    }
}
