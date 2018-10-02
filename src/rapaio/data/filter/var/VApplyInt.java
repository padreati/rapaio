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

import rapaio.data.Var;
import rapaio.data.filter.VFilter;

import java.util.function.Function;

/**
 * Apply a given transformation function over each integer value of the variable.
 * The integer values are updated after transformed. Thus, a variable can be modified
 * after this call, to not update the original variable a copy of
 * the variable must be created before.
 *
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public class VApplyInt implements VFilter {

    public static VApplyInt with(Function<Integer, Integer> f) {
        return new VApplyInt(f);
    }

    private static final long serialVersionUID = -9017598696178273627L;
    private final Function<Integer, Integer> f;

    private VApplyInt(Function<Integer, Integer> f) {
        this.f = f;
    }

    @Override
    public Var apply(Var var) {
        var.stream().forEach(s -> s.setInt(f.apply(s.getInt())));
        return var;
    }
}
