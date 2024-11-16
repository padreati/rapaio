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

import java.awt.Color;
import java.util.Arrays;

import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.data.VarRange;
import rapaio.data.transform.OneHotEncoding;
import rapaio.datasets.Datasets;
import rapaio.math.narray.DType;
import rapaio.nn.Autograd;
import rapaio.nn.Loss;
import rapaio.nn.Net;
import rapaio.nn.Optimizer;
import rapaio.nn.layer.BatchNorm1D;
import rapaio.nn.layer.Linear;
import rapaio.nn.layer.LogSoftmax;
import rapaio.nn.layer.ReLU;
import rapaio.nn.layer.Sequential;
import rapaio.nn.loss.NegativeLikelihoodLoss;
import rapaio.sys.WS;
import rapaio.util.collection.IntArrays;

public class SandboxIris {
    public static void main() {
        Frame iris = Datasets.loadIrisDataset();

        DType<?> dt = DType.FLOAT;

        var x = iris.mapVars(VarRange.of("0~3")).tensor().cast(dt);
        var y = iris.fapply(OneHotEncoding.on(false, false, VarRange.of("class"))).mapVars("4~6").tensor().cast(dt);

        int[] indexes = IntArrays.newSeq(x.dim(0));
        int[] train_sample = Arrays.stream(indexes).filter(v -> v % 4 != 0).toArray();
        int[] test_sample = Arrays.stream(indexes).filter(v -> v % 4 == 0).toArray();

        var x_train = x.take(0, train_sample);
        var x_test = x.take(0, test_sample);
        var y_train = y.take(0, train_sample);
        var y_test = y.take(0, test_sample);

        Net nn = new Sequential(
                new BatchNorm1D(dt, 4),
                new Linear(dt, 4, 100, true),
                new ReLU(),
                new Linear(dt, 100, 64, true),
                new ReLU(),
                new Linear(dt, 64, 3, true),
                new ReLU(),
                new LogSoftmax(1)
        );

        Optimizer optimizer = Optimizer.Adam(nn.parameters())
                .lr.set(1e-3);
        VarDouble trainLoss = VarDouble.empty().name("trainLoss");
        VarDouble testLoss = VarDouble.empty().name("trainLoss");

        for (int epoch = 0; epoch < 1_000; epoch++) {
            optimizer.zeroGrad();

            Loss loss = new NegativeLikelihoodLoss();

            var outputTrain = nn.forward11(Autograd.var(x_train));
            loss.forward(outputTrain, Autograd.var(y_train));

            loss.backward();
            double trainLossValue = loss.loss();
            optimizer.step();

            loss.forward(nn.forward(Autograd.var(x_test))[0], Autograd.var(y_test));
            double testLossValue = loss.loss();

            trainLoss.addDouble(trainLossValue);
            testLoss.addDouble(testLossValue);

            if (epoch < 10 || epoch % 50 == 0) {
                System.out.println("Epoch: " + (epoch + 1) + ", train loss:" + trainLossValue + ", test loss:" + testLossValue);
            }
        }

        WS.draw(lines(trainLoss, color(Color.RED), lwd(1)).lines(testLoss, color(2), lwd(1)));
    }
}
