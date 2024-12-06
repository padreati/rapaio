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

package rapaio.nn.tensors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import rapaio.darray.DArray;
import rapaio.darray.DType;
import rapaio.darray.Shape;
import rapaio.nn.BackFun;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

public abstract class AbstractTensor implements Tensor {

    protected String name;
    protected TensorManager tm;
    private DArray<?> value;
    private DArray<?> grad;
    protected boolean requiresGrad;
    private final List<BackFun> backfuns = new ArrayList<>();

    protected AbstractTensor(TensorManager tm, String name) {
        this.tm = tm;
        this.name = name;
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public final Tensor name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public final DType<?> dtype() {
        return tm.dtype();
    }

    @Override
    public TensorManager tm() {
        return tm;
    }

    @Override
    public final Shape shape() {
        return value.shape();
    }

    @Override
    public final int rank() {
        return value.rank();
    }

    @Override
    public final int size() {
        return value.size();
    }

    @Override
    public int dim(int axis) {
        return value.dim(axis);
    }

    @Override
    public final DArray<?> value() {
        return value;
    }

    @Override
    public final void setValue(DArray<?> data) {
        value = data;
    }

    @Override
    public final DArray<?> grad() {
        return grad;
    }

    @Override
    public final void setGrad(DArray<?> grad) {
        this.grad = grad;
    }

    @Override
    public final void addGrad(DArray<?> grad) {
        if (this.grad == null) {
            this.grad = grad;
        } else {
            this.grad.add_(grad);
        }
    }

    @Override
    public final void zeroGrad() {
        this.grad = null;
    }

    @Override
    public final boolean requiresGrad() {
        return requiresGrad;
    }

    @Override
    public final Tensor requiresGrad(boolean requiresGrad) {
        this.requiresGrad = requiresGrad;
        return this;
    }

    @Override
    public final List<BackFun> backfuns() {
        return backfuns;
    }

    protected final void backEdge(Tensor ref, Supplier<DArray<?>> backFun) {
        backfuns.add(BackFun.of(ref, backFun));
    }

    @Override
    public final String toString() {
        return String.format("name:%s\nval:%sgrad:%s", name == null ? "null" : "(" + name + ")", value != null ? value.toString() : "\n",
                grad);
    }
}
