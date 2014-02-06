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

import rapaio.data.collect.VInstance;
import rapaio.data.collect.VIterator;
import rapaio.data.mapping.Mapping;

import java.io.Serializable;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * Random access list of observed values (observations) for a specific variable.
 *
 * @author Aurelian Tutuianu
 */
public interface Vector extends Serializable {

	VectorType getType();

	boolean isMappedVector();

	Vector getSourceVector();

	Mapping getMapping();

	/**
	 * Number of observations contained by the vector.
	 *
	 * @return size of vector
	 */
	int getRowCount();

	/**
	 * Returns observation identifier which is an integer.
	 * <p>
	 * When a vector or frame is created from scratch as a solid vector/frame then
	 * row identifiers are the row numbers. When the vector/frame wraps other
	 * vector/frame then row identifier is the wrapped row identifier.
	 * <p>
	 * This is mostly used to keep track of the original row numbers even after a series
	 * of transformations which use wrapped vectors/frames.
	 *
	 * @param row row for which row identifier is returned
	 * @return row identifier
	 */
	int getRowId(int row);

	/**
	 * Returns numeric setValue for the observation specified by {@code row}.
	 * <p>
	 * Returns valid values for numerical vector types, otherwise the method
	 * returns unspeified values.
	 *
	 * @param row
	 * @return numerical setValue
	 */
	double getValue(int row);

	/**
	 * Set numeric setValue for the observation specified by {@param row} to {@param setValue}.
	 * <p>
	 * Returns valid values for numerical vector types, otherwise the method
	 * returns unspeified values.
	 *
	 * @param row   position of the observation
	 * @param value numeric setValue from position {@param row}
	 */
	void setValue(int row, double value);


	void addValue(double value);

	void addValue(int row, double value);

	/**
	 * Returns getIndex setValue for the observation specified by {@param row}
	 *
	 * @param row position of the observation
	 * @return getIndex setValue
	 */
	int getIndex(int row);

	/**
	 * Set getIndex setValue for the observation specified by {@param row}.
	 *
	 * @param row   position of the observation
	 * @param value getIndex setValue for the observation
	 */
	void setIndex(int row, int value);

	void addIndex(int value);

	void addIndex(int row, int value);

	/**
	 * Returns nominal getLabel for the observation specified by {@param row}.
	 *
	 * @param row position of the observation
	 * @return getLabel setValue for the observation
	 */
	String getLabel(int row);

	/**
	 * Set nominal getLabel for the observation specified by {@param row}.
	 *
	 * @param row   position of the observation
	 * @param value getLabel setValue of the observation
	 */
	void setLabel(int row, String value);

	void addLabel(String value);

	void addLabel(int row, String value);

	/**
	 * Returns the term getDictionary used by the nominal values.
	 * <p>
	 * Term getDictionary contains all the nominal labels used by
	 * observations and might contain also additional nominal labels.
	 * Term getDictionary defines the domain of the definition for the nominal vector.
	 * <p>
	 * The term getDictionary contains nominal labels sorted in lexicografical order,
	 * so binary search techniques may be used on this vector.
	 * <p>
	 * For other vector types like numerical ones this method returns nothing.
	 *
	 * @return term getDictionary defined by the nominal vector.
	 */
	String[] getDictionary();

	void setDictionary(String[] dict);

	/**
	 * Returns true if the setValue for the observation specified by {@param row} is missing, not available.
	 * <p>
	 * A missing setValue for the observation means taht the measurement
	 * was not completed or the result of the measurement was not documented,
	 * thus the setValue is not available for analysis.
	 *
	 * @param row position of the observation
	 * @return true if the observation measurement is not specified
	 */
	boolean isMissing(int row);

	/**
	 * Set the setValue of the observation specified by {@param row} as missing, not available for analysis.
	 *
	 * @param row position of the observation.
	 */
	void setMissing(int row);

	void addMissing();

	void remove(int row);

	void removeRange(int from, int to);

	void clear();

	void trimToSize();

	void ensureCapacity(int minCapacity);

	public VIterator getIterator();

	public VIterator getIterator(boolean complete);

	public VIterator getCycleIterator(int size);

	public Stream<VInstance> getStream();

	public DoubleStream getDoubleStream();
}
