package rapaio.core.stat;

import java.util.stream.IntStream;

import rapaio.data.Var;

/**
 * 
 * @author algoshipda
 *
 */
public abstract class QuantilesEstimator {
    public enum Type {
        R7,
        R8
    }
    
    public double[] estimate(Var complete, double[] percentiles) {
        if (complete.rowCount() <= 1) {
            return estimateTrivial(complete, percentiles);
        } else {
            return estimateNontrivial(complete, percentiles);
        }
    }
    
    private double[] estimateTrivial(Var complete, double[] percentiles) {
        if (complete.rowCount() == 0) {
            return IntStream.range(0, percentiles.length).mapToDouble(i -> Double.NaN).toArray();
        }
        else if (complete.rowCount() == 1) {
            double[] values = new double[percentiles.length];
            for (int i = 0; i < values.length; i++) {
                values[i] = complete.value(0);
            }
            return values;
        }
        return null;
    }
    
    protected abstract double[] estimateNontrivial(Var complete, double[] percentiles);

    public static QuantilesEstimator newInstance(Type type) {
        switch(type) {
            case R7:
                return new QuantilesEstimatorR7();
            case R8:
                return new QuantilesEstimatorR8();
            default:
                throw new IllegalArgumentException("Not implemented");
        }
    }
}