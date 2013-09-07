/*
 * Copyright 2013 Aurelian Tutuianu
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

package rapaio.core;

import rapaio.data.*;
import rapaio.distributions.Normal;
import rapaio.filters.FilterRename;
import rapaio.functions.UnivariateFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static rapaio.core.BaseMath.getRandomSource;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class BaseFilters {

    private BaseFilters() {
    }

    public static Vector applyFunction(Vector v, UnivariateFunction f) {
        Vector ret = new NumericVector(v.getName(), v.getRowCount());
        for (int i = 0; i < v.getRowCount(); i++) {
            if (v.isMissing(i)) {
                continue;
            }
            ret.setValue(i, f.eval(v.getValue(i)));
        }
        return ret;
    }

    /**
     * Alter isNumeric values with normally distributed noise
     *
     * @param v input values
     * @return altered values
     */
    public static Vector jitter(Vector v) {
        return jitter(v, 0.1);
    }

    /**
     * Alter isNumeric values with normally distributed noise
     *
     * @param v  input values
     * @param sd standard deviation of the normally distributed noise
     * @return altered values
     */
    public static Vector jitter(Vector v, double sd) {
        Normal d = new Normal(0, sd);
        Vector result = new NumericVector(v.getName(), v.getRowCount());
        Vector jitter = d.sample(result.getRowCount(), getRandomSource());
        for (int i = 0; i < result.getRowCount(); i++) {
            if (v.isMissing(i)) {
                continue;
            }
            result.setValue(i, v.getValue(i) + jitter.getValue(i));
        }
        return result;
    }

    /**
     * Shuffle the order of size from specified frame.
     *
     * @param df source frame
     * @return shuffled frame
     */
    public static Frame shuffle(Frame df) {
        ArrayList<Integer> mapping = new ArrayList<>();
        for (int i = 0; i < df.getRowCount(); i++) {
            mapping.add(i);
        }
        for (int i = mapping.size(); i > 1; i--) {
            mapping.set(i - 1, mapping.set(getRandomSource().nextInt(i), mapping.get(i - 1)));
        }
        return new MappedFrame(df, mapping);
    }

    /**
     * Sort ascending the values according to the type comparator
     *
     * @param v input values
     * @return sorted values
     */
    public static Vector sort(Vector v) {
        return sort(v, true);
    }

    /**
     * Sort the values according to the type comparator
     *
     * @param vector input vector
     * @param asc    true if ascending, false if descending
     * @return sorted values
     */
    public static Vector sort(Vector vector, boolean asc) {
        List<Integer> mapping = new ArrayList<>();
        for (int i = 0; i < vector.getRowCount(); i++) {
            mapping.add(i);
        }
        Collections.sort(mapping, vector.getComparator(asc));
        return new SortedVector(vector, mapping);
    }

    public static Vector toValue(Vector v) {
        return toValue(v.getName(), v);
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
    public static Vector toValue(String name, Vector v) {
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

    /**
     * Convert to isNumeric values all the columns which are isNominal.
     * <p/>
     * All the other columns remain the same.
     *
     * @param df input frame
     * @return frame with getValue converted columns
     */
    public static Frame toValue(Frame df) {
        Vector[] vectors = new Vector[df.getColCount()];
        for (int i = 0; i < vectors.length; i++) {
            vectors[i] = toValue(df.getCol(i));
        }
        return new SolidFrame(df.getName(), df.getRowCount(), vectors);
    }

    public static Vector rename(Vector vector, String name) {
        return new FilterRename().rename(vector, name);
    }
}
