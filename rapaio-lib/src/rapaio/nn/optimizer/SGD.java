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

package rapaio.nn.optimizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import rapaio.core.param.Param;
import rapaio.core.param.ParamSet;
import rapaio.core.param.ValueParam;
import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.nn.Optimizer;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

public class SGD extends ParamSet<SGD> implements Optimizer {

    public final Param<Double, SGD> lr = new ValueParam<>(this, 1e-4, "learning rate");
    public final Param<Double, SGD> weightDecay = new ValueParam<>(this, 0d, "weight decay");
    public final Param<Double, SGD> momentum = new ValueParam<>(this, 0d, "momentum");
    public final Param<Double, SGD> dampening = new ValueParam<>(this, 0d, "dampening");
    public final Param<Boolean, SGD> nesterov = new ValueParam<>(this, false, "nesterov");
    public final Param<Boolean, SGD> maximize = new ValueParam<>(this, false, "maximize");


    private final TensorManager tm;
    private final Collection<Tensor> params;

    private final HashMap<Tensor, DArray<?>> mus = new HashMap<>();

    public SGD(TensorManager tm, Collection<Tensor> params) {
        this.tm = tm;
        this.params = params;
    }

    @Override
    public final void zeroGrad() {
        params.forEach(Tensor::zeroGrad);
    }

    @Override
    public void step() {
        List<Future<?>> futures = new ArrayList<>();
        try (ExecutorService executor = Executors.newFixedThreadPool(DArrayManager.base().cpuThreads())) {
            for (var parameter : params) {
                futures.add(executor.submit(() -> step(parameter)));
            }
            while (!futures.isEmpty()) {
                Future<?> future = futures.removeFirst();
                future.get();
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void step(Tensor tensor) {
        DArray<?> gt = tensor.grad();
        if (weightDecay.get() != 0) {
            gt = gt.add(tensor.value().mul(weightDecay.get()));
        }
        if (momentum.get() != 0) {
            var mu = mus.get(tensor);
            mu = (mu == null) ? gt : mu.mul(momentum.get()).add(gt.mul(1 - dampening.get()));
            mus.put(tensor, mu);

            if (nesterov.get()) {
                gt = gt.add(mu.mul(momentum.get()));
            } else {
                gt = mu;
            }
        }
        if (maximize.get()) {
            tensor.value().add_(gt.mul(lr.get()));
        } else {
            tensor.value().sub_(gt.mul(lr.get()));
        }
    }
}
