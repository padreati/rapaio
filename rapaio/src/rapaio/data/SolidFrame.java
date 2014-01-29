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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * A frame which is not mapped, its values are contained in vectors.
 *
 * @author Aurelian Tutuianu
 */
public class SolidFrame extends AbstractFrame {

	private final int rows;
	private final Vector[] vectors;
	private final HashMap<String, Integer> colIndex;
	private final String[] names;

	public SolidFrame(int rows, List<Vector> vectors, List<String> names) {
		this(rows, vectors, names.toArray(new String[]{}));
	}

	public SolidFrame(int rows, List<Vector> vectors, String[] names) {
		for (int i = 0; i < vectors.size(); i++) {
			if (vectors.get(i).isMappedVector())
				throw new IllegalArgumentException("Not allowed mapped vectors in solid frame");
		}
		this.rows = rows;
		this.vectors = new Vector[vectors.size()];
		this.colIndex = new HashMap<>();
		this.names = new String[vectors.size()];

		for (int i = 0; i < vectors.size(); i++) {
			this.vectors[i] = vectors.get(i);
			this.colIndex.put(names[i], i);
			this.names[i] = names[i];
		}
	}

	public SolidFrame(int rows, Vector[] vectors, String[] names) {
		this(rows, Arrays.asList(vectors), names);
	}

	@Override
	public int getRowCount() {
		return rows;
	}

	@Override
	public int getColCount() {
		return vectors.length;
	}

	@Override
	public int getRowId(int row) {
		return row;
	}

	@Override
	public boolean isMappedFrame() {
		return false;
	}

	@Override
	public Frame sourceFrame() {
		return this;
	}

	@Override
	public String[] getColNames() {
		return names;
	}

	@Override
	public int getColIndex(String name) {
		if (!colIndex.containsKey(name)) {
			throw new IllegalArgumentException("Column name is invalid");
		}
		return colIndex.get(name);
	}

	@Override
	public Vector getCol(int col) {
		if (col >= 0 && col < vectors.length) {
			return vectors[col];
		}
		throw new IllegalArgumentException("Invalid column getIndex");
	}

	@Override
	public Vector getCol(String name) {
		return getCol(getColIndex(name));
	}

	@Override
	public boolean isMissing(int row, int col) {
		return getCol(col).isMissing(row);
	}

	@Override
	public boolean isMissing(int row, String colName) {
		return getCol(colName).isMissing(row);
	}
}
