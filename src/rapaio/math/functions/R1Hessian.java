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

package rapaio.math.functions;

import rapaio.math.linear.RM;
import rapaio.math.linear.RV;
import rapaio.math.linear.dense.SolidRM;

import java.util.function.Function;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/27/17.
 */
public class R1Hessian implements RHessian {

    private static final long serialVersionUID = -7499515114017044967L;

    private final Function<Double, Double> f;

    public R1Hessian(Function<Double, Double> f) {
        this.f = f;
    }

    @Override
    public RM apply(double... x) {
        return SolidRM.fill(1, 1, f.apply(x[0]));
    }

    @Override
    public RM apply(RV x) {
        return SolidRM.fill(1, 1, f.apply(x.get(0)));
    }
}
