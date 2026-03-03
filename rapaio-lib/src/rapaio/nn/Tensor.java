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
import rapaio.nn.tensors.BatchVtmNode;
import rapaio.nn.tensors.CompareFalseNode;
import rapaio.nn.tensors.CompareTrueNode;
import rapaio.nn.tensors.Conv1dNode;
import rapaio.nn.tensors.Conv2dNode;
import rapaio.nn.tensors.DropoutNode;
import rapaio.nn.tensors.GatherNode;
import rapaio.nn.tensors.IdentityNode;
import rapaio.nn.tensors.LogSoftmaxNode;
import rapaio.nn.tensors.SoftmaxNode;
import rapaio.nn.tensors.Standardize1dNode;
import rapaio.nn.tensors.StandardizeOnNode;
import rapaio.nn.tensors.Std1dNode;
import rapaio.nn.tensors.StdOnNode;
import rapaio.nn.tensors.StretchNode;
import rapaio.nn.tensors.binary.AddNode;
import rapaio.nn.tensors.binary.DivNode;
import rapaio.nn.tensors.binary.MulNode;
import rapaio.nn.tensors.binary.SubNode;
import rapaio.nn.tensors.reduce.MaxNode;
import rapaio.nn.tensors.reduce.Mean1dNode;
import rapaio.nn.tensors.reduce.MeanOnNode;
import rapaio.nn.tensors.reduce.SumNode;
import rapaio.nn.tensors.reduce.Sum1dNode;
import rapaio.nn.tensors.shape.NarrowNode;
import rapaio.nn.tensors.shape.ReshapeNode;
import rapaio.nn.tensors.unary.ExpNode;
import rapaio.nn.tensors.unary.LogNode;
import rapaio.nn.tensors.unary.NegNode;
import rapaio.nn.tensors.unary.SigmoidNode;
import rapaio.nn.tensors.unary.SqrNode;
import rapaio.nn.tensors.unary.SqrtNode;
import rapaio.nn.tensors.unary.TanhNode;

/**
 * Defines a tensor which is a multidimensional array with gradient computation.
 * <p>
 * A tensor has a value and a gradient, both of them being of type DArray. The values for a tensor
 * are created at initialization and the gradient values are computed during the backpropagation
 * phase.
 * <p>
 * Thus a tensor can be seen as a computational node, and it also contains traces in the computational graph.
 * Those traces consist of backpropagation functions which are called if needed to compute and collect gradients.
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
    public final IdentityNode identity() {
        return new IdentityNode(this);
    }

    public final ReshapeNode reshape(Shape shape) {
        return new ReshapeNode(this, shape, Order.defaultOrder());
    }

    public final ReshapeNode reshape(Shape shape, Order askOrder) {
        return new ReshapeNode(this, shape, askOrder);
    }

    public final NarrowNode narrow(int axis, int start, int end) {
        return new NarrowNode(this, axis, start, end);
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
    public final AddNode add(Tensor other) {
        return new AddNode(this, other);
    }

    /**
     * Adds a scalar value with the current tensor.
     *
     * @param value value to be added
     * @return new tensor which contains the sum
     */
    public final AddNode add(double value) {
        return new AddNode(this, tm.scalarTensor(value));
    }

    public final SubNode sub(Tensor other) {
        return new SubNode(this, other);
    }

    public final SubNode sub(double value) {
        return new SubNode(this, tm.scalarTensor(value));
    }

    public final MulNode mul(Tensor other) {
        return new MulNode(this, other);
    }

    public final MulNode mul(double value) {
        return new MulNode(this, tm.scalarTensor(value));
    }

    public final DivNode div(Tensor other) {
        return new DivNode(this, other);
    }

    public final DivNode div(double value) {
        return new DivNode(this, tm.scalarTensor(value));
    }

    public final SumNode sum() {
        return new SumNode(this);
    }

    public final Sum1dNode sum1d(int axis) {
        return new Sum1dNode(this, axis);
    }

    public final Mean1dNode mean1d(int axis) {
        return new Mean1dNode(this, axis);
    }

    public final MeanOnNode meanOn(Shape shape) {
        return new MeanOnNode(this, shape);
    }

    public final Std1dNode std1d(int axis) {
        return new Std1dNode(this, axis, 0, 1e-3, null);
    }

    public final Std1dNode std1d(int axis, int ddof) {
        return new Std1dNode(this, axis, ddof, 1e-3, null);
    }

    public final Std1dNode std1d(int axis, int ddof, Tensor mean) {
        return new Std1dNode(this, axis, ddof, 1e-3, mean);
    }

    public final Std1dNode std1d(int axis, int ddof, double epsilon, Tensor mean) {
        return new Std1dNode(this, axis, ddof, epsilon, mean);
    }

    public final StdOnNode stdOn(Shape shape) {
        return new StdOnNode(this, shape, 0, 1e-3, null);
    }

    public final StdOnNode stdOn(Shape shape, int ddof) {
        return new StdOnNode(this, shape, ddof, 1e-3, null);
    }

    public final StdOnNode stdOn(Shape shape, int ddof, Tensor mean) {
        return new StdOnNode(this, shape, ddof, 1e-3, mean);
    }

    public final StdOnNode stdOn(Shape shape, int ddof, double epsilon, Tensor mean) {
        return new StdOnNode(this, shape, ddof, epsilon, mean);
    }

    public final Standardize1dNode standardize1d(int axis, int ddof, double eps) {
        return new Standardize1dNode(this, axis, ddof, eps);
    }

    public final StandardizeOnNode standardizeOn(Shape shape, int ddof, double eps) {
        return new StandardizeOnNode(this, shape, ddof, eps);
    }

    public final SqrNode sqr() {
        return new SqrNode(this);
    }

    public final SqrtNode sqrt() {
        return new SqrtNode(this);
    }

    public final BatchVtmNode bvtm(Tensor other) {
        return new BatchVtmNode(this, other);
    }

    public final DropoutNode dropout(double p, Random random) {
        return dropout(p, random, false);
    }

    public final DropoutNode dropout(double p, Random random, boolean inplace) {
        return new DropoutNode(this, p, random, inplace);
    }

    public final SigmoidNode sigmoid() {
        return new SigmoidNode(this);
    }

    public final TanhNode tanh() {
        return new TanhNode(this);
    }

    public final MaxNode max(double threshold) {
        return new MaxNode(this, threshold);
    }

    public final NegNode neg() {
        return new NegNode(this);
    }

    public final ExpNode exp() {
        return new ExpNode(this);
    }

    public final LogNode log() {
        return new LogNode(this, -1);
    }

    public final LogNode log(double eps) {
        return new LogNode(this, eps);
    }

    public final SoftmaxNode softmax(int axis) {
        return new SoftmaxNode(this, axis);
    }

    public final LogSoftmaxNode logsoftmax(int axis) {
        return new LogSoftmaxNode(this, axis);
    }

    public final CompareTrueNode compareTrue(Compare cmp, double threshold) {
        return new CompareTrueNode(this, cmp, threshold);
    }

    public final CompareFalseNode compareFalse(Compare cmp, double threshold) {
        return new CompareFalseNode(this, cmp, threshold);
    }

    public final GatherNode gather(int axis, Tensor index) {
        return new GatherNode(this, axis, index);
    }

    public final StretchNode stretch(int axis) {
        return new StretchNode(this, axis);
    }

    public final Conv1dNode conv1d(Tensor weight, Tensor bias, int padding, int stride, int dilation, int groups) {
        return new Conv1dNode(this, weight, bias, padding, stride, dilation, groups);
    }

    public final Conv2dNode conv2d(Tensor weight, Tensor bias, int padding, int stride, int dilation, int groups) {
        return new Conv2dNode(this, weight, bias, padding, stride, dilation, groups);
    }
}
