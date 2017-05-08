package rapaio.math.linear.dense;

public class CholeskyDecompositionData {
	/**
	 * Array for internal storage of decomposition.
	 */
	private double[][] decompositionArray;
	/**
	 * Row and column dimension (square matrix).
	 */
	private int dimension;
	/**
	 * Symmetric and positive definite flag.
	 */
	private boolean isSymAndPositive;

	public CholeskyDecompositionData() {
	}

	public double[][] getDecompositionArray() {
		return decompositionArray;
	}
	
	public double getDecompositionArraySelect(int row, int col) {
		return decompositionArray[row][col];
	}

	public void setDecompositionArray(double[][] decompositionArray) {
		this.decompositionArray = decompositionArray;
	}
	
	public void setDecompositionArraySelect(int row, int col, double arrayValue) {
		this.decompositionArray[row][col] = arrayValue;
	}

	public int getDimension() {
		return dimension;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

	public boolean isSymAndPositive() {
		return isSymAndPositive;
	}

	public void setSymAndPositive(boolean isSymAndPositive) {
		this.isSymAndPositive = isSymAndPositive;
	}
}