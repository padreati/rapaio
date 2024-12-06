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

import java.util.List;
import java.util.Random;

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

public interface Tensor {

    String name();

    Tensor name(String name);

    DType<?> dtype();

    TensorManager tm();

    Shape shape();

    int rank();

    int size();

    int dim(int axis);

    DArray<?> value();

    void setValue(DArray<?> data);

    DArray<?> grad();

    void setGrad(DArray<?> grad);

    void addGrad(DArray<?> grad);

    void zeroGrad();

    boolean requiresGrad();

    Tensor requiresGrad(boolean requiresGrad);

    List<BackFun> backfuns();

    /// OPERATIONS

    default IdentityOp identity() {
        return new IdentityOp(this);
    }

    default AddOp add(Tensor other) {
        return new AddOp(this, other);
    }

    default AddOp add(double value) {
        return new AddOp(this, tm().scalarTensor(value));
    }

    default SubOp sub(Tensor other) {
        return new SubOp(this, other);
    }

    default SubOp sub(double value) {
        return new SubOp(this, tm().scalarTensor(value));
    }

    default MulOp mul(Tensor other) {
        return new MulOp(this, other);
    }

    default MulOp mul(double value) {
        return new MulOp(this, tm().scalarTensor(value));
    }

    default DivOp div(Tensor other) {
        return new DivOp(this, other);
    }

    default DivOp div(double value) {
        return new DivOp(this, tm().scalarTensor(value));
    }

    default SumOp sum() {
        return new SumOp(this);
    }

    default Sum1dOp sum1d(int axis) {
        return new Sum1dOp(this, axis);
    }

    default Mean1dOp mean1d(int axis) {
        return new Mean1dOp(this, axis);
    }

    default Std1dOp std1d(int axis) {
        return new Std1dOp(this, axis, 0, 1e-3, null);
    }

    default Std1dOp std1d(int axis, int ddof) {
        return new Std1dOp(this, axis, ddof, 1e-3, null);
    }

    default Std1dOp std1d(int axis, int ddof, Tensor mean) {
        return new Std1dOp(this, axis, ddof, 1e-3, mean);
    }

    default Std1dOp std1d(int axis, int ddof, double epsilon, Tensor mean) {
        return new Std1dOp(this, axis, ddof, epsilon, mean);
    }

    default SqrOp sqr() {
        return new SqrOp(this);
    }

    default SqrtOp sqrt() {
        return new SqrtOp(this);
    }

    default BatchVtmOp bvtm(Tensor other) {
        return new BatchVtmOp(this, other);
    }

    default DropoutOp dropout(double p, Random random) {
        return dropout(p, random, false);
    }

    default DropoutOp dropout(double p, Random random, boolean inplace) {
        return new DropoutOp(this, p, random, inplace);
    }

    default SigmoidOp sigmoid() {
        return new SigmoidOp(this);
    }

    default TanhOp tanh() {
        return new TanhOp(this);
    }

    default Max max(double threshold) {
        return new Max(this, threshold);
    }

    default NegOp neg() {
        return new NegOp(this);
    }

    default ExpOp exp() {
        return new ExpOp(this);
    }

    default LogOp log() {
        return new LogOp(this, -1);
    }

    default LogOp log(double eps) {
        return new LogOp(this, eps);
    }

    default SoftmaxOp softmax(int axis) {
        return new SoftmaxOp(this, axis);
    }

    default LogSoftmaxOp logsoftmax(int axis) {
        return new LogSoftmaxOp(this, axis);
    }

    default CompareTrueOp compareTrue(Compare cmp, double threshold) {
        return new CompareTrueOp(this, cmp, threshold);
    }

    default CompareFalseOp compareFalse(Compare cmp, double threshold) {
        return new CompareFalseOp(this, cmp, threshold);
    }
}
