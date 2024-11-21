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

package rapaio.narray.factories;

import java.util.Random;

import rapaio.core.distributions.Normal;
import rapaio.narray.NArray;
import rapaio.narray.NArrayManager;
import rapaio.narray.Order;
import rapaio.narray.Shape;
import rapaio.narray.layout.StrideLayout;
import rapaio.util.collection.IntArrays;

public final class DoubleDenseStride extends DoubleDense {

    public DoubleDenseStride(NArrayManager manager) {
        super(manager);
    }

    @Override
    public NArray<Double> seq(Shape shape) {
        int[] strides = IntArrays.newFill(shape.rank(), 1);
        int[] ordering = IntArrays.newSeq(0, shape.rank());
        IntArrays.shuffle(ordering, new Random(42));
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

        t.apply_(Order.C, (i, p) -> (double) i);

        return t;
    }

    @Override
    public NArray<Double> zeros(Shape shape) {
        int offset = 10;
        int[] strides = IntArrays.newFill(shape.rank(), 1);
        int[] ordering = IntArrays.newSeq(0, shape.rank());
        IntArrays.shuffle(ordering, new Random(42));

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
    public NArray<Double> random(Shape shape) {
        int offset = 10;
        int[] strides = IntArrays.newFill(shape.rank(), 1);
        int[] ordering = IntArrays.newSeq(0, shape.rank());
        IntArrays.shuffle(ordering, new Random(42));

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
        Normal normal = Normal.std();
        for (int i = 0; i < array.size(); i++) {
            array.setDouble(i, normal.sampleNext(random));
        }
        return manager.stride(dt, StrideLayout.of(shape, offset, strides), array);
    }
}
