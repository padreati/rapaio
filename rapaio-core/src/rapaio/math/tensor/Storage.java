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

package rapaio.math.tensor;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorSpecies;
import rapaio.data.OperationNotAvailableException;

public interface Storage<N extends Number> {

    DType<N> dType();

    int size();

    N get(int ptr);

    void set(int ptr, N value);

    void inc(int ptr, N value);

    void fill(N value);


    byte getByte(int ptr);

    void setByte(int ptr, byte value);

    void incByte(int ptr, byte value);

    void fillByte(byte value);


    int getInt(int ptr);

    void setInt(int ptr, int value);

    void incInt(int ptr, int value);

    void fillInt(int value);


    float getFloat(int ptr);

    void setFloat(int ptr, float value);

    void incFloat(int ptr, float value);

    void fillFloat(float value);


    double getDouble(int ptr);

    void setDouble(int ptr, double value);

    void incDouble(int ptr, double value);

    void fillDouble(double value);

    default ByteVector loadByte(VectorSpecies<Byte> species, int offset) {
        throw new OperationNotAvailableException();
    }

    default ByteVector loadByte(VectorSpecies<Byte> species, int offset, VectorMask<Byte> mask) {
        throw new OperationNotAvailableException();
    }

    default ByteVector loadByte(VectorSpecies<Byte> species, int offset, int[] index, int indexOffset) {
        throw new OperationNotAvailableException();
    }

    default ByteVector loadByte(VectorSpecies<Byte> species, int offset, int[] index, int indexOffset, VectorMask<Byte> mask) {
        throw new OperationNotAvailableException();
    }

    default void saveByte(ByteVector a, int offset) {
        throw new OperationNotAvailableException();
    }

    default void saveByte(ByteVector a, int offset, VectorMask<Byte> mask) {
        throw new OperationNotAvailableException();
    }

    default void saveByte(ByteVector a, int offset, int[] index, int indexOffset) {
        throw new OperationNotAvailableException();
    }

    default void saveByte(ByteVector a, int offset, int[] index, int indexOffset, VectorMask<Byte> mask) {
        throw new OperationNotAvailableException();
    }



    default IntVector loadInt(VectorSpecies<Integer> species, int offset) {
        throw new OperationNotAvailableException();
    }

    default IntVector loadInt(VectorSpecies<Integer> species, int offset, VectorMask<Integer> mask) {
        throw new OperationNotAvailableException();
    }

    default IntVector loadInt(VectorSpecies<Integer> species, int offset, int[] index, int indexOffset) {
        throw new OperationNotAvailableException();
    }

    default IntVector loadInt(VectorSpecies<Integer> species, int offset, int[] index, int indexOffset, VectorMask<Integer> mask) {
        throw new OperationNotAvailableException();
    }

    default void saveInt(IntVector a, int offset) {
        throw new OperationNotAvailableException();
    }

    default void saveInt(IntVector a, int offset, VectorMask<Integer> mask) {
        throw new OperationNotAvailableException();
    }

    default void saveInt(IntVector a, int offset, int[] index, int indexOffset) {
        throw new OperationNotAvailableException();
    }

    default void saveInt(IntVector a, int offset, int[] index, int indexOffset, VectorMask<Integer> mask) {
        throw new OperationNotAvailableException();
    }


    default FloatVector loadFloat(VectorSpecies<Float> species, int offset) {
        throw new OperationNotAvailableException();
    }

    default FloatVector loadFloat(VectorSpecies<Float> species, int offset, VectorMask<Float> mask) {
        throw new OperationNotAvailableException();
    }

    default FloatVector loadFloat(VectorSpecies<Float> species, int offset, int[] index, int indexOffset) {
        throw new OperationNotAvailableException();
    }

    default FloatVector loadFloat(VectorSpecies<Float> species, int offset, int[] index, int indexOffset, VectorMask<Float> mask) {
        throw new OperationNotAvailableException();
    }

    default void saveFloat(FloatVector a, int offset) {
        throw new OperationNotAvailableException();
    }

    default void saveFloat(FloatVector a, int offset, VectorMask<Float> mask) {
        throw new OperationNotAvailableException();
    }

    default void saveFloat(FloatVector a, int offset, int[] index, int indexOffset) {
        throw new OperationNotAvailableException();
    }

    default void saveFloat(FloatVector a, int offset, int[] index, int indexOffset, VectorMask<Float> mask) {
        throw new OperationNotAvailableException();
    }


    default DoubleVector loadDouble(VectorSpecies<Double> species, int offset) {
        throw new OperationNotAvailableException();
    }

    default DoubleVector loadDouble(VectorSpecies<Double> species, int offset, VectorMask<Double> mask) {
        throw new OperationNotAvailableException();
    }

    default DoubleVector loadDouble(VectorSpecies<Double> species, int offset, int[] index, int indexOffset) {
        throw new OperationNotAvailableException();
    }

    default DoubleVector loadDouble(VectorSpecies<Double> species, int offset, int[] index, int indexOffset, VectorMask<Double> mask) {
        throw new OperationNotAvailableException();
    }

    default void saveDouble(DoubleVector a, int offset) {
        throw new OperationNotAvailableException();
    }

    default void saveDouble(DoubleVector a, int offset, VectorMask<Double> mask) {
        throw new OperationNotAvailableException();
    }

    default void saveDouble(DoubleVector a, int offset, int[] index, int indexOffset) {
        throw new OperationNotAvailableException();
    }

    default void saveDouble(DoubleVector a, int offset, int[] index, int indexOffset, VectorMask<Double> mask) {
        throw new OperationNotAvailableException();
    }
}
