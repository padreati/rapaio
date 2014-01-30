package rapaio.data.collect;

import rapaio.data.Frame;
import rapaio.data.Mapping;

import java.util.Map;
import java.util.Set;

/**
 * Frame iterator. Allows collecting in mappings for later use.
 * <p/>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public interface FIterator {

	public boolean next();

	public void reset();

	public int getRowId();

	public int getRow();

	public double getValue(int col);

	public double getValue(String colName);

	public void setValue(int col, double value);

	public void setValue(String colName, double value);

	public int getIndex(int col);

	public int getIndex(String colName);

	public void setIndex(int col, int value);

	public void setIndex(String colName, int value);

	public String getLabel(int col);

	public String getLabel(String colName);

	public void setLabel(int col, String value);

	public void setLabel(String colName, String value);

	public boolean isMissing();

	public boolean isMissing(int col);

	public boolean isMissing(String colName);

	public void setMissing(int col);

	public void setMissing(String colName);

	public void appendToMapping();

	public void appendToMapping(String key);

	public int getMappingsCount();

	public Set<String> getMappingsKeys();

	public Mapping getMapping();

	public Mapping getMapping(String key);

	public Frame getMappedFrame();

	public Frame getMappedFrame(String key);

	public Map<String, Frame> getMappedFrames();
}
