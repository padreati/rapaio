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

/**
 * A vector which is learn on the base of another vector and the row selection
 * and order is specified by a getMapping give at construction time.
 * <p/>
 * This vector does not hold actual values, it delegate the behavior to the
 * wrapped vector, thus the wrapping affects only the getRowCount selected anf the
 * order of these getRowCount.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class MappedVector extends AbstractVector {

	private final Vector source;
	private final Mapping mapping;

	public MappedVector(Vector source, Mapping mapping) {
		if (source.isMappedVector()) {
			throw new IllegalArgumentException("Now allowed mapped vector as source");
		}
		this.source = source;
		this.mapping = mapping;
	}

	@Override
	public VectorType getType() {
		return source.getType();
	}

	@Override
	public int getRowCount() {
		return mapping.size();
	}

	@Override
	public boolean isMappedVector() {
		return true;
	}

	@Override
	public Vector getSourceVector() {
		return source;
	}

	@Override
	public Mapping getMapping() {
		return mapping;
	}

	@Override
	public int getRowId(int row) {
		return source.getRowId(mapping.get(row));
	}

	@Override
	public double getValue(int row) {
		return source.getValue(mapping.get(row));
	}

	@Override
	public void setValue(int row, double value) {
		source.setValue(mapping.get(row), value);
	}

	@Override
	public void addValue(double value) {
		throw new IllegalArgumentException("operation not available on mapped vectors");
	}

	@Override
	public void addValue(int row, double value) {
		throw new IllegalArgumentException("operation not available on mapped vectors");
	}

	@Override
	public int getIndex(int row) {
		return source.getIndex(mapping.get(row));
	}

	@Override
	public void setIndex(int row, int value) {
		source.setIndex(mapping.get(row), value);
	}

	@Override
	public void addIndex(int value) {
		throw new IllegalArgumentException("operation not available on mapped vectors");
	}

	@Override
	public void addIndex(int row, int value) {
		throw new IllegalArgumentException("operation not available on mapped vectors");
	}

	@Override
	public String getLabel(int row) {
		return source.getLabel(mapping.get(row));
	}

	@Override
	public void setLabel(int row, String value) {
		source.setLabel(mapping.get(row), value);
	}

	@Override
	public void addLabel(String value) {
		throw new IllegalArgumentException("operation not available on mapped vectors");
	}

	@Override
	public void addLabel(int row, String value) {
		throw new IllegalArgumentException("operation not available on mapped vectors");
	}

	@Override
	public String[] getDictionary() {
		return source.getDictionary();
	}

	@Override
	public void setDictionary(String[] dict) {
		source.setDictionary(dict);
	}

	@Override
	public boolean isMissing(int row) {
		return source.isMissing(mapping.get(row));
	}

	@Override
	public void setMissing(int row) {
		source.setMissing(mapping.get(row));
	}

	@Override
	public void addMissing() {
		throw new IllegalArgumentException("operation not available on mapped vectors");
	}

	@Override
	public void remove(int row) {
		throw new IllegalArgumentException("operation not available on mapped vectors");
	}

	@Override
	public void removeRange(int from, int to) {
		throw new IllegalArgumentException("operation not available on mapped vectors");
	}

	@Override
	public void clear() {
		throw new IllegalArgumentException("operation not available on mapped vectors");
	}

	@Override
	public void trimToSize() {
		throw new IllegalArgumentException("operation not available on mapped vectors");
	}

	@Override
	public void ensureCapacity(int minCapacity) {
		throw new IllegalArgumentException("operation not available on mapped vectors");
	}
}
