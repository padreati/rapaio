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

package rapaio.nn.loss;

import java.util.List;

import rapaio.core.param.ParamSet;
import rapaio.nn.Loss;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;
import rapaio.datasets.Batch;

public abstract class AbstractLoss<L extends AbstractLoss<L>> extends ParamSet<L> implements Loss {

    protected final TensorManager tm;
    protected String name;

    public AbstractLoss(TensorManager tm) {
        this.tm = tm;
    }

    @Override
    public TensorManager tm() {
        return tm;
    }

    @Override
    public Loss.Output batchForward(List<Batch> batches, Tensor trueValues) {
        if (batches.isEmpty()) {
            throw new IllegalArgumentException("Cannot compute a loss over an empty list of batches.");
        }
        Tensor lossTensor = null;

        for (Batch batch : batches) {
            Tensor batchPredValues = batch.outputs()[0];
            Tensor batchTrueValues = tm.var(trueValues.value().sel(0, batch.indices()));
            // forward keeps no state, and calling it on this instance keeps the configured parameters (reduce),
            // which a fresh instance from newInstance() would reset to their defaults
            Loss.Output lossOut = forward(batchPredValues, batchTrueValues);
            // forward seeds its own root gradient with 1, so it can be back propagated on its own; here the batch loss
            // is an inner node, and a seed left in place would be added to the gradient flowing from the total
            lossOut.tensor().zeroGrad();
            lossTensor = lossTensor == null ? lossOut.tensor() : lossTensor.add(lossOut.tensor());
        }
        // the reported value is the average of the batch losses, so the tensor which is back propagated is the same
        // average; a plain sum would scale the gradient by the number of batches
        lossTensor = lossTensor.div(batches.size());
        lossTensor.setGrad(tm.scalarTensor(1).value());
        return new Loss.Output(lossTensor, lossTensor.value().getDouble());
    }
}
