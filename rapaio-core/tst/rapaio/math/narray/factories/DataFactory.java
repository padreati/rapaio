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

import rapaio.math.narray.DType;
import rapaio.math.narray.NArray;
import rapaio.math.narray.NArrayManager;
import rapaio.math.narray.Shape;

public abstract class DataFactory<N extends Number> {

    final NArrayManager engine;
    final NArrayManager.OfType<N> ofType;
    final DType<N> dType;
    final Random random = new Random(7654);


    public DataFactory(NArrayManager arrayManager, NArrayManager.OfType<N> ofType, DType<N> dType) {
        this.engine = arrayManager;
        this.ofType = ofType;
        this.dType = dType;
    }

    public NArrayManager engine() {
        return engine;
    }

    public DType<N> dType() {
        return dType;
    }

    public abstract N value(double x);

    public abstract N inc(N x);

    public abstract N sum(N x, N y);

    public NArray<N> scalar(N value) {
        return ofType.scalar(value);
    }

    public abstract NArray<N> seq(Shape shape);

    public abstract NArray<N> zeros(Shape shape);

    public abstract NArray<N> random(Shape shape);
}
