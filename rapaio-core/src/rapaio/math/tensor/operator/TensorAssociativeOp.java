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

    byte initByte();

    byte applyByte(byte a, byte b);

    ByteVector initByte(VectorSpecies<Byte> species);

    ByteVector applyByte(ByteVector a, ByteVector b);


    int initInt();

    int applyInt(int a, int b);

    IntVector initInt(VectorSpecies<Integer> species);

    IntVector applyInt(IntVector a, IntVector b);


    float initFloat();

    float applyFloat(float a, float b);

    FloatVector initFloat(VectorSpecies<Float> species);

    FloatVector applyFloat(FloatVector a, FloatVector b);


    double initDouble();

    double applyDouble(double a, double b);

    DoubleVector initDouble(VectorSpecies<Double> species);

    DoubleVector applyDouble(DoubleVector a, DoubleVector b);
}

