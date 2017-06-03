package rapaio.math.linear.dense;

import rapaio.math.linear.RM;

public abstract class RMSubstitutionStrategy {
	public abstract RM getSubstitution(RM X, double[][] L, int n, int nx);
}
