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

package rapaio.darray.operator;

import rapaio.darray.Compare;
import rapaio.darray.DType;
import rapaio.darray.operator.impl.BinaryOpAdd;
import rapaio.darray.operator.impl.BinaryOpDiv;
import rapaio.darray.operator.impl.BinaryOpMax;
import rapaio.darray.operator.impl.BinaryOpMin;
import rapaio.darray.operator.impl.BinaryOpMul;
import rapaio.darray.operator.impl.BinaryOpSub;
import rapaio.darray.operator.impl.ReduceOpMax;
import rapaio.darray.operator.impl.ReduceOpMean;
import rapaio.darray.operator.impl.ReduceOpMin;
import rapaio.darray.operator.impl.ReduceOpNanMax;
import rapaio.darray.operator.impl.ReduceOpNanMean;
import rapaio.darray.operator.impl.ReduceOpNanMin;
import rapaio.darray.operator.impl.ReduceOpNanProd;
import rapaio.darray.operator.impl.ReduceOpNanSum;
import rapaio.darray.operator.impl.ReduceOpProd;
import rapaio.darray.operator.impl.ReduceOpSum;
import rapaio.darray.operator.impl.ReduceOpVarc;
import rapaio.darray.operator.unary.UnaryOpAbs;
import rapaio.darray.operator.unary.UnaryOpAcos;
import rapaio.darray.operator.unary.UnaryOpAsin;
import rapaio.darray.operator.unary.UnaryOpAtan;
import rapaio.darray.operator.unary.UnaryOpCeil;
import rapaio.darray.operator.unary.UnaryOpClamp;
import rapaio.darray.operator.unary.UnaryOpCompareMask;
import rapaio.darray.operator.unary.UnaryOpCos;
import rapaio.darray.operator.unary.UnaryOpCosh;
import rapaio.darray.operator.unary.UnaryOpExp;
import rapaio.darray.operator.unary.UnaryOpExpm1;
import rapaio.darray.operator.unary.UnaryOpFill;
import rapaio.darray.operator.unary.UnaryOpFillNan;
import rapaio.darray.operator.unary.UnaryOpFloor;
import rapaio.darray.operator.unary.UnaryOpLog;
import rapaio.darray.operator.unary.UnaryOpLog1p;
import rapaio.darray.operator.unary.UnaryOpLogSoftmax;
import rapaio.darray.operator.unary.UnaryOpNanToNum;
import rapaio.darray.operator.unary.UnaryOpNeg;
import rapaio.darray.operator.unary.UnaryOpPow;
import rapaio.darray.operator.unary.UnaryOpRint;
import rapaio.darray.operator.unary.UnaryOpSigmoid;
import rapaio.darray.operator.unary.UnaryOpSin;
import rapaio.darray.operator.unary.UnaryOpSinh;
import rapaio.darray.operator.unary.UnaryOpSoftmax;
import rapaio.darray.operator.unary.UnaryOpSqr;
import rapaio.darray.operator.unary.UnaryOpSqrt;
import rapaio.darray.operator.unary.UnaryOpTan;
import rapaio.darray.operator.unary.UnaryOpTanh;

public final class DArrayOp {

    private static final UnaryOpAbs ABS = new UnaryOpAbs();
    private static final UnaryOpNeg NEG = new UnaryOpNeg();

    private static final UnaryOpExp EXP = new UnaryOpExp();
    private static final UnaryOpExpm1 EXPM1 = new UnaryOpExpm1();
    private static final UnaryOpLog LOG = new UnaryOpLog();
    private static final UnaryOpLog1p LOG1P = new UnaryOpLog1p();

    private static final UnaryOpCeil CEIL = new UnaryOpCeil();
    private static final UnaryOpFloor FLOOR = new UnaryOpFloor();
    private static final UnaryOpRint RINT = new UnaryOpRint();

    private static final UnaryOpSin SIN = new UnaryOpSin();
    private static final UnaryOpAsin ASIN = new UnaryOpAsin();
    private static final UnaryOpSinh SINH = new UnaryOpSinh();
    private static final UnaryOpCos COS = new UnaryOpCos();
    private static final UnaryOpAcos ACOS = new UnaryOpAcos();
    private static final UnaryOpCosh COSH = new UnaryOpCosh();
    private static final UnaryOpTan TAN = new UnaryOpTan();
    private static final UnaryOpAtan ATAN = new UnaryOpAtan();
    private static final UnaryOpTanh TANH = new UnaryOpTanh();

    private static final UnaryOpSqr SQR = new UnaryOpSqr();
    private static final UnaryOpSqrt SQRT = new UnaryOpSqrt();

    private static final UnaryOpSigmoid SIGMOID = new UnaryOpSigmoid();
    private static final UnaryOpSoftmax SOFTMAX = new UnaryOpSoftmax();
    private static final UnaryOpLogSoftmax LOG_SOFTMAX = new UnaryOpLogSoftmax();

    private static final BinaryOpAdd ADD = new BinaryOpAdd();
    private static final BinaryOpSub SUB = new BinaryOpSub();
    private static final BinaryOpMul MUL = new BinaryOpMul();
    private static final BinaryOpDiv DIV = new BinaryOpDiv();
    private static final BinaryOpMin MIN = new BinaryOpMin();
    private static final BinaryOpMax MAX = new BinaryOpMax();

    private static final ReduceOpSum REDUCE_SUM = new ReduceOpSum();
    private static final ReduceOpProd REDUCE_PROD = new ReduceOpProd();
    private static final ReduceOpMin REDUCE_MIN = new ReduceOpMin();
    private static final ReduceOpMax REDUCE_MAX = new ReduceOpMax();
    private static final ReduceOpMean REDUCE_MEAN = new ReduceOpMean();

    private static final ReduceOpNanSum REDUCE_NAN_SUM = new ReduceOpNanSum();
    private static final ReduceOpNanProd REDUCE_NAN_PROD = new ReduceOpNanProd();
    private static final ReduceOpNanMin REDUCE_NAN_MIN = new ReduceOpNanMin();
    private static final ReduceOpNanMax REDUCE_NAN_MAX = new ReduceOpNanMax();
    private static final ReduceOpNanMean REDUCE_NAN_MEAN = new ReduceOpNanMean();


    public static UnaryOpRint unaryRint() {
        return RINT;
    }

    public static UnaryOpCeil unaryCeil() {
        return CEIL;
    }

    public static UnaryOpFloor unaryFloor() {
        return FLOOR;
    }


    public static UnaryOpAbs unaryAbs() {
        return ABS;
    }

    public static UnaryOpNeg unaryNeg() {
        return NEG;
    }

    public static UnaryOpLog unaryLog() {
        return LOG;
    }

    public static UnaryOpLog1p unaryLog1p() {
        return LOG1P;
    }

    public static UnaryOpExp unaryExp() {
        return EXP;
    }

    public static UnaryOpExpm1 unaryExpm1() {
        return EXPM1;
    }

    public static UnaryOpSin unarySin() {
        return SIN;
    }

    public static UnaryOpAsin unaryAsin() {
        return ASIN;
    }

    public static UnaryOpSinh unarySinh() {
        return SINH;
    }

    public static UnaryOpCos unaryCos() {
        return COS;
    }

    public static UnaryOpAcos unaryAcos() {
        return ACOS;
    }

    public static UnaryOpCosh unaryCosh() {
        return COSH;
    }

    public static UnaryOpTan unaryTan() {
        return TAN;
    }

    public static UnaryOpAtan unaryAtan() {
        return ATAN;
    }

    public static UnaryOpTanh unaryTanh() {
        return TANH;
    }

    public static UnaryOpSqr unarySqr() {
        return SQR;
    }

    public static UnaryOpSqrt unarySqrt() {
        return SQRT;
    }

    public static UnaryOpPow unaryPow(double pow) {
        return new UnaryOpPow(pow);
    }

    public static <N extends Number> UnaryOpClamp<N> unaryClamp(DType<N> dtype, N min, N max) {
        return new UnaryOpClamp<>(dtype, min, max);
    }

    public static <N extends Number> UnaryOpFill<N> unaryFill(N fill) {
        return new UnaryOpFill<>(fill);
    }

    public static <N extends Number> UnaryOpFillNan<N> unaryFillNan(N fill) {
        return new UnaryOpFillNan<>(fill);
    }

    public static <N extends Number> UnaryOpNanToNum<N> unaryNanToNum(N nan, N ninf, N pinf) {
        return new UnaryOpNanToNum<>(nan, ninf, pinf);
    }

    public static <N extends Number> UnaryOpCompareMask<N> unaryOpCompareMask(Compare cmp, N value) {
        return new UnaryOpCompareMask<>(cmp, value);
    }

    public static UnaryOpSigmoid unarySigmoid() {
        return SIGMOID;
    }

    public static UnaryOpSoftmax unarySoftmax() {
        return SOFTMAX;
    }

    public static UnaryOpLogSoftmax unaryLogSoftmax() {
        return LOG_SOFTMAX;
    }

    public static BinaryOpAdd binaryAdd() {
        return ADD;
    }

    public static BinaryOpSub binarySub() {
        return SUB;
    }

    public static BinaryOpMul binaryMul() {
        return MUL;
    }

    public static BinaryOpDiv binaryDiv() {
        return DIV;
    }

    public static BinaryOpMin binaryMin() {
        return MIN;
    }

    public static BinaryOpMax binaryMax() {
        return MAX;
    }

    public static ReduceOpSum reduceSum() {
        return REDUCE_SUM;
    }

    public static ReduceOpProd reduceProd() {
        return REDUCE_PROD;
    }

    public static ReduceOpMin reduceMin() {
        return REDUCE_MIN;
    }

    public static ReduceOpMax reduceMax() {
        return REDUCE_MAX;
    }

    public static ReduceOpMean reduceMean() {
        return REDUCE_MEAN;
    }

    public static ReduceOpVarc reduceVarc(int ddof) {
        return new ReduceOpVarc(ddof, Double.NaN);
    }

    public static ReduceOpVarc reduceVarc(int ddof, double mean) {
        return new ReduceOpVarc(ddof, mean);
    }

    public static ReduceOpNanSum reduceNanSum() {
        return REDUCE_NAN_SUM;
    }

    public static ReduceOpNanProd reduceNanProd() {
        return REDUCE_NAN_PROD;
    }

    public static ReduceOpNanMin reduceNanMin() {
        return REDUCE_NAN_MIN;
    }

    public static ReduceOpNanMax reduceNanMax() {
        return REDUCE_NAN_MAX;
    }

    public static ReduceOpNanMean reduceNanMean() {
        return REDUCE_NAN_MEAN;
    }

}
