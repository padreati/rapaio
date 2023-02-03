/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.experiment.math.tensor.storage;

/**
 * A storage is an array contained decorated with access and operations on the stored values.
 * <p>
 * A storage container contains numerical values of the same type and cannot be allocated dynamically.
 * Operations on storage are elementary operations which are not thread safe. The purpose of the storage
 * is to hold array values and to offer building block operations for tensors of other data structures.
 * <p>
 * There is a dedicated container for each implemented numerical type.
 * <p>
 * This interface offers generic methods for uniformity. Those operations does boxing/unboxing and
 * are not ment to be used in efficient implementations. Each implementation offers operations dedicated for
 * implemented data type.
 *
 * @param <N> generic type of the numeric data type of the elements
 */
public interface Storage<N extends Number> {

    /**
     * @return number of stored elements
     */
    int size();

    N get(int offset);

    void set(int offset, N v);

    void swap(int left, int right);

    /**
     * Swaps two sequences of elements of an array.
     *
     * @param a a position in {@code x}.
     * @param b another position in {@code x}.
     * @param n the number of elements to exchange starting at {@code a} and {@code b}.
     */
    void swap(int a, int b, final int n);

    void fill(int start, int len, N v);

    void reverse(int start, int len);

    void quicksort(int from, int to, boolean asc);

    void add(int start, int len, N v);

    void sub(int start, int len, N v);

    void mul(int start, int len, N v);

    void div(int start, int len, N v);

    N min(int start, int len);

    int argMin(int start, int len);
}
