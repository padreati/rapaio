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
package rapaio.core.stat;

import static rapaio.core.BaseMath.*;

/**
 * Class which implements core online statistics. This class does not hold
 * values used for calculations, just the statistics itself and some additional
 * elements required for calculations.
 * <p/>
 * This is an extension over an algorithm presented by John D Cook
 * http://www.johndcook.com/skewness_kurtosis.html.
 * Which itself it is an extension of a method presented by Donald Knuth's The Art of Programming.
 * Which itself it was proposed by B.P Welford.
 * Which itself .. joking. This is the end of the recursion.
 * <p/>
 * This class provides online statistics for:
 * <ul>
 * <ui>min - minimum getValue</ui>
 * <ui>max - maximum getValue</ui>
 * <ui>mean - mean of the values</ui>
 * </ul>
 *
 * @author Aurelian Tutuianu
 */
public class StatOnline {

    int n; // number of elements
    double m1;
    double m2;
    double m3;
    double m4;
    double min = 0;
    double max = 0;

    public StatOnline() {
        clean();
    }

    public final void clean() {
        n = 0;
        min = 0;
        max = 0;
        m1 = 0;
        m2 = 0;
        m3 = 0;
        m4 = 0;
    }

    /**
     * For now implement this method using only positive values for times. It
     * may be later modified in order to support negative values for times, with
     * the new meaning that we "remove" elements from calculations and as a side
     * effect to decrease the getValue of N;
     *
     * @param x getValue to be used to update statistics
     */
    public void update(double x) {
        double delta, delta_n, delta_n2, term1;

        long n1 = n;
        n++;
        delta = x - m1;
        delta_n = delta / n;
        delta_n2 = delta_n * delta_n;
        term1 = delta * delta_n * n1;
        m1 += delta_n;
        m4 += term1 * delta_n2 * (n * n - 3 * n + 3) + 6 * delta_n2 * m2 - 4 * delta_n * m3;
        m3 += term1 * delta_n * (n - 2) - 3 * delta_n * m2;
        m2 += term1;
        min = min(min, x);
        max = max(max, x);
    }

    /**
     * @return the number of elements seen so far and used in calculation
     */
    public int getN() {
        return n;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getMean() {
        return m1;
    }

    public double getVariance() {
        return m2 / (n - 1.0);
    }

    public double getStandardDeviation() {
        return sqrt(getVariance());
    }

    public double getSkewness() {
        return sqrt(n) * m3 / pow(m2, 1.5);
    }

    public double getKurtosis() {
        return n * m4 / (m2 * m2) - 3.0;
    }

    public void apply(StatOnline a) {
        StatOnline combined = new StatOnline();
        combined.n += a.n + this.n;

        double delta = this.m1 - a.m1;
        double delta2 = delta * delta;
        double delta3 = delta * delta2;
        double delta4 = delta2 * delta2;

        combined.m1 = (a.n * a.m1 + this.n * this.m1) / combined.n;
        combined.m2 = a.m2 + this.m2 + delta2 * a.n * this.n / combined.n;
        combined.m3 = a.m3 + this.m3 + delta3 * a.n * this.n * (a.n - this.n) / (combined.n * combined.n);
        combined.m3 += 3.0 * delta * (a.n * this.m2 - this.n * a.m2) / combined.n;
        combined.m4 = a.m4 + this.m4 + delta4 * a.n * this.n * (a.n * a.n - a.n * this.n + this.n * this.n) /
                (combined.n * combined.n * combined.n);
        combined.m4 += 6.0 * delta2 * (a.n * a.n * this.m2 + this.n * this.n * a.m2) / (combined.n * combined.n) +
                4.0 * delta * (a.n * this.m3 - this.n * a.m3) / combined.n;
        combined.min = min(this.min, a.min);
        combined.max = max(this.max, a.max);

        n = combined.n;
        m1 = combined.m1;
        m2 = combined.m2;
        m3 = combined.m3;
        m4 = combined.m4;
        min = combined.min;
        max = combined.max;
    }

}
