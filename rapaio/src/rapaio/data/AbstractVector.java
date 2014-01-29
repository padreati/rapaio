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

import java.util.HashMap;
import java.util.Set;

/**
 * Base class for a vector which enforces to read-only name given at construction time.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public abstract class AbstractVector implements Vector {

	@Override
	public String toString() {
		return "Vector{ size='" + getRowCount() + "\'}";
	}

	@Override
	public VectorIterator getIterator(boolean complete) {
		return new SolidVectorIterator(complete, getRowCount(), this);
	}

	@Override
	public VectorIterator getCyclingIterator(int size) {
		return new SolidVectorIterator(true, size, this);
	}
}

class SolidVectorIterator implements VectorIterator {

	private static final String DEFAULT_MAPPING_KEY = "$$DEFAULT$$";
	final boolean complete;
	final int size;
	final Vector vector;
	private final HashMap<String, Mapping> mappings = new HashMap<>();

	int pos = -1;
	int cyclePos = -1;

	SolidVectorIterator(boolean complete, int size, Vector vector) {
		this.complete = complete;
		this.size = size;
		this.vector = vector;
	}

	@Override
	public boolean next() {
		while (pos < size - 1) {
			pos++;
			cyclePos++;
			if (cyclePos >= vector.getRowCount()) {
				cyclePos = 0;
			}
			if (complete && vector.isMissing(cyclePos)) continue;
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
		return vector.getRowId(cyclePos);
	}

	@Override
	public int getRow() {
		return cyclePos;
	}

	@Override
	public double getValue() {
		return vector.getValue(cyclePos);
	}

	@Override
	public void setValue(double value) {
		vector.setValue(cyclePos, value);
	}

	@Override
	public int getIndex() {
		return vector.getIndex(cyclePos);
	}

	@Override
	public void setIndex(int value) {
		vector.setIndex(cyclePos, value);
	}

	@Override
	public String getLabel() {
		return vector.getLabel(cyclePos);
	}

	@Override
	public void setLabel(String value) {
		vector.setLabel(cyclePos, value);
	}

	@Override
	public boolean isMissing() {
		return vector.isMissing(cyclePos);
	}

	@Override
	public void setMissing() {
		vector.setMissing(cyclePos);
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
		return mappings.keySet().size();
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
}