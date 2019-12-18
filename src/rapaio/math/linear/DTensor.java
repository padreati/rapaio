package rapaio.math.linear;

/**
 * Interface which models an N-dimensional double array.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 12/3/19.
 */
public interface DTensor {

    /**
     * @return number of dimensions
     */
    int ndim();

    /**
     * Returns the shape of the tensor. The shape is an array of
     * integers and in each position of the array it returns the length
     * of the tensor in that dimension.
     *
     * @return shape of tensor
     */
    int[] shape();

    /**
     * Gets value at the given position
     *
     * @param index position array index
     * @return value at the given position
     */
    double get(int... index);


}
