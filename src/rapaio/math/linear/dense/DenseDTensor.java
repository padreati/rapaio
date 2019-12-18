package rapaio.math.linear.dense;

import rapaio.math.linear.DTensor;
import rapaio.util.collection.IntArrays;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 12/3/19.
 */
public class DenseDTensor implements DTensor {

    private final int ndim;
    private final int[] dim;
    private final double[] values;
    private final int[] stride;

    private DenseDTensor(int[] dim, double[] values) {
        ndim = dim.length;
        if (ndim == 0) {
            throw new IllegalArgumentException("Number of dimensions must be greater than 0.");
        }
        int last = IntArrays.product(dim, 0, ndim);
        if (last >= values.length) {
            throw new IllegalArgumentException("Values array is smaller than last index positions.");
        }
        this.dim = dim;
        this.values = values;
        this.stride = new int[ndim];
        int s = 1;
        for (int i = 0; i < ndim; i++) {
            stride[ndim - 1 - i] = s;
            s *= dim[ndim - 1 - i];
        }
    }

    @Override
    public int ndim() {
        return ndim;
    }

    @Override
    public int[] shape() {
        return dim;
    }

    @Override
    public double get(int... index) {
        if (index.length != ndim) {
            throw new IllegalArgumentException("Index does not have the same dimension as the tensor.");
        }
        return values[IntArrays.product(stride, index, 0, ndim)];
    }
}
