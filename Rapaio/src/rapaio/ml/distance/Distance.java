package rapaio.ml.distance;

import rapaio.data.Frame;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public interface Distance {

    double measure(Frame df, int row1, int row2);

    double measure(Frame df1, int row1, Frame df2, int row2);
}
