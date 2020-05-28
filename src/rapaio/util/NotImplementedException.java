package rapaio.util;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/22/20.
 */
public class NotImplementedException extends RuntimeException {

    private static final long serialVersionUID = -7221120750981669838L;

    public NotImplementedException() {
    }

    public NotImplementedException(String message) {
        super(message);
    }
}
