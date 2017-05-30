/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Var;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/30/15.
 */
public class VFStandardize extends AbstractVF {

    private static final long serialVersionUID = -2817341319523250499L;

    private double mean;
    private double sd;

    public VFStandardize() {
        this(Double.NaN, Double.NaN);
    }

    public VFStandardize(double mean) {
        this(mean, Double.NaN);
    }

    public VFStandardize(double mean, double sd) {
        this.mean = mean;
        this.sd = sd;
    }

    @Override
    public void fit(Var... vars) {
        checkSingleVar(vars);

        if (Double.isNaN(mean)) {
            mean = Mean.from(vars[0]).getValue();
        }
        if (Double.isNaN(sd)) {
            sd = Variance.from(vars[0]).sdValue();
        }
    }

    @Override
    public Var apply(Var... vars) {
        checkSingleVar(vars);
        if (!vars[0].getType().isNumeric()) {
            return vars[0];
        }
        if(Math.abs(sd)<1e-20)
            return vars[0];
        return vars[0].stream().transValue(x -> (x - mean) / sd).toMappedVar();
    }
}
