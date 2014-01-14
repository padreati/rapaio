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

    public static IdxVector newSeq(int size) {
        IdxVector result = new IdxVector(size, size, 0);
        for (int i = 0; i < size; i++) {
            result.setIndex(i, i);
        }
        return result;
    }

    public static IdxVector newSeq(int start, int end) {
        IdxVector result = new IdxVector(end - start + 1, end - start + 1, 0);
        for (int i = start; i <= end; i++) {
            result.setIndex(i - start, i);
        }
        return result;
    }

    public static IdxVector newSeq(int from, int to, int step) {
        int len = (to - from) / step;
        if ((to - from) % step == 0) {
            len++;
        }
        IdxVector values = new IdxVector(len, len, 0);
        for (int i = 0; i < len; i++) {
            values.setIndex(i, from + i * step);
        }
        return values;
    }

    public static IdxVector newIdxFrom(int[] values) {
        return new IdxVector(values);
    }

    public static NumVector newNumFrom(List<Double> values) {
        NumVector vector = new NumVector(values.size());
        for (int i = 0; i < vector.getRowCount(); i++) {
            vector.setValue(i, values.get(i));
        }
        return vector;
    }

    public static NumVector newNumFrom(double... values) {
        NumVector vector = new NumVector(values.length);
        for (int i = 0; i < vector.getRowCount(); i++) {
            vector.setValue(i, values[i]);
        }
        return vector;
    }

    public static IdxVector newIdx(int rows) {
        return new IdxVector(rows, rows, 0);
    }

    public static NumVector newNum(int rows, int fill) {
        return new NumVector(rows, rows, fill);
    }

    public static NumVector newNumOne(double value) {
        return new NumVector(new double[]{value});
    }

    public static IdxVector newIdxOne(int value) {
        return new IdxVector(1, 1, value);
    }
}
