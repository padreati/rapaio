/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data;

import rapaio.data.unique.UniqueDouble;
import rapaio.data.unique.UniqueInt;
import rapaio.data.unique.UniqueLabel;
import rapaio.printer.Printable;

/**
 * Interface which exposes information about unique values in a variable.
 * For each unique value an integer (32 bit) id is generated. The unique ids
 * have values in interval [0, count-1], where count is the number of
 * unique values.
 * <p>
 * The unique values are build upon data representation. There are
 * implementation for each data representation, no matter variable type,
 * as long the variable itself support the data representation.
 * <p>
 * The default representation for each data type is instead in accordance
 * with the variable type, i.e. for double variable the default unique value
 * will use double representation.
 * <p>
 * This interface does not expose the value themselves, but the ids of those
 * values. The specialized implementations, however, expose more data specific
 * information.
 * <p>
 * Unique value ids can be in sorted order if desired. This is useful if one
 * wants to do further operations with the data structure. If one does not want
 * the unique values to be sorted for speed purposes, than he had this option using
 * the sorted parameter flag.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/22/18.
 */
public interface Unique extends Printable {

    static Unique of(Var var) {
        return Unique.of(var, false);
    }

    static Unique of(Var var, boolean sorted) {
        return switch (var.type()) {
            case DOUBLE -> ofDouble(var, sorted);
            case STRING, NOMINAL -> ofLabel(var, sorted);
            case INT, BINARY -> ofInt(var, sorted);
            default -> throw new IllegalArgumentException("Cannot build unique structure for given type: not implemented.");
        };
    }

    static UniqueDouble ofDouble(Var var) {
        return UniqueDouble.of(var, false);
    }

    static UniqueDouble ofDouble(Var var, boolean sorted) {
        return UniqueDouble.of(var, sorted);
    }

    static UniqueInt ofInt(Var var) {
        return UniqueInt.of(var, false);
    }

    static UniqueInt ofInt(Var var, boolean sorted) {
        return UniqueInt.of(var, sorted);
    }

    static UniqueLabel ofLabel(Var var) {
        return UniqueLabel.of(var, false);
    }

    static UniqueLabel ofLabel(Var var, boolean sorted) {
        return UniqueLabel.of(var, sorted);
    }

    /**
     * Tells if the unique structure has group ids in sorted order
     */
    boolean isSorted();

    /**
     * @return total number of unique values, including missing value if found
     */
    int uniqueCount();

    /**
     * @return a list of unique group ids ordered increasing by count of values with the same unique value
     */
    VarInt countSortedIds();

    /**
     * @return a list of unique group ids ordered increasing by the unique value
     */
    VarInt valueSortedIds();

    /**
     * @param id unique group id
     * @return list of row ids for the given unique group id
     */
    Mapping rowList(int id);

    /**
     * @param row row number from the original frame
     * @return unique group id
     */
    int idByRow(int row);
}
