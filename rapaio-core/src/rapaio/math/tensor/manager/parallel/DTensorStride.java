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

package rapaio.math.tensor.manager.parallel;

import jdk.incubator.concurrent.StructuredTaskScope;
import rapaio.math.tensor.DTensor;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.layout.StrideLayout;
import rapaio.math.tensor.storage.DStorage;

public final class DTensorStride extends rapaio.math.tensor.manager.standard.DTensorStride {

    public DTensorStride(ParallelTensorManager manager, StrideLayout layout, DStorage storage) {
        super(manager, layout, storage);
    }

    public DTensorStride(ParallelTensorManager manager, Shape shape, int offset, int[] strides, DStorage storage) {
        this(manager, StrideLayout.of(shape, offset, strides), storage);
    }

    public DTensorStride(ParallelTensorManager manager, Shape shape, int offset, Order order, DStorage storage) {
        super(manager, StrideLayout.ofDense(shape, offset, order), storage);
    }

    @Override
    public DTensor flatten(Order askOrder) {
        // TODO: this is basically a test, an optimized code would require a different strategy
        askOrder = Order.autoFC(askOrder);
        var out = manager.storageFactory().ofDoubleZeros(layout.shape().size());
        int p = 0;
        var it = chunkIterator(askOrder);

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            while (it.hasNext()) {
                int pointer = it.nextInt();
                int pstart = p;
                scope.fork(() -> {
                    for (int i = pointer, j = pstart; i < pointer + it.loopBound(); i += it.loopStep(), j++) {
                        out.set(j, storage().get(i));
                    }
                    return null;
                });
                p += it.loopSize();
            }
            scope.join();
            scope.throwIfFailed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return manager.ofDoubleStride(Shape.of(layout.shape().size()), 0, new int[] {1}, out);
    }
}