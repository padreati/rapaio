package rapaio.printer.opt;

import java.io.Serializable;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/25/20.
 */
public interface POption<T> extends Serializable {

    /**
     * Binds an option to a given set of printing options
     */
    void bind(POpts opts);

    /**
     * Produce the printing option
     */
    T apply(POpts opts);
}
