package rapaio.util.function;

import java.util.function.DoubleUnaryOperator;

@FunctionalInterface
public interface Double2DoubleFunction extends SFunction<Double, Double>, DoubleUnaryOperator {

    @Override
    double applyAsDouble(double value);

    default Double apply(Double value) {
        return applyAsDouble(value);
    }
}
