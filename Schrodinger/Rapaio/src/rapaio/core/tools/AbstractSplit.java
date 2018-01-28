package rapaio.core.tools;

public abstract class AbstractSplit {
	public double getSplitInfo(int start, int rowLength, int colLength, double[][] values) {
		double[] totals = new double[chooseLine(rowLength, colLength)];
        for (int i = start; i < rowLength; i++) {
            for (int j = start; j < colLength; j++) {
                totals[chooseLine(i, j)] += values[i][j];
            }
        }
        double total = 0;
        for (int i = start; i < totals.length; i++) {
            total += totals[i];
        }
        double splitInfo = 0;
        
        splitInfo = getInfo(start, total, totals, values, rowLength, colLength);
        
        return splitInfo;
	}

	protected abstract int chooseLine(int row, int col);
	protected abstract double getInfo(int start, double total, double[] totals, double[][] values, int rowLength, int colLength);
}
