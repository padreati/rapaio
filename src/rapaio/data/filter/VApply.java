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

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import rapaio.data.Var;
import rapaio.data.stream.VSpot;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Apply a given consumer function over each spot of the variable.
 * The consumer can use all the methods from spot, which includes
 * updating the underlying values. Thus, a variable can be modified
 * after this call, to not update the original variable a copy of
 * the variable must be created before.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public class VApply implements VFilter {

    public static VApply onSpot(Consumer<VSpot> consumer) {
        return new VApply(Type.SPOT, consumer, F_DOUBLE, F_INT, F_STRING);
    }

    public static VApply onDouble(Double2DoubleFunction function) {
        return new VApply(Type.DOUBLE, F_SPOT, function, F_INT, F_STRING);
    }

    public static VApply onInt(Int2IntFunction function) {
        return new VApply(Type.INT, F_SPOT, F_DOUBLE, function, F_STRING);
    }

    public static VApply onLabel(Function<String, String> function) {
        return new VApply(Type.LABEL, F_SPOT, F_DOUBLE, F_INT, function);
    }

    private static final long serialVersionUID = 3929781693784001199L;

    private static final Consumer<VSpot> F_SPOT = vSpot -> {
    };
    private static final Double2DoubleFunction F_DOUBLE = key -> key;
    private static final Int2IntFunction F_INT = key -> key;
    private static final Function<String, String> F_STRING = key -> key;

    private final Type type;
    private final Consumer<VSpot> spotConsumer;
    private final Double2DoubleFunction doubleFunction;
    private final Int2IntFunction intFunction;
    private final Function<String, String> stringFunction;

    private VApply(Type type,
                   Consumer<VSpot> spotConsumer,
                   Double2DoubleFunction doubleFunction,
                   Int2IntFunction intFunction,
                   Function<String, String> stringFunction) {
        this.type = type;
        this.spotConsumer = spotConsumer;
        this.doubleFunction = doubleFunction;
        this.intFunction = intFunction;
        this.stringFunction = stringFunction;
    }

    @Override
    public Var apply(Var var) {
        switch (type) {
            case SPOT:
                var.stream().forEach(spotConsumer);
                break;
            case DOUBLE:
                var.stream().forEach(s -> s.setDouble(doubleFunction.applyAsDouble(s.getDouble())));
                break;
            case INT:
                var.stream().forEach(s -> s.setInt(intFunction.applyAsInt(s.getInt())));
                break;
            case LABEL:
                var.stream().forEach(s -> s.setLabel(stringFunction.apply(s.getLabel())));
                break;
            default:

        }
        return var;
    }

    private enum Type {
        SPOT,
        DOUBLE,
        INT,
        LABEL
    }
}
