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

package rapaio.data.filter;

import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Var;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/30/15.
 */
@Deprecated
public class VFAbstractStandardize extends VFAbstract {

    private double mean;
    private double sd;

    public VFAbstractStandardize() {
        this(Double.NaN, Double.NaN);
    }

    public VFAbstractStandardize(double mean) {
        this(mean, Double.NaN);
    }

    public VFAbstractStandardize(double mean, double sd) {
        this.mean = mean;
        this.sd = sd;
    }

    @Override
    public void fit(Var... vars) {
        checkSingleVar(vars);

        if (Double.isNaN(mean)) {
            mean = new Mean(vars[0]).value();
        }
        if (Double.isNaN(sd)) {
            sd = new Variance(vars[0]).sdValue();
        }
    }

    @Override
    public Var apply(Var... vars) {
        checkSingleVar(vars);
        if (!vars[0].getType().isNumeric()) {
            return vars[0];
        }
        return vars[0].stream().transValue(x -> (x - mean) / sd).toMappedVar();
    }
}
