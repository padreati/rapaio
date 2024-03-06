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

package rapaio.data.transform;

import java.io.Serial;
import java.util.function.Function;

import rapaio.data.Var;
import rapaio.data.VarInt;
import rapaio.data.stream.VSpot;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;
import rapaio.util.function.Double2IntFunction;
import rapaio.util.function.Int2IntFunction;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public class VarToIntTransform extends AbstractVarTransform {

    public static VarToIntTransform byDefault() {
        return new VarToIntTransform(new DefaultByType());
    }

    public static VarToIntTransform fromSpot(Function<VSpot, Integer> fun) {
        return new VarToIntTransform(new TransformSpot(fun));
    }

    public static VarToIntTransform fromDouble(Double2IntFunction fun) {
        return new VarToIntTransform(new TransformDouble(fun));
    }

    public static VarToIntTransform fromInt(Int2IntFunction fun) {
        return new VarToIntTransform(new TransformInt(fun));
    }

    public static VarToIntTransform fromLabel(Function<String, Integer> fun) {
        return new VarToIntTransform(new TransformLabel(fun));
    }

    @Serial
    private static final long serialVersionUID = -699221182441440988L;
    private final Function<Var, Var> fun;

    private VarToIntTransform(Function<Var, Var> fun) {
        this.fun = fun;
    }

    @Override
    public VarTransform newInstance() {
        return new VarToIntTransform(fun);
    }

    @Override
    public Var coreApply(Var var) {
        return fun.apply(var);
    }

    /* Implements default transformations */
    static class DefaultByType implements Function<Var, Var> {

        @Override
        public Var apply(Var var) {
            return switch (var.type()) {
                case DOUBLE -> new TransformDouble(x -> (int) Math.rint(x)).apply(var);
                case INT, BINARY -> new TransformInt(x -> x).apply(var);
                case NOMINAL, STRING -> new TransformLabel(x -> {
                    try {
                        return Integer.parseInt(x);
                    } catch (NumberFormatException ex) {
                        return VarInt.MISSING_VALUE;
                    }
                }).apply(var);
                default -> throw new IllegalArgumentException("Variable type: " + var.type().code() + " is not supported.");
            };
        }
    }


    static class TransformSpot implements Function<Var, Var> {
        private final Function<VSpot, Integer> function;

        TransformSpot(Function<VSpot, Integer> function) {
            this.function = function;
        }

        @Override
        public Var apply(Var var) {
            int[] value = new int[var.size()];
            for (int i = 0; i < var.size(); i++) {
                value[i] = var.isMissing(i) ? VarInt.MISSING_VALUE : function.apply(new VSpot(i, var));
            }
            return VarInt.wrap(value).name(var.name());
        }
    }

    static class TransformDouble implements Function<Var, Var> {
        private final Double2IntFunction function;

        TransformDouble(Double2IntFunction function) {
            this.function = function;
        }

        @Override
        public Var apply(Var var) {
            int[] value = new int[var.size()];
            for (int i = 0; i < var.size(); i++) {
                value[i] = var.isMissing(i) ? VarInt.MISSING_VALUE : function.applyAsInt(var.getDouble(i));
            }
            return VarInt.wrap(value).name(var.name());
        }
    }

    static class TransformInt implements Function<Var, Var> {
        private final Int2IntFunction function;

        TransformInt(Int2IntFunction function) {
            this.function = function;
        }

        @Override
        public Var apply(Var var) {
            int[] value = new int[var.size()];
            for (int i = 0; i < var.size(); i++) {
                value[i] = var.isMissing(i) ? VarInt.MISSING_VALUE : function.applyAsInt(var.getInt(i));
            }
            return VarInt.wrap(value).name(var.name());
        }
    }

    static class TransformLabel implements Function<Var, Var> {
        private final Function<String, Integer> function;

        TransformLabel(Function<String, Integer> function) {
            this.function = function;
        }

        @Override
        public Var apply(Var var) {
            int[] values = new int[var.size()];
            for (int i = 0; i < var.size(); i++) {
                values[i] = function.apply(var.getLabel(i));
            }
            return VarInt.wrap(values).name(var.name());
        }
    }

    @Override
    public String toContent(Printer printer, POpt<?>... options) {
        return "VToInt";
    }

    @Override
    public String toString() {
        return "VToInt";
    }
}
