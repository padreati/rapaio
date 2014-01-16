package rapaio.data.matrix;

import org.junit.Test;
import rapaio.data.Numeric;

import java.text.DecimalFormat;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class QRTest {

    @Test
    public void testQR() {
        Matrix x = new Matrix(new double[][]{
                {1, 1, 1},
                {1, 2, 4},
                {1, 3, 9},
                {1, 4, 16},
                {1, 5, 25}
        });

        Matrix y = new Matrix(
                new Numeric(new double[]{2.8, 3.2, 7.1, 6.8, 8.8}),
                new Numeric(new double[]{2.8, 3.2, 7.1, 6.8, 8.9})
        );

        QRDecomposition qr = new QRDecomposition(x);
//        System.out.println("Q");
//        qr.getQ().print(new DecimalFormat("0.000000000"), 14);
//        System.out.println("R");
//        qr.getR().print(new DecimalFormat("0.000000000"), 14);
//        System.out.println("QR");
//        qr.getQR().print(new DecimalFormat("0.000000000"), 14);
//        System.out.println("H");
//        qr.getH().print(new DecimalFormat("0.000000000"), 14);

        qr.solve(y).print(new DecimalFormat("0.000000000"), 14);

        x.print(new DecimalFormat("0.000"), 10);
        qr.getQ().times(qr.getR()).print(new DecimalFormat("0.000"), 10);
    }
}
