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

import rapaio.math.nn.operations.OpAdd;
import rapaio.math.nn.operations.OpAxisMean;
import rapaio.math.nn.operations.OpAxisStd;
import rapaio.math.nn.operations.OpAxisSum;
import rapaio.math.nn.operations.OpBatchVtm;
import rapaio.math.nn.operations.OpDiv;
import rapaio.math.nn.operations.OpDropout;
import rapaio.math.nn.operations.OpIdentity;
import rapaio.math.nn.operations.OpLog;
import rapaio.math.nn.operations.OpLogSoftmax;
import rapaio.math.nn.operations.OpMax;
import rapaio.math.nn.operations.OpMul;
import rapaio.math.nn.operations.OpNeg;
import rapaio.math.nn.operations.OpSigmoid;
import rapaio.math.nn.operations.OpSoftmax;
import rapaio.math.nn.operations.OpSqr;
import rapaio.math.nn.operations.OpSqrt;
import rapaio.math.nn.operations.OpSub;
import rapaio.math.nn.operations.OpSum;
import rapaio.math.nn.operations.OpTanh;
import rapaio.math.tensor.DType;
import rapaio.math.tensor.Tensor;

public abstract class Node {

    protected String name;
    protected DType<?> dtype;
    private Tensor<?> value;
    private Tensor<?> grad;
    protected boolean requiresGrad;
    private final List<BackFun> backfuns = new ArrayList<>();

    public final String name() {
        return name;
    }

    public final Node name(String name) {
        this.name = name;
        return this;
    }

    public final DType<?> dtype() {
        return dtype;
    }

    public final Tensor<?> value() {
        return value;
    }

    public final void setValue(Tensor<?> data) {
        value = data;
    }

    public final Tensor<?> grad() {
        return grad;
    }

    public final void setGrad(Tensor<?> grad) {
        this.grad = grad;
    }

    public final void addGrad(Tensor<?> grad) {
        if (this.grad == null) {
            this.grad = grad;
        } else {
            this.grad = this.grad.add(grad);
        }
    }

    public final void subGrad(Tensor<?> grad) {
        if (this.grad == null) {
            this.grad = grad.neg();
        } else {
            this.grad = this.grad.sub(grad);
        }
    }

    public final void zeroGrad() {
        this.grad = null;
    }

    public final boolean requiresGrad() {
        return requiresGrad;
    }

    public final Node requiresGrad(boolean requiresGrad) {
        this.requiresGrad = requiresGrad;
        return this;
    }

    public final List<BackFun> backfuns() {
        return backfuns;
    }

    protected void backEdge(Node ref, Supplier<Tensor<?>> backFun) {
        backfuns.add(BackFun.of(ref, backFun));
    }

    @Override
    public final String toString() {
        return String.format("name:%s\nval:%sgrad:%s", name == null ? "null" : "(" + name + ")", value != null ? value.toString() : "\n",
                grad);
    }

    /// OPERATIONS


    public OpIdentity identity() {
        return new OpIdentity(this);
    }

    public OpAdd add(Node other) {
        return new OpAdd(this, other);
    }

    public OpAdd add(double value) {
        return new OpAdd(this, Autograd.scalar(dtype, value));
    }

    public OpSub sub(Node other) {
        return new OpSub(this, other);
    }

    public OpSub sub(double value) {
        return new OpSub(this, Autograd.scalar(dtype, value));
    }

    public OpMul mul(Node other) {
        return new OpMul(this, other);
    }

    public OpMul mul(double value) {
        return new OpMul(this, Autograd.scalar(dtype, value));
    }

    public Node div(Node other) {
        return new OpDiv(this, other);
    }

    public Node div(double value) {
        return new OpDiv(this, Autograd.scalar(dtype, value));
    }

    public Node sum() {
        return new OpSum(this);
    }

    public Node axisSum(int axis) {
        return new OpAxisSum(this, axis);
    }

    public Node axisMean(int axis) {
        return new OpAxisMean(this, axis);
    }

    public OpAxisStd axisStd(int axis) {
        return new OpAxisStd(this, axis, 0, null);
    }

    public OpAxisStd axisStd(int axis, int ddof) {
        return new OpAxisStd(this, axis, ddof, null);
    }

    public OpAxisStd axisStd(int axis, int ddof, Node mean) {
        return new OpAxisStd(this, axis, ddof, mean);
    }

    public Node sqr() {
        return new OpSqr(this);
    }

    public OpSqrt sqrt() {
        return new OpSqrt(this);
    }

    public Node bvtm(Node other) {
        return new OpBatchVtm(this, other);
    }

    public Node dropout(double p, Random random) {
        return dropout(p, random, false);
    }

    public Node dropout(double p, Random random, boolean inplace) {
        return new OpDropout(this, p, random, inplace);
    }

    public Node sigmoid() {
        return new OpSigmoid(this);
    }

    public Node tanh() {
        return new OpTanh(this);
    }

    public Node max(double threshold) {
        return new OpMax(this, threshold);
    }

    public Node neg() {
        return new OpNeg(this);
    }

    public Node log() {
        return new OpLog(this);
    }

    public Node softmax(int axis) {
        return new OpSoftmax(this, axis);
    }

    public Node logsoftmax(int axis) {
        return new OpLogSoftmax(this, axis);
    }
}
