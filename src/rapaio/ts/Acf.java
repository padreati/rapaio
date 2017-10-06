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

package rapaio.ts;

import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.IdxVar;
import rapaio.data.NumVar;
import rapaio.data.Var;
import rapaio.printer.Printable;
import rapaio.printer.format.TextTable;
import rapaio.sys.WS;

/**
 * Sample AutoCorrelation Function
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/11/17.
 */
public class Acf implements Printable {

    private final Var ts;
    private final IdxVar lags;
    private final NumVar correlation;
    private final NumVar covariance;

    public static Acf from(Var ts, int maxLag) {
        return new Acf(ts, IdxVar.seq(0, maxLag).withName("lags"));
    }

    public static Acf from(Var ts, IdxVar lags) {
        return new Acf(ts, lags.withName("lags"));
    }

    private Acf(Var ts, IdxVar lags) {
        if (ts.stream().complete().count() != ts.rowCount()) {
            throw new IllegalArgumentException("Acf does not allow missing values.");
        }
        this.ts = ts.solidCopy();
        this.lags = lags.solidCopy();
        this.correlation = NumVar.fill(lags.rowCount(), 0).withName("correlation");
        this.covariance = NumVar.fill(lags.rowCount(), 0).withName("covariance");

        compute();
    }

    public NumVar correlation() {
        return correlation;
    }

    public NumVar covariance() {
        return covariance;
    }

    private void compute() {
        double mu = Mean.from(ts).value();
        double var = Variance.from(ts).biasedValue();
        for (int i = 0; i < lags.rowCount(); i++) {
            int lag = lags.index(i);
            double acf = 0.0;
            for (int j = 0; j < ts.rowCount() - lag; j++) {
                acf += (ts.value(j) - mu) * (ts.value(j + lag) - mu);
            }
            correlation.setValue(i, acf / (var * ts.rowCount()));
            covariance.setValue(i, acf / ts.rowCount());
        }
    }


    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Acf summary\n");
        sb.append("===========\n");
        sb.append("\n");

        TextTable tt = TextTable
                .newEmpty(lags.rowCount()+1, 3)
                .withHeaderRows(1)
                ;
        tt.set(0, 0, "Lag", 0);
        tt.set(0, 1, "correlation", 0);
        tt.set(0, 2, "covariance", 0);
        for (int i = 0; i < lags.rowCount(); i++) {
            tt.set(i+1, 0, lags.label(i), 1);
            tt.set(i+1, 1, WS.formatFlex(correlation.value(i)), 1);
            tt.set(i+1, 2, WS.formatFlex(covariance.value(i)), 1);
        }
        sb.append(tt.summary());
        sb.append("\n");
        return sb.toString();
    }
}
