package rapaio.data;

import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public final class MathNumeric {

	public static Numeric sum(final Numeric num) {
		double sum = 0;
		for (int i = 0; i < num.rowCount(); i++) {
			sum += num.value(i);
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
			if (len < nums[i].rowCount())
				len = nums[i].rowCount();
		}
		Numeric c = new Numeric(len, len, 0);
		for (int i = 0; i < nums.length; i++) {
			for (int j = 0; j < len; j++) {
				c.setValue(j, c.value(j) + nums[i].value(j % nums[i].rowCount()));
			}
		}
		return c;
	}

	public static Numeric minus(final Numeric a, final Numeric b) {
		int len = StrictMath.max(a.rowCount(), b.rowCount());
		Numeric c = new Numeric(len);
		for (int i = 0; i < len; i++) {
			c.setValue(i, a.value(i % a.rowCount()) - b.value(i % b.rowCount()));
		}
		return c;
	}

	public static Numeric dot(final Numeric a, final Numeric b) {
		final int len = StrictMath.max(a.rowCount(), b.rowCount());
		Numeric c = new Numeric(len);
		for (int i = 0; i < len; i++) {
			c.setValue(i, a.value(i % a.rowCount()) * b.value(i % b.rowCount()));
		}
		return c;
	}

	public static Numeric dotSum(final Numeric a, final Numeric b) {
		final int len = StrictMath.max(a.rowCount(), b.rowCount());
		double sum = 0;
		for (int i = 0; i < len; i++) {
			sum += a.value(i % a.rowCount()) * b.value(i % b.rowCount());
		}
		Numeric c = new Numeric();
		c.addValue(sum);
		return c;
	}

	public static Numeric div(final Numeric a, final Numeric b) {
		final int len = StrictMath.max(a.rowCount(), b.rowCount());
		Numeric c = new Numeric(len);
		for (int i = 0; i < len; i++) {
			c.setValue(i, a.value(i % a.rowCount()) / b.value(i % b.rowCount()));
		}
		return c;
	}

	public static Numeric scale(final Numeric a) {
		final Numeric v = new Numeric(a.rowCount());
		double mean = mean(a).value(0);
		double sd = sd(a).value(0);
		for (int i = 0; i < v.rowCount(); i++) {
			v.setValue(i, (a.value(i) - mean) / sd);
		}
		return v;
	}

	public static Numeric pow(final Vector a, double pow) {
		Numeric v = new Numeric();
		for (int i = 0; i < a.rowCount(); i++) {
			v.addValue(StrictMath.pow(a.value(i), pow));
		}
		return v;
	}

}
