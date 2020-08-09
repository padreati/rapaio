package rapaio.ml.common;

/**
 * Interface used by any object which uses parameters to describe it's behavior.
 * <p>
 * It introduces a method similar with equals, called {@link #equalOnParams(T)}
 * which returns true if two classes of the same type have the same parameter values,
 * and, and a consequence will behave the
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/9/20.
 */
public interface ParametricEquals<T> {

    boolean equalOnParams(T object);
}
