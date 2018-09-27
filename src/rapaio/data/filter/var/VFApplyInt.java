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
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public class VFApplyInt implements VFilter {

    private static final long serialVersionUID = -9017598696178273627L;

    public static VFApplyInt with(Function<Integer, Integer> f) {
        return new VFApplyInt(f);
    }

    private final Function<Integer, Integer> f;

    private VFApplyInt(Function<Integer, Integer> f) {
        this.f = f;
    }

    @Override
    public void fit(Var var) {
    }

    @Override
    public Var apply(Var var) {
        var.stream().forEach(s -> s.setInt(f.apply(s.getInt())));
        return var;
    }
}
