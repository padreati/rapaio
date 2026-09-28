/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

import static rapaio.printer.Format.floatFlex;

import rapaio.data.Var;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;

/**
 * Computes sample skewness. Formulas for sample skewness are taken from wikipedia page
 * <a href="https://en.wikipedia.org/wiki/Skewness#Sample_skewness">Sample_skewness</a>.
 * <p>
 * Three estimators are computed, named as in
 * <a href="https://www.rdocumentation.org/packages/e1071/versions/1.7-0/topics/skewness">R e1071 skewness</a>,
 * with {@code m_k} the central moments {@code sum((x - mean)^k) / n}:
 * <ul>
 * <li>{@link #g1()} {@code = m_3 / m_2^{3/2}}, the method of moments (plug-in) estimator of the
 * distribution skewness (e1071 type 1, {@code scipy.stats.skew} default, {@code OnlineStat.skewness()}).
 * This is what {@link #value()} returns.</li>
 * <li>{@link #bigG1()} {@code = g1 * sqrt(n (n - 1)) / (n - 2)}, the adjusted Fisher-Pearson coefficient,
 * unbiased under normality (e1071 type 2, {@code scipy.stats.skew(bias=False)}, SAS, SPSS, Excel).</li>
 * <li>{@link #b1()} {@code = g1 * ((n - 1) / n)^{3/2}}, the third moment over the cube of the sample
 * standard deviation (e1071 type 3, Minitab).</li>
 * </ul>
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/10/18.
 */
public class Skewness implements Printable {

    public static Skewness of(Var x) {
        return new Skewness(x);
    }

    private final double g1;
    private final double b1;
    private final double bigG1;
    private final int rows;
    private final int complete;
    private final String varName;

    private Skewness(Var x) {

        rows = x.size();
        varName = x.name();

        double mean = Mean.of(x).value();
        double n = 0;
        double m2 = 0.0;
        double m3 = 0.0;

        for (int i = 0; i < x.size(); i++) {
            if (x.isMissing(i)) continue;
            n++;
            double diff = x.getDouble(i) - mean;
            double diff2 = diff * diff;
            m2 += diff2;
            m3 += diff2 * diff;
        }
        m2 /= n;
        m3 /= n;
        complete = (int) n;

        g1 = m3 / Math.pow(m2, 1.5);
        bigG1 = g1 * Math.sqrt(n * (n - 1)) / (n - 2);
        b1 = g1 * Math.pow((n - 1) / n, 1.5);
    }

    /**
     * Default skewness estimator, {@link #g1()}: the plug-in estimator of the distribution skewness,
     * consistent with {@code OnlineStat.skewness()}.
     */
    public double value() {
        return g1;
    }

    /**
     * Method of moments estimator {@code m_3 / m_2^{3/2}} (e1071 type 1).
     */
    public double g1() {
        return g1;
    }

    /**
     * Third central moment over the cube of the sample standard deviation,
     * {@code g1 * ((n - 1) / n)^{3/2}} (e1071 type 3).
     */
    public double b1() {
        return b1;
    }

    /**
     * Adjusted Fisher-Pearson standardized moment coefficient
     * {@code g1 * sqrt(n (n - 1)) / (n - 2)}, unbiased under normality (e1071 type 2).
     */
    public double bigG1() {
        return bigG1;
    }

    @Override
    public String toString() {
        return "skewness[" + varName + "] = g1: " + floatFlex(g1) + ", b1: " + floatFlex(b1) + ", G1: " + floatFlex(bigG1);
    }

    @Override
    public String toContent(Printer printer, POpt<?>... options) {
        return "> skewness[" + varName + "]\n" +
                "total rows: " + rows + " (complete: " + complete + ", missing: " + (rows - complete) + ")\n" +
                "skewness (g1): " + floatFlex(g1) + "\n" +
                "skewness (b1): " + floatFlex(b1) + "\n" +
                "skewness (G1): " + floatFlex(bigG1) + "\n";
    }

    @Override
    public String toFullContent(Printer printer, POpt<?>... options) {
        return toContent(printer, options);
    }

    @Override
    public String toSummary(Printer printer, POpt<?>... options) {
        return toContent(printer, options);
    }
}
