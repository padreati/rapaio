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

package rapaio.math.tensor.operator;

import jdk.incubator.vector.VectorOperators;

public interface TensorUnaryOp {

    TensorUnaryOp ABS = new OpAbs();
    TensorUnaryOp NEG = new OpNeg();
    TensorUnaryOp LOG = new OpLog();
    TensorUnaryOp LOG1P = new OpLog1p();
    TensorUnaryOp EXP = new OpExp();
    TensorUnaryOp EXPM1 = new OpExpm1();
    TensorUnaryOp SIN = new OpSin();
    TensorUnaryOp ASIN = new OpAsin();
    TensorUnaryOp SINH = new OpSinh();
    TensorUnaryOp COS = new OpCos();
    TensorUnaryOp ACOS = new OpAcos();
    TensorUnaryOp COSH = new OpCosh();
    TensorUnaryOp TAN = new OpTan();
    TensorUnaryOp ATAN = new OpAtan();
    TensorUnaryOp TANH = new OpTanh();

    VectorOperators.Unary vop();

    double applyDouble(double v);

    float applyFloat(float v);

    default int applyInt(int v) {
        // default implementation for float only
        return 0;
    }

    boolean isFloatOnly();
}

final class OpAbs implements TensorUnaryOp {

    @Override
    public boolean isFloatOnly() {
        return false;
    }

    @Override
    public VectorOperators.Unary vop() {
        return VectorOperators.ABS;
    }

    @Override
    public double applyDouble(double v) {
        return Math.abs(v);
    }

    @Override
    public float applyFloat(float v) {
        return Math.abs(v);
    }

    @Override
    public int applyInt(int v) {
        return Math.abs(v);
    }
}

final class OpNeg implements TensorUnaryOp {

    @Override
    public boolean isFloatOnly() {
        return false;
    }

    @Override
    public VectorOperators.Unary vop() {
        return VectorOperators.NEG;
    }

    @Override
    public double applyDouble(double v) {
        return -v;
    }

    @Override
    public float applyFloat(float v) {
        return -v;
    }

    @Override
    public int applyInt(int v) {
        return -v;
    }
}

final class OpLog implements TensorUnaryOp {

    @Override
    public boolean isFloatOnly() {
        return true;
    }

    @Override
    public VectorOperators.Unary vop() {
        return VectorOperators.LOG;
    }

    @Override
    public double applyDouble(double v) {
        return Math.log(v);
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.log(v);
    }
}

final class OpLog1p implements TensorUnaryOp {

    @Override
    public boolean isFloatOnly() {
        return true;
    }

    @Override
    public VectorOperators.Unary vop() {
        return VectorOperators.LOG1P;
    }

    @Override
    public double applyDouble(double v) {
        return Math.log1p(v);
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.log1p(v);
    }
}

final class OpExp implements TensorUnaryOp {

    @Override
    public boolean isFloatOnly() {
        return true;
    }

    @Override
    public VectorOperators.Unary vop() {
        return VectorOperators.EXP;
    }

    @Override
    public double applyDouble(double v) {
        return Math.exp(v);
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.exp(v);
    }
}

final class OpExpm1 implements TensorUnaryOp {

    @Override
    public boolean isFloatOnly() {
        return true;
    }

    @Override
    public VectorOperators.Unary vop() {
        return VectorOperators.EXPM1;
    }

    @Override
    public double applyDouble(double v) {
        return Math.expm1(v);
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.expm1(v);
    }
}

final class OpSin implements TensorUnaryOp {

    @Override
    public boolean isFloatOnly() {
        return true;
    }

    @Override
    public VectorOperators.Unary vop() {
        return VectorOperators.SIN;
    }

    @Override
    public double applyDouble(double v) {
        return Math.sin(v);
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.sin(v);
    }
}

final class OpAsin implements TensorUnaryOp {

    @Override
    public boolean isFloatOnly() {
        return true;
    }

    @Override
    public VectorOperators.Unary vop() {
        return VectorOperators.ASIN;
    }

    @Override
    public double applyDouble(double v) {
        return Math.asin(v);
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.asin(v);
    }
}

final class OpSinh implements TensorUnaryOp {

    @Override
    public boolean isFloatOnly() {
        return true;
    }

    @Override
    public VectorOperators.Unary vop() {
        return VectorOperators.SINH;
    }

    @Override
    public double applyDouble(double v) {
        return Math.sinh(v);
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.sinh(v);
    }
}

final class OpCos implements TensorUnaryOp {

    @Override
    public boolean isFloatOnly() {
        return true;
    }

    @Override
    public VectorOperators.Unary vop() {
        return VectorOperators.COS;
    }

    @Override
    public double applyDouble(double v) {
        return Math.cos(v);
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.cos(v);
    }
}

final class OpAcos implements TensorUnaryOp {

    @Override
    public boolean isFloatOnly() {
        return true;
    }

    @Override
    public VectorOperators.Unary vop() {
        return VectorOperators.ACOS;
    }

    @Override
    public double applyDouble(double v) {
        return Math.acos(v);
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.acos(v);
    }
}

final class OpCosh implements TensorUnaryOp {

    @Override
    public boolean isFloatOnly() {
        return true;
    }

    @Override
    public VectorOperators.Unary vop() {
        return VectorOperators.COSH;
    }

    @Override
    public double applyDouble(double v) {
        return Math.cosh(v);
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.cosh(v);
    }
}

final class OpTan implements TensorUnaryOp {

    @Override
    public boolean isFloatOnly() {
        return true;
    }

    @Override
    public VectorOperators.Unary vop() {
        return VectorOperators.TAN;
    }

    @Override
    public double applyDouble(double v) {
        return Math.tan(v);
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.tan(v);
    }
}

final class OpAtan implements TensorUnaryOp {

    @Override
    public boolean isFloatOnly() {
        return true;
    }

    @Override
    public VectorOperators.Unary vop() {
        return VectorOperators.ATAN;
    }

    @Override
    public double applyDouble(double v) {
        return Math.atan(v);
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.atan(v);
    }
}

final class OpTanh implements TensorUnaryOp {

    @Override
    public boolean isFloatOnly() {
        return true;
    }

    @Override
    public VectorOperators.Unary vop() {
        return VectorOperators.TANH;
    }

    @Override
    public double applyDouble(double v) {
        return Math.tanh(v);
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.tanh(v);
    }
}
