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

public interface IntTensor extends Tensor<Integer, IntTensor> {

    @Override
    default DType<Integer, IntTensor> dtype() {
        return DTypes.INTEGER;
    }

    @Override
    default Integer get(int... indexes) {
        return getInt(indexes);
    }

    /**
     * Gets unboxed value at indexed position. An indexed position is a tuple with
     * an index value for each dimension. Index values must be between 0 and
     * dimension minus one.
     *
     * @param indexes indexed position
     * @return unboxed int value
     */
    int getInt(int... indexes);

    @Override
    default void set(Integer value, int... indexes) {
        setInt(value, indexes);
    }

    /**
     * Sets unboxed value at indexed position.
     *
     * @param value   unboxed value
     * @param indexes indexed position
     */
    void setInt(int value, int... indexes);

    @Override
    default Integer ptrGet(int ptr) {
        return ptrGetInteger(ptr);
    }

    /**
     * Get value at the given data pointer. A data pointer is a position
     * in the internal memory layout.
     *
     * @param ptr data pointer
     * @return unboxed value
     */
    int ptrGetInteger(int ptr);

    @Override
    default void ptrSet(int ptr, Integer value) {
        ptrSetInteger(ptr, value);
    }

    /**
     * Sets value at given data pointer. A data pointer  is a position
     * in the internal memory layout.
     *
     * @param ptr   data pointer
     * @param value unboxed value
     */
    void ptrSetInteger(int ptr, int value);

    int[] toArray();

    int[] asArray();
}
