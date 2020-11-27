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

package rapaio.ml.regression.linear;

import rapaio.core.stat.Variance;
import rapaio.data.Var;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/26/20.
 */
public enum Scaling {
    NONE {
        @Override
        public double compute(Var x) {
            return 1;
        }
    },
    SD {
        @Override
        public double compute(Var x) {
            return Variance.of(x).biasedSdValue();
        }
    },
    NORM {
        @Override
        public double compute(Var x) {
            return Math.sqrt(x.copy().op().apply(a -> a * a).op().nansum());
        }
    };

    public abstract double compute(Var x);
}
