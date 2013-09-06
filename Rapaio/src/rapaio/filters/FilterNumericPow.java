package rapaio.filters;

import rapaio.data.NumericVector;
import rapaio.data.Vector;

import static rapaio.core.BaseMath.pow;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class FilterNumericPow implements Filter {

    public Vector filter(Vector source, double pow) {
        return filter(source, pow, 0);
    }

    public Vector filter(Vector source, double pow, double offset) {
        if (!source.isNumeric()) {
            throw new IllegalArgumentException("Source vector must be isNumeric");
        }
        Vector dest = new NumericVector(source.getName(), source.getRowCount());
        for (int i = 0; i < source.getRowCount(); i++) {
            dest.setValue(i, pow(offset + source.getValue(i), pow));
        }
        return dest;
    }
}
