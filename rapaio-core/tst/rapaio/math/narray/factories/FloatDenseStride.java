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

package rapaio.math.narray.factories;

import java.util.Random;

import rapaio.core.distributions.Normal;
import rapaio.math.narray.NArray;
import rapaio.math.narray.NArrayManager;
import rapaio.math.narray.Order;
import rapaio.math.narray.Shape;
import rapaio.math.narray.layout.StrideLayout;
import rapaio.util.collection.IntArrays;

public final class FloatDenseStride extends FloatDense {

    public FloatDenseStride(NArrayManager manager) {
        super(manager);
    }

    @Override
    public NArray<Float> seq(Shape shape) {
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
        var t = ofType.stride(StrideLayout.of(shape, offset, strides), ofType.storage().zeros(offset + shape.size()));

        t.apply_(Order.C, (i, p) -> (float) i);

        return t;
    }

    @Override
    public NArray<Float> zeros(Shape shape) {
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

        return ofType.stride(StrideLayout.of(shape, offset, strides), ofType.storage().zeros(offset + shape.size()));
    }

    @Override
    public NArray<Float> random(Shape shape) {
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

        var array = ofType.storage().zeros(offset + shape.size());
        Normal normal = Normal.std();
        for (int i = 0; i < array.size(); i++) {
            array.setFloat(i, (float)normal.sampleNext(random));
        }
        return ofType.stride(StrideLayout.of(shape, offset, strides), array);
    }
}
