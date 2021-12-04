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

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;

import rapaio.ml.model.RegressionModel;
import rapaio.printer.Format;

/**
 * Base class for any algorithm which wants to handle its parameters via {@link Param}.
 * <p>
 * It makes life easier for the algorithms because one can use them either
 * declared as public final, thus through autocomplete, and also can benefit
 * from generic facilities which can be implemented on the map of the parameters
 * (where parameters register themselves at construction time). One example
 * of this facility is {@link #getStringParameterValues(boolean)}, but any other
 * can be implemented.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/29/20.
 */
public abstract class ParamSet<T extends ParamSet<T>> implements Serializable {

    @Serial
    private static final long serialVersionUID = -3083758110942840984L;

    private final TreeMap<String, Param<?, T>> parameterMap = new TreeMap<>();

    public ParamSet() {
    }

    public ParamSet(T source) {
        this.copyParameterValues(source);
    }

    public TreeMap<String, Param<?, T>> getParameterMap() {
        return parameterMap;
    }

    public void registerParameter(Param<?, T> parameter) {
        if (parameterMap.containsKey(parameter.name())) {
            throw new IllegalArgumentException("Parameters contains a prameter with the same name.");
        }
        parameterMap.put(parameter.name(), parameter);
    }

    @SuppressWarnings("unchecked")
    protected T copyParameterValues(T paramSet) {
        for (var e : parameterMap.entrySet()) {
            e.getValue().copyFrom(paramSet.getParameterMap().get(e.getKey()));
        }
        return (T) this;
    }

    public String getStringParameterValues(boolean skipDefaults) {
        StringBuilder sb = new StringBuilder();
        for (var v : parameterMap.values()) {
            if (skipDefaults && v.hasDefaultValue()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(v.name()).append("=").append(format(v.get()));
        }
        return sb.toString();
    }

    private String format(Object value) {
        if (value instanceof RegressionModel<?, ?, ?>) {
            return ((RegressionModel<?, ?, ?>) value).fullName();
        }
        if (value instanceof Double) {
            return Format.floatFlex((double) value);
        }
        for (Method m : value.getClass().getMethods()) {
            if ("fullName".equals(m.getName()) || "name".equals(m.getName())) {
                try {
                    m.setAccessible(true);
                    return (String) m.invoke(value);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    return e.getMessage();
                }
            }
        }
        if (value instanceof Map) {
            StringBuilder inner = new StringBuilder();
            inner.append("{");
            for (var e : ((Map<?, ?>) value).entrySet()) {
                if (inner.length() > 1) {
                    inner.append(',');
                }
                inner.append(format(e.getKey())).append('=').append(format(e.getValue()));
            }
            inner.append("}");
            return inner.toString();
        }
        if (value instanceof Function<?, ?>) {
            return "Function()";
        }
        if (value instanceof Consumer<?>) {
            return "Consumer()";
        }
        return value.toString();
    }
}
