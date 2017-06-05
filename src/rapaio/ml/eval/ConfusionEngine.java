package rapaio.ml.eval;

import rapaio.data.Var;

public interface ConfusionEngine {
	public String getSummary();
	public double accuracy();
	public double error();
	public int acceptedCases();
	public int errorCases();
	public int completeCases();
	public int[][] matrix();
}
