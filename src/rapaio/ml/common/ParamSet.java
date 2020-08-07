package rapaio.ml.common;

import lombok.Getter;
import rapaio.ml.regression.RegressionModel;
import rapaio.printer.Format;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

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

    @Getter
    private final TreeMap<String, Param<?, T>> parameterMap = new TreeMap<>();

    public ParamSet() {
    }

    public ParamSet(T source) {
        this.copyParameterValues(source);
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
            sb.append(v.name()).append("=").append(format(v));
        }
        return sb.toString();
    }

    private String format(Param<?, T> p) {

        Object value = p.get();
        if (value instanceof RegressionModel) {
            return ((RegressionModel) value).fullName();
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
        if (value instanceof Function<?, ?>) {
            return "Function()";
        }
        if (value instanceof BiFunction<?, ?, ?>) {
            return "BiFunction()";
        }
        if (value instanceof BiConsumer<?, ?>) {
            return "BiCosumer()";
        }
        return p.get().toString();
    }
}
