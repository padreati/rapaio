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

import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.data.VarRange;
import rapaio.data.transform.OneHotEncoding;
import rapaio.datasets.Datasets;
import rapaio.math.nn.Autograd;
import rapaio.math.nn.Loss;
import rapaio.math.nn.Net;
import rapaio.math.nn.Optimizer;
import rapaio.math.nn.layer.BatchNorm1D;
import rapaio.math.nn.layer.Linear;
import rapaio.math.nn.layer.LogSoftmax;
import rapaio.math.nn.layer.Sequential;
import rapaio.math.nn.layer.Sigmoid;
import rapaio.math.nn.loss.NegativeLikelihoodLoss;
import rapaio.math.narrays.DType;
import rapaio.sys.WS;
import rapaio.util.collection.IntArrays;

public class SandboxIris {
    public static void main(String[] args) {
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
                new Linear(dtype, 4, 64, true),
                new Sigmoid(),
                new BatchNorm1D(dtype, 64),
                new Linear(dtype, 64, 3, true),
                new LogSoftmax(1)
        );
        nn.seed(423);

        Optimizer optimizer = Optimizer.Adam(nn.parameters())
                .lr.set(1e-4)
                .amsgrad.set(true);
        VarDouble trainLoss = VarDouble.empty().name("trainLoss");
        VarDouble testLoss = VarDouble.empty().name("trainLoss");

        int batch = 10;

        for (int i = 0; i < 1_000; i++) {
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
