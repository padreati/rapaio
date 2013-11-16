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

import static rapaio.core.BaseMath.max;
import static rapaio.core.BaseMath.min;

/**
 * Class which implements core online statistics. This class does not hold
 * values used for calculations, just the statistics itself and some additional
 * elements required for calculations.
 * <p/>
 * This class implements for now statistics for:
 * <ul>
 * <ui>min - minimum getValue</ui>
 * <ui>max - maximum getValue</ui>
 * <ui>mean - mean of the values</ui>
 * </ul>
 *
 * @author Aurelian Tutuianu
 */
@Deprecated
public class OnlineCoreStat {

    private int n = 0; // number of elements
    private double mean = 0; // running mean
    private double min = 0;
    private double max = 0;

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
        return mean;
    }

    /**
     * For now implement this method using only positive values for times. It
     * may be later modified in order to support negative values for times, with
     * the new meaning that we "remove" elements from calculations and as a side
     * effect to decrease the getValue of N;
     *
     * @param value getValue to be used to update statistics
     * @param times how many data points withe the same getValue to be used
     */
    public void update(double value, int times) {
        if (times == 0) {
            return;
        }
        if (n == 0) {
            max = value;
            min = value;
            mean = value;
            n = 1;
        } else {
            min = min(min, value);
            max = max(max, value);

            mean = (n * mean) / (n + times + 0.) + (times * value) / (n + times + 0.);
            n += times;
        }
    }
}
