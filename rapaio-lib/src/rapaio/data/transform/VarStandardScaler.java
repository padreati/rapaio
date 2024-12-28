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

package rapaio.data.transform;

import java.io.Serial;

import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Var;
import rapaio.printer.Format;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/30/15.
 */
public class VarStandardScaler extends AbstractVarTransform {

    public static VarStandardScaler filter() {
        return new VarStandardScaler(Double.NaN, Double.NaN);
    }

    public static VarStandardScaler filter(double mean) {
        return new VarStandardScaler(mean, Double.NaN);
    }

    public static VarStandardScaler filter(double mean, double sd) {
        return new VarStandardScaler(mean, sd);
    }

    @Serial
    private static final long serialVersionUID = -2817341319523250499L;

    private final boolean hasMean;
    private final boolean hasSd;
    private double mean;
    private double sd;

    private VarStandardScaler(double mean, double sd) {
        hasMean = !Double.isNaN(mean);
        hasSd = !Double.isNaN(sd);
        this.mean = mean;
        this.sd = sd;
    }

    @Override
    public VarTransform newInstance() {
        return new VarStandardScaler(hasMean ? mean : Double.NaN, hasSd ? sd : Double.NaN);
    }

    @Override
    public VarStandardScaler coreFit(Var var) {
        if (!hasMean) {
            mean = Mean.of(var).value();
        }
        if (!hasSd) {
            sd = Variance.of(var).sdValue();
        }
        return this;
    }

    @Override
    public Var coreApply(Var var) {
        if (!var.type().isNumeric()) {
            return var;
        }
        if (Math.abs(sd) < 1e-20)
            return var;
        for (int i = 0; i < var.size(); i++) {
            double x = var.getDouble(i);
            var.setDouble(i, (x - mean) / sd);
        }
        return var;
    }

    @Override
    public String toContent(Printer printer, POpt<?>... options) {
        return toString();
    }

    @Override
    public String toString() {
        return "VarStandardScaler(mean="
                + Format.floatFlex(mean) + ", sd:"
                + Format.floatFlex(sd) + ")";
    }
}
