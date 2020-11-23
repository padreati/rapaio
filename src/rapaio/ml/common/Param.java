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

    String description();

    boolean validate(T value);
}
