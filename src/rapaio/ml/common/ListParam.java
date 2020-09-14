package rapaio.ml.common;

import rapaio.util.function.SBiFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/7/20.
 */
public class ListParam<T, S extends ParamSet<S>> implements Param<List<T>, S> {

    private static final long serialVersionUID = 4085743402578672837L;
    private final S params;
    private final List<T> defaultValues;
    private final List<T> values = new ArrayList<>();
    private final String name;
    private final String description;
    private final SBiFunction<List<T>, List<T>, Boolean> validator;

    public ListParam(S params, List<T> defaultValues, String name, String description, SBiFunction<List<T>, List<T>, Boolean> validator) {
        this.params = params;
        this.defaultValues = defaultValues;
        this.name = name;
        this.description = description;
        this.validator = validator;
        set(defaultValues);
    }

    @Override
    public List<T> get() {
        return values;
    }

    @Override
    public S set(List<T> values) {
        clear();
        if (!validate(values)) {
            throw new IllegalArgumentException("Parameter values are invalid.");
        }
        this.values.addAll(values);
        return params;
    }

    @SafeVarargs
    public final S set(T... arrayValues) {
        List<T> values = Arrays.asList(arrayValues);
        clear();
        if (!validate(values)) {
            throw new IllegalArgumentException("Parameter values are invalid.");
        }
        this.values.addAll(values);
        return params;
    }

    @SafeVarargs
    public final S add(T... values) {
        if (!validate(Arrays.asList(values))) {
            throw new IllegalArgumentException("Parameter values are invalid.");
        }
        this.values.addAll(Arrays.asList(values));
        return params;
    }

    @Override
    public List<T> defaultValue() {
        return defaultValues;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean hasDefaultValue() {
        if (defaultValues == null) {
            return true;
        }
        if (defaultValues.size() != values.size()) {
            return false;
        }
        for (int i = 0; i < defaultValues.size(); i++) {
            if (defaultValues.get(i) instanceof ParametricEquals) {
                boolean eq = ((ParametricEquals<T>) defaultValues.get(i)).equalOnParams(values.get(i));
                if (!eq) {
                    return false;
                }
            }
            if (!defaultValues.get(i).equals(values.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public S clear() {
        values.clear();
        return params;
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
    public boolean validate(List<T> newValues) {
        return validator.apply(this.values, newValues);
    }

    @Override
    public void copyFrom(Param<?, ?> param) {
        set((List<T>) param);
    }
}
