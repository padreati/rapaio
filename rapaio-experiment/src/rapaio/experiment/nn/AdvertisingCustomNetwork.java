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
import static rapaio.graphics.opt.GOpts.labels;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import rapaio.darray.DArray;
import rapaio.darray.Shape;
import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plotter;
import rapaio.graphics.plot.artist.Legend;
import rapaio.nn.Autograd;
import rapaio.nn.Loss;
import rapaio.nn.Network;
import rapaio.nn.Optimizer;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;
import rapaio.nn.data.Batch;
import rapaio.nn.data.TabularDataset;
import rapaio.nn.layer.AbstractNetwork;
import rapaio.nn.layer.ELU;
import rapaio.nn.layer.LayerNorm;
import rapaio.nn.layer.Linear;
import rapaio.nn.loss.MSELoss;
import rapaio.sys.WS;

public class AdvertisingCustomNetwork {

    public static void main(String[] args) throws IOException {
        TensorManager tm = TensorManager.ofFloat();
        tm.seed(42);

        Frame df = Datasets.loadISLAdvertising();
        DArray<?> x = df.mapVars("TV", "Radio", "Newspaper").darray().cast(tm.dt());
        DArray<?> y = df.mapVars("Sales").darray().cast(tm.dt());

        TabularDataset ds = new TabularDataset(tm, x, y);
        TabularDataset[] split = ds.trainTestSplit(0.1);

        TabularDataset train = split[0];
        TabularDataset test = split[1];

        int epochs = 1_000;
        double lr = 2e-4;

        Network nn = new AdvertisingNetwork(tm);

        Optimizer optimizer = Optimizer.Adam(tm, nn.parameters())
                .lr.set(lr);
        Loss loss = new MSELoss(tm);

        VarDouble trainLoss = VarDouble.empty().name("trainLoss");
        VarDouble testLoss = VarDouble.empty().name("testLoss");

        for (int i = 0; i < epochs; i++) {

            optimizer.zeroGrad();
            nn.train();

            int batchCount = 0;
            double trainLossValue = 0;
            Iterator<Batch> batchIterator = train.batchIterator(10, true, false);
            while (batchIterator.hasNext()) {
                Batch batch = batchIterator.next();
                Tensor pred = nn.forward11(batch.tensor(0));
                Loss.Output lossOut = loss.forward(pred, batch.tensor(1));

                trainLossValue += lossOut.lossValue();
                batchCount++;

                Autograd.backward(lossOut.tensor());
                optimizer.step();
            }

            trainLossValue /= batchCount;
            trainLoss.addDouble(trainLossValue);

            nn.eval();
            Tensor pred = nn.forward11(test.tensor(0));
            Loss.Output lossOut = loss.forward(pred, test.tensor(1));
            double testLossValue = lossOut.lossValue();
            testLoss.addDouble(testLossValue);

            if (i % 10 == 0) {
                System.out.printf("epoch %d, train loss: %.3f, test loss: %.3f%n",
                        i, trainLossValue, testLossValue);
            }

        }

        WS.draw(Plotter.lines(trainLoss, color(1)).lines(testLoss, color(2)).legend(Legend.UP_LEFT, labels("train", "test")));

        String fileName = "/home/ati/work/rapaio/rapaio-experiment/src/rapaio/experiment/nn/advertising_net.rbin";
        nn.saveState(new File(fileName));

        Network nn2 = new AdvertisingNetwork(tm);
        nn2.loadState(new File(fileName));

        DArray<?> pred1 = nn.forward11(test.tensor(0)).value();
        DArray<?> pred2 = nn2.forward11(test.tensor(0)).value();

        System.out.println(pred1.sub(pred2).sum());
    }

    static class AdvertisingNetwork extends AbstractNetwork {

        final LayerNorm norm1;
        final Linear linear1;
        final ELU act1;
        final LayerNorm norm2;
        final Linear linear2;

        private static final int hidden = 24;

        public AdvertisingNetwork(TensorManager tm) {
            super(tm);
            norm1 = new LayerNorm(tm, Shape.of(3));
            linear1 = new Linear(tm, 3, hidden, true);
            act1 = new ELU(tm);

            norm2 = new LayerNorm(tm, Shape.of(hidden));
            linear2 = new Linear(tm, hidden, 1, true);
        }

        @Override
        public Tensor forward11(Tensor x) {
            x = norm1.forward11(x);
            x = linear1.forward11(x);
            x = act1.forward11(x);
            x = norm2.forward11(x);
            x = linear2.forward11(x);
            return x;
        }
    }

}
