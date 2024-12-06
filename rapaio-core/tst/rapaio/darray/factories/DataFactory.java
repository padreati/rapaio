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
import rapaio.darray.DType;
import rapaio.darray.Shape;

public abstract class DataFactory<N extends Number> {

    final DType<N> dt;
    final DArrayManager manager;
    final Random random = new Random(7654);


    public DataFactory(DArrayManager manager, DType<N> dt) {
        this.manager = manager;
        this.dt = dt;
    }

    public DArrayManager engine() {
        return manager;
    }

    public DType<N> dt() {
        return dt;
    }

    public abstract N value(double x);

    public abstract N inc(N x);

    public abstract N sum(N x, N y);

    public DArray<N> scalar(N value) {
        return manager.scalar(dt, value.doubleValue());
    }

    public abstract DArray<N> seq(Shape shape);

    public abstract DArray<N> zeros(Shape shape);

    public abstract DArray<N> random(Shape shape);
}
