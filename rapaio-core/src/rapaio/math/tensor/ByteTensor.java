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

public interface ByteTensor extends Tensor<Byte, ByteTensor> {

    @Override
    default DType<Byte, ByteTensor> dtype() {
        return DTypes.BYTE;
    }

    @Override
    default Byte get(int... indexes) {
        return getByte(indexes);
    }

    /**
     * Gets unboxed value at indexed position. An indexed position is a tuple with
     * an index value for each dimension. Index values must be between 0 and
     * dimension minus one.
     *
     * @param indexes indexed position
     * @return unboxed byte value
     */
    byte getByte(int... indexes);

    @Override
    default void set(Byte value, int... indexes) {
        setByte(value, indexes);
    }

    /**
     * Sets unboxed value at indexed position.
     *
     * @param value unboxed value
     * @param indexes indexed position
     */
    void setByte(byte value, int... indexes);

    @Override
    default Byte ptrGet(int ptr) {
        return ptrGetByte(ptr);
    }

    /**
     * Get value at the given data pointer. A data pointer is a position
     * in the internal memory layout.
     *
     * @param ptr data pointer
     * @return unboxed value
     */
    byte ptrGetByte(int ptr);

    @Override
    default void ptrSet(int ptr, Byte value) {
        ptrSetByte(ptr, value);
    }

    /**
     * Sets value at given data pointer. A data pointer  is a position
     * in the internal memory layout.
     *
     * @param ptr data pointer
     * @param value unboxed value
     */
    void ptrSetByte(int ptr, byte value);

    byte[] toArray();
}
