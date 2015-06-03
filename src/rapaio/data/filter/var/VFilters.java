/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.data.filter.var;

import rapaio.core.distributions.Distribution;
import rapaio.data.Numeric;
import rapaio.data.Var;

import java.util.function.Function;

/**
 * Utility class which offers static shortcut methods for filters.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/21/15.
 */
public final class VFilters {

    public static Var jitter(Var x) {
        return new VFJitter().fitApply(x);
    }

    public static Var jitter(double sd, Var x) {
        return new VFJitter(sd).fitApply(x);
    }

    public static Var jitter(Distribution d, Var x) {
        return new VFJitter(d).fitApply(x);
    }

    public static Var divide(Var a, Var b) {
        Numeric result = Numeric.newEmpty();
        for (int i = 0; i < a.rowCount(); i++) {
            result.addValue(a.value(i) / b.value(i));
        }
        return result;
    }

    public static Var valueUpdate(Var a, Function<Double, Double> f) {
        return a.stream().mapToDouble().map(f::apply).boxed().collect(Numeric.collector());
    }
}
