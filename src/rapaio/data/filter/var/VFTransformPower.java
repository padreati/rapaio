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

import rapaio.core.stat.GeometricMean;
import rapaio.data.Var;
import rapaio.data.filter.VFilter;

/**
 * Filter to create monotonic power transformations
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/11/14.
 */
public class VFTransformPower implements VFilter {

    private static final long serialVersionUID = -4496756339460112649L;
    private final double lambda;
    private double gm = 0.0;

    public VFTransformPower(double lambda) {
        this.lambda = lambda;
    }

    @Override
    public void fit(Var var) {
        GeometricMean mygm = GeometricMean.from(var);
        if (mygm.isDefined()) {
            gm = mygm.value();
        } else {
            throw new IllegalArgumentException("The transformed variable " + var.name() + "contains negative values, geometric mean cannot be computed");
        }
    }

    @Override
    public Var apply(Var var) {
        if (lambda == 0) {
            for (int i = 0; i < var.rowCount(); i++) {
                double x = var.getDouble(i);
                var.setDouble(i, gm * Math.log(x));
            }
        } else {
            for (int i = 0; i < var.rowCount(); i++) {
                double x = var.getDouble(i);
                var.setDouble(i, (Math.pow(x, lambda) - 1.0) / (lambda * Math.pow(gm, lambda - 1)));
            }
        }
        return var;
    }
}
