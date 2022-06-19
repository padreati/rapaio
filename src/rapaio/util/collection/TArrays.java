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

package rapaio.util.collection;

import java.util.Arrays;

import rapaio.math.linear.DVector;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/22/20.
 */
public final class TArrays {

    public static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    @SafeVarargs
    public static <T> T[] concat(T[]... arrays) {
        int totalLength = 0;
        for (T[] ts : arrays) {
            totalLength += ts.length;
        }
        T[] copy = Arrays.copyOf(arrays[0], totalLength);
        int pos = 0;
        for (T[] array : arrays) {
            for (T value : array) {
                copy[pos++] = value;
            }
        }
        return copy;
    }

    public static void swap(int[] array, int i, int j) {
        int tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    public static void swap(double[] array, int i, int j) {
        double tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    public static void swap(float[] array, int i, int j) {
        float tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    public static void swap(byte[] array, int i, int j) {
        byte tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    public static void swap(long[] array, int i, int j) {
        long tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    public static void swap(DVector[] array, int i, int j) {
        DVector tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    /**
     * Ensures that a range given by its first (inclusive) and last (exclusive) elements fits an array of given length.
     *
     * <p>This method may be used whenever an array range check is needed.
     *
     * @param arrayLength an array length.
     * @param from        a start index (inclusive).
     * @param to          an end index (inclusive).
     * @throws IllegalArgumentException       if {@code from} is greater than {@code to}.
     * @throws ArrayIndexOutOfBoundsException if {@code from} or {@code to} are greater than {@code arrayLength} or negative.
     */
    public static void ensureFromTo(final int arrayLength, final int from, final int to) {
        if (from < 0) {
            throw new ArrayIndexOutOfBoundsException("Start index (" + from + ") is negative");
        }
        if (from > to) {
            throw new IllegalArgumentException("Start index (" + from + ") is greater than end index (" + to + ")");
        }
        if (to > arrayLength) {
            throw new ArrayIndexOutOfBoundsException("End index (" + to + ") is greater than array length (" + arrayLength + ")");
        }
    }

    /**
     * Ensures that a range given by an offset and a length fits an array of given length.
     *
     * <p>This method may be used whenever an array range check is needed.
     *
     * @param arrayLength an array length.
     * @param offset      a start index for the fragment
     * @param length      a length (the number of elements in the fragment).
     * @throws IllegalArgumentException       if {@code length} is negative.
     * @throws ArrayIndexOutOfBoundsException if {@code offset} is negative or {@code offset}+{@code length} is greater than {@code arrayLength}.
     */
    public static void ensureOffsetLength(final int arrayLength, final int offset, final int length) {
        if (offset < 0) {
            throw new ArrayIndexOutOfBoundsException("Offset (" + offset + ") is negative");
        }
        if (length < 0) {
            throw new IllegalArgumentException("Length (" + length + ") is negative");
        }
        if (offset + length > arrayLength) {
            throw new ArrayIndexOutOfBoundsException(
                    "Last index (" + (offset + length) + ") is greater than array length (" + arrayLength + ")");
        }
    }
}
