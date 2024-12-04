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

import java.io.IOException;

import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.data.VarRange;
import rapaio.data.transform.OneHotEncoding;
import rapaio.datasets.Datasets;
import rapaio.ml.eval.metric.Confusion;
import rapaio.narray.DType;
import rapaio.nn.Autograd;
import rapaio.nn.Loss;
import rapaio.nn.Net;
import rapaio.nn.Optimizer;
import rapaio.nn.TensorManager;
import rapaio.nn.data.ArrayDataset;
import rapaio.nn.layer.BatchNorm1D;
import rapaio.nn.layer.ELU;
import rapaio.nn.layer.Linear;
import rapaio.nn.layer.LogSoftmax;
import rapaio.nn.layer.Sequential;
import rapaio.nn.loss.NegativeLikelihoodLoss;

public class SandboxIris {
    public static void main() throws IOException {
        Frame iris = Datasets.loadIrisDataset();

        TensorManager tm = TensorManager.ofFloat();
        tm.seed(42);

        var x = iris.mapVars(VarRange.of("0~3")).narray().cast(DType.FLOAT);
        var y = iris.fapply(OneHotEncoding.on(false, false, VarRange.of("class"))).mapVars("4~6").narray().cast(DType.FLOAT);

        ArrayDataset[] split = new ArrayDataset(x, y).trainTestSplit(0.15);
        ArrayDataset train = split[0];
        ArrayDataset test = split[1];

        Net nn = new Sequential(tm,
                new BatchNorm1D(tm, 4),
                new Linear(tm, 4, 10_000, true),
                new ELU(tm),
//                new BatchNorm1D(tm, 400),
                new Linear(tm, 10_000, 3, true),
                new ELU(tm),
//                new BatchNorm1D(tm, 3),
                new LogSoftmax(1)
        );

        Optimizer optimizer = Optimizer.Adam(tm, nn.parameters()).lr.set(1e-4);

        VarDouble trainLoss = VarDouble.empty().name("trainLoss");
        VarDouble testLoss = VarDouble.empty().name("trainLoss");

        for (int epoch = 0; epoch < 1_000; epoch++) {

            optimizer.zeroGrad();
            nn.train();

            Net.BatchOutput batchOut = nn.batchForward(20, train.tensor(tm, 0));
            Net.BatchLoss batchLoss = batchOut.applyLoss(new NegativeLikelihoodLoss(), train.tensor(tm, 1));

            double trainLossValue = batchLoss.lossValue();
            Autograd.backward(batchLoss.loss());

            optimizer.step();

            nn.eval();
            Loss loss = new NegativeLikelihoodLoss();
            loss.forward(nn.forward11(tm.var(test.array(0))), tm.var(test.array(1)));
            double testLossValue = loss.loss();

            trainLoss.addDouble(trainLossValue);
            testLoss.addDouble(testLossValue);

            if (epoch < 10 || epoch % 50 == 0) {
                System.out.println("Epoch: " + (epoch + 1) + ", train loss:" + trainLossValue + ", test loss:" + testLossValue);

                var y_pred = nn.forward11(tm.var(x)).value().exp().argmax1d(1);
                VarNominal y_pred_nom = VarNominal.empty(iris.rowCount(), iris.rvar("class").levels());
                for (int i = 0; i < y_pred_nom.size(); i++) {
                    y_pred_nom.setInt(i, y_pred.getInt(i) + 1);
                }

                Confusion.from(iris.rvar("class"), y_pred_nom).printContent();
            }
        }

//        WS.draw(lines(trainLoss, color(Color.RED)).lines(testLoss, color(2)));

//        NetSerialization.printNetState(nn);


        tm.close();
    }
}
