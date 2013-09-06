package rapaio.data;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class NumericOneVector extends NumericVector {

    public NumericOneVector(double value) {
        this("", value);
    }

    public NumericOneVector(String name, double value) {
        super(name, new double[]{value});
    }
}
