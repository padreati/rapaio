/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

package rapaio.experiment.math.functions;

import rapaio.math.linear.DV;
import rapaio.math.linear.dense.DVDense;

import java.util.function.Function;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/27/17.
 */
public class R1Derivative implements RDerivative {

    private static final long serialVersionUID = -8662264063600073136L;

    private final Function<Double, Double> f;

    public R1Derivative(Function<Double, Double> f) {
        this.f = f;
    }

    @Override
    public DV apply(double... x) {
        return DVDense.wrap(f.apply(x[0]));
    }

    @Override
    public DV apply(DV x) {
        return DVDense.wrap(f.apply(x.get(0)));
    }
}
