package rapaio.ml.param;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/3/20.
 */
public interface Param<T, S extends ParamSet> {

    T get();

    S set(T value);

    T defaultValue();

    boolean hasDefaultValue();

    void clear();

    default void copyFrom(Param<?, ?> param) {
        set((T) param.get());
    }

    String name();

    String description();

    boolean validate(T value);
}
