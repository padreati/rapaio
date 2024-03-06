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

package rapaio.math.tensor.operator.impl;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorSpecies;
import rapaio.math.tensor.operator.TensorAssociativeOp;

public final class AssocOpMul implements TensorAssociativeOp {

    @Override
    public double initDouble() {
        return 1;
    }

    @Override
    public double aggDouble(double a, double b) {
        return a * b;
    }

    @Override
    public DoubleVector initDouble(VectorSpecies<Double> species) {
        return DoubleVector.broadcast(species, 1);
    }

    @Override
    public DoubleVector aggDouble(VectorSpecies<Double> species, DoubleVector a, DoubleVector b) {
        return a.mul(b);
    }

    @Override
    public float initFloat() {
        return 1f;
    }

    @Override
    public float aggFloat(float a, float b) {
        return a * b;
    }

    @Override
    public FloatVector initFloat(VectorSpecies<Float> species) {
        return FloatVector.broadcast(species, 1);
    }

    @Override
    public FloatVector aggFloat(VectorSpecies<Float> species, FloatVector a, FloatVector b) {
        return a.mul(b);
    }

    @Override
    public int initInt() {
        return 1;
    }

    @Override
    public int aggInt(int a, int b) {
        return a * b;
    }

    @Override
    public IntVector initInt(VectorSpecies<Integer> species) {
        return IntVector.broadcast(species, 1);
    }

    @Override
    public IntVector aggInt(VectorSpecies<Integer> species, IntVector a, IntVector b) {
        return a.mul(b);
    }

    @Override
    public byte initByte() {
        return 1;
    }

    @Override
    public byte aggByte(byte a, byte b) {
        return (byte) (a * b);
    }

    @Override
    public ByteVector initByte(VectorSpecies<Byte> species) {
        return ByteVector.broadcast(species, 1);
    }

    @Override
    public ByteVector aggByte(VectorSpecies<Byte> species, ByteVector a, ByteVector b) {
        return a.mul(b);
    }
}
