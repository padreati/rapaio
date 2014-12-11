/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.data.filter.var;

import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.Normal;
import rapaio.data.Var;

/**
 * Applies a random noise from a given distribution to a numeric vector.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public class VFJitter extends AbstractVF {

    private final Distribution d;

    /**
     * Builds a jitter filter with Gaussian distribution with mean=0 and sd=0.1
     */
    public VFJitter() {
        this(new Normal(0, 0.1));
    }

    /**
     * Builds a jitter filter with a Gaussian distribution with mean=0 and given standard deviation
     *
     * @param sd standard deviation of zero mean Gaussian noise
     */
    public VFJitter(double sd) {
        this(new Normal(0, sd));
    }

    /**
     * Builds a jitter filter with noise generated from given distribution
     *
     * @param d noise distribution
     */
    public VFJitter(Distribution d) {
        super(false);
        this.d = d;
    }

    @Override
    public void fit(Var... vars) {
        checkSingleVar(vars);
    }

    @Override
    public Var apply(Var... vars) {
        checkSingleVar(vars);
        for (int i = 0; i < vars[0].rowCount(); i++) {
            vars[0].setValue(i, vars[0].value(i) + d.sampleNext());
        }
        return vars[0];
    }
}
