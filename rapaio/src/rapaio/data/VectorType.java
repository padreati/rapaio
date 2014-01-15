package rapaio.data;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public enum VectorType {
    NUMERIC(true, false), INDEX(true, false), NOMINAL(false, true);

    private final boolean numeric;
    private final boolean nominal;

    VectorType(boolean numeric, boolean nominal) {
        this.numeric = numeric;
        this.nominal = nominal;
    }

    public boolean isNumeric() {
        return numeric;
    }

    public boolean isNominal() {
        return nominal;
    }
}
