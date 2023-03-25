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

package rapaio.math.tensor.manager.cpuparallel;

import java.util.ArrayList;
import java.util.List;

import rapaio.math.tensor.DTensor;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.layout.StrideLayout;
import rapaio.math.tensor.storage.DStorage;

public final class DTensorStride extends rapaio.math.tensor.manager.cpusingle.DTensorStride {

    private CpuArrayParallelTensorManager parallelManager;

    public DTensorStride(CpuArrayParallelTensorManager manager, StrideLayout layout, DStorage storage) {
        super(manager, layout, storage);
    }

    public DTensorStride(CpuArrayParallelTensorManager manager, Shape shape, int offset, int[] strides, DStorage storage) {
        super(manager, StrideLayout.of(shape, offset, strides), storage);
    }

    public DTensorStride(CpuArrayParallelTensorManager manager, Shape shape, int offset, Order order, DStorage storage) {
        super(manager, StrideLayout.ofDense(shape, offset, order), storage);
    }

    @Override
    public DTensor flatten(Order askOrder) {
        if (!(askOrder == Order.C || askOrder == Order.F)) {
            throw new IllegalArgumentException("Ask order is invalid.");
        }
        var out = manager.storageFactory().ofDoubleZeros(layout.shape().size());
        int p = 0;
        var it = chunkIterator(askOrder);

        List<Runnable> tasks = new ArrayList<>();
        while (it.hasNext()) {
            int pointer = it.nextInt();
            int pstart = p;
            tasks.add(() -> {
                for (int i = pointer; i < pointer + it.loopBound(); i += it.loopStep()) {
                    out.set(pstart + i - pointer, storage().get(i));
                }
            });
            p += it.loopSize();
        }
        try {
            parallelManager.executeRunnableTasks(tasks);

            Shape askShape = Shape.of(layout.shape().size());
            StrideLayout strideLayout = StrideLayout.ofDense(askShape, layout.offset(), askOrder);
            return manager.ofDoubleStride(strideLayout, out);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
