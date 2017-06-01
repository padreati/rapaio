package rapaio.math.linear.dense;

import rapaio.math.linear.RM;

public abstract class RMMultiplyStrategy {
	public abstract RM getMultiply(RM X, double[][] L, int n, int nx);
}
