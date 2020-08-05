package rapaio.ml.param;

import rapaio.ml.regression.RegressionModel;
import rapaio.printer.Format;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/29/20.
 */
public abstract class ParamSet {

    private final TreeMap<String, Param<?, ? extends ParamSet>> parameterMap = new TreeMap<>();

    public ParamSet() {
    }

    public ParamSet(ParamSet source) {
        this.copyParameterValues(source);
    }

    public void registerParameter(Param<?, ? extends ParamSet> parameter) {
        if (parameterMap.containsKey(parameter.name())) {
            throw new IllegalArgumentException("Parameters contains a prameter with the same name.");
        }
        parameterMap.put(parameter.name(), parameter);
    }

    protected void copyParameterValues(ParamSet paramSet) {
        for (var e : parameterMap.entrySet()) {
            e.getValue().copyFrom(paramSet.parameterMap.get(e.getKey()));
        }
    }

    public String getParameterValues(boolean skipDefaults) {
        return parameterMap.values().stream()
                .filter(p -> !skipDefaults || !p.hasDefaultValue())
                .map(p -> p.name() + "=" + format(p))
                .collect(Collectors.joining(","));
    }

    private String format(Param<?, ? extends ParamSet> p) {

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
