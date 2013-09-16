/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

import java.util.Comparator;

/**
 * Provides filters for type conversion, metadata changing.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class BaseFilters {

    private BaseFilters() {
    }

    /**
     * Renames a vector.
     *
     * @param vector original vector
     * @param name   new name for the vector
     * @return a wrapped vector which is like the original, with a different name
     */
    public static Vector renameVector(final Vector vector, final String name) {
        return new Vector() {
            @Override
            public boolean isNumeric() {
                return vector.isNumeric();
            }

            @Override
            public boolean isNominal() {
                return vector.isNominal();
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public int getRowCount() {
                return vector.getRowCount();
            }

            @Override
            public int getRowId(int row) {
                return vector.getRowId(row);
            }

            @Override
            public double getValue(int row) {
                return vector.getValue(row);
            }

            @Override
            public void setValue(int row, double value) {
                vector.setValue(row, value);
            }

            @Override
            public int getIndex(int row) {
                return vector.getIndex(row);
            }

            @Override
            public void setIndex(int row, int value) {
                vector.setIndex(row, value);
            }

            @Override
            public String getLabel(int row) {
                return vector.getLabel(row);
            }

            @Override
            public void setLabel(int row, String value) {
                vector.setLabel(row, value);
            }

            @Override
            public String[] getDictionary() {
                return vector.getDictionary();
            }

            @Override
            public boolean isMissing(int row) {
                return vector.isMissing(row);
            }

            @Override
            public void setMissing(int row) {
                vector.setMissing(row);
            }

            @Override
            public Comparator<Integer> getComparator(boolean asc) {
                return vector.getComparator(asc);
            }
        };
    }

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
            vectors[i] = toNumeric(df.getCol(i).getName(), df.getCol(i));
        }
        return new SolidFrame(df.getName(), df.getRowCount(), vectors);
    }

    /**
     * Convert a isNominal vector to isNumeric parsing as numbers the isNominal
     * labels.
     * <p/>
     * If the input getValue is already a isNumeric vector, the input vector is
     * returned
     *
     * @param name
     * @param v    input vector
     * @return converted getValue vector
     */
    public static Vector toNumeric(String name, Vector v) {
        if (v.isNumeric()) {
            return v;
        }
        Vector result = new NumericVector(name, v.getRowCount());
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
}
