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

package rapaio.data.preprocessing;

import java.io.Serial;

import rapaio.data.Var;
import rapaio.printer.Format;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/11/14.
 */
public class VarBoxCoxTransform extends AbstractVarTransform {

    public static VarBoxCoxTransform with(double lambda) {
        return new VarBoxCoxTransform(lambda, 0.0);
    }

    public static VarBoxCoxTransform with(double lambda, double shift) {
        return new VarBoxCoxTransform(lambda, shift);
    }

    @Serial
    private static final long serialVersionUID = 1914770412929840529L;
    private final double lambda;
    private final double shift;

    private VarBoxCoxTransform(double lambda, double shift) {
        this.lambda = lambda;
        this.shift = shift;
    }

    @Override
    public VarTransform newInstance() {
        return new VarBoxCoxTransform(lambda, shift);
    }

    public double lambda() {
        return lambda;
    }

    public double shift() {
        return shift;
    }

    @Override
    public Var coreApply(Var var) {
        if (lambda == 0)
            for (int i = 0; i < var.size(); i++) {
                var.setDouble(i, Math.log(var.getDouble(i) + shift));
            }
        else
            for (int i = 0; i < var.size(); i++) {
                var.setDouble(i, (Math.pow(var.getDouble(i) + shift, lambda) - 1) / lambda);
            }
        return var;
    }

    @Override
    public String toContent(Printer printer, POpt<?>... options) {
        return "VarBoxCoxTransform(lambda=" + Format.floatFlex(lambda) + ", shift:" + Format.floatFlex(shift) + ")";
    }

    @Override
    public String toString() {
        return "VarBoxCoxTransform(lambda=" + Format.floatFlex(lambda) + ", shift:" + Format.floatFlex(shift) + ")";
    }
}
