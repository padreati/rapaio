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

package rapaio.nn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rapaio.nn.data.ArrayDataset;
import rapaio.util.NotImplementedException;

public interface Net extends Serializable {

    TensorManager tm();

    List<Tensor> parameters();

    void seed(long seed);

    Random random();

    void train();

    void eval();

    default Tensor[] forward(Tensor... xs) {
        if (xs.length == 1) {
            return new Tensor[] {forward11(xs[0])};
        }
        throw new NotImplementedException();
    }

    default Tensor forward11(Tensor x) {
        throw new NotImplementedException();
    }

    default BatchOutput batchForward(int batchSize, Tensor... inputs) {
        ArrayDataset dataset = new ArrayDataset(inputs);
        List<CompletableFuture<Tensor[]>> futures = new ArrayList<>();
        long seed = random().nextLong();
        dataset.seed(seed);
        var batchIt = dataset.batchIndexIterator(batchSize, true, false);

        try (ExecutorService executor = Executors.newWorkStealingPool(tm().threads())) {
            while (batchIt.hasNext()) {
                var batchIndexes = batchIt.next();
                futures.add(CompletableFuture.supplyAsync(() -> {
                    Tensor[] batch = Arrays.stream(dataset.arrays()).map(a -> tm().var(a.take(0, batchIndexes))).toArray(Tensor[]::new);
                    return forward(batch);
                }, executor));
            }
            try {
                List<Tensor[]> outputs = futures.stream().map(CompletableFuture::join).toList();
                return new BatchOutput(tm(), batchSize, seed, outputs);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    class BatchOutput {

        private final TensorManager tm;
        private final int batchSize;
        private final long seed;
        private final List<Tensor[]> outputs;

        private BatchOutput(TensorManager tm, int batchSize, long seed, List<Tensor[]> outputs) {
            this.tm = tm;
            this.batchSize = batchSize;
            this.seed = seed;
            this.outputs = outputs;
        }

        public BatchLoss applyLoss(Loss localLoss, Tensor trueValues) {
            if(outputs.isEmpty()) {
                return null;
            }
            Tensor gradLoss = null;
            ArrayDataset dataset = new ArrayDataset(trueValues);
            dataset.seed(seed);
            var batchIt = dataset.batchIndexIterator(batchSize, true, false);
            for (int i = 0; i < outputs.size(); i++) {
                Loss loss = localLoss.newInstance();
                loss.forward(outputs.get(i)[0], tm.var(trueValues.value().take(0, batchIt.next())));
                loss.last().name("loss[" + i + "]");
                gradLoss = gradLoss == null ? loss.last() : gradLoss.add(loss.last());
            }
            gradLoss.setGrad(tm.scalarTensor(1).value());
            return new BatchLoss(gradLoss, gradLoss.value().getDouble() / outputs.size());
        }
    }

    record BatchLoss(Tensor generalLoss, double lossValue) {
    }

}
