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

import rapaio.data.*;


/**
 * Provides filters for type conversion, metadata changing.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class BaseFilters {

    /**
     * Convert to isNumeric values all the columns which are isNominal.
     * <p/>
     * All the other columns remain the same.
     *
     * @param df input frame
     * @return frame with getValue converted columns
     */
    public static Frame toNumeric(Frame df) {
        Vector[] vectors = new Vector[df.getColCount()];
        for (int i = 0; i < vectors.length; i++) {
            vectors[i] = toNumeric(df.getCol(i));
        }
        return new SolidFrame(df.getRowCount(), vectors, df.getColNames());
    }

    /**
     * Convert a isNominal vector to isNumeric parsing as numbers the isNominal
     * labels.
     * <p/>
     * If the input getValue is already a isNumeric vector, the input vector is
     * returned
     *
     * @param v input vector
     * @return converted getValue vector
     */
    public static Vector toNumeric(Vector v) {
        if (v.isNumeric()) {
            return v;
        }
        Vector result = new NumericVector(v.getRowCount());
        for (int i = 0; i < result.getRowCount(); i++) {
            if (v.isMissing(i)) {
                continue;
            }
            try {
                double value = Double.parseDouble(v.getLabel(i));
                result.setValue(i, value);
            } catch (NumberFormatException nfe) {
                result.setMissing(i);
            }
        }
        return result;
    }

    public static Vector toIndex(Vector v) {
        Vector result = Vectors.newIndex(v.getRowCount());
        for (int i = 0; i < v.getRowCount(); i++) {
            if (v.isMissing(i)) {
                continue;
            }
            try {
                if (v.isNominal()) {
                    int value = Integer.parseInt(v.getLabel(i));
                    result.setIndex(i, value);
                }
                if (v.isNumeric()) {
                    result.setIndex(i, result.getIndex(i));
                }
            } catch (NumberFormatException nfe) {
                result.setMissing(i);
            }
        }
        return result;
    }
}
