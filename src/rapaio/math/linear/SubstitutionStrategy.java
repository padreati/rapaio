package rapaio.math.linear;

public abstract class SubstitutionStrategy {
	public abstract RM getSubstitution(int n, int nx, RM X, double[][] L);
}
