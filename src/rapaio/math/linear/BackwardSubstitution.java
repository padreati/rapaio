package rapaio.math.linear;

public class BackwardSubstitution extends SubstitutionStrategy{

	@Override
	public RM getSubstitution(int n, int nx, RM X, double[][] L) {
		
        // Solve L'*X = Y;
		for (int k = n - 1; k >= 0; k--) {
            for (int j = 0; j < nx; j++) {
                for (int i = k + 1; i < n; i++) {
                    X.set(k, j, X.get(k, j) - X.get(i, j) * L[i][k]);
                }
                X.set(k, j, X.get(k, j) / L[k][k]);
            }
        }
		return X;
	}

}
