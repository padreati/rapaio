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

import java.util.List;

/**
 * Utility class factory which offers methods for creating vectors of various
 * forms. Used to shorted the syntax for creating common vector constructs.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class Vectors {

	public static Index newSeq(int size) {
		Index result = new Index(size, size, 0);
		for (int i = 0; i < size; i++) {
			result.setIndex(i, i);
		}
		return result;
	}

	public static Index newSeq(int start, int end) {
		Index result = new Index(end - start + 1, end - start + 1, 0);
		for (int i = start; i <= end; i++) {
			result.setIndex(i - start, i);
		}
		return result;
	}

	public static Index newSeq(int from, int to, int step) {
		int len = (to - from) / step;
		if ((to - from) % step == 0) {
			len++;
		}
		Index values = new Index(len, len, 0);
		for (int i = 0; i < len; i++) {
			values.setIndex(i, from + i * step);
		}
		return values;
	}

	public static Index newIdxFrom(int[] values) {
		return new Index(values);
	}

	public static Numeric newNumFrom(List<Double> values) {
		Numeric vector = new Numeric(values.size());
		for (int i = 0; i < vector.getRowCount(); i++) {
			vector.setValue(i, values.get(i));
		}
		return vector;
	}

	public static Numeric newNumFrom(double... values) {
		Numeric vector = new Numeric(values.length);
		for (int i = 0; i < vector.getRowCount(); i++) {
			vector.setValue(i, values[i]);
		}
		return vector;
	}

	public static Index newIdx(int rows) {
		return new Index(rows, rows, 0);
	}

	public static Index newIdx(int rows, int fill) {
		return new Index(rows, rows, fill);
	}

	public static Numeric newNum(int rows, double fill) {
		return new Numeric(rows, rows, fill);
	}

	public static Numeric newNumOne(double value) {
		return new Numeric(new double[]{value});
	}

	public static Index newIdxOne(int value) {
		return new Index(1, 1, value);
	}
}
