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
import java.util.function.Consumer;
import java.util.function.Supplier;

import rapaio.darray.Compare;
import rapaio.darray.DArray;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.nn.tensors.BatchVtm;
import rapaio.nn.tensors.CompareFalse;
import rapaio.nn.tensors.CompareTrue;
import rapaio.nn.tensors.Dropout;
import rapaio.nn.tensors.Gather;
import rapaio.nn.tensors.Identity;
import rapaio.nn.tensors.LogSoftmax;
import rapaio.nn.tensors.Softmax;
import rapaio.nn.tensors.Standardize1d;
import rapaio.nn.tensors.StandardizeOn;
import rapaio.nn.tensors.Std1d;
import rapaio.nn.tensors.StdOn;
import rapaio.nn.tensors.Stretch;
import rapaio.nn.tensors.binary.Add;
import rapaio.nn.tensors.binary.Div;
import rapaio.nn.tensors.binary.Mul;
import rapaio.nn.tensors.binary.Sub;
import rapaio.nn.tensors.reduce.Max;
import rapaio.nn.tensors.reduce.Mean1d;
import rapaio.nn.tensors.reduce.MeanOn;
import rapaio.nn.tensors.reduce.Sum;
import rapaio.nn.tensors.reduce.Sum1d;
import rapaio.nn.tensors.shape.Narrow;
import rapaio.nn.tensors.shape.Reshape;
import rapaio.nn.tensors.unary.Exp;
import rapaio.nn.tensors.unary.Log;
import rapaio.nn.tensors.unary.Neg;
import rapaio.nn.tensors.unary.Sigmoid;
import rapaio.nn.tensors.unary.Sqr;
import rapaio.nn.tensors.unary.Sqrt;
import rapaio.nn.tensors.unary.Tanh;

/**
 * Defines a tensor which is an multidimensional array with gradient computation.
 * <p>
 * A tensor has a value and a gradient, both of them being DArrays. The values for a tensor
 * are created at initialization and the gradient values are computed during the backpropagation
 * phase.
 * <p>
 * For that purpose when a tensor can be seen as a computational node, and it also contains
 * traces in the computational graph. Those traces consist of backpropagation functions
 * which are called if needed to compute and collect gradients.
 * <p>
 * A tensor can require its gradient to be computed. For that purpose it has to call
 * {@link #requiresGrad(boolean)} with {@code true} value. However, it is possible that even
 * if a tensor does not explicitly request gradient computation, the gradient still is computed.
 * This happens if the tensor is on the back path from the last computation node towards a tensor
 * which explicitly requested computation. For example if we have {@code a=function(x)}, {@code b=function(a)},
 * and {@code c=function{b}}. The that example if {@code a} requested explicit gradient computation,
 * and the last computation node is {@code c}, than the node {@code b} will have also computed gradient,
 * since that is required to compute the gradient of {@code a}.
 * <p>
 * By default a tensor does not request explicit gradient computation.
 * <p>
 * A tensor requires a tensor manager instance {@link TensorManager} in order to perform its computations.
 * The tensor manager is used to create new DArrays and to provide data types, execution pools and other things.
 */
public abstract class Tensor {

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

    /**
     * @return name of the tensor, for easy identification
     */
    public final String name() {
        return name;
    }

    /**
     * Sets tensor's name
     *
     * @param name name of the tensor
     * @return tensor instance for fluent calls
     */
    public final Tensor name(String name) {
        this.name = name;
        return this;
    }

    /**
     * @return tensor manager instance
     */
    public TensorManager tm() {
        return tm;
    }

    /**
     * @return Shape of the tensor's value. The gradient has to has the same shape when computed.
     */
    public final Shape shape() {
        return value.shape();
    }

    /**
     * @return number of dimension of the tensor
     */
    public final int rank() {
        return value.rank();
    }

    /**
     * @return number of elements from a tensor
     */
    public final int size() {
        return value.size();
    }

    /**
     * Size of the dimension indexed by {@code axis}
     *
     * @param axis the index of the dimension
     * @return size of the given dimension
     */
    public int dim(int axis) {
        return value.dim(axis);
    }

    /**
     * Tensor value. The value of a tensor is an {@link DArray} and is usually computed when the
     * tensor is created. This is the usual behavior for the tensors which are results of tensor
     * operations.
     *
     * @return tensor's value
     */
    public final DArray<?> value() {
        return value;
    }

    /**
     * Sets the value of a tensor
     *
     * @param value the new value of the tensor
     */
    public final void setValue(DArray<?> value) {
        this.value = value;
    }

    /**
     * Computed gradient of a tensor. The gradient can be computed or not, depending on various conditions.
     * If the gradient is computed, it has to has the same shape as the tensor value.
     *
     * @return computed tensor's gradient, or null if not computed
     */
    public final DArray<?> grad() {
        return grad;
    }

    /**
     * Sets the computer gradient of the tensor.
     *
     * @param grad new value of the gradient
     */
    public final void setGrad(DArray<?> grad) {
        this.grad = grad;
    }

    /**
     * Adds the given value to the gradient. Since a tensor can be used in more than one computation,
     * its gradient can receive updates from more than one place. This method can be used to add values
     * to a gradient.
     *
     * @param grad added value to the current gradient
     */
    public final void addGrad(DArray<?> grad) {
        if (this.grad == null) {
            this.grad = grad;
        } else {
            this.grad.add_(grad);
        }
    }

    /**
     * Deletes the current gradient, if any. This method does not set a gradient which is an array
     * filled with zero, but sets the gradient value to null, for performance reasons.
     */
    public final void zeroGrad() {
        this.grad = null;
    }

    /**
     * @return true if gradient computation is required, false otherwise
     */
    public final boolean requiresGrad() {
        return requiresGrad;
    }

    /**
     * Explicitly set if gradient computation is required.
     *
     * @param requiresGrad true if gradient computation is explicitly requested, false otherwise
     * @return tensor instance for fluent api calling
     */
    public final Tensor requiresGrad(boolean requiresGrad) {
        this.requiresGrad = requiresGrad;
        return this;
    }

    /**
     * @return list of backpropagation functions
     */
    public final List<BackFunction> backFunctions() {
        return backFunctions;
    }

    protected final void backEdge(Tensor ref, Supplier<DArray<?>> addFun) {
        backFunctions.add(BackFunction.of(ref, addFun));
    }

    protected final void backEdge(Tensor ref, Consumer<DArray<?>> updateFun) {
        backFunctions.add(BackFunction.of(ref, updateFun));
    }

    @Override
    public final String toString() {
        return String.format(
                "name:%s\nval:%sgrad:%s", name == null ? "null" : "(" + name + ")", value != null ? value.toString() : "\n", grad);
    }

    // OPERATIONS

    /**
     * Creates a new tensor which is a copy of the current tensor. Gradients will be copied also.
     *
     * @return new tensor instance copy
     */
    public final Identity identity() {
        return new Identity(this);
    }

    public final Reshape reshape(Shape shape) {
        return new Reshape(this, shape, Order.defaultOrder());
    }

    public final Reshape reshape(Shape shape, Order askOrder) {
        return new Reshape(this, shape, askOrder);
    }

    public final Narrow narrow(int axis, int start, int end) {
        return new Narrow(this, axis, start, end);
    }

    public final List<Tensor> split(int axis, int... indices) {
        List<Tensor> tensors = new ArrayList<>();
        for (int i = 0; i < indices.length; i++) {
            tensors.add(this.narrow(axis, indices[i], i == indices.length - 1 ? this.dim(axis) : indices[i + 1]));
        }
        return tensors;
    }

    /**
     * Adds a tensor with the current tensor. Shapes allows broadcasting.
     *
     * @param other tensor to be added with the current one
     * @return new tensor which is the sum
     */
    public final Add add(Tensor other) {
        return new Add(this, other);
    }

    /**
     * Adds a scalar value with the current tensor.
     *
     * @param value value to be added
     * @return new tensor which contains the sum
     */
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
