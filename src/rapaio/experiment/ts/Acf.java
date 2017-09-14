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

package rapaio.experiment.ts;

import rapaio.core.stat.Covariance;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.NumericVar;
import rapaio.data.Var;
import rapaio.printer.Printable;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/11/17.
 */
public class Acf implements Printable {

    private final Var ts;
    private final int[] lags;
    private final double[] values;

    public static Acf from(Var ts, int maxLag) {
        return new Acf(ts, maxLag);
    }

    public Acf(Var ts, int maxLag) {
        if (ts.stream().complete().count() != ts.getRowCount()) {
            throw new IllegalArgumentException("Acf does not allow missing values.");
        }
        this.ts = ts.solidCopy();
        lags = new int[maxLag];
        values = new double[maxLag];

        compute();
    }

    public NumericVar getValues() {
        return NumericVar.wrap(values);
    }

    private void compute() {
        double mu = Mean.from(ts).getValue();
        double var = Variance.from(ts).getBiasedValue();
        for (int i = 1; i <= lags.length; i++) {
            lags[i-1] = i;
            double acf = 0.0;
            for (int j = 0; j < ts.getRowCount() - i; j++) {
                acf += (ts.getValue(j) - mu) * (ts.getValue(j + i) - mu);
            }
            values[i-1] = acf / (var * ts.getRowCount());
        }
    }


    @Override
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(NumericVar.wrap(values).getSummary());
        return sb.toString();
    }
}
