/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
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
 *
 */

package rapaio.data;


import java.util.List;

/**
 * Builds a numeric variable. Numeric variables stores data as double values
 * and allows modelling of any type of continuous or discrete numeric variable.
 * <p>
 * The placeholder for missing value is Double.NaN. Any form of usage of Double.NaN
 * on set/add operation will result in a missing value.
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public interface VarDouble extends Var {

    @Override
    default VarType type() {
        return VarType.DOUBLE;
    }

    VarDouble withName(String name);

    @Override
    default String getLabel(int row) {
        if (isMissing(row))
            return "?";
        return String.valueOf(getDouble(row));
    }

    @Override
    default void setLabel(int row, String value) {
        if ("?".equals(value)) {
            setMissing(row);
            return;
        }
        if ("Inf".equals(value)) {
            setDouble(row, Double.POSITIVE_INFINITY);
            return;
        }
        if ("-Inf".equals(value)) {
            setDouble(row, Double.NEGATIVE_INFINITY);
            return;
        }
        setDouble(row, Double.parseDouble(value));
    }

    @Override
    default void addLabel(String value) {
        if ("?".equals(value)) {
            addMissing();
            return;
        }
        if ("Inf".equals(value)) {
            addDouble(Double.POSITIVE_INFINITY);
            return;
        }
        if ("-Inf".equals(value)) {
            addDouble(Double.NEGATIVE_INFINITY);
            return;
        }
        addDouble(Double.parseDouble(value));
    }

    @Override
    default List<String> levels() {
        throw new RuntimeException("Operation not available for numeric vectors.");
    }

    @Override
    default void setLevels(String[] dict) {
        throw new RuntimeException("Operation not available for numeric vectors.");
    }

    VarDouble solidCopy();
}
