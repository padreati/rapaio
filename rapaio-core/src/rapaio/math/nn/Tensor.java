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

package rapaio.math.nn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import rapaio.math.nn.tensors.Add;
import rapaio.math.nn.tensors.BatchVtm;
import rapaio.math.nn.tensors.Div;
import rapaio.math.nn.tensors.Dropout;
import rapaio.math.nn.tensors.Identity;
import rapaio.math.nn.tensors.Log;
import rapaio.math.nn.tensors.LogSoftmax;
import rapaio.math.nn.tensors.Max;
import rapaio.math.nn.tensors.Mean1D;
import rapaio.math.nn.tensors.Mul;
import rapaio.math.nn.tensors.Neg;
import rapaio.math.nn.tensors.Sigmoid;
import rapaio.math.nn.tensors.Softmax;
import rapaio.math.nn.tensors.Sqr;
import rapaio.math.nn.tensors.Sqrt;
import rapaio.math.nn.tensors.Std1D;
import rapaio.math.nn.tensors.Sub;
import rapaio.math.nn.tensors.Sum;
import rapaio.math.nn.tensors.Sum1D;
import rapaio.math.nn.tensors.Tanh;
import rapaio.math.narrays.DType;
import rapaio.math.narrays.NArray;
import rapaio.math.narrays.Shape;

public abstract class Tensor {

    protected String name;
    protected DType<?> dtype;
    private NArray<?> value;
    private NArray<?> grad;
    protected boolean requiresGrad;
    private final List<BackFun> backfuns = new ArrayList<>();

    public final String name() {
        return name;
    }

    public final Tensor name(String name) {
        this.name = name;
        return this;
    }

    public final DType<?> dtype() {
        return dtype;
    }

    public final NArray<?> value() {
        return value;
    }

    public final void setValue(NArray<?> data) {
        value = data;
    }

    public Shape shape() {
        return value.shape();
    }

    public int rank() {
        return value.rank();
    }

    public int size() {
        return value.size();
    }

    public final NArray<?> grad() {
        return grad;
    }

    public final void setGrad(NArray<?> grad) {
        this.grad = grad;
    }

    public final void addGrad(NArray<?> grad) {
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

    protected void backEdge(Tensor ref, Supplier<NArray<?>> backFun) {
        backfuns.add(BackFun.of(ref, backFun));
    }

    @Override
    public final String toString() {
        return String.format("name:%s\nval:%sgrad:%s", name == null ? "null" : "(" + name + ")", value != null ? value.toString() : "\n",
                grad);
    }

    /// OPERATIONS


    public Identity identity() {
        return new Identity(this);
    }

    public Add add(Tensor other) {
        return new Add(this, other);
    }

    public Add add(double value) {
        return new Add(this, Autograd.scalar(dtype, value));
    }

    public Sub sub(Tensor other) {
        return new Sub(this, other);
    }

    public Sub sub(double value) {
        return new Sub(this, Autograd.scalar(dtype, value));
    }

    public Mul mul(Tensor other) {
        return new Mul(this, other);
    }

    public Mul mul(double value) {
        return new Mul(this, Autograd.scalar(dtype, value));
    }

    public Div div(Tensor other) {
        return new Div(this, other);
    }

    public Div div(double value) {
        return new Div(this, Autograd.scalar(dtype, value));
    }

    public Sum sum() {
        return new Sum(this);
    }

    public Sum1D sum1d(int axis) {
        return new Sum1D(this, axis);
    }

    public Mean1D mean1d(int axis) {
        return new Mean1D(this, axis);
    }

    public Std1D std1d(int axis) {
        return new Std1D(this, axis, 0, null);
    }

    public Std1D std1d(int axis, int ddof) {
        return new Std1D(this, axis, ddof, null);
    }

    public Std1D std1d(int axis, int ddof, Tensor mean) {
        return new Std1D(this, axis, ddof, mean);
    }

    public Sqr sqr() {
        return new Sqr(this);
    }

    public Sqrt sqrt() {
        return new Sqrt(this);
    }

    public BatchVtm bvtm(Tensor other) {
        return new BatchVtm(this, other);
    }

    public Dropout dropout(double p, Random random) {
        return dropout(p, random, false);
    }

    public Dropout dropout(double p, Random random, boolean inplace) {
        return new Dropout(this, p, random, inplace);
    }

    public Sigmoid sigmoid() {
        return new Sigmoid(this);
    }

    public Tanh tanh() {
        return new Tanh(this);
    }

    public Max max(double threshold) {
        return new Max(this, threshold);
    }

    public Neg neg() {
        return new Neg(this);
    }

    public Log log() {
        return new Log(this, -1);
    }

    public Log log(double eps) {
        return new Log(this, eps);
    }

    public Softmax softmax(int axis) {
        return new Softmax(this, axis);
    }

    public LogSoftmax logsoftmax(int axis) {
        return new LogSoftmax(this, axis);
    }
}
