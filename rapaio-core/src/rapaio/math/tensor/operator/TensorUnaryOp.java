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

public abstract class TensorUnaryOp {

    public static final TensorUnaryOp ABS = new OpAbs(false);
    public static final TensorUnaryOp NEG = new OpNeg(false);
    public static final TensorUnaryOp LOG = new OpLog(true);
    public static final TensorUnaryOp LOG1P = new OpLog1p(true);
    public static final TensorUnaryOp EXP = new OpExp(true);
    public static final TensorUnaryOp EXPM1 = new OpExpm1(true);
    public static final TensorUnaryOp SIN = new OpSin(true);
    public static final TensorUnaryOp ASIN = new OpAsin(true);
    public static final TensorUnaryOp SINH = new OpSinh(true);
    public static final TensorUnaryOp COS = new OpCos(true);
    public static final TensorUnaryOp ACOS = new OpAcos(true);
    public static final TensorUnaryOp COSH = new OpCosh(true);
    public static final TensorUnaryOp TAN = new OpTan(true);
    public static final TensorUnaryOp ATAN = new OpAtan(true);
    public static final TensorUnaryOp TANH = new OpTanh(true);

    protected final boolean floatOnly;

    public TensorUnaryOp(boolean floatOnly) {
        this.floatOnly = floatOnly;
    }

    public abstract VectorOperators.Unary vop();

    public abstract double applyDouble(double v);

    public abstract float applyFloat(float v);

    public abstract int applyInt(int v);

    public boolean isFloatOnly() {
        return floatOnly;
    }

    private static class OpAbs extends TensorUnaryOp {

        public OpAbs(boolean floatOnly) {
            super(floatOnly);
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

    private static class OpNeg extends TensorUnaryOp {

        public OpNeg(boolean floatOnly) {
            super(floatOnly);
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

    private static class OpLog extends TensorUnaryOp {

        public OpLog(boolean floatOnly) {
            super(floatOnly);
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

        @Override
        public int applyInt(int v) {
            return (int) Math.log(v);
        }
    }

    private static class OpLog1p extends TensorUnaryOp {

        public OpLog1p(boolean floatOnly) {
            super(floatOnly);
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

        @Override
        public int applyInt(int v) {
            return (int) Math.log1p(v);
        }
    }

    private static class OpExp extends TensorUnaryOp {

        public OpExp(boolean floatOnly) {
            super(floatOnly);
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

        @Override
        public int applyInt(int v) {
            return (int) Math.exp(v);
        }
    }

    private static class OpExpm1 extends TensorUnaryOp {

        public OpExpm1(boolean floatOnly) {
            super(floatOnly);
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

        @Override
        public int applyInt(int v) {
            return (int) Math.expm1(v);
        }
    }

    private static class OpSin extends TensorUnaryOp {

        public OpSin(boolean floatOnly) {
            super(floatOnly);
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

        @Override
        public int applyInt(int v) {
            return (int) Math.sin(v);
        }
    }

    private static class OpAsin extends TensorUnaryOp {

        public OpAsin(boolean floatOnly) {
            super(floatOnly);
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

        @Override
        public int applyInt(int v) {
            return (int) Math.asin(v);
        }
    }

    private static class OpSinh extends TensorUnaryOp {

        public OpSinh(boolean floatOnly) {
            super(floatOnly);
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

        @Override
        public int applyInt(int v) {
            return (int) Math.sinh(v);
        }
    }

    private static class OpCos extends TensorUnaryOp {

        public OpCos(boolean floatOnly) {
            super(floatOnly);
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

        @Override
        public int applyInt(int v) {
            return (int) Math.cosh(v);
        }
    }

    private static class OpAcos extends TensorUnaryOp {

        public OpAcos(boolean floatOnly) {
            super(floatOnly);
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

        @Override
        public int applyInt(int v) {
            return (int) Math.acos(v);
        }
    }

    private static class OpCosh extends TensorUnaryOp {

        public OpCosh(boolean floatOnly) {
            super(floatOnly);
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

        @Override
        public int applyInt(int v) {
            return (int) Math.cosh(v);
        }
    }

    private static class OpTan extends TensorUnaryOp {

        public OpTan(boolean floatOnly) {
            super(floatOnly);
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

        @Override
        public int applyInt(int v) {
            return (int) Math.tan(v);
        }
    }

    private static class OpAtan extends TensorUnaryOp {

        public OpAtan(boolean floatOnly) {
            super(floatOnly);
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

        @Override
        public int applyInt(int v) {
            return (int) Math.atan(v);
        }
    }

    private static class OpTanh extends TensorUnaryOp {

        public OpTanh(boolean floatOnly) {
            super(floatOnly);
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

        @Override
        public int applyInt(int v) {
            return (int) Math.tanh(v);
        }
    }
}
