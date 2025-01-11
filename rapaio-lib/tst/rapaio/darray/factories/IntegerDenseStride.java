/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.darray.factories;

import java.util.Random;

import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.darray.layout.StrideLayout;
import rapaio.util.collection.Ints;

public final class IntegerDenseStride extends IntegerDense {

    public IntegerDenseStride(DArrayManager manager) {
        super(manager);
    }

    @Override
    public DArray<Integer> seq(Shape shape) {
        int[] strides = Ints.fill(shape.rank(), 1);
        int[] ordering = Ints.seq(0, shape.rank());
        Ints.shuffle(ordering, new Random(42));
        for (int i = 1; i < shape.rank(); i++) {
            int next = -1;
            int prev = -1;
            for (int j = 0; j < ordering.length; j++) {
                if (ordering[j] == i) {
                    next = j;
                    break;
                }
            }
            for (int j = 0; j < ordering.length; j++) {
                if (ordering[j] == i - 1) {
                    prev = j;
                    break;
                }
            }
            strides[next] = strides[prev] * shape.dim(prev);
        }

        int offset = 10;
        var t = manager.stride(dt, StrideLayout.of(shape, offset, strides), manager.storageManager().zeros(dt, offset + shape.size()));

        t.apply_(Order.C, (i, p) -> i);

        return t;
    }

    @Override
    public DArray<Integer> zeros(Shape shape) {
        int offset = 10;
        int[] strides = Ints.fill(shape.rank(), 1);
        int[] ordering = Ints.seq(0, shape.rank());
        Ints.shuffle(ordering, new Random(42));

        for (int i = 1; i < shape.rank(); i++) {
            int next = -1;
            int prev = -1;
            for (int j = 0; j < ordering.length; j++) {
                if (ordering[j] == i) {
                    next = j;
                    break;
                }
            }
            for (int j = 0; j < ordering.length; j++) {
                if (ordering[j] == i - 1) {
                    prev = j;
                    break;
                }
            }
            strides[next] = strides[prev] * shape.dim(prev);
        }

        return manager.stride(dt, StrideLayout.of(shape, offset, strides), manager.storageManager().zeros(dt, offset + shape.size()));
    }

    @Override
    public DArray<Integer> random(Shape shape) {
        int offset = 10;
        int[] strides = Ints.fill(shape.rank(), 1);
        int[] ordering = Ints.seq(0, shape.rank());
        Ints.shuffle(ordering, new Random(42));

        for (int i = 1; i < shape.rank(); i++) {
            int next = -1;
            int prev = -1;
            for (int j = 0; j < ordering.length; j++) {
                if (ordering[j] == i) {
                    next = j;
                    break;
                }
            }
            for (int j = 0; j < ordering.length; j++) {
                if (ordering[j] == i - 1) {
                    prev = j;
                    break;
                }
            }
            strides[next] = strides[prev] * shape.dim(prev);
        }

        var array = manager.storageManager().zeros(dt, offset + shape.size());
        for (int i = 0; i < array.size(); i++) {
            array.setInt(i, random.nextInt());
        }
        return manager.stride(dt, StrideLayout.of(shape, offset, strides), array);
    }
}
