package rapaio.ml.eval;

import rapaio.data.Var;

public class ConfusionAdapter implements ConfusionEngine{
	Confusion cfs;
	
	public ConfusionAdapter(Confusion cfs) {
		this.cfs = cfs;
	}

	@Override
	public String getSummary() {
		return cfs.getSummary();
	}

	@Override
	public double accuracy() {
		return cfs.accuracy();
	}

	@Override
	public double error() {
		return cfs.error();
	}

	@Override
	public int acceptedCases() {
		return cfs.acceptedCases();
	}

	@Override
	public int errorCases() {
		return cfs.errorCases();
	}

	@Override
	public int completeCases() {
		return cfs.completeCases();
	}

	@Override
	public int[][] matrix() {
		return cfs.matrix();
	}
	
}
