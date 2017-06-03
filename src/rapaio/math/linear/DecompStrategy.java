package rapaio.math.linear;

public abstract class DecompStrategy {
	
	public abstract EigenPair eigenDecomp(RM s, int maxRuns, double tol);
	
}
