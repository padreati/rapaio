package rapaio.core.tools;

import static rapaio.math.MTools.log2;

public class ConcreteColAverageEntropy extends AbstractSplit{

	@Override
	protected double getInfo(int start, double total, double[] totals, double[][] values, int rowLength,
			int colLength) {
		double gain = 0;
        for (int i = start; i < rowLength; i++) {
            for (int j = start; j < colLength; j++) {
                if (values[i][j] > 0)
                    gain += -log2(values[i][j] / totals[j]) * values[i][j] / total;
            }
        }
        return gain;
	}

	@Override
	protected int chooseLine(int row, int col) {
		return col;
	}

}
