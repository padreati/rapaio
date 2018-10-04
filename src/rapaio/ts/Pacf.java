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

import rapaio.core.stat.Maximum;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.printer.Printable;
import rapaio.printer.format.TextTable;
import rapaio.sys.WS;

/**
 * Partial auto correlation function
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/2/17.
 */
public class Pacf implements Printable {

    public static Pacf from(Var ts, int maxLag) {
        return new Pacf(ts, VarInt.seq(1, maxLag));
    }

    private final Var ts;
    private final VarInt lags;

    private VarDouble pacf;

    private Pacf(Var ts, VarInt indexes) {
        this.ts = ts;
        this.lags = indexes;

        computeDurbinLevinson();
    }

    private void computeDurbinLevinson() {
        Acf acf = Acf.from(ts, (int) Maximum.of(lags).value() + 1);
        VarDouble cor = acf.correlation();

        double a, b, c;
        int nlag = cor.rowCount()-1;

        double[] v = new double[nlag];
        double[] w = new double[nlag];
        pacf = VarDouble.empty(lags.rowCount()).withName("pacf");
        w[0] = cor.getDouble(1);
        pacf.setDouble(0, cor.getDouble(1));
        for (int ll = 1; ll < nlag; ll++) {
            a = cor.getDouble(ll + 1);
            b = 1.0;
            for (int i = 0; i < ll; i++) {
                a -= w[i] * cor.getDouble(ll - i);
                b -= w[i] * cor.getDouble(i + 1);
            }
            c = a / b;
            pacf.setDouble(ll, c);
            ;
            if (ll + 1 == nlag) break;
            w[ll] = c;
            for (int i = 0; i < ll; i++)
                v[ll - i - 1] = w[i];
            for (int i = 0; i < ll; i++)
                w[i] -= c * v[i];
        }
    }

    public VarInt lags() {
        return lags;
    }

    public VarDouble values() {
        return pacf;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Pacf summary\n");
        sb.append("===========\n");
        sb.append("\n");

        TextTable tt = TextTable
                .newEmpty(lags.rowCount() + 1, 2)
                .withHeaderRows(1);
        tt.set(0, 0, "Lag", 0);
        tt.set(0, 1, "pacf", 0);
        for (int i = 0; i < lags.rowCount(); i++) {
            tt.set(i + 1, 0, lags.getLabel(i), 1);
            tt.set(i + 1, 1, WS.formatFlex(pacf.getDouble(i)), 1);
        }
        sb.append(tt.summary());
        sb.append("\n");
        return sb.toString();
    }
}
