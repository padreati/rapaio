package rapaio.data.collect;

import rapaio.data.Mapping;
import rapaio.data.Vector;

import java.util.Map;
import java.util.Set;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public interface VIterator {

	public boolean next();

	public void reset();

	public int getRowId();

	public int getRow();

	public double getValue();

	public void setValue(double value);

	public int getIndex();

	public void setIndex(int value);

	public String getLabel();

	public void setLabel(String value);

	public boolean isMissing();

	public void setMissing();

	public void appendToMapping();

	public void appendToMapping(String key);

	public int getMappingsCount();

	public Set<String> getMappingsKeys();

	public Mapping getMapping();

	public Mapping getMapping(String key);

	public Vector getMappedVector();

	public Vector getMappedVector(String key);

	public Map<String, Vector> getMappedVectors();
}
