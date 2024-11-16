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

import rapaio.math.narray.NArray;
import rapaio.math.narray.NArrayManager;
import rapaio.math.narray.Order;
import rapaio.math.narray.Shape;

public final class DoubleDenseRow extends DoubleDense {

    public DoubleDenseRow(NArrayManager manager) {
        super(manager);
    }

    @Override
    public NArray<Double> seq(Shape shape) {
        return ofType.seq(shape, Order.C);
    }

    @Override
    public NArray<Double> zeros(Shape shape) {
        return ofType.zeros(shape, Order.C);
    }

    @Override
    public NArray<Double> random(Shape shape) {
        return ofType.random(shape, random, Order.C);
    }
}
