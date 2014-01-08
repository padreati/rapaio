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

    public static IndexVector newSequence(int size) {
        IndexVector result = new IndexVector(size);
        for (int i = 0; i < size; i++) {
            result.setIndex(i, i);
        }
        return result;
    }

    public static IndexVector newSequence(int start, int end) {
        IndexVector result = new IndexVector(end - start + 1);
        for (int i = start; i <= end; i++) {
            result.setIndex(i - start, i);
        }
        return result;
    }

    public static NumericVector newNumeric(List<Double> values) {
        NumericVector vector = new NumericVector(values.size());
        for (int i = 0; i < vector.getRowCount(); i++) {
            vector.setValue(i, values.get(i));
        }
        return vector;
    }
}
