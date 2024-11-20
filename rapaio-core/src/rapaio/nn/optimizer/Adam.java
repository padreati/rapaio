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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rapaio.core.param.Param;
import rapaio.core.param.ParamSet;
import rapaio.core.param.ValueParam;
import rapaio.narray.NArray;
import rapaio.narray.NArrayManager;
import rapaio.narray.NArrays;
import rapaio.nn.Optimizer;
import rapaio.nn.Tensor;

public class Adam extends ParamSet<Adam> implements Optimizer {

    public final Param<Double, Adam> lr = new ValueParam<>(this, 1e-3, "lr");
    public final Param<Double, Adam> beta1 = new ValueParam<>(this, 0.9, "beta1 used for computing running averages of gradient");
    public final Param<Double, Adam> beta2 = new ValueParam<>(this, 0.999, "beta2 used for computing running averages of squared gradient");
    public final Param<Double, Adam> eps = new ValueParam<>(this, 1e-8, "eps");
    public final Param<Double, Adam> weightDecay = new ValueParam<>(this, 0d, "weight decay");
    public final Param<Boolean, Adam> amsgrad = new ValueParam<>(this, false, "whether to use the AMSGrad variant of this algorithm");
    public final Param<Boolean, Adam> maximize = new ValueParam<>(this, false, "maximize the optimization algorithm");


    private final List<Tensor> parameters;
    private double t = 1;
    private final ConcurrentHashMap<Tensor, NArray<?>> mts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Tensor, NArray<?>> vts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Tensor, NArray<?>> vtmaxs = new ConcurrentHashMap<>();

    public Adam(Collection<Tensor> parameters) {
        this.parameters = new ArrayList<>(parameters);
    }

    @Override
    public void zeroGrad() {
        for (var parameter : parameters) {
            parameter.zeroGrad();
        }
    }

    @Override
    public void step() {
        CountDownLatch latch = new CountDownLatch(parameters.size());
        try (ExecutorService executor = Executors.newFixedThreadPool(NArrayManager.base().cpuThreads())) {
            for (var parameter : parameters) {
                executor.submit(() -> {
                    step(parameter);
                    latch.countDown();
                });
            }
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        t++;
    }

    private void step(Tensor param) {
        var gt = param.grad();
        if (maximize.get()) {
            gt = gt.mul(-1);
        }

        if (weightDecay.get() > 0) {
            gt = gt.add(param.value().mul(weightDecay.get()));
        }

        var mt = getMt(param).mul(beta1.get()).add_(gt.mul(1 - beta1.get()));
        mts.put(param, mt);

        var vt = getVt(param).mul(beta2.get()).add_(gt.sqr().mul(1 - beta2.get()));
        vts.put(param, vt);

        var amt = mt.div(1 - Math.pow(beta1.get(), t));
        var avt = vt.div(1 - Math.pow(beta2.get(), t));

        if (amsgrad.get()) {
            var vtmax = getVtMax(param).max(avt);
            vtmaxs.put(param, vtmax);
            param.value().sub_(amt.mul(lr.get()).div_(vtmax.sqrt().add_(eps.get())));
        } else {
            param.value().sub_(amt.mul(lr.get()).div_(avt.sqrt().add_(eps.get())));
        }
    }

    private NArray<?> getMt(Tensor tensor) {
        if (mts.containsKey(tensor)) {
            return mts.get(tensor);
        } else {
            return NArrays.ofType(tensor.value().dtype()).zeros(tensor.value().shape());
        }
    }

    private NArray<?> getVt(Tensor tensor) {
        if (vts.containsKey(tensor)) {
            return vts.get(tensor);
        } else {
            return NArrays.ofType(tensor.value().dtype()).zeros(tensor.value().shape());
        }
    }

    private NArray<?> getVtMax(Tensor tensor) {
        if (vtmaxs.containsKey(tensor)) {
            return vtmaxs.get(tensor);
        } else {
            return NArrays.ofType(tensor.value().dtype()).zeros(tensor.value().shape());
        }
    }
}
