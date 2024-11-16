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

package rapaio.math.narray.operator;

import rapaio.math.narray.Compare;
import rapaio.math.narray.DType;
import rapaio.math.narray.operator.impl.BinaryOpAdd;
import rapaio.math.narray.operator.impl.BinaryOpDiv;
import rapaio.math.narray.operator.impl.BinaryOpMax;
import rapaio.math.narray.operator.impl.BinaryOpMin;
import rapaio.math.narray.operator.impl.BinaryOpMul;
import rapaio.math.narray.operator.impl.BinaryOpSub;
import rapaio.math.narray.operator.impl.ReduceOpAdd;
import rapaio.math.narray.operator.impl.ReduceOpMax;
import rapaio.math.narray.operator.impl.ReduceOpMin;
import rapaio.math.narray.operator.impl.ReduceOpMul;
import rapaio.math.narray.operator.impl.UnaryOpAbs;
import rapaio.math.narray.operator.impl.UnaryOpAcos;
import rapaio.math.narray.operator.impl.UnaryOpAsin;
import rapaio.math.narray.operator.impl.UnaryOpAtan;
import rapaio.math.narray.operator.impl.UnaryOpCeil;
import rapaio.math.narray.operator.impl.UnaryOpClamp;
import rapaio.math.narray.operator.impl.UnaryOpCompareMask;
import rapaio.math.narray.operator.impl.UnaryOpCos;
import rapaio.math.narray.operator.impl.UnaryOpCosh;
import rapaio.math.narray.operator.impl.UnaryOpExp;
import rapaio.math.narray.operator.impl.UnaryOpExpm1;
import rapaio.math.narray.operator.impl.UnaryOpFill;
import rapaio.math.narray.operator.impl.UnaryOpFillNan;
import rapaio.math.narray.operator.impl.UnaryOpFloor;
import rapaio.math.narray.operator.impl.UnaryOpLog;
import rapaio.math.narray.operator.impl.UnaryOpLog1p;
import rapaio.math.narray.operator.impl.UnaryOpNanToNum;
import rapaio.math.narray.operator.impl.UnaryOpNeg;
import rapaio.math.narray.operator.impl.UnaryOpPow;
import rapaio.math.narray.operator.impl.UnaryOpRint;
import rapaio.math.narray.operator.impl.UnaryOpSigmoid;
import rapaio.math.narray.operator.impl.UnaryOpSin;
import rapaio.math.narray.operator.impl.UnaryOpSinh;
import rapaio.math.narray.operator.impl.UnaryOpSqr;
import rapaio.math.narray.operator.impl.UnaryOpSqrt;
import rapaio.math.narray.operator.impl.UnaryOpTan;
import rapaio.math.narray.operator.impl.UnaryOpTanh;

public final class NArrayOp {

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

    private static final BinaryOpAdd ADD = new BinaryOpAdd();
    private static final BinaryOpSub SUB = new BinaryOpSub();
    private static final BinaryOpMul MUL = new BinaryOpMul();
    private static final BinaryOpDiv DIV = new BinaryOpDiv();
    private static final BinaryOpMin MIN = new BinaryOpMin();
    private static final BinaryOpMax MAX = new BinaryOpMax();

    private static final ReduceOpAdd ADD_ASSOC = new ReduceOpAdd();
    private static final ReduceOpMul MUL_ASSOC = new ReduceOpMul();
    private static final ReduceOpMin MIN_ASSOC = new ReduceOpMin();
    private static final ReduceOpMax MAX_ASSOC = new ReduceOpMax();


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

    public static <N extends Number> UnaryOpPow<N> unaryPow(double pow) {
        return new UnaryOpPow<>(pow);
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

    public static ReduceOpAdd reduceAdd() {
        return ADD_ASSOC;
    }

    public static ReduceOpMul reduceMul() {
        return MUL_ASSOC;
    }

    public static ReduceOpMin reduceMin() {
        return MIN_ASSOC;
    }

    public static ReduceOpMax reduceMax() {
        return MAX_ASSOC;
    }
}
