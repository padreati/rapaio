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

public interface TensorBinaryOp {

    TensorBinaryOp ADD = new BinaryOpAdd();
    TensorBinaryOp SUB = new BinaryOpSub();
    TensorBinaryOp MUL = new BinaryOpMul();
    TensorBinaryOp DIV = new BinaryOpDiv();

    double applyDouble(double a, double b);

    float applyFloat(float a, float b);

    int applyInt(int a, int b);

    byte applyByte(int a, int b);

    ByteVector applyByte(ByteVector a, ByteVector b);

    IntVector applyInt(IntVector a, IntVector b);

    FloatVector applyFloat(FloatVector a, FloatVector b);

    DoubleVector applyDouble(DoubleVector a, DoubleVector b);
}

final class BinaryOpAdd implements TensorBinaryOp {

    @Override
    public double applyDouble(double v, double a) {
        return v + a;
    }

    @Override
    public float applyFloat(float v, float a) {
        return v + a;
    }

    @Override
    public int applyInt(int a, int b) {
        return a + b;
    }

    @Override
    public byte applyByte(int a, int b) {
        return (byte) (a + b);
    }

    @Override
    public DoubleVector applyDouble(DoubleVector a, DoubleVector b) {
        return a.add(b);
    }

    @Override
    public FloatVector applyFloat(FloatVector a, FloatVector b) {
        return a.add(b);
    }

    @Override
    public IntVector applyInt(IntVector a, IntVector b) {
        return a.add(b);
    }

    @Override
    public ByteVector applyByte(ByteVector a, ByteVector b) {
        return a.add(b);
    }
}

final class BinaryOpSub implements TensorBinaryOp {

    @Override
    public double applyDouble(double v, double a) {
        return v - a;
    }

    @Override
    public float applyFloat(float v, float a) {
        return v - a;
    }

    @Override
    public int applyInt(int a, int b) {
        return a - b;
    }

    @Override
    public byte applyByte(int a, int b) {
        return (byte) (a - b);
    }

    @Override
    public DoubleVector applyDouble(DoubleVector a, DoubleVector b) {
        return a.sub(b);
    }

    @Override
    public FloatVector applyFloat(FloatVector a, FloatVector b) {
        return a.sub(b);
    }

    @Override
    public IntVector applyInt(IntVector a, IntVector b) {
        return a.sub(b);
    }

    @Override
    public ByteVector applyByte(ByteVector a, ByteVector b) {
        return a.sub(b);
    }
}

final class BinaryOpMul implements TensorBinaryOp {

    @Override
    public double applyDouble(double v, double a) {
        return v * a;
    }

    @Override
    public float applyFloat(float v, float a) {
        return v * a;
    }

    @Override
    public int applyInt(int a, int b) {
        return a * b;
    }

    @Override
    public byte applyByte(int a, int b) {
        return (byte) (a * b);
    }

    @Override
    public DoubleVector applyDouble(DoubleVector a, DoubleVector b) {
        return a.mul(b);
    }

    @Override
    public FloatVector applyFloat(FloatVector a, FloatVector b) {
        return a.mul(b);
    }

    @Override
    public IntVector applyInt(IntVector a, IntVector b) {
        return a.mul(b);
    }

    @Override
    public ByteVector applyByte(ByteVector a, ByteVector b) {
        return a.mul(b);
    }
}

final class BinaryOpDiv implements TensorBinaryOp {

    @Override
    public double applyDouble(double v, double a) {
        return v / a;
    }

    @Override
    public float applyFloat(float v, float a) {
        return v / a;
    }

    @Override
    public int applyInt(int a, int b) {
        return a / b;
    }

    @Override
    public byte applyByte(int a, int b) {
        return (byte) (a / b);
    }

    @Override
    public DoubleVector applyDouble(DoubleVector a, DoubleVector b) {
        return a.div(b);
    }

    @Override
    public FloatVector applyFloat(FloatVector a, FloatVector b) {
        return a.div(b);
    }

    @Override
    public IntVector applyInt(IntVector a, IntVector b) {
        return a.div(b);
    }

    @Override
    public ByteVector applyByte(ByteVector a, ByteVector b) {
        return a.div(b);
    }
}
