package rapaio.math.linear;

public class ForwardSubstitution extends SubstitutionStrategy{

	@Override
	public RM getSubstitution(int n, int nx, RM X, double[][] L) {

        // Solve L*Y = B;
		for (int k = 0; k < n; k++) {
            for (int j = 0; j < nx; j++) {
                for (int i = 0; i < k; i++) {
                    X.set(k, j, X.get(k, j) - X.get(i, j) * L[k][i]);
                }
                X.set(k, j, X.get(k, j) / L[k][k]);
            }
        }
		return X;
	}
	
}
