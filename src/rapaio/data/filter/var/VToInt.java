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

package rapaio.data.filter.var;

import it.unimi.dsi.fastutil.doubles.Double2IntFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import rapaio.data.Var;
import rapaio.data.VarInt;
import rapaio.data.filter.VFilter;
import rapaio.data.stream.VSpot;

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
            switch (var.type()) {
                case DOUBLE:
                    return new VToInt.TransformDouble(x -> (int) Math.rint(x)).apply(var);
                case INT:
                case BINARY:
                    return new VToInt.TransformInt(x -> x).apply(var);
                case NOMINAL:
                case TEXT:
                    return new VToInt.TransformLabel(x -> {
                        try {
                            return Integer.parseInt(x);
                        } catch (NumberFormatException ex) {
                            return Integer.MIN_VALUE;
                        }
                    }).apply(var);
                default:
                    throw new IllegalArgumentException("Variable type: " + var.type().code() + " is not supported.");
            }
        }
    }


    static class TransformSpot implements Function<Var, Var> {
        private final Function<VSpot, Integer> function;

        TransformSpot(Function<VSpot, Integer> function) {
            this.function = function;
        }

        @Override
        public Var apply(Var var) {
            int[] value = new int[var.rowCount()];
            for (int i = 0; i < var.rowCount(); i++) {
                value[i] = var.isMissing(i) ? Integer.MIN_VALUE : function.apply(new VSpot(i, var));
            }
            return VarInt.wrap(value).withName(var.name());
        }
    }

    static class TransformDouble implements Function<Var, Var> {
        private final Double2IntFunction function;

        TransformDouble(Double2IntFunction function) {
            this.function = function;
        }

        @Override
        public Var apply(Var var) {
            int[] value = new int[var.rowCount()];
            for (int i = 0; i < var.rowCount(); i++) {
                value[i] = var.isMissing(i) ? Integer.MIN_VALUE : function.applyAsInt(var.getDouble(i));
            }
            return VarInt.wrap(value).withName(var.name());
        }
    }

    static class TransformInt implements Function<Var, Var> {
        private final Int2IntFunction function;

        TransformInt(Int2IntFunction function) {
            this.function = function;
        }

        @Override
        public Var apply(Var var) {
            int[] value = new int[var.rowCount()];
            for (int i = 0; i < var.rowCount(); i++) {
                value[i] = var.isMissing(i) ? Integer.MIN_VALUE : function.applyAsInt(var.getInt(i));
            }
            return VarInt.wrap(value).withName(var.name());
        }
    }

    static class TransformLabel implements Function<Var, Var> {
        private final Function<String, Integer> function;

        TransformLabel(Function<String, Integer> function) {
            this.function = function;
        }

        @Override
        public Var apply(Var var) {
            int[] values = new int[var.rowCount()];
            for (int i = 0; i < var.rowCount(); i++) {
                values[i] = function.apply(var.getLabel(i));
            }
            return VarInt.wrap(values).withName(var.name());
        }
    }

    @Override
    public String content() {
        return "VToInt";
    }

    @Override
    public String toString() {
        return content();
    }
}
