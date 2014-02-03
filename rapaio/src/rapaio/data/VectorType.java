package rapaio.data;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public enum VectorType {
	NUMERIC(true, false, 0), INDEX(true, false, 1), NOMINAL(false, true, 2);

	private final boolean numeric;
	private final boolean nominal;
	private final int code;

	VectorType(boolean numeric, boolean nominal, int code) {
		this.numeric = numeric;
		this.nominal = nominal;
		this.code = code;
	}

	public boolean isNumeric() {
		return numeric;
	}

	public boolean isNominal() {
		return nominal;
	}

	public int getCode() {
		return code;
	}

	public static VectorType fromCode(int code) {
		for (VectorType t : values()) {
			if (t.getCode() == code) return t;
		}
		return null;
	}
}
