/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.filter;

import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.Normal;
import rapaio.data.Var;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

import java.io.Serial;

/**
 * Applies a random noise from a given distribution to a numeric vector.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public class VJitter implements VFilter {

    /**
     * Builds a jitter filter with Gaussian distribution with mean=0 and sd=0.1
     */
    public static VJitter standard() {
        return new VJitter(Normal.std());
    }

    /**
     * Builds a jitter filter with Gaussian distribution with given mu and sd.
     *
     * @param mu mean of Gaussian distribution
     * @param sd standard deviation of Gaussian distribution
     */
    public static VJitter gaussian(double mu, double sd) {
        return new VJitter(Normal.of(mu, sd));
    }

    /**
     * Builds a jitter with noise randomly sampled from the given distribution.
     */
    public static VJitter with(Distribution d) {
        return new VJitter(d);
    }

    @Serial
    private static final long serialVersionUID = -8411939170432884225L;
    private final Distribution d;

    private VJitter(Distribution d) {
        this.d = d;
    }

    @Override
    public Var apply(Var var) {
        for (int i = 0; i < var.size(); i++) {
            var.setDouble(i, var.getDouble(i) + d.sampleNext());
        }
        return var;
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        return "VJitter(d=" + d.name() + ")";
    }

    @Override
    public String toString() {
        return "VJitter(d=" + d.name() + ")";
    }
}
