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

package rapaio.nn.loss;

import java.util.List;

import rapaio.core.param.ParamSet;
import rapaio.nn.Loss;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;
import rapaio.nn.data.Batch;

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
        Tensor lossTensor = null;
        double lossValue = 0;

        for (Batch batch : batches) {
            Loss loss = this.newInstance();
            Tensor batchPredValues = batch.outputs()[0];
            Tensor batchTrueValues = tm.var(trueValues.value().sel(0, batch.indices()));
            Loss.Output lossOut = loss.forward(batchPredValues, batchTrueValues);
            lossTensor = lossTensor == null ? lossOut.tensor() : lossTensor.add(lossOut.tensor());
            lossValue += lossOut.lossValue();
        }
        lossTensor.setGrad(tm.scalarTensor(1).value());
        return new Loss.Output(lossTensor, lossValue / batches.size());
    }
}
