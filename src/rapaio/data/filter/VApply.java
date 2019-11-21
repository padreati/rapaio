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

package rapaio.data.filter;

import rapaio.data.Var;
import rapaio.data.stream.VSpot;

import java.util.function.Consumer;

/**
 * Apply a given consumer function over each spot of the variable.
 * The consumer can use all the methods from spot, which includes
 * updating the underlying values. Thus, a variable can be modified
 * after this call, to not update the original variable a copy of
 * the variable must be created before.
 *
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public class VApply implements VFilter {

    public static VApply with(Consumer<VSpot> consumer) {
        return new VApply(consumer);
    }

    private static final long serialVersionUID = 3929781693784001199L;
    private final Consumer<VSpot> consumer;

    private VApply(Consumer<VSpot> consumer) {
        this.consumer = consumer;
    }

    @Override
    public Var apply(Var var) {
        var.stream().forEach(consumer);
        return var;
    }

    @Override
    public String toString() {
        return "VApply";
    }
}
