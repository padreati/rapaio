package rapaio.ml.param;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/3/20.
 */
public class MultiParam<K, T, S extends ParamSet> implements Param<Map<K, T>, S> {

    private final S params;
    private final TreeMap<K, T> defaultValue;
    private final TreeMap<K, T> valueMap;
    private final String name;
    private final String description;
    private final Function<Map<K, T>, Boolean> validator;

    public MultiParam(S params, Map<K, T> defaultValue, String name, String description,
                      Function<Map<K, T>, Boolean> validator) {
        this.params = params;
        this.defaultValue = new TreeMap<>(defaultValue);
        this.valueMap = new TreeMap<>(defaultValue);
        this.name = name;
        this.description = description;
        this.validator = validator;

        params.registerParameter(this);
    }

    @Override
    public Map<K, T> get() {
        return valueMap;
    }

    public T get(K key) {
        return valueMap.get(key);
    }

    @Override
    public S set(Map<K, T> value) {
        this.valueMap.clear();
        this.valueMap.putAll(value);
        return params;
    }

    public S set(K key, T value) {
        this.valueMap.put(key, value);
        return params;
    }

    @Override
    public Map<K, T> defaultValue() {
        return defaultValue;
    }

    @Override
    public boolean hasDefaultValue() {
        return defaultValue.equals(valueMap);
    }

    @Override
    public void clear() {
        valueMap.clear();
        valueMap.putAll(defaultValue);
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
    public boolean validate(Map<K, T> value) {
        return validator.apply(value);
    }
}
