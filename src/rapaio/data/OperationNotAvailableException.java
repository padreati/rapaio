package rapaio.data;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/29/19.
 */
public class OperationNotAvailableException extends RuntimeException {

    private static final long serialVersionUID = 3051557424272123529L;

    private static final String DEFAULT_MESSAGE = "Operation not available.";

    public OperationNotAvailableException() {
        super(DEFAULT_MESSAGE);
    }

    public OperationNotAvailableException(String message) {
        super(message);
    }
}
