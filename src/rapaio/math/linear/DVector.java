package rapaio.math.linear;

import rapaio.printer.Printable;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 12/3/19.
 */
public interface DVector extends DMatrix, Printable {

    /**
     *
     * @return vector size
     */
    int size();
}
