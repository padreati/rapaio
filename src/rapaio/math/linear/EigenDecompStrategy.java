package rapaio.math.linear;

public abstract class EigenDecompStrategy {
	public abstract EigenPair getEigenDecomp(RM s, int maxRuns, double tol);
}
