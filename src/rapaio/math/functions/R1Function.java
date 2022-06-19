/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.math.functions;

import java.io.Serial;
import java.util.function.Function;

import rapaio.math.linear.DVector;

/**
 * Function in one dimension.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/27/17.
 */
public record R1Function(Function<Double, Double> f) implements RFunction {

    @Serial
    private static final long serialVersionUID = -2881307255778321959L;

    @Override
    public double apply(double... x) {
        return f.apply(x[0]);
    }

    @Override
    public double apply(DVector x) {
        return f.apply(x.get(0));
    }
}
