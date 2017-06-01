package rapaio.math.linear.dense;

import rapaio.math.linear.RM;

public class ForwardMultiply extends RMMultiplyStrategy{

	@Override
	public RM getMultiply(RM X, double[][] L, int n, int nx) {
		for (int k = 0; k < n; k++) {
            for (int j = 0; j < nx; j++) {
                for (int i = 0; i < k; i++) {
                    X.increment(k, j, -X.get(i, j) * L[k][i]);
                }
                X.set(k, j, X.get(k, j) / L[k][k]);
            }
        }
		return X;
	}

}
