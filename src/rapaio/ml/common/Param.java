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

package rapaio.ml.common;

import java.io.Serializable;

/**
 * Interface which model a parameter from a parameter set {@link ParamSet}.
 * <p>
 * The purpose of modeling parameters in this way is to be easier to add parameters
 * to algorithms without additional code for get, set, inherit values, default values,
 * copy constructors, etc.
 * <p>
 * Each parameter belongs to a parameter set and each algorithm or base class of algorithms must
 * derive from that base class. Declaring parameters as public final members of algorithms
 * makes easier to configure algorithms in IDE using autocompletion and names.
 * At the same time, declaring a parameter in a class at the construction time of the parameter
 * register itself in the parent parameter set wih default values.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/3/20.
 */
public interface Param<T, S extends ParamSet<S>> extends Serializable {

    T get();

    S set(T value);

    T defaultValue();

    boolean hasDefaultValue();

    S clear();

    @SuppressWarnings("unchecked")
    default void copyFrom(Param<?, ?> param) {
        set((T) param.get());
    }

    String name();

    boolean validate(T value);
}
