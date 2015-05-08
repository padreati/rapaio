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

package rapaio.core.stat;

import rapaio.printer.Printable;

import static rapaio.WS.formatFlex;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/13/15.
 */
public class WeightedOnlineStat implements Printable {

    double m1 = Double.NaN;
    double w1 = 0;
    double w2 = 0;

    public void update(double value, double weight) {
        if (Double.isNaN(m1)) {
            m1 = value;
            w1 = weight;
            w2 = weight * weight;
        } else {
            m1 = (m1 * w1 + value * weight) / (weight + w1);
            w1 += weight;
            w2 += weight * weight;
        }
    }

    public double mean() {
        return m1;
    }

    public double variance() {
        throw new IllegalArgumentException("not implemented");
    }

    @Override
    public void buildPrintSummary(StringBuilder sb) {
        sb.append("WeightedOnlineStat\n");
        sb.append("total weight: ").append(formatFlex(w1)).append("\n");
        sb.append("mean: ").append(formatFlex(mean())).append("\n");
    }
}
