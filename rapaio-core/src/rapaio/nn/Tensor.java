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
import rapaio.darray.Shape;
import rapaio.nn.tensors.Add;
import rapaio.nn.tensors.BatchVtm;
import rapaio.nn.tensors.CompareFalse;
import rapaio.nn.tensors.CompareTrue;
import rapaio.nn.tensors.Div;
import rapaio.nn.tensors.Dropout;
import rapaio.nn.tensors.Exp;
import rapaio.nn.tensors.Gather;
import rapaio.nn.tensors.Identity;
import rapaio.nn.tensors.Log;
import rapaio.nn.tensors.LogSoftmax;
import rapaio.nn.tensors.Max;
import rapaio.nn.tensors.Mean1d;
import rapaio.nn.tensors.MeanOn;
import rapaio.nn.tensors.Mul;
import rapaio.nn.tensors.Neg;
import rapaio.nn.tensors.Sigmoid;
import rapaio.nn.tensors.Softmax;
import rapaio.nn.tensors.Sqr;
import rapaio.nn.tensors.Sqrt;
import rapaio.nn.tensors.Standardize1d;
import rapaio.nn.tensors.StandardizeOn;
import rapaio.nn.tensors.Std1d;
import rapaio.nn.tensors.StdOn;
import rapaio.nn.tensors.Stretch;
import rapaio.nn.tensors.Sub;
import rapaio.nn.tensors.Sum;
import rapaio.nn.tensors.Sum1d;
import rapaio.nn.tensors.Tanh;

public abstract class Tensor {

    public record BackFunction(Tensor ref, Supplier<DArray<?>> fun) {
    }

    protected final TensorManager tm;
    protected String name;
    protected DArray<?> value;
    protected DArray<?> grad;
    protected boolean requiresGrad;
    protected final List<BackFunction> backFunctions = new ArrayList<>();

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

    public final List<BackFunction> backFunctions() {
        return backFunctions;
    }

    protected final void backEdge(Tensor ref, Supplier<DArray<?>> backFun) {
        backFunctions.add(new BackFunction(ref, backFun));
    }

    @Override
    public final String toString() {
        return String.format(
                "name:%s\nval:%sgrad:%s", name == null ? "null" : "(" + name + ")", value != null ? value.toString() : "\n", grad);
    }

    /// OPERATIONS

    public final Identity identity() {
        return new Identity(this);
    }

    public final Add add(Tensor other) {
        return new Add(this, other);
    }

    public final Add add(double value) {
        return new Add(this, tm.scalarTensor(value));
    }

    public final Sub sub(Tensor other) {
        return new Sub(this, other);
    }

    public final Sub sub(double value) {
        return new Sub(this, tm.scalarTensor(value));
    }

    public final Mul mul(Tensor other) {
        return new Mul(this, other);
    }

    public final Mul mul(double value) {
        return new Mul(this, tm.scalarTensor(value));
    }

    public final Div div(Tensor other) {
        return new Div(this, other);
    }

    public final Div div(double value) {
        return new Div(this, tm.scalarTensor(value));
    }

    public final Sum sum() {
        return new Sum(this);
    }

    public final Sum1d sum1d(int axis) {
        return new Sum1d(this, axis);
    }

    public final Mean1d mean1d(int axis) {
        return new Mean1d(this, axis);
    }

    public final MeanOn meanOn(Shape shape) {
        return new MeanOn(this, shape);
    }

    public final Std1d std1d(int axis) {
        return new Std1d(this, axis, 0, 1e-3, null);
    }

    public final Std1d std1d(int axis, int ddof) {
        return new Std1d(this, axis, ddof, 1e-3, null);
    }

    public final Std1d std1d(int axis, int ddof, Tensor mean) {
        return new Std1d(this, axis, ddof, 1e-3, mean);
    }

    public final Std1d std1d(int axis, int ddof, double epsilon, Tensor mean) {
        return new Std1d(this, axis, ddof, epsilon, mean);
    }

    public final StdOn stdOn(Shape shape) {
        return new StdOn(this, shape, 0, 1e-3, null);
    }

    public final StdOn stdOn(Shape shape, int ddof) {
        return new StdOn(this, shape, ddof, 1e-3, null);
    }

    public final StdOn stdOn(Shape shape, int ddof, Tensor mean) {
        return new StdOn(this, shape, ddof, 1e-3, mean);
    }

    public final StdOn stdOn(Shape shape, int ddof, double epsilon, Tensor mean) {
        return new StdOn(this, shape, ddof, epsilon, mean);
    }

    public final Standardize1d standardize1d(int axis, int ddof, double eps) {
        return new Standardize1d(this, axis, ddof, eps);
    }

    public final StandardizeOn standardizeOn(Shape shape, int ddof, double eps) {
        return new StandardizeOn(this, shape, ddof, eps);
    }

    public final Sqr sqr() {
        return new Sqr(this);
    }

    public final Sqrt sqrt() {
        return new Sqrt(this);
    }

    public final BatchVtm bvtm(Tensor other) {
        return new BatchVtm(this, other);
    }

    public final Dropout dropout(double p, Random random) {
        return dropout(p, random, false);
    }

    public final Dropout dropout(double p, Random random, boolean inplace) {
        return new Dropout(this, p, random, inplace);
    }

    public final Sigmoid sigmoid() {
        return new Sigmoid(this);
    }

    public final Tanh tanh() {
        return new Tanh(this);
    }

    public final Max max(double threshold) {
        return new Max(this, threshold);
    }

    public final Neg neg() {
        return new Neg(this);
    }

    public final Exp exp() {
        return new Exp(this);
    }

    public final Log log() {
        return new Log(this, -1);
    }

    public final Log log(double eps) {
        return new Log(this, eps);
    }

    public final Softmax softmax(int axis) {
        return new Softmax(this, axis);
    }

    public final LogSoftmax logsoftmax(int axis) {
        return new LogSoftmax(this, axis);
    }

    public final CompareTrue compareTrue(Compare cmp, double threshold) {
        return new CompareTrue(this, cmp, threshold);
    }

    public final CompareFalse compareFalse(Compare cmp, double threshold) {
        return new CompareFalse(this, cmp, threshold);
    }

    public final Gather gather(int axis, Tensor index) {
        return new Gather(this, axis, index);
    }

    public final Stretch stretch(int axis) {
        return new Stretch(this, axis);
    }
}
