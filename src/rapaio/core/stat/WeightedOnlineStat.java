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

    public static WeightedOnlineStat from(WeightedOnlineStat... woss) {
        WeightedOnlineStat wos = new WeightedOnlineStat();
        for (WeightedOnlineStat w : woss) {
            wos.update(w);
        }
        return wos;
    }

    private int count = 0;
    private double wsum = 0;
    private double mean = 0;
    private double S = 0;

    public WeightedOnlineStat() {
    }

    public int count() {
        return count;
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
        if (w <= 0.0 || !Double.isFinite(x) || !Double.isFinite(w)) {
            return this;
        }
        if (count == 0) {
            wsum = w;
            mean = x;
            count = 1;
            S = 0;
            return this;
        }
        count++;
        wsum += w;
        double meanOld = mean;
        mean = meanOld + (w / wsum) * (x - meanOld);
        S += w * (x - meanOld) * (x - mean);
        return this;
    }

    public WeightedOnlineStat update(WeightedOnlineStat wos) {
        if (count == 0) {
            wsum = wos.wsum;
            mean = wos.mean;
            count = wos.count;
            S = wos.S;
            return this;
        }

        double delta = wos.mean - mean;
        double sumw = wos.wsum + wsum;
        mean = (mean * wsum + wos.mean * wos.wsum) / sumw;
        S += wos.S + (delta * delta * wsum * wos.wsum) / sumw;
        wsum = sumw;
        count += wos.count;
        return this;
    }
}
