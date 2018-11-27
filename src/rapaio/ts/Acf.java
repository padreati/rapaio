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

package rapaio.ts;

import rapaio.core.stat.*;
import rapaio.data.*;
import rapaio.printer.*;
import rapaio.printer.format.*;

import static rapaio.printer.format.Format.*;

/**
 * Sample AutoCorrelation Function
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/11/17.
 */
public class Acf implements Printable {

    private final Var ts;
    private final VarInt lags;
    private final VarDouble correlation;
    private final VarDouble covariance;

    public static Acf from(Var ts, int maxLag) {
        return new Acf(ts, VarInt.seq(0, maxLag).withName("lags"));
    }

    public static Acf from(Var ts, VarInt lags) {
        return new Acf(ts, lags.withName("lags"));
    }

    private Acf(Var ts, VarInt lags) {
        if (ts.stream().complete().count() != ts.rowCount()) {
            throw new IllegalArgumentException("Acf does not allow missing values.");
        }
        this.ts = ts.solidCopy();
        this.lags = lags.solidCopy();
        this.correlation = VarDouble.fill(lags.rowCount(), 0).withName("correlation");
        this.covariance = VarDouble.fill(lags.rowCount(), 0).withName("covariance");

        compute();
    }

    public VarDouble correlation() {
        return correlation;
    }

    public VarDouble covariance() {
        return covariance;
    }

    private void compute() {
        double mu = Mean.of(ts).value();
        double var = Variance.of(ts).biasedValue();
        for (int i = 0; i < lags.rowCount(); i++) {
            int lag = lags.getInt(i);
            double acf = 0.0;
            for (int j = 0; j < ts.rowCount() - lag; j++) {
                acf += (ts.getDouble(j) - mu) * (ts.getDouble(j + lag) - mu);
            }
            correlation.setDouble(i, acf / (var * ts.rowCount()));
            covariance.setDouble(i, acf / ts.rowCount());
        }
    }


    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Acf summary\n");
        sb.append("===========\n");
        sb.append("\n");

        TextTableRenderer tt = TextTableRenderer.empty(lags.rowCount()+1, 3, 1, 0);
        tt.set(0, 0, "Lag", 0);
        tt.set(0, 1, "correlation", 0);
        tt.set(0, 2, "covariance", 0);
        for (int i = 0; i < lags.rowCount(); i++) {
            tt.set(i+1, 0, lags.getLabel(i), 1);
            tt.set(i+1, 1, floatFlex(correlation.getDouble(i)), 1);
            tt.set(i+1, 2, floatFlex(covariance.getDouble(i)), 1);
        }
        sb.append(tt.getDefaultText());
        sb.append("\n");
        return sb.toString();
    }
}
