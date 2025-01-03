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

package rapaio.darray.manager.base;

import java.util.Random;

import rapaio.core.distributions.Normal;
import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.DType;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.darray.Storage;
import rapaio.darray.StorageManager;
import rapaio.darray.layout.StrideLayout;

public class BaseDArrayManager extends DArrayManager {

    public BaseDArrayManager(int cpuThreads) {
        super(cpuThreads, StorageManager.array());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <N extends Number> DArray<N> stride(DType<N> dt, StrideLayout layout, Storage storage) {
        return (DArray<N>) switch (dt.id()) {
            case DOUBLE -> new BaseDoubleDArrayStride(this, layout, storage);
            case FLOAT -> new BaseFloatDArrayStride(this, layout, storage);
            case INTEGER -> new BaseIntDArrayStride(this, layout, storage);
            case BYTE -> new BaseByteDArrayStride(this, layout, storage);
        };
    }

    @Override
    public <N extends Number> DArray<N> random(DType<N> dt, Shape shape, Random random, Order order) {
        switch (dt.id()) {
            case DOUBLE, FLOAT -> {
                Normal normal = Normal.std();
                return zeros(dt, shape, Order.autoFC(order)).apply_(order, (_, _) -> dt.cast(normal.sampleNext(random)));
            }
            case INTEGER -> {
                return zeros(dt, shape, Order.autoFC(order)).apply_(order, (_, _) -> dt.cast(random.nextInt()));
            }
            case BYTE -> {
                byte[] buff = new byte[shape.size()];
                random.nextBytes(buff);
                return zeros(dt, shape, Order.autoFC(order)).apply_(order, (i, _) -> dt.cast(buff[i]));
            }
        }
        return null;
    }
}
