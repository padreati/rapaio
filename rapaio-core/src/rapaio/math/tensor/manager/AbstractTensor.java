/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.math.tensor.manager;

import rapaio.math.tensor.Order;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.operator.TensorUnaryOp;

public abstract class AbstractTensor<N extends Number> implements Tensor<N> {

    @Override
    public final Tensor<N> op(TensorUnaryOp op) {
        return op(op, Order.defaultOrder());
    }

    @Override
    public final Tensor<N> op(TensorUnaryOp op, Order order) {
        return copy(order).op_(op);
    }

    @Override
    public abstract Tensor<N> op_(TensorUnaryOp op);

    @Override
    public final Tensor<N> rint() {
        return op(TensorUnaryOp.RINT);
    }

    @Override
    public final Tensor<N> rint(Order order) {
        return op(TensorUnaryOp.RINT, order);
    }

    @Override
    public final Tensor<N> rint_() {
        return op_(TensorUnaryOp.RINT);
    }

    @Override
    public final Tensor<N> ceil() {
        return op(TensorUnaryOp.CEIL);
    }

    @Override
    public final Tensor<N> ceil(Order order) {
        return op(TensorUnaryOp.CEIL, order);
    }

    @Override
    public final Tensor<N> ceil_() {
        return op_(TensorUnaryOp.CEIL);
    }

    @Override
    public final Tensor<N> floor() {
        return op(TensorUnaryOp.FLOOR);
    }

    @Override
    public final Tensor<N> floor(Order order) {
        return op(TensorUnaryOp.FLOOR, order);
    }

    @Override
    public final Tensor<N> floor_() {
        return op_(TensorUnaryOp.FLOOR);
    }

    @Override
    public final Tensor<N> abs() {
        return op(TensorUnaryOp.ABS);
    }

    @Override
    public final Tensor<N> abs(Order order) {
        return op(TensorUnaryOp.ABS, order);
    }

    @Override
    public final Tensor<N> abs_() {
        return op_(TensorUnaryOp.ABS);
    }

    @Override
    public final Tensor<N> negate() {
        return op(TensorUnaryOp.NEG);
    }

    @Override
    public final Tensor<N> negate(Order order) {
        return op(TensorUnaryOp.NEG, order);
    }

    @Override
    public final Tensor<N> negate_() {
        return op_(TensorUnaryOp.NEG);
    }

    @Override
    public final Tensor<N> log() {
        return op(TensorUnaryOp.LOG);
    }

    @Override
    public final Tensor<N> log(Order order) {
        return op(TensorUnaryOp.LOG, order);
    }

    @Override
    public final Tensor<N> log_() {
        return op_(TensorUnaryOp.LOG);
    }

    @Override
    public final Tensor<N> log1p() {
        return op(TensorUnaryOp.LOG1P);
    }

    @Override
    public final Tensor<N> log1p(Order order) {
        return op(TensorUnaryOp.LOG1P, order);
    }

    @Override
    public final Tensor<N> log1p_() {
        return op_(TensorUnaryOp.LOG1P);
    }

    @Override
    public final Tensor<N> exp() {
        return op(TensorUnaryOp.EXP);
    }

    @Override
    public final Tensor<N> exp(Order order) {
        return op(TensorUnaryOp.EXP, order);
    }

    @Override
    public final Tensor<N> exp_() {
        return op_(TensorUnaryOp.EXP);
    }

    @Override
    public final Tensor<N> expm1() {
        return op(TensorUnaryOp.EXPM1);
    }

    @Override
    public final Tensor<N> expm1(Order order) {
        return op(TensorUnaryOp.EXPM1, order);
    }

    @Override
    public final Tensor<N> expm1_() {
        return op_(TensorUnaryOp.EXPM1);
    }

    @Override
    public final Tensor<N> sin() {
        return op(TensorUnaryOp.SIN);
    }

    @Override
    public final Tensor<N> sin(Order order) {
        return op(TensorUnaryOp.SIN, order);
    }

    @Override
    public final Tensor<N> sin_() {
        return op_(TensorUnaryOp.SIN);
    }

    @Override
    public final Tensor<N> asin() {
        return op(TensorUnaryOp.ASIN);
    }

    @Override
    public final Tensor<N> asin(Order order) {
        return op(TensorUnaryOp.ASIN, order);
    }

    @Override
    public final Tensor<N> asin_() {
        return op_(TensorUnaryOp.ASIN);
    }

    @Override
    public final Tensor<N> sinh() {
        return op(TensorUnaryOp.SINH);
    }

    @Override
    public final Tensor<N> sinh(Order order) {
        return op(TensorUnaryOp.SINH, order);
    }

    @Override
    public final Tensor<N> sinh_() {
        return op_(TensorUnaryOp.SINH);
    }

    @Override
    public final Tensor<N> cos() {
        return op(TensorUnaryOp.COS);
    }

    @Override
    public final Tensor<N> cos(Order order) {
        return op(TensorUnaryOp.COS, order);
    }

    @Override
    public final Tensor<N> cos_() {
        return op_(TensorUnaryOp.COS);
    }

    @Override
    public final Tensor<N> acos() {
        return op(TensorUnaryOp.ACOS);
    }

    @Override
    public final Tensor<N> acos(Order order) {
        return op(TensorUnaryOp.ACOS, order);
    }

    @Override
    public final Tensor<N> acos_() {
        return op_(TensorUnaryOp.ACOS);
    }

    @Override
    public final Tensor<N> cosh() {
        return op(TensorUnaryOp.COSH);
    }

    @Override
    public final Tensor<N> cosh(Order order) {
        return op(TensorUnaryOp.COSH, order);
    }

    @Override
    public final Tensor<N> cosh_() {
        return op_(TensorUnaryOp.COSH);
    }

    @Override
    public final Tensor<N> tan() {
        return op(TensorUnaryOp.TAN);
    }

    @Override
    public final Tensor<N> tan(Order order) {
        return op(TensorUnaryOp.TAN, order);
    }

    @Override
    public final Tensor<N> tan_() {
        return op_(TensorUnaryOp.TAN);
    }

    @Override
    public final Tensor<N> atan() {
        return op(TensorUnaryOp.ATAN);
    }

    @Override
    public final Tensor<N> atan(Order order) {
        return op(TensorUnaryOp.ATAN, order);
    }

    @Override
    public final Tensor<N> atan_() {
        return op_(TensorUnaryOp.ATAN);
    }

    @Override
    public final Tensor<N> tanh() {
        return op(TensorUnaryOp.TANH);
    }

    @Override
    public final Tensor<N> tanh(Order order) {
        return op(TensorUnaryOp.TANH, order);
    }

    @Override
    public final Tensor<N> tanh_() {
        return op_(TensorUnaryOp.TANH);
    }

    @Override
    public final Tensor<N> sqr() {
        return op(TensorUnaryOp.SQR);
    }

    @Override
    public final Tensor<N> sqr(Order order) {
        return op(TensorUnaryOp.SQR, order);
    }

    @Override
    public final Tensor<N> sqr_() {
        return op_(TensorUnaryOp.SQR);
    }

    @Override
    public final Tensor<N> sqrt() {
        return op(TensorUnaryOp.SQRT);
    }

    @Override
    public final Tensor<N> sqrt(Order order) {
        return op(TensorUnaryOp.SQRT, order);
    }

    @Override
    public final Tensor<N> sqrt_() {
        return op_(TensorUnaryOp.SQRT);
    }
}
