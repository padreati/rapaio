/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POpt;

/**
 * Sample AutoCorrelation Function
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/11/17.
 */
public final class Acf extends Correlation {

    private final VarDouble correlation;
    private final VarDouble covariance;

    public static Acf from(Var ts, int maxLag) {
        return new Acf(ts, VarInt.seq(0, maxLag).name("lags"));
    }

    public static Acf from(Var ts, VarInt lags) {
        return new Acf(ts, lags.name("lags"));
    }

    private Acf(Var ts, VarInt lags) {
        super(ts, lags);
        if (ts.stream().complete().count() != ts.size()) {
            throw new IllegalArgumentException("Acf does not allow missing values.");
        }
        this.correlation = VarDouble.fill(lags.size(), 0).name("correlation");
        this.covariance = VarDouble.fill(lags.size(), 0).name("covariance");

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
        for (int i = 0; i < lags.size(); i++) {
            int lag = lags.getInt(i);
            double acf = 0.0;
            for (int j = 0; j < ts.size() - lag; j++) {
                acf += (ts.getDouble(j) - mu) * (ts.getDouble(j + lag) - mu);
            }
            correlation.setDouble(i, acf / (var * ts.size()));
            covariance.setDouble(i, acf / ts.size());
        }
    }

    @Override
    public String toSummary(Printer printer, POpt<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("Acf summary\n");
        sb.append("===========\n");
        sb.append("\n");

        TextTable tt = TextTable.empty(lags.size() + 1, 3, 1, 0);
        tt.textCenter(0, 0, "Lag");
        tt.textCenter(0, 1, "correlation");
        tt.textCenter(0, 2, "covariance");
        for (int i = 0; i < lags.size(); i++) {
            tt.textRight(i + 1, 0, lags.getLabel(i));
            tt.floatFlex(i + 1, 1, correlation.getDouble(i));
            tt.floatFlex(i + 1, 2, covariance.getDouble(i));
        }
        sb.append(tt.getDynamicText(printer, options));
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String toContent(Printer printer, POpt<?>... options) {
        return toSummary(printer, options);
    }

    @Override
    public String toFullContent(Printer printer, POpt<?>... options) {
        return toSummary(printer, options);
    }
}
