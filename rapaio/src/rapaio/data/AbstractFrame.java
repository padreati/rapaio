/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package rapaio.data;

import rapaio.data.collect.FIterator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Base class for a frame, which provides behavior for the utility
 * access methods based on row and column indexes.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public abstract class AbstractFrame implements Frame {

	@Override
	public double getValue(int row, int col) {
		return getCol(col).getValue(row);
	}

	@Override
	public double getValue(int row, String colName) {
		return getCol(colName).getValue(row);
	}

	@Override
	public void setValue(int row, int col, double value) {
		getCol(col).setValue(row, value);
	}

	@Override
	public void setValue(int row, String colName, double value) {
		getCol(colName).setValue(row, value);
	}

	@Override
	public int getIndex(int row, int col) {
		return getCol(col).getIndex(row);
	}

	@Override
	public int getIndex(int row, String colName) {
		return getCol(colName).getIndex(row);
	}

	@Override
	public void setIndex(int row, int col, int value) {
		getCol(col).setIndex(row, value);
	}

	@Override
	public void setIndex(int row, String colName, int value) {
		getCol(colName).setIndex(row, value);
	}

	@Override
	public String getLabel(int row, int col) {
		return getCol(col).getLabel(row);
	}

	@Override
	public String getLabel(int row, String colName) {
		return getCol(colName).getLabel(row);
	}

	@Override
	public void setLabel(int row, int col, String value) {
		getCol(col).setLabel(row, value);
	}

	@Override
	public void setLabel(int row, String colName, String value) {
		getCol(colName).setLabel(row, value);
	}

	@Override
	public boolean isMissing(int row, int col) {
		return getCol(col).isMissing(row);
	}

	@Override
	public boolean isMissing(int row, String colName) {
		return getCol(colName).isMissing(row);
	}

	@Override
	public boolean isMissing(int row) {
		for (String colName : getColNames()) {
			if (getCol(colName).isMissing(row)) return true;
		}
		return false;
	}

	@Override
	public void setMissing(int row, int col) {
		getCol(col).setMissing(row);
	}

	@Override
	public void setMissing(int row, String colName) {
		getCol(colName).setMissing(row);
	}

	@Override
	public FIterator getCycleIterator(int size) {
		return new FrameIterator(false, size, this);
	}

	@Override
	public FIterator getIterator(boolean complete) {
		return new FrameIterator(complete, getRowCount(), this);
	}

	@Override
	public FIterator getIterator() {
		return getIterator(false);
	}
}

class FrameIterator implements FIterator {

	private static final String DEFAULT_MAPPING_KEY = "$$DEFAULT$$";
	final boolean complete;
	final int size;
	final Frame frame;
	private final HashMap<String, Mapping> mappings = new HashMap<>();

	int pos = -1;
	int cyclePos = -1;

	FrameIterator(boolean complete, int size, Frame frame) {
		this.complete = complete;
		this.size = size;
		this.frame = frame;
	}

	@Override
	public boolean next() {
		while (pos < size - 1) {
			pos++;
			cyclePos++;
			if (cyclePos >= frame.getRowCount()) {
				cyclePos = 0;
			}
			if (complete && frame.isMissing(cyclePos)) continue;
			return true;
		}
		return false;
	}

	@Override
	public void reset() {
		pos = -1;
		cyclePos = -1;
	}

	@Override
	public int getRowId() {
		return frame.getRowId(cyclePos);
	}

	@Override
	public int getRow() {
		return cyclePos;
	}

	@Override
	public double getValue(int col) {
		return frame.getValue(cyclePos, col);
	}

	@Override
	public double getValue(String colName) {
		return frame.getValue(cyclePos, colName);
	}

	@Override
	public void setValue(int col, double value) {
		frame.setValue(cyclePos, col, value);
	}

	@Override
	public void setValue(String colName, double value) {
		frame.setValue(cyclePos, colName, value);
	}

	@Override
	public int getIndex(int col) {
		return frame.getIndex(cyclePos, col);
	}

	@Override
	public int getIndex(String colName) {
		return frame.getIndex(cyclePos, colName);
	}

	@Override
	public void setIndex(int col, int value) {
		frame.setIndex(cyclePos, col, value);
	}

	@Override
	public void setIndex(String colName, int value) {
		frame.setIndex(cyclePos, colName, value);
	}

	@Override
	public String getLabel(int col) {
		return frame.getLabel(cyclePos, col);
	}

	@Override
	public String getLabel(String colName) {
		return frame.getLabel(cyclePos, colName);
	}

	@Override
	public void setLabel(int col, String value) {
		frame.setLabel(cyclePos, col, value);
	}

	@Override
	public void setLabel(String colName, String value) {
		frame.setLabel(cyclePos, colName, value);
	}

	@Override
	public boolean isMissing(int col) {
		return frame.isMissing(cyclePos, col);
	}

	@Override
	public boolean isMissing(String colName) {
		return frame.isMissing(cyclePos, colName);
	}

	@Override
	public boolean isMissing() {
		return frame.isMissing(cyclePos);
	}

	@Override
	public void setMissing(int col) {
		frame.setMissing(cyclePos, col);
	}

	@Override
	public void setMissing(String colName) {
		frame.setMissing(cyclePos, colName);
	}

	@Override
	public void appendToMapping() {
		if (!mappings.containsKey(DEFAULT_MAPPING_KEY)) {
			mappings.put(DEFAULT_MAPPING_KEY, new Mapping());
		}
		mappings.get(DEFAULT_MAPPING_KEY).add(getRowId());
	}

	@Override
	public void appendToMapping(String key) {
		if (!mappings.containsKey(key)) {
			mappings.put(key, new Mapping());
		}
		mappings.get(key).add(getRowId());
	}

	@Override
	public int getMappingsCount() {
		return mappings.size();
	}

	@Override
	public Set<String> getMappingsKeys() {
		return mappings.keySet();
	}

	@Override
	public Mapping getMapping() {
		return mappings.get(DEFAULT_MAPPING_KEY);
	}

	@Override
	public Mapping getMapping(String key) {
		return mappings.get(key);
	}

	@Override
	public Frame getMappedFrame() {
		return new MappedFrame(frame.getSourceFrame(), getMapping());
	}

	@Override
	public Frame getMappedFrame(String key) {
		return new MappedFrame(frame.getSourceFrame(), getMapping(key));
	}

	@Override
	public Map<String, Frame> getMappedFrames() {
		Map<String, Frame> map = new HashMap<>();
		for (String key : getMappingsKeys()) {
			map.put(key, getMappedFrame(key));
		}
		return map;
	}
}