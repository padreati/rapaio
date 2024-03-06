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
import rapaio.math.tensor.operator.impl.AssocOpAdd;
import rapaio.math.tensor.operator.impl.AssocOpMax;
import rapaio.math.tensor.operator.impl.AssocOpMin;
import rapaio.math.tensor.operator.impl.AssocOpMul;

public interface TensorAssociativeOp {

    TensorAssociativeOp ADD = new AssocOpAdd();
    TensorAssociativeOp MUL = new AssocOpMul();
    TensorAssociativeOp MAX = new AssocOpMax();
    TensorAssociativeOp MIN = new AssocOpMin();

    double initDouble();

    double aggDouble(double a, double b);

    float initFloat();

    float aggFloat(float a, float b);

    int initInt();

    int aggInt(int a, int b);

    byte initByte();

    byte aggByte(byte a, byte b);

    ByteVector initByte(VectorSpecies<Byte> species);

    IntVector initInt(VectorSpecies<Integer> species);

    FloatVector initFloat(VectorSpecies<Float> species);

    DoubleVector initDouble(VectorSpecies<Double> species);

    ByteVector aggByte(VectorSpecies<Byte> species, ByteVector a, ByteVector b);

    IntVector aggInt(VectorSpecies<Integer> species, IntVector a, IntVector b);

    FloatVector aggFloat(VectorSpecies<Float> species, FloatVector a, FloatVector b);

    DoubleVector aggDouble(VectorSpecies<Double> species, DoubleVector a, DoubleVector b);
}

