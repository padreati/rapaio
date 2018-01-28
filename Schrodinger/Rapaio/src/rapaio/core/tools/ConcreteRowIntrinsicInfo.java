package rapaio.core.tools;

import static rapaio.math.MTools.log2;

public class ConcreteRowIntrinsicInfo extends AbstractSplit{

	@Override
	protected double getInfo(int start, double total, double[] totals, double[][] values, int rowLength,
			int colLength) {
		double splitInfo = 0;
		for (int i = start; i < totals.length; i++) {
            if (totals[i] > 0) {
                splitInfo += -log2(totals[i] / total) * totals[i] / total;
            }
        }
		return splitInfo;
	}

	@Override
	protected int chooseLine(int row, int col) {
		return row;
	}

}
