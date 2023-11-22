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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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
 * There are 3 types of computed sample skewness, according with:
 * <a href="https://www.rdocumentation.org/packages/e1071/versions/1.7-0/topics/skewness">R e1071 skewness</a>
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/10/18.
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
            m2 += Math.pow(diff, 2);
            m3 += Math.pow(diff, 3);
        }
        m2 /= n;
        m3 /= n;
        complete = (int) n;

        g1 = m3 / Math.pow(m2, 1.5);
        bigG1 = g1 * Math.sqrt(n * (n - 1)) / (n - 2);
        b1 = g1 * Math.pow((n - 1) / n, 1.5);
    }

    public double value() {
        return b1();
    }

    public double b1() {
        return b1;
    }

    public double g1() {
        return g1;
    }

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
