package rapaio.data;

import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public final class MathNumeric {

	public static Numeric sum(final Numeric num) {
		double sum = 0;
		for (int i = 0; i < num.getRowCount(); i++) {
			sum += num.getValue(i);
		}
		return new Numeric(1, 1, sum);
	}

	public static Numeric mean(final Numeric num) {
		return new Numeric(1, 1, new Mean(num).getValue());
	}

	public static Numeric sd(final Numeric num) {
		return new Numeric(1, 1, StrictMath.sqrt(new Variance(num).getValue()));
	}

	public static Numeric var(final Numeric num) {
		return new Numeric(1, 1, new Variance(num).getValue());
	}

	public static Numeric plus(final Numeric... nums) {
		int len = 0;
		for (int i = 0; i < nums.length; i++) {
			if (len < nums[i].getRowCount())
				len = nums[i].getRowCount();
		}
		Numeric c = new Numeric(len, len, 0);
		for (int i = 0; i < nums.length; i++) {
			for (int j = 0; j < len; j++) {
				c.setValue(j, c.getValue(j) + nums[i].getValue(j % nums[i].getRowCount()));
			}
		}
		return c;
	}

	public static Numeric minus(final Numeric a, final Numeric b) {
		Numeric c = new Numeric();
		VectorIterator aIt = a.getCyclingIterator(StrictMath.max(a.getRowCount(), b.getRowCount()));
		VectorIterator bIt = b.getCyclingIterator(StrictMath.max(a.getRowCount(), b.getRowCount()));
		while (aIt.next() && bIt.next()) {
			c.addValue(aIt.getValue() - bIt.getValue());
		}
		return c;
	}

	public static Numeric dot(final Numeric a, final Numeric b) {
		final int len = StrictMath.max(a.getRowCount(), b.getRowCount());
		Numeric c = new Numeric(len);
		for (int i = 0; i < len; i++) {
			c.setValue(i, a.getValue(i % a.getRowCount()) * b.getValue(i % b.getRowCount()));
		}
		return c;
	}

	public static Numeric dotSum(final Numeric a, final Numeric b) {
		final int len = StrictMath.max(a.getRowCount(), b.getRowCount());
		double sum = 0;
		for (int i = 0; i < len; i++) {
			sum += a.getValue(i % a.getRowCount()) * b.getValue(i % b.getRowCount());
		}
		Numeric c = new Numeric();
		c.addValue(sum);
		return c;
	}

	public static Numeric div(final Numeric a, final Numeric b) {
		final int len = StrictMath.max(a.getRowCount(), b.getRowCount());
		Numeric c = new Numeric(len);
		for (int i = 0; i < len; i++) {
			c.setValue(i, a.getValue(i % a.getRowCount()) / b.getValue(i % b.getRowCount()));
		}
		return c;
	}

	public static Numeric scale(final Numeric a) {
		final Numeric v = new Numeric(a.getRowCount());
		double mean = mean(a).getValue(0);
		double sd = sd(a).getValue(0);
		for (int i = 0; i < v.getRowCount(); i++) {
			v.setValue(i, (a.getValue(i) - mean) / sd);
		}
		return v;
	}

	public static Numeric pow(final Vector a, double pow) {
		Numeric v = new Numeric();
		for (int i = 0; i < a.getRowCount(); i++) {
			v.addValue(StrictMath.pow(a.getValue(i), pow));
		}
		return v;
	}

}
