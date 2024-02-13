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

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import rapaio.data.OperationNotAvailableException;

public interface TensorUnaryOp {

    TensorUnaryOp RINT = new OpRint();
    TensorUnaryOp CEIL = new OpCeil();
    TensorUnaryOp FLOOR = new OpFloor();
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
    TensorUnaryOp SQR = new OpSqr();
    TensorUnaryOp SQRT = new OpSqrt();

    /**
     * @return true if vector operations are implemented, false otherwise
     */
    boolean vectorSupport();

    boolean floatingPointOnly();

    byte applyByte(byte v);

    int applyInt(int v);

    double applyDouble(double v);

    float applyFloat(float v);

    ByteVector applyByte(ByteVector v);

    IntVector applyInt(IntVector v);

    FloatVector applyFloat(FloatVector v);

    DoubleVector applyDouble(DoubleVector v);
}

final class OpRint implements TensorUnaryOp {

    @Override
    public boolean vectorSupport() {
        return false;
    }

    @Override
    public boolean floatingPointOnly() {
        return false;
    }

    @Override
    public double applyDouble(double v) {
        return Math.rint(v);
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.rint(v);
    }

    @Override
    public int applyInt(int v) {
        return v;
    }

    @Override
    public byte applyByte(byte v) {
        return v;
    }

    @Override
    public ByteVector applyByte(ByteVector v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public IntVector applyInt(IntVector v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public FloatVector applyFloat(FloatVector v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public DoubleVector applyDouble(DoubleVector v) {
        throw new OperationNotAvailableException();
    }
}

final class OpCeil implements TensorUnaryOp {

    @Override
    public boolean vectorSupport() {
        return false;
    }

    @Override
    public boolean floatingPointOnly() {
        return false;
    }

    @Override
    public double applyDouble(double v) {
        return Math.ceil(v);
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.ceil(v);
    }

    @Override
    public int applyInt(int v) {
        return v;
    }

    @Override
    public byte applyByte(byte v) {
        return v;
    }

    @Override
    public ByteVector applyByte(ByteVector v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public IntVector applyInt(IntVector v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public FloatVector applyFloat(FloatVector v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public DoubleVector applyDouble(DoubleVector v) {
        throw new OperationNotAvailableException();
    }
}

final class OpFloor implements TensorUnaryOp {

    @Override
    public boolean vectorSupport() {
        return true;
    }

    @Override
    public boolean floatingPointOnly() {
        return false;
    }

    @Override
    public byte applyByte(byte v) {
        return v;
    }

    @Override
    public int applyInt(int v) {
        return v;
    }

    @Override
    public float applyFloat(float v) {
        return (float) StrictMath.floor(v);
    }

    @Override
    public double applyDouble(double v) {
        return StrictMath.floor(v);
    }

    @Override
    public ByteVector applyByte(ByteVector v) {
        return v;
    }

    @Override
    public IntVector applyInt(IntVector v) {
        return v;
    }

    @Override
    public FloatVector applyFloat(FloatVector v) {
        VectorMask<Float> m = v.compare(VectorOperators.LT, 0);
        if (m.anyTrue()) {
            v = v.sub(1.f, m);
        }
        return v.convert(VectorOperators.F2I, 0).convert(VectorOperators.I2F, 0).reinterpretAsFloats();
    }

    @Override
    public DoubleVector applyDouble(DoubleVector v) {
        VectorMask<Double> m = v.compare(VectorOperators.LT, 0);
        if (m.anyTrue()) {
            v = v.sub(1., m);
        }
        return v.convert(VectorOperators.D2L, 0).convert(VectorOperators.L2D, 0).reinterpretAsDoubles();
    }
}

final class OpAbs implements TensorUnaryOp {

    @Override
    public boolean vectorSupport() {
        return true;
    }

    @Override
    public boolean floatingPointOnly() {
        return false;
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

    @Override
    public byte applyByte(byte v) {
        return (byte) Math.abs(v);
    }

    @Override
    public ByteVector applyByte(ByteVector v) {
        return v.abs();
    }

    @Override
    public IntVector applyInt(IntVector v) {
        return v.abs();
    }

    @Override
    public FloatVector applyFloat(FloatVector v) {
        return v.abs();
    }

    @Override
    public DoubleVector applyDouble(DoubleVector v) {
        return v.abs();
    }
}

final class OpNeg implements TensorUnaryOp {

    @Override
    public boolean vectorSupport() {
        return true;
    }

    @Override
    public boolean floatingPointOnly() {
        return false;
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

    @Override
    public byte applyByte(byte v) {
        return (byte) -v;
    }

    @Override
    public ByteVector applyByte(ByteVector v) {
        return v.neg();
    }

    @Override
    public IntVector applyInt(IntVector v) {
        return v.neg();
    }

    @Override
    public FloatVector applyFloat(FloatVector v) {
        return v.neg();
    }

    @Override
    public DoubleVector applyDouble(DoubleVector v) {
        return v.neg();
    }
}

final class OpLog implements TensorUnaryOp {

    @Override
    public boolean vectorSupport() {
        return true;
    }

    @Override
    public boolean floatingPointOnly() {
        return true;
    }

    @Override
    public byte applyByte(byte v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public int applyInt(int v) {
        throw new OperationNotAvailableException();
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
    public ByteVector applyByte(ByteVector v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public IntVector applyInt(IntVector v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public FloatVector applyFloat(FloatVector v) {
        return v.lanewise(VectorOperators.LOG);
    }

    @Override
    public DoubleVector applyDouble(DoubleVector v) {
        return v.lanewise(VectorOperators.LOG);
    }
}

final class OpLog1p implements TensorUnaryOp {

    @Override
    public boolean vectorSupport() {
        return true;
    }

    @Override
    public boolean floatingPointOnly() {
        return true;
    }

    @Override
    public byte applyByte(byte v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public int applyInt(int v) {
        throw new OperationNotAvailableException();
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
    public ByteVector applyByte(ByteVector v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public IntVector applyInt(IntVector v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public FloatVector applyFloat(FloatVector v) {
        return v.lanewise(VectorOperators.LOG1P);
    }

    @Override
    public DoubleVector applyDouble(DoubleVector v) {
        return v.lanewise(VectorOperators.LOG1P);
    }
}

final class OpExp implements TensorUnaryOp {

    @Override
    public boolean vectorSupport() {
        return true;
    }

    @Override
    public boolean floatingPointOnly() {
        return true;
    }

    @Override
    public byte applyByte(byte v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public int applyInt(int v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.exp(v);
    }

    @Override
    public double applyDouble(double v) {
        return Math.exp(v);
    }

    @Override
    public ByteVector applyByte(ByteVector v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public IntVector applyInt(IntVector v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public FloatVector applyFloat(FloatVector v) {
        return v.lanewise(VectorOperators.EXP);
    }

    @Override
    public DoubleVector applyDouble(DoubleVector v) {
        return v.lanewise(VectorOperators.EXP);
    }
}

final class OpExpm1 implements TensorUnaryOp {

    @Override
    public boolean vectorSupport() {
        return true;
    }

    @Override
    public boolean floatingPointOnly() {
        return true;
    }

    @Override
    public byte applyByte(byte v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public int applyInt(int v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.expm1(v);
    }

    @Override
    public double applyDouble(double v) {
        return Math.expm1(v);
    }

    @Override
    public ByteVector applyByte(ByteVector v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public IntVector applyInt(IntVector v) {
        throw new OperationNotAvailableException();
    }


    @Override
    public FloatVector applyFloat(FloatVector v) {
        return v.lanewise(VectorOperators.EXPM1);
    }

    @Override
    public DoubleVector applyDouble(DoubleVector v) {
        return v.lanewise(VectorOperators.EXPM1);
    }
}

final class OpSin implements TensorUnaryOp {

    @Override
    public boolean vectorSupport() {
        return true;
    }

    @Override
    public boolean floatingPointOnly() {
        return true;
    }

    @Override
    public byte applyByte(byte v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public int applyInt(int v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.sin(v);
    }

    @Override
    public double applyDouble(double v) {
        return Math.sin(v);
    }

    @Override
    public ByteVector applyByte(ByteVector v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public IntVector applyInt(IntVector v) {
        throw new OperationNotAvailableException();
    }


    @Override
    public FloatVector applyFloat(FloatVector v) {
        return v.lanewise(VectorOperators.SIN);
    }

    @Override
    public DoubleVector applyDouble(DoubleVector v) {
        return v.lanewise(VectorOperators.SIN);
    }
}

final class OpAsin implements TensorUnaryOp {

    @Override
    public boolean vectorSupport() {
        return true;
    }

    @Override
    public boolean floatingPointOnly() {
        return true;
    }

    @Override
    public byte applyByte(byte v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public int applyInt(int v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.asin(v);
    }

    @Override
    public double applyDouble(double v) {
        return Math.asin(v);
    }

    @Override
    public ByteVector applyByte(ByteVector v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public IntVector applyInt(IntVector v) {
        throw new OperationNotAvailableException();
    }


    @Override
    public FloatVector applyFloat(FloatVector v) {
        return v.lanewise(VectorOperators.ASIN);
    }

    @Override
    public DoubleVector applyDouble(DoubleVector v) {
        return v.lanewise(VectorOperators.ASIN);
    }
}

final class OpSinh implements TensorUnaryOp {

    @Override
    public boolean vectorSupport() {
        return true;
    }

    @Override
    public boolean floatingPointOnly() {
        return true;
    }

    @Override
    public byte applyByte(byte v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public int applyInt(int v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.sinh(v);
    }

    @Override
    public double applyDouble(double v) {
        return Math.sinh(v);
    }

    @Override
    public ByteVector applyByte(ByteVector v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public IntVector applyInt(IntVector v) {
        throw new OperationNotAvailableException();
    }


    @Override
    public FloatVector applyFloat(FloatVector v) {
        return v.lanewise(VectorOperators.SINH);
    }

    @Override
    public DoubleVector applyDouble(DoubleVector v) {
        return v.lanewise(VectorOperators.SINH);
    }
}

final class OpCos implements TensorUnaryOp {

    @Override
    public boolean vectorSupport() {
        return true;
    }

    @Override
    public boolean floatingPointOnly() {
        return true;
    }

    @Override
    public byte applyByte(byte v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public int applyInt(int v) {
        throw new OperationNotAvailableException();
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
    public ByteVector applyByte(ByteVector v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public IntVector applyInt(IntVector v) {
        throw new OperationNotAvailableException();
    }


    @Override
    public FloatVector applyFloat(FloatVector v) {
        return v.lanewise(VectorOperators.COS);
    }

    @Override
    public DoubleVector applyDouble(DoubleVector v) {
        return v.lanewise(VectorOperators.COS);
    }
}

final class OpAcos implements TensorUnaryOp {

    @Override
    public boolean vectorSupport() {
        return true;
    }

    @Override
    public boolean floatingPointOnly() {
        return true;
    }

    @Override
    public byte applyByte(byte v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public int applyInt(int v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.acos(v);
    }

    @Override
    public double applyDouble(double v) {
        return Math.acos(v);
    }

    @Override
    public ByteVector applyByte(ByteVector v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public IntVector applyInt(IntVector v) {
        throw new OperationNotAvailableException();
    }


    @Override
    public FloatVector applyFloat(FloatVector v) {
        return v.lanewise(VectorOperators.ACOS);
    }

    @Override
    public DoubleVector applyDouble(DoubleVector v) {
        return v.lanewise(VectorOperators.ACOS);
    }
}

final class OpCosh implements TensorUnaryOp {

    @Override
    public boolean vectorSupport() {
        return true;
    }

    @Override
    public boolean floatingPointOnly() {
        return true;
    }

    @Override
    public byte applyByte(byte v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public int applyInt(int v) {
        throw new OperationNotAvailableException();
    }


    @Override
    public float applyFloat(float v) {
        return (float) Math.cosh(v);
    }

    @Override
    public double applyDouble(double v) {
        return Math.cosh(v);
    }

    @Override
    public ByteVector applyByte(ByteVector v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public IntVector applyInt(IntVector v) {
        throw new OperationNotAvailableException();
    }


    @Override
    public FloatVector applyFloat(FloatVector v) {
        return v.lanewise(VectorOperators.COSH);
    }

    @Override
    public DoubleVector applyDouble(DoubleVector v) {
        return v.lanewise(VectorOperators.COSH);
    }
}

final class OpTan implements TensorUnaryOp {

    @Override
    public boolean vectorSupport() {
        return true;
    }

    @Override
    public boolean floatingPointOnly() {
        return true;
    }

    @Override
    public byte applyByte(byte v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public int applyInt(int v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.tan(v);
    }

    @Override
    public double applyDouble(double v) {
        return Math.tan(v);
    }

    @Override
    public ByteVector applyByte(ByteVector v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public IntVector applyInt(IntVector v) {
        throw new OperationNotAvailableException();
    }


    @Override
    public FloatVector applyFloat(FloatVector v) {
        return v.lanewise(VectorOperators.TAN);
    }

    @Override
    public DoubleVector applyDouble(DoubleVector v) {
        return v.lanewise(VectorOperators.TAN);
    }
}

final class OpAtan implements TensorUnaryOp {

    @Override
    public boolean vectorSupport() {
        return true;
    }

    @Override
    public boolean floatingPointOnly() {
        return true;
    }

    @Override
    public byte applyByte(byte v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public int applyInt(int v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.atan(v);
    }

    @Override
    public double applyDouble(double v) {
        return Math.atan(v);
    }

    @Override
    public ByteVector applyByte(ByteVector v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public IntVector applyInt(IntVector v) {
        throw new OperationNotAvailableException();
    }


    @Override
    public FloatVector applyFloat(FloatVector v) {
        return v.lanewise(VectorOperators.ATAN);
    }

    @Override
    public DoubleVector applyDouble(DoubleVector v) {
        return v.lanewise(VectorOperators.ATAN);
    }
}

final class OpTanh implements TensorUnaryOp {

    @Override
    public boolean vectorSupport() {
        return true;
    }

    @Override
    public boolean floatingPointOnly() {
        return true;
    }

    @Override
    public byte applyByte(byte v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public int applyInt(int v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.tanh(v);
    }

    @Override
    public double applyDouble(double v) {
        return Math.tanh(v);
    }

    @Override
    public ByteVector applyByte(ByteVector v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public IntVector applyInt(IntVector v) {
        throw new OperationNotAvailableException();
    }


    @Override
    public FloatVector applyFloat(FloatVector v) {
        return v.lanewise(VectorOperators.TANH);
    }

    @Override
    public DoubleVector applyDouble(DoubleVector v) {
        return v.lanewise(VectorOperators.TANH);
    }
}

final class OpSqr implements TensorUnaryOp {

    @Override
    public boolean vectorSupport() {
        return true;
    }

    @Override
    public boolean floatingPointOnly() {
        return false;
    }

    @Override
    public byte applyByte(byte v) {
        return (byte) (v * v);
    }

    @Override
    public int applyInt(int v) {
        return v * v;
    }

    @Override
    public float applyFloat(float v) {
        return v * v;
    }

    @Override
    public double applyDouble(double v) {
        return v * v;
    }

    @Override
    public ByteVector applyByte(ByteVector v) {
        return v.mul(v);
    }

    @Override
    public IntVector applyInt(IntVector v) {
        return v.mul(v);
    }


    @Override
    public FloatVector applyFloat(FloatVector v) {
        return v.mul(v);
    }

    @Override
    public DoubleVector applyDouble(DoubleVector v) {
        return v.mul(v);
    }
}

final class OpSqrt implements TensorUnaryOp {

    @Override
    public boolean vectorSupport() {
        return true;
    }

    @Override
    public boolean floatingPointOnly() {
        return true;
    }

    @Override
    public byte applyByte(byte v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public int applyInt(int v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.sqrt(v);
    }

    @Override
    public double applyDouble(double v) {
        return Math.sqrt(v);
    }

    @Override
    public ByteVector applyByte(ByteVector v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public IntVector applyInt(IntVector v) {
        throw new OperationNotAvailableException();
    }


    @Override
    public FloatVector applyFloat(FloatVector v) {
        return v.lanewise(VectorOperators.SQRT);
    }

    @Override
    public DoubleVector applyDouble(DoubleVector v) {
        return v.lanewise(VectorOperators.SQRT);
    }
}
