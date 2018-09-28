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

package rapaio.core.distributions;

public abstract class AbstractDistribution implements Distribution {

    private static final long serialVersionUID = -1768002987965368978L;


    @Override
    public double quantile(double p) {
        return discrete() ? discreteQuantile(p) : continuousQuantile(p);
    }

    public double discreteQuantile(double p) {
        if (p == 1)
            return Double.POSITIVE_INFINITY;

        double cdf0 = cdf(0);
        if (p <= cdf0)
            return 0;

        // unbounded binary search
        int low = 0;
        int up = 1;

        // double up until we found a bound
        double cdf_up = cdf(up);
        while (cdf_up <= p) {
            up *= 2;
            cdf_up = cdf(up);
        }
        while (low < up) {
            int mid = Math.floorDiv(low + up, 2);
            if (mid == low)
                return up;
            double cdf_mid = cdf(mid);
            if (cdf_mid < p) {
                low = mid;
            } else {
                up = mid;
            }
        }

        return 0;
    }

    public double continuousQuantile(double p) {
        if (p == 1)
            return Double.POSITIVE_INFINITY;

        double cdf0 = cdf(0);
        if (p <= cdf0)
            return 0;

        // unbounded binary search
        double low = 0;
        double up = 1;

        // double up until we found a bound
        double cdf_up = cdf(up);
        while (cdf_up <= p) {
            up *= 2;
            cdf_up = cdf(up);
        }
        while (low < up) {
            double mid = (low + up) / 2;
            double cdf_mid = cdf(mid);
            double err = Math.abs(cdf_mid - cdf_up);
            if (err <= 1e-15)
                return up;
            if (cdf_mid < p) {
                if (low >= mid)
                    return up;
                low = mid;
            } else {
                if (up <= mid)
                    return up;
                up = mid;
            }
        }
        return 0;
    }


}
