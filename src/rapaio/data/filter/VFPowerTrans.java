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

import rapaio.data.Var;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/11/14.
 */
@Deprecated
public class VFPowerTrans extends VFAbstract {

    private final double lambda;
    private double gm = 0.0;

    public VFPowerTrans(double lambda) {
        this.lambda = lambda;
    }

    @Override
    public void fit(Var... vars) {
        checkSingleVar(vars);

    }

    @Override
    public Var apply(Var... vars) {
        checkSingleVar(vars);

        double gm = gm(vars[0]);
        return vars[0].spotStream().transValue(
                x -> (lambda == 0) ?
                        gm * Math.log(x) :
                        (Math.pow(x, lambda) - 1.0) / (lambda * Math.pow(gm, lambda - 1))
        ).toMappedVar();
    }

    private double gm(Var v) {
        double p = 1;
        double count = 0;
        for (int i = 0; i < v.rowCount(); i++) {
            if (!v.missing(i)) {
                count++;
            }
        }
        for (int i = 0; i < v.rowCount(); i++) {
            if (!v.missing(i)) {
                p *= Math.pow(v.value(i), 1 / count);
            }
        }
        return p;
    }
}
