/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.preprocessing;

import java.io.Serial;

import rapaio.core.stat.GeometricMean;
import rapaio.data.Var;
import rapaio.printer.Format;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

/**
 * Filter to create monotonic power transformations
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/11/14.
 */
public class VarPowerTransform extends AbstractVarTransform {

    public static VarPowerTransform with(double lambda) {
        return new VarPowerTransform(lambda);
    }

    @Serial
    private static final long serialVersionUID = -4496756339460112649L;
    private final double lambda;
    private double gm = 0.0;

    private VarPowerTransform(double lambda) {
        this.lambda = lambda;
    }

    @Override
    public VarTransform newInstance() {
        return new VarPowerTransform(lambda);
    }

    @Override
    public VarPowerTransform coreFit(Var var) {
        GeometricMean mygm = GeometricMean.of(var);
        if (mygm.isDefined()) {
            gm = mygm.value();
        } else {
            throw new IllegalArgumentException(
                    "The source variable " + var.name() + " contains negative values, geometric mean cannot be computed");
        }
        return this;
    }

    @Override
    public Var coreApply(Var var) {
        if (lambda == 0) {
            for (int i = 0; i < var.size(); i++) {
                double x = var.getDouble(i);
                var.setDouble(i, gm * Math.log(x));
            }
        } else {
            for (int i = 0; i < var.size(); i++) {
                double x = var.getDouble(i);
                var.setDouble(i, (Math.pow(x, lambda) - 1.0) / (lambda * Math.pow(gm, lambda - 1)));
            }
        }
        return var;
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        return "VarPowerTransform(lambda=" + Format.floatFlex(lambda) + ")";
    }

    @Override
    public String toString() {
        return "VarPowerTransform(lambda=" + Format.floatFlex(lambda) + ")";
    }

}
