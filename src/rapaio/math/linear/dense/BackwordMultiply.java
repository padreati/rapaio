package rapaio.math.linear.dense;

import rapaio.math.linear.RM;

public class BackwordMultiply extends RMMultiplyStrategy{

	@Override
	public RM getMultiply(RM X, double[][] L, int n, int nx) {
		for (int k = n - 1; k >= 0; k--) {
            for (int j = 0; j < nx; j++) {
                for (int i = k + 1; i < n; i++) {
                    X.increment(k, j, -X.get(i, j) * L[i][k]);
                }
                X.set(k, j, X.get(k, j) / L[k][k]);
            }
        }
		return X;
	}
}
