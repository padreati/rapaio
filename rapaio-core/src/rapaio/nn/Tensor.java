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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import rapaio.darray.Compare;
import rapaio.darray.DArray;
import rapaio.darray.DType;
import rapaio.darray.Shape;
import rapaio.nn.tensors.AddOp;
import rapaio.nn.tensors.BatchVtmOp;
import rapaio.nn.tensors.CompareFalseOp;
import rapaio.nn.tensors.CompareTrueOp;
import rapaio.nn.tensors.DivOp;
import rapaio.nn.tensors.DropoutOp;
import rapaio.nn.tensors.ExpOp;
import rapaio.nn.tensors.GatherOp;
import rapaio.nn.tensors.IdentityOp;
import rapaio.nn.tensors.LogOp;
import rapaio.nn.tensors.LogSoftmaxOp;
import rapaio.nn.tensors.Max;
import rapaio.nn.tensors.Mean1dOp;
import rapaio.nn.tensors.MulOp;
import rapaio.nn.tensors.NegOp;
import rapaio.nn.tensors.SigmoidOp;
import rapaio.nn.tensors.SoftmaxOp;
import rapaio.nn.tensors.SqrOp;
import rapaio.nn.tensors.SqrtOp;
import rapaio.nn.tensors.Std1dOp;
import rapaio.nn.tensors.SubOp;
import rapaio.nn.tensors.Sum1dOp;
import rapaio.nn.tensors.SumOp;
import rapaio.nn.tensors.TanhOp;

public abstract class Tensor {

    protected TensorManager tm;
    protected String name;
    protected DArray<?> value;
    protected DArray<?> grad;
    protected boolean requiresGrad;
    protected final List<BackFun> backfuns = new ArrayList<>();

    protected Tensor(TensorManager tm, String name) {
        this.tm = tm;
        this.name = name;
    }

    public final String name() {
        return name;
    }

    public final Tensor name(String name) {
        this.name = name;
        return this;
    }

    public final DType<?> dt() {
        return tm.dt();
    }

    public TensorManager tm() {
        return tm;
    }

    public final Shape shape() {
        return value.shape();
    }

    public final int rank() {
        return value.rank();
    }

    public final int size() {
        return value.size();
    }

    public int dim(int axis) {
        return value.dim(axis);
    }

    public final DArray<?> value() {
        return value;
    }

    public final void setValue(DArray<?> data) {
        value = data;
    }

    public final DArray<?> grad() {
        return grad;
    }

    public final void setGrad(DArray<?> grad) {
        this.grad = grad;
    }

    public final void addGrad(DArray<?> grad) {
        if (this.grad == null) {
            this.grad = grad;
        } else {
            this.grad.add_(grad);
        }
    }

    public final void zeroGrad() {
        this.grad = null;
    }

    public final boolean requiresGrad() {
        return requiresGrad;
    }

    public final Tensor requiresGrad(boolean requiresGrad) {
        this.requiresGrad = requiresGrad;
        return this;
    }

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

    /// OPERATIONS

    public final IdentityOp identity() {
        return new IdentityOp(this);
    }

    public final AddOp add(Tensor other) {
        return new AddOp(this, other);
    }

    public final AddOp add(double value) {
        return new AddOp(this, tm.scalarTensor(value));
    }

    public final SubOp sub(Tensor other) {
        return new SubOp(this, other);
    }

    public final SubOp sub(double value) {
        return new SubOp(this, tm.scalarTensor(value));
    }

    public final MulOp mul(Tensor other) {
        return new MulOp(this, other);
    }

    public final MulOp mul(double value) {
        return new MulOp(this, tm.scalarTensor(value));
    }

    public final DivOp div(Tensor other) {
        return new DivOp(this, other);
    }

    public final DivOp div(double value) {
        return new DivOp(this, tm.scalarTensor(value));
    }

    public final SumOp sum() {
        return new SumOp(this);
    }

    public final Sum1dOp sum1d(int axis) {
        return new Sum1dOp(this, axis);
    }

    public final Mean1dOp mean1d(int axis) {
        return new Mean1dOp(this, axis);
    }

    public final Std1dOp std1d(int axis) {
        return new Std1dOp(this, axis, 0, 1e-3, null);
    }

    public final Std1dOp std1d(int axis, int ddof) {
        return new Std1dOp(this, axis, ddof, 1e-3, null);
    }

    public final Std1dOp std1d(int axis, int ddof, Tensor mean) {
        return new Std1dOp(this, axis, ddof, 1e-3, mean);
    }

    public final Std1dOp std1d(int axis, int ddof, double epsilon, Tensor mean) {
        return new Std1dOp(this, axis, ddof, epsilon, mean);
    }

    public final SqrOp sqr() {
        return new SqrOp(this);
    }

    public final SqrtOp sqrt() {
        return new SqrtOp(this);
    }

    public final BatchVtmOp bvtm(Tensor other) {
        return new BatchVtmOp(this, other);
    }

    public final DropoutOp dropout(double p, Random random) {
        return dropout(p, random, false);
    }

    public final DropoutOp dropout(double p, Random random, boolean inplace) {
        return new DropoutOp(this, p, random, inplace);
    }

    public final SigmoidOp sigmoid() {
        return new SigmoidOp(this);
    }

    public final TanhOp tanh() {
        return new TanhOp(this);
    }

    public final Max max(double threshold) {
        return new Max(this, threshold);
    }

    public final NegOp neg() {
        return new NegOp(this);
    }

    public final ExpOp exp() {
        return new ExpOp(this);
    }

    public final LogOp log() {
        return new LogOp(this, -1);
    }

    public final LogOp log(double eps) {
        return new LogOp(this, eps);
    }

    public final SoftmaxOp softmax(int axis) {
        return new SoftmaxOp(this, axis);
    }

    public final LogSoftmaxOp logsoftmax(int axis) {
        return new LogSoftmaxOp(this, axis);
    }

    public final CompareTrueOp compareTrue(Compare cmp, double threshold) {
        return new CompareTrueOp(this, cmp, threshold);
    }

    public final CompareFalseOp compareFalse(Compare cmp, double threshold) {
        return new CompareFalseOp(this, cmp, threshold);
    }

    public final GatherOp gather(int axis, Tensor index) {
        return new GatherOp(this, axis, index);
    }
}
