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

package rapaio.core.stat;

import rapaio.data.Var;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

import static rapaio.printer.format.Format.floatFlex;

/**
 * Computes sample kurtosis. Formulas for sample kurtosis are taken from wikipedia page
 * <a href="https://en.wikipedia.org/wiki/Kurtosis#Sample_kurtosis">Sample_kurtosis</a>.
 * <p>
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/10/18.
 */
public class Kurtosis implements Printable {

    public static Kurtosis of(Var x) {
        return new Kurtosis(x);
    }

    private final double g2;
    private final double b2;
    private final double G2;
    private final int rows;
    private final int complete;
    private final String varName;

    private Kurtosis(Var x) {

        rows = x.rowCount();
        varName = x.name();

        double mean = Mean.of(x).value();
        double n = 0;
        double m2 = 0.0;
        double m4 = 0.0;

        for (int i = 0; i < x.rowCount(); i++) {
            if (x.isMissing(i)) continue;
            n++;
            double diff = x.getDouble(i) - mean;
            m2 += Math.pow(diff, 2);
            m4 += Math.pow(diff, 4);
        }
        m2 /= n;
        m4 /= n;
        complete = (int) n;

        g2 = m4 / (m2 * m2) - 3;
        b2 = (g2 + 3) * Math.pow(1 - 1 / n, 2) - 3;
        G2 = ((n + 1) * g2 + 6) * (n - 1) / ((n - 2) * n - 3);
    }

    public double value() {
        return b2;
    }

    public double g2() {
        return g2;
    }

    public double b2() {
        return b2;
    }

    public double bigG2() {
        return G2;
    }

    @Override
    public String toString() {
        return "kurtosis[" + varName + "] = g2:" + floatFlex(g2) + ", b2:" + floatFlex(b2) + ", G2:" + floatFlex(G2);
    }

    @Override
    public String toContent(Printer printer, POption... options) {
        return "> kurtosis[" + varName + "]\n" +
                "total rows: " + rows + " (complete: " + complete + ", missing: " + (rows - complete) + ")\n" +
                "kurtosis (g2): " + floatFlex(g2) + "\n" +
                "kurtosis (b2): " + floatFlex(b2) + "\n" +
                "kurtosis (G2): " + floatFlex(G2) + "\n";
    }

    @Override
    public String toFullContent(Printer printer, POption... options) {
        return toContent(printer, options);
    }

    @Override
    public String toSummary(Printer printer, POption... options) {
        return toContent(printer, options);
    }
}
