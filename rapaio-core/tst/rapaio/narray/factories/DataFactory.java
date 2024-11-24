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

import rapaio.narray.DType;
import rapaio.narray.NArray;
import rapaio.narray.NArrayManager;
import rapaio.narray.Shape;

public abstract class DataFactory<N extends Number> {

    final DType<N> dt;
    final NArrayManager manager;
    final Random random = new Random(7654);


    public DataFactory(NArrayManager manager, DType<N> dt) {
        this.manager = manager;
        this.dt = dt;
    }

    public NArrayManager engine() {
        return manager;
    }

    public DType<N> dt() {
        return dt;
    }

    public abstract N value(double x);

    public abstract N inc(N x);

    public abstract N sum(N x, N y);

    public NArray<N> scalar(N value) {
        return manager.scalar(dt, value.doubleValue());
    }

    public abstract NArray<N> seq(Shape shape);

    public abstract NArray<N> zeros(Shape shape);

    public abstract NArray<N> random(Shape shape);
}
