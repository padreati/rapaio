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

package rapaio.data.filter;

import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Var;
import rapaio.printer.Format;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/30/15.
 */
public class VStandardize implements VFilter {

    public static VStandardize filter() {
        return new VStandardize(Double.NaN, Double.NaN);
    }

    public static VStandardize filter(double mean) {
        return new VStandardize(mean, Double.NaN);
    }

    public static VStandardize filter(double mean, double sd) {
        return new VStandardize(mean, sd);
    }

    private static final long serialVersionUID = -2817341319523250499L;

    private double mean;
    private double sd;

    public VStandardize(double mean, double sd) {
        this.mean = mean;
        this.sd = sd;
    }

    @Override
    public VStandardize fit(Var var) {
        if (Double.isNaN(mean)) {
            mean = Mean.of(var).value();
        }
        if (Double.isNaN(sd)) {
            sd = Variance.of(var).sdValue();
        }
        return this;
    }

    @Override
    public Var apply(Var var) {
        if (!var.type().isNumeric()) {
            return var;
        }
        if (Math.abs(sd) < 1e-20)
            return var;
        for (int i = 0; i < var.rowCount(); i++) {
            double x = var.getDouble(i);
            var.setDouble(i, (x - mean) / sd);
        }
        return var;
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        return toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("VStandardize(mean=");
        sb.append(Format.floatFlex(mean)).append(", sd:");
        sb.append(Format.floatFlex(sd)).append(")");
        return sb.toString();
    }
}
