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
import java.util.function.Consumer;
import java.util.function.Function;

import rapaio.data.Var;
import rapaio.data.stream.VSpot;
import rapaio.util.function.Double2DoubleFunction;
import rapaio.util.function.Int2IntFunction;

/**
 * Apply a given consumer function over each spot of the variable.
 * The consumer can use all the methods from spot, which includes
 * updating the underlying values. Thus, a variable can be modified
 * after this call, to not update the original variable a copy of
 * the variable must be created before.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public class VarApply extends AbstractVarTransform {

    public static VarApply onSpot(Consumer<VSpot> consumer) {
        return new VarApply(Type.SPOT, consumer, F_DOUBLE, F_INT, F_STRING);
    }

    public static VarApply onDouble(Double2DoubleFunction function) {
        return new VarApply(Type.DOUBLE, F_SPOT, function, F_INT, F_STRING);
    }

    public static VarApply onInt(Int2IntFunction function) {
        return new VarApply(Type.INT, F_SPOT, F_DOUBLE, function, F_STRING);
    }

    public static VarApply onLabel(Function<String, String> function) {
        return new VarApply(Type.LABEL, F_SPOT, F_DOUBLE, F_INT, function);
    }

    @Serial
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

    private VarApply(Type type,
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
    public VarApply newInstance() {
        return new VarApply(type, spotConsumer, doubleFunction, intFunction, stringFunction);
    }

    @Override
    public Var coreApply(Var var) {
        switch (type) {
            case SPOT -> var.stream().forEach(spotConsumer);
            case DOUBLE -> var.stream().forEach(s -> s.setDouble(doubleFunction.applyAsDouble(s.getDouble())));
            case INT -> var.stream().forEach(s -> s.setInt(intFunction.applyAsInt(s.getInt())));
            case LABEL -> var.stream().forEach(s -> s.setLabel(stringFunction.apply(s.getLabel())));
            default -> {
            }
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
