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
 */

package rapaio.core.distributions;

/**
 * Bernoulli distribution
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class Bernoulli implements Distribution {

    private final double theta;

    public Bernoulli(double theta) {
        this.theta = theta;
    }

    @Override
    public boolean isDiscrete() {
        return true;
    }

    @Override
    public String getName() {
        return "Bernoulli(theta=" + theta + ")";
    }

    @Override
    public double pdf(double x) {
        return x < 1 ? 1 - theta : theta;
    }

    @Override
    public double cdf(double x) {
        if (x < 0)
            return 0;
        if (x < 1)
            return 1 - theta;
        return 1;
    }

    @Override
    public double quantile(double p) {
        return (p <= 1 - theta) ? 0 : 1;
    }

    @Override
    public double min() {
        return 0;
    }

    @Override
    public double max() {
        return 1;
    }

    @Override
    public double mean() {
        return theta;
    }

    @Override
    public double mode() {
        if ((1 - theta) > theta)
            return 0;
        if ((1 - theta) < theta)
            return 1;
        return 0.5; // this is possible?
    }

    @Override
    public double var() {
        return theta * (1 - theta);
    }

    @Override
    public double skewness() {
        return 1 / Math.sqrt((1 - theta) * theta);
    }

    @Override
    public double kurtosis() {
        double prod = (1 - theta) * theta;
        return (1 - 6 * prod) / prod;
    }

    @Override
    public double entropy() {
        return -theta * Math.log(theta) - (1 - theta) * Math.log(1 - theta);
    }
}
