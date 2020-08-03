package rapaio.ml.param;

import java.util.function.Function;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/29/20.
 */
public class ValueParam<T, S extends ParamSet> implements Param<T, S> {

    protected final S params;
    protected final T defaultValue;
    protected final String name;
    protected final String description;
    protected T value;
    protected final Function<T, Boolean> validator;

    public ValueParam(S params, T defaultValue, String name, String description, Function<T, Boolean> validator) {
        this.params = params;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.name = name;
        this.description = description;
        this.validator = validator;

        params.registerParameter(this);
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public S set(T value) {
        if (!validate(value)) {
            throw new RuntimeException("Value " + value + " for parameter " + name() + " is invalid!");
        }
        this.value = value;
        return params;
    }

    @Override
    public boolean hasDefaultValue() {
        return defaultValue().equals(value);
    }

    @Override
    public void clear() {
        value = defaultValue();
    }

    @Override
    public T defaultValue() {
        return defaultValue;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public boolean validate(T value) {
        return validator.apply(value);
    }
}
