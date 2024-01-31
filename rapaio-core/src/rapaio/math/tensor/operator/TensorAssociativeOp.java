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
import jdk.incubator.vector.VectorSpecies;

public interface TensorAssociativeOp {

    TensorAssociativeOp ADD = new AssocOpAdd();
    TensorAssociativeOp MUL = new AssocOpMul();
    TensorAssociativeOp MAX = new AssocOpMax();
    TensorAssociativeOp MIN = new AssocOpMin();

    double initialDouble();
    double aggDouble(double a, double b);

    float initialFloat();
    float aggFloat(float a, float b);

    int initialInt();
    int aggInt(int a, int b);

    byte initialByte();
    byte aggByte(byte a, byte b);

    DoubleVector initVecDouble(VectorSpecies<Double> species);
    FloatVector initVecFloat(VectorSpecies<Float> species);
    IntVector initVecInt(VectorSpecies<Integer> species);
    ByteVector initVecByte(VectorSpecies<Byte> species);

    DoubleVector aggVecDouble(VectorSpecies<Double> species, DoubleVector a, DoubleVector b);
    FloatVector aggVecFloat(VectorSpecies<Float> species, FloatVector a, FloatVector b);
    IntVector aggVecInt(VectorSpecies<Integer> species, IntVector a, IntVector b);
    ByteVector aggVecByte(VectorSpecies<Byte> species, ByteVector a, ByteVector b);
}

final class AssocOpAdd implements TensorAssociativeOp {

    @Override
    public double initialDouble() {
        return 0;
    }

    @Override
    public double aggDouble(double a, double b) {
        return a + b;
    }

    @Override
    public DoubleVector initVecDouble(VectorSpecies<Double> species) {
        return DoubleVector.zero(species);
    }

    @Override
    public DoubleVector aggVecDouble(VectorSpecies<Double> species, DoubleVector a, DoubleVector b) {
        return a.add(b);
    }

    @Override
    public float initialFloat() {
        return 0f;
    }

    @Override
    public float aggFloat(float a, float b) {
        return a + b;
    }

    @Override
    public FloatVector initVecFloat(VectorSpecies<Float> species) {
        return FloatVector.zero(species);
    }

    @Override
    public FloatVector aggVecFloat(VectorSpecies<Float> species, FloatVector a, FloatVector b) {
        return a.add(b);
    }

    @Override
    public int initialInt() {
        return 0;
    }

    @Override
    public int aggInt(int a, int b) {
        return a + b;
    }

    @Override
    public IntVector initVecInt(VectorSpecies<Integer> species) {
        return IntVector.zero(species);
    }

    @Override
    public IntVector aggVecInt(VectorSpecies<Integer> species, IntVector a, IntVector b) {
        return a.add(b);
    }

    @Override
    public byte initialByte() {
        return 0;
    }

    @Override
    public byte aggByte(byte a, byte b) {
        return (byte) (a + b);
    }

    @Override
    public ByteVector initVecByte(VectorSpecies<Byte> species) {
        return ByteVector.zero(species);
    }

    @Override
    public ByteVector aggVecByte(VectorSpecies<Byte> species, ByteVector a, ByteVector b) {
        return a.add(b);
    }
}

final class AssocOpMul implements TensorAssociativeOp {

    @Override
    public double initialDouble() {
        return 1;
    }

    @Override
    public double aggDouble(double a, double b) {
        return a * b;
    }

    @Override
    public DoubleVector initVecDouble(VectorSpecies<Double> species) {
        return DoubleVector.broadcast(species, 1);
    }

    @Override
    public DoubleVector aggVecDouble(VectorSpecies<Double> species, DoubleVector a, DoubleVector b) {
        return a.mul(b);
    }

    @Override
    public float initialFloat() {
        return 1f;
    }

    @Override
    public float aggFloat(float a, float b) {
        return a * b;
    }

    @Override
    public FloatVector initVecFloat(VectorSpecies<Float> species) {
        return FloatVector.broadcast(species, 1);
    }

    @Override
    public FloatVector aggVecFloat(VectorSpecies<Float> species, FloatVector a, FloatVector b) {
        return a.mul(b);
    }

    @Override
    public int initialInt() {
        return 1;
    }

    @Override
    public int aggInt(int a, int b) {
        return a * b;
    }

    @Override
    public IntVector initVecInt(VectorSpecies<Integer> species) {
        return IntVector.broadcast(species, 1);
    }

    @Override
    public IntVector aggVecInt(VectorSpecies<Integer> species, IntVector a, IntVector b) {
        return a.mul(b);
    }

    @Override
    public byte initialByte() {
        return 1;
    }

    @Override
    public byte aggByte(byte a, byte b) {
        return (byte) (a * b);
    }

    @Override
    public ByteVector initVecByte(VectorSpecies<Byte> species) {
        return ByteVector.broadcast(species, 1);
    }

    @Override
    public ByteVector aggVecByte(VectorSpecies<Byte> species, ByteVector a, ByteVector b) {
        return a.mul(b);
    }
}

final class AssocOpMax implements TensorAssociativeOp {

    @Override
    public double initialDouble() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double aggDouble(double a, double b) {
        return Math.max(a, b);
    }

    @Override
    public DoubleVector initVecDouble(VectorSpecies<Double> species) {
        return DoubleVector.broadcast(species, Double.NEGATIVE_INFINITY);
    }

    @Override
    public DoubleVector aggVecDouble(VectorSpecies<Double> species, DoubleVector a, DoubleVector b) {
        return a.max(b);
    }

    @Override
    public float initialFloat() {
        return Float.NEGATIVE_INFINITY;
    }

    @Override
    public float aggFloat(float a, float b) {
        return Math.max(a, b);
    }

    @Override
    public FloatVector initVecFloat(VectorSpecies<Float> species) {
        return FloatVector.broadcast(species, Float.NEGATIVE_INFINITY);
    }

    @Override
    public FloatVector aggVecFloat(VectorSpecies<Float> species, FloatVector a, FloatVector b) {
        return a.max(b);
    }

    @Override
    public int initialInt() {
        return Integer.MIN_VALUE;
    }

    @Override
    public int aggInt(int a, int b) {
        return Math.max(a, b);
    }

    @Override
    public IntVector initVecInt(VectorSpecies<Integer> species) {
        return IntVector.broadcast(species, Integer.MIN_VALUE);
    }

    @Override
    public IntVector aggVecInt(VectorSpecies<Integer> species, IntVector a, IntVector b) {
        return a.max(b);
    }

    @Override
    public byte initialByte() {
        return Byte.MIN_VALUE;
    }

    @Override
    public byte aggByte(byte a, byte b) {
        return a >= b ? a : b;
    }

    @Override
    public ByteVector initVecByte(VectorSpecies<Byte> species) {
        return ByteVector.broadcast(species, Byte.MIN_VALUE);
    }

    @Override
    public ByteVector aggVecByte(VectorSpecies<Byte> species, ByteVector a, ByteVector b) {
        return a.max(b);
    }
}

final class AssocOpMin implements TensorAssociativeOp {

    @Override
    public double initialDouble() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double aggDouble(double a, double b) {
        return Math.min(a, b);
    }

    @Override
    public DoubleVector initVecDouble(VectorSpecies<Double> species) {
        return DoubleVector.broadcast(species, Double.POSITIVE_INFINITY);
    }

    @Override
    public DoubleVector aggVecDouble(VectorSpecies<Double> species, DoubleVector a, DoubleVector b) {
        return a.min(b);
    }

    @Override
    public float initialFloat() {
        return Float.POSITIVE_INFINITY;
    }

    @Override
    public float aggFloat(float a, float b) {
        return Math.min(a, b);
    }

    @Override
    public FloatVector initVecFloat(VectorSpecies<Float> species) {
        return FloatVector.broadcast(species, Float.POSITIVE_INFINITY);
    }

    @Override
    public FloatVector aggVecFloat(VectorSpecies<Float> species, FloatVector a, FloatVector b) {
        return a.min(b);
    }

    @Override
    public int initialInt() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int aggInt(int a, int b) {
        return Math.min(a, b);
    }

    @Override
    public IntVector initVecInt(VectorSpecies<Integer> species) {
        return IntVector.broadcast(species, Integer.MAX_VALUE);
    }

    @Override
    public IntVector aggVecInt(VectorSpecies<Integer> species, IntVector a, IntVector b) {
        return a.min(b);
    }

    @Override
    public byte initialByte() {
        return Byte.MAX_VALUE;
    }

    @Override
    public byte aggByte(byte a, byte b) {
        return a >= b ? b : a;
    }

    @Override
    public ByteVector initVecByte(VectorSpecies<Byte> species) {
        return ByteVector.broadcast(species, Byte.MAX_VALUE);
    }

    @Override
    public ByteVector aggVecByte(VectorSpecies<Byte> species, ByteVector a, ByteVector b) {
        return a.min(b);
    }
}

