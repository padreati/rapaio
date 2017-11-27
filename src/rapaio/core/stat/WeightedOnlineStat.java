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

package rapaio.core.stat;

/**
 * Weighted online mean and variance.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/10/17.
 */
public class WeightedOnlineStat {

    public static WeightedOnlineStat empty() {
        return new WeightedOnlineStat();
    }

    private double wsum = 0;
    private double wsum2 = 0;
    private double mean = 0;
    private double S = 0;

    public WeightedOnlineStat() {

    }

    public double weightSum() {
        return wsum;
    }

    public double mean() {
        return mean;
    }

    public double variance() {
        return S / wsum;
    }

    public WeightedOnlineStat update(double x, double w) {
        if (w == 0.0 || !Double.isFinite(x) || !Double.isFinite(w))
            return this;
        wsum += w;
        wsum2 += w * w;
        double meanOld = mean;
        mean = meanOld + (w / wsum) * (x - meanOld);
        S += w * (x - meanOld) * (x - mean);
        return this;
    }
}
