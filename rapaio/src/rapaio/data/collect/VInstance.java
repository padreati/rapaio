package rapaio.data.collect;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public interface VInstance {

	boolean isMissing();

	void setMissing();

	double getValue();

	void setValue(double value);
}
