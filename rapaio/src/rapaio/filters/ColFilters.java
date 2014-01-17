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
package rapaio.filters;

import rapaio.core.ColRange;
import rapaio.core.stat.Quantiles;
import rapaio.data.Frame;
import rapaio.data.Nominal;
import rapaio.data.SolidFrame;
import rapaio.data.Vector;

import java.util.*;

/**
 * Provides filters which manipulate columns from a frame.
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class ColFilters {

	private ColFilters() {
	}

	/**
	 * Remove columns specified in a column range from a frame.
	 *
	 * @param df        frame
	 * @param colRange  column range
	 * @param colRange}
	 * @return original frame without columns specified in {
	 */
	public static Frame removeCols(Frame df, String colRange) {
		ColRange range = new ColRange(colRange);
		final List<Integer> indexes = range.parseColumnIndexes(df);
		Vector[] vectors = new Vector[df.colCount() - indexes.size()];
		String[] names = new String[df.colCount() - indexes.size()];
		int posIndexes = 0;
		int posFinal = 0;
		for (int i = 0; i < df.colCount(); i++) {
			if (posIndexes < indexes.size() && i == indexes.get(posIndexes)) {
				posIndexes++;
				continue;
			}
			vectors[posFinal] = df.col(i);
			names[posFinal] = df.colNames()[i];
			posFinal++;
		}
		return new SolidFrame(df.rowCount(), vectors, names);
	}

	/**
	 * Remove columns from a frame by specifying which columns to keep.
	 *
	 * @param df        frame
	 * @param colRange  column range
	 * @param colRange}
	 * @return original frame which has only columns specified in {
	 */
	public static Frame retainCols(Frame df, String colRange) {
		ColRange range = new ColRange(colRange);
		final List<Integer> indexes = range.parseColumnIndexes(df);
		Vector[] vectors = new Vector[indexes.size()];
		String[] names = new String[indexes.size()];
		int posIndexes = 0;
		for (int i = 0; i < df.colCount(); i++) {
			if (posIndexes < indexes.size() && i == indexes.get(posIndexes)) {
				vectors[posIndexes] = df.col(i);
				names[posIndexes] = df.colNames()[i];
				posIndexes++;
			}
		}
		return new SolidFrame(df.rowCount(), vectors, names);
	}

	/**
	 * Retain only numeric columns from a frame.
	 */
	public static Frame retainNumeric(Frame df) {
		List<Vector> vectors = new ArrayList<>();
		List<String> names = new ArrayList<>();
		for (int i = 0; i < df.colCount(); i++) {
			if (df.col(i).type().isNumeric()) {
				vectors.add(df.col(i));
				names.add(df.colNames()[i]);
			}
		}
		return new SolidFrame(df.rowCount(), vectors, names);
	}

	/**
	 * Retain only nominal columns from a frame.
	 */
	public static Frame retainNominal(Frame df) {
		List<Vector> vectors = new ArrayList<>();
		List<String> names = new ArrayList<>();
		for (int i = 0; i < df.colCount(); i++) {
			if (df.col(i).type().isNominal()) {
				vectors.add(df.col(i));
				names.add(df.colNames()[i]);
			}
		}
		return new SolidFrame(df.rowCount(), vectors, names);
	}

	public static Frame discretizeNumericToNominal(Frame df, ColRange colRange, int bins, boolean useQuantiles) {
		if (df.isMappedFrame()) {
			throw new IllegalArgumentException("Not allowed for mapped frame");
		}
		if (df.rowCount() < bins) {
			throw new IllegalArgumentException("Number of bins greater than number of rowCount");
		}
		Set<Integer> colSet = new HashSet<>(colRange.parseColumnIndexes(df));
		for (int col : colSet) {
			if (!df.col(col).type().isNumeric()) {
				throw new IllegalArgumentException("Non-numeric column found in column range");
			}
		}
		Set<String> dict = new HashSet<>();
		for (int i = 0; i < bins; i++) {
			dict.add(String.valueOf(i + 1));
		}
		List<Vector> vectors = new ArrayList<>();
		List<String> names = new ArrayList<>();
		for (int i = 0; i < df.colCount(); i++) {
			if (!colSet.contains(i)) {
				vectors.add(df.col(i));
				continue;
			}
			Vector origin = df.col(i);
			Vector discrete = new Nominal(origin.rowCount(), dict);
			if (!useQuantiles) {
				Vector sorted = RowFilters.sort(df.col(i));
				int width = (int) Math.ceil(df.rowCount() / (1. * bins));
				for (int j = 0; j < bins; j++) {
					for (int k = 0; k < width; k++) {
						if (j * width + k >= df.rowCount())
							break;
						if (sorted.isMissing(j * width + k))
							continue;
						int rowId = sorted.rowId(j * width + k);
						discrete.setLabel(rowId, String.valueOf(j + 1));
					}
				}
			} else {
				double[] p = new double[bins];
				for (int j = 0; j < p.length; j++) {
					p[j] = j / (1. * bins);
				}
				double[] q = new Quantiles(origin, p).getValues();
				for (int j = 0; j < origin.rowCount(); j++) {
					if (origin.isMissing(j))
						continue;
					double value = origin.value(j);
					int index = Arrays.binarySearch(q, value);
					if (index < 0) {
						index = -index - 1;
					} else {
						index++;
					}
					discrete.setLabel(j, String.valueOf(index));
				}
			}
			vectors.add(discrete);
			names.add(df.colNames()[i]);
		}
		return new SolidFrame(df.rowCount(), vectors, names);
	}
}
