package rapaio.math.linear;

public class PdEigenDecompStatistics extends EigenDecompStrategy{

	@Override
	public EigenPair getEigenDecomp(RM s, int maxRuns, double tol) {
		// runs QR decomposition algorithm for maximum of iterations
        // to provide a solution which has other than diagonals under
        // tolerance

        // this works only for positive definite
        // here we check only symmetry
		//QR is not implemented
		/*
        if (s.getRowCount() != s.getColCount())
            throw new IllegalArgumentException("This eigen pair method works only for positive definite matrices");
        QR qr = s.qr();
        s = qr.getR().dot(qr.getQ());
        RM ev = qr.getQ();
        for (int i = 0; i < maxRuns - 1; i++) {
            qr = s.qr();
            s = qr.getR().dot(qr.getQ());
            ev = ev.dot(qr.getQ());
            if (inTolerance(s, tol))
                break;
        }
        return EigenPair.from(s.diag(), ev.solidCopy());
        */
		return null;
	}

}
