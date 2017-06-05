package rapaio.math.linear;

import rapaio.math.linear.dense.EigenDecomposition;
import rapaio.math.linear.dense.SolidRM;
import rapaio.math.linear.dense.SolidRV;

public class EigenDecompStatistics extends EigenDecompStrategy{

	@Override
	public EigenPair getEigenDecomp(RM s, int maxRuns, double tol) {
		int n = s.getColCount();
        EigenDecomposition evd = EigenDecomposition.from(s);

        double[] _values = evd.getRealEigenvalues();
        RM _vectors = evd.getV();

        RV values = SolidRV.empty(n);
        RM vectors = SolidRM.empty(n, n);

        for (int i = 0; i < values.count(); i++) {
            values.set(values.count() - i - 1, _values[i]);
        }
        for (int i = 0; i < vectors.getRowCount(); i++) {
            for (int j = 0; j < vectors.getColCount(); j++) {
                vectors.set(i, vectors.getColCount() - j - 1, _vectors.get(i, j));
            }
        }
        return EigenPair.from(values, vectors);
	}

}
