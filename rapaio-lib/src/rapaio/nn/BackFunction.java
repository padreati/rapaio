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

package rapaio.nn;

import java.util.function.Consumer;
import java.util.function.Supplier;

import rapaio.darray.DArray;
import rapaio.darray.Order;

public abstract class BackFunction {

    public static BackFunction of(Tensor ref, Supplier<DArray<?>> supplier) {
        return new AdditiveBackFunction(ref, supplier);
    }

    public static BackFunction of(Tensor ref, Consumer<DArray<?>> updater) {
        return new UpdatingBackFunction(ref, updater);
    }

    protected Tensor ref;

    BackFunction(Tensor ref) {
        this.ref = ref;
    }

    public final Tensor ref() {
        return ref;
    }

    public abstract void apply();

    private static final class AdditiveBackFunction extends BackFunction {

        private final Supplier<DArray<?>> supplier;

        private AdditiveBackFunction(Tensor ref, Supplier<DArray<?>> supplier) {
            super(ref);
            this.supplier = supplier;
        }

        @Override
        public void apply() {
            ref.addGrad(supplier.get());
        }
    }

    private static final class UpdatingBackFunction extends BackFunction {
        private final Consumer<DArray<?>> updater;

        private UpdatingBackFunction(Tensor ref, Consumer<DArray<?>> updater) {
            super(ref);
            this.updater = updater;
        }

        public void apply() {
            if (ref.grad == null) {
                ref.setGrad(ref.tm.zerosArray(ref.tm.dt(), ref.shape(), Order.C));
            }
            updater.accept(ref.grad());
        }
    }
}