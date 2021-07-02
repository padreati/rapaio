/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.filter;

import rapaio.data.Var;
import rapaio.data.VarInt;
import rapaio.data.stream.VSpot;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;
import rapaio.util.function.Double2IntFunction;
import rapaio.util.function.Int2IntFunction;

import java.io.Serial;
import java.util.function.Function;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public class VToInt implements VFilter {

    public static VToInt byDefault() {
        return new VToInt(new DefaultByType());
    }

    public static VToInt bySpot(Function<VSpot, Integer> fun) {
        return new VToInt(new TransformSpot(fun));
    }

    public static VToInt fromDouble(Double2IntFunction fun) {
        return new VToInt(new TransformDouble(fun));
    }

    public static VToInt fromInt(Int2IntFunction fun) {
        return new VToInt(new TransformInt(fun));
    }

    public static VToInt byLabel(Function<String, Integer> fun) {
        return new VToInt(new TransformLabel(fun));
    }

    @Serial
    private static final long serialVersionUID = -699221182441440988L;
    private final Function<Var, Var> fun;

    private VToInt(Function<Var, Var> fun) {
        this.fun = fun;
    }

    @Override
    public Var apply(Var var) {
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
    public String toContent(Printer printer, POption<?>... options) {
        return "VToInt";
    }

    @Override
    public String toString() {
        return "VToInt";
    }
}
