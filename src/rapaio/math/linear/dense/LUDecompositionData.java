package rapaio.math.linear.dense;

import rapaio.math.linear.RM;

public class LUDecompositionData {
	private RM LU;
	private int rowCount;
	private int colCount;
	private int pivSign;
	private int[] piv;

	public LUDecompositionData() {
	}

	public RM getLU() {
		return LU;
	}

	public void setLU(RM lU) {
		LU = lU;
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public int getColCount() {
		return colCount;
	}

	public void setColCount(int colCount) {
		this.colCount = colCount;
	}

	public int getPivSign() {
		return pivSign;
	}

	public void setPivSign(int pivSign) {
		this.pivSign = pivSign;
	}

	public int[] getPiv() {
		return piv;
	}

	public void setPiv(int[] piv) {
		this.piv = piv;
	}
}