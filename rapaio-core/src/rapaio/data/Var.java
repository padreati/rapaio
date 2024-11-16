/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;

import rapaio.core.SamplingTools;
import rapaio.data.stream.VSpot;
import rapaio.data.stream.VSpots;
import rapaio.data.transform.VarTransform;
import rapaio.math.narray.DType;
import rapaio.math.narray.NArray;
import rapaio.math.narray.NArrays;
import rapaio.math.narray.Order;
import rapaio.math.narray.Shape;
import rapaio.math.narray.storage.wrapper.VarDoubleStorage;
import rapaio.math.narray.storage.wrapper.VarFloatStorage;
import rapaio.printer.Printable;
import rapaio.util.IntComparator;
import rapaio.util.NotImplementedException;
import rapaio.util.collection.IntArrays;

/**
 * Random access list of observed values (observations) of a random variable (a vector with sample values).
 *
 * @author Aurelian Tutuianu
 */
public interface Var extends Serializable, Printable {

    /**
     * @return name of the variable
     */
    String name();

    /**
     * Sets the variable name
     *
     * @param name future name of the variable
     */
    Var name(String name);

    /**
     * @return variable type
     */
    VarType type();

    /**
     * Number of observations contained by the variable.
     *
     * @return size of var
     */
    int size();

    /**
     * Builds a new variable having rows of the current variable,
     * followed by the rows of the bounded frame.
     *
     * @param var given var with additional rows
     * @return new var with all union of rows
     */
    default Var bindRows(Var var) {
        return BoundVar.from(this, var);
    }

    /**
     * Builds a new variable only with rows specified in mapping.
     *
     * @param mapping a list of rows from a frame
     * @return new frame with selected rows
     */
    default MappedVar mapRows(Mapping mapping) {
        return MappedVar.byRows(this, mapping);
    }

    /**
     * Builds a new variable only with rows specified in mapping.
     *
     * @param rows a list of rows
     * @return new frame with selected rows
     */
    default MappedVar mapRows(int... rows) {
        return mapRows(Mapping.wrap(rows));
    }

    default MappedVar removeRows(Mapping mapping) {
        Set<Integer> remove = mapping.stream().boxed().collect(Collectors.toSet());
        int[] map = new int[Math.max(0, size() - remove.size())];
        int pos = 0;
        for (int i = 0; i < size(); i++) {
            if (!remove.contains(i)) {
                map[pos++] = i;
            }
        }
        return mapRows(Mapping.wrap(map));
    }

    /**
     * Adds empty rows to the current variable. All new values will be added at the end of the
     * variable and will be filled with missing values.
     *
     * @param rowCount number of rows to be added
     */
    void addRows(int rowCount);

    /**
     * Removes the observation value at a given position.
     * The new size of the variable is the old size decremented by 1
     *
     * @param row position of the observation to be removed
     */
    void removeRow(int row);

    /**
     * Removes all the observation values specified by the variable.
     * The new size of the variable is 0.
     */
    void clearRows();

    /**
     * Returns integer value for the observation specified by {@param row}
     *
     * @param row position of the observation
     * @return integer value
     */
    int getInt(int row);

    /**
     * Adds an integer value to the last position of the variable
     *
     * @param value value to be added at the end of the variable
     */
    void addInt(int value);

    /**
     * Set integer value for the observation specified by {@param row}.
     *
     * @param row   position of the observation
     * @param value integer value for the observation
     */
    void setInt(int row, int value);

    /**
     * Gets long value.
     *
     * @param row position of the observation
     * @return long value
     */
    long getLong(int row);

    /**
     * Set long value
     *
     * @param row   position of the observation
     * @param value long value to be set
     */
    void setLong(int row, long value);

    /**
     * Adds a long value
     *
     * @param value long value to be added
     */
    void addLong(long value);

    /**
     * Returns numeric float value for the observation specified by row.
     * <p>
     * Returns valid values for numerical var types, otherwise the method
     * returns unspecified value.
     *
     * @param row position of the observation
     * @return numerical float value
     */
    float getFloat(int row);

    /**
     * Adds a new double value to the last position of the variable.
     *
     * @param value value to be added variable
     */
    void addFloat(float value);

    /**
     * Set double value for the observation specified by {@param row} to the given {@param value}.
     *
     * @param row   position of the observation
     * @param value numeric double value from position {@param row}
     */
    void setFloat(int row, float value);

    /**
     * Returns numeric double value for the observation specified by row.
     * <p>
     * Returns valid values for numerical var types, otherwise the method
     * returns unspecified value.
     *
     * @param row position of the observation
     * @return numerical double value
     */
    double getDouble(int row);

    /**
     * Adds a new double value to the last position of the variable.
     *
     * @param value value to be added variable
     */
    void addDouble(double value);

    /**
     * Set double value for the observation specified by {@param row} to the given {@param value}.
     *
     * @param row   position of the observation
     * @param value numeric double value from position {@param row}
     */
    void setDouble(int row, double value);


    /**
     * Returns nominal label for the observation specified by {@param row}.
     *
     * @param row position of the observation
     * @return label value for the observation
     */
    String getLabel(int row);

    /**
     * Set nominal label for the observation specified by {@param row}.
     *
     * @param row   position of the observation
     * @param value label value of the observation
     */
    void setLabel(int row, String value);

    /**
     * Adds a nominal label value to the last position of the variable, updates levels
     * if is necessary.
     *
     * @param value text label to be added at the end of the variable
     */
    void addLabel(String value);

    /**
     * Adds nominal label values to the last positions of the variables,
     * updates levels if required. This operation calls repeatedly
     * {@link #addLabel(String)}.
     */
    default void addLabels(Iterable<String> values) {
        for (String value : values) {
            addLabel(value);
        }
    }

    /**
     * Returns the term levels used by the nominal values.
     * <p>
     * Term levels contains all the nominal labels used by
     * observations and might contain also additional nominal labels.
     * Term levels defines the domain of the definition for the nominal variable.
     * <p>
     * For other var types like numerical ones this method returns nothing.
     *
     * @return term levels defined by the nominal var.
     */
    List<String> levels();

    /**
     * Replace the used levels with a new one. A mapping between the
     * old values of the levels with the new values is done. The mapping
     * is done based on position.
     * <p>
     * The new levels can have repeated levels. This feature can be used
     * to unite multiple old labels with new ones. However the actual new
     * levels used will have only unique levels and indexed accordingly.
     * Thus a nominal with labels a,b,a,c,a,c which will have levels a,b,c,
     * when replaced with levels x,y,x will have as a result the following
     * labels: x,y,x,x,x,x and indexes 1,2,1,1,1,1
     *
     * @param dict array of levels which comprises the new levels
     */
    void setLevels(String... dict);

    /**
     * Replace the used levels with a new one. A mapping between the
     * old values of the levels with the new values is done. The mapping
     * is done based on position.
     * <p>
     * The new levels can have repeated levels. This feature can be used
     * to unite multiple old labels with new ones. However the actual new
     * levels used will have only unique levels and indexed accordingly.
     * Thus a nominal with labels a,b,a,c,a,c which will have levels a,b,c,
     * when replaced with levels x,y,x will have as a result the following
     * labels: x,y,x,x,x,x and indexes 1,2,1,1,1,1
     *
     * @param dict list of levels which comprises the new levels
     */
    default void setLevels(List<String> dict) {
        setLevels(dict.toArray(new String[0]));
    }

    /**
     * Adds an instant value to the last position of the variable.
     *
     * @param value value to be added
     */
    void addInstant(Instant value);

    /**
     * Sets the instant value ad the position specified by {@param row}
     *
     * @param row   position
     * @param value instant value
     */
    void setInstant(int row, Instant value);

    /**
     * Gets instant value from the given position
     *
     * @param row position
     * @return instant value
     */
    Instant getInstant(int row);

    /**
     * Returns true if the value for the observation specified by {@param row} is missing, not available.
     * <p>
     * A missing value for the observation means that the measurement
     * was not completed or the result of the measurement was not documented,
     * thus the value is not available for analysis.
     *
     * @param row position of the observation
     * @return true if the observation measurement is not specified or not assigned
     */
    boolean isMissing(int row);

    /**
     * Set the value of the observation specified by {@param row} as missing, not available for analysis.
     *
     * @param row position of the observation.
     */
    void setMissing(int row);

    /**
     * Adds a new observation unspecified observation value
     */
    void addMissing();

    /**
     * Creates a solid copy of the variable, even if the variable is mapped or not.
     *
     * @return a solid copy of the current variable
     */
    Var copy();

    default NArray<Double> narray_() {
        return narray_(DType.DOUBLE);
    }

    @SuppressWarnings("unchecked")
    default <N extends Number> NArray<N> narray_(DType<N> dtype) {
        return (NArray<N>) switch (dtype.id()) {
            case DOUBLE -> NArrays.ofDouble().stride(Shape.of(size()), Order.C, new VarDoubleStorage(this));
            case FLOAT -> NArrays.ofFloat().stride(Shape.of(size()), Order.C, new VarFloatStorage(this));
            default -> throw new NotImplementedException();
        };
    }

    default NArray<Double> narray() {
        return narray(DType.DOUBLE);
    }

    @SuppressWarnings("unchecked")
    default <N extends Number> NArray<N> narray(DType<N> dtype) {
        return switch (dtype.id()) {
            case DOUBLE -> {
                double[] copy = new double[size()];
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = getDouble(i);
                }
                yield (NArray<N>) NArrays.stride(Shape.of(size()), Order.C, copy);
            }
            case FLOAT -> {
                float[] copy = new float[size()];
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = getFloat(i);
                }
                yield (NArray<N>) NArrays.ofFloat().stride(Shape.of(size()), Order.C, copy);
            }
            default -> throw new NotImplementedException();
        };
    }

    /**
     * Builds a new empty instance of given size
     *
     * @param rows size of the new variable
     * @return new empty instance of given size
     */
    Var newInstance(int rows);

    /**
     * @return a stream of variables spots
     */
    default VSpots stream() {
        return new VSpots(this);
    }

    default void forEachSpot(Consumer<VSpot> consumer) {
        for (int i = 0; i < size(); i++) {
            consumer.accept(new VSpot(i, this));
        }
    }

    default void forEachInt(IntConsumer consumer) {
        for (int i = 0; i < size(); i++) {
            consumer.accept(getInt(i));
        }
    }

    default void forEachDouble(DoubleConsumer consumer) {
        for (int i = 0; i < size(); i++) {
            consumer.accept(getDouble(i));
        }
    }

    default int[] rowsComplete() {
        int len = 0;
        for (int i = 0; i < size(); i++) {
            if (isMissing(i)) {
                continue;
            }
            len++;
        }
        int[] rows = new int[len];
        int pos = 0;
        for (int i = 0; i < size(); i++) {
            if (isMissing(i)) {
                continue;
            }
            rows[pos++] = i;
        }
        return rows;
    }

    default int[] rowsAll() {
        return IntArrays.newSeq(0, size());
    }

    /**
     * Fit and apply the given variable filters. The filters received as parameters are applied in
     * the order they appear in the array. Depending on the filter variable instance, it creates or not
     * a new copy of the variable.
     *
     * @param filters filters to be applied
     * @return transformed variable after the given variable filter are applied successively
     */
    default Var fapply(VarTransform... filters) {
        Var var = this;
        for (VarTransform filter : filters) {
            var = filter.fapply(var);
        }
        return var;
    }

    /**
     * Apply the already fitted variable filters. The filters received as parameters are applied in
     * the order they appear in the array. Depending on the filter variable instance, it creates or not
     * a new copy of the variable.
     *
     * @param filters array of filters
     * @return transformed variable after the given variable filter are applied successively
     */
    default Var apply(VarTransform... filters) {
        Var var = this;
        for (VarTransform filter : filters) {
            var = filter.apply(var);
        }
        return var;
    }

    default Var shuffle() {
        return mapRows(SamplingTools.sampleWOR(size(), size()));
    }

    default IntComparator refComparator() {
        return refComparator(true);
    }

    default IntComparator refComparator(boolean asc) {
        return switch (this.type()) {
            case DOUBLE -> RowComparators.doubleComparator(this, asc);
            case LONG -> RowComparators.longComparator(this, asc);
            case INT, BINARY -> RowComparators.integerComparator(this, asc);
            default -> RowComparators.labelComparator(this, asc);
        };
    }

    /**
     * Tests if two variables has identical content, it does not matter the implementation.
     *
     * @param var variable on which the deep equals applied
     * @return true if type, size and content is identical
     */
    default boolean deepEquals(Var var) {
        return deepEquals(var, 1e-100);
    }

    /**
     * Tests if two variables has identical content, it does not matter the implementation.
     *
     * @param var variable on which the deep equals applied
     * @return true if type, size and content is identical
     */
    default boolean deepEquals(Var var, double tol) {
        if (!Objects.equals(name(), var.name())) {
            return false;
        }
        if (size() != var.size()) {
            return false;
        }
        if (type() != var.type()) {
            return false;
        }

        for (int i = 0; i < size(); i++) {
            if (isMissing(i) != var.isMissing(i)) {
                return false;
            }
            switch (type()) {
                case DOUBLE -> {
                    if (Math.abs(getDouble(i) - var.getDouble(i)) > tol) {
                        return false;
                    }
                }
                case INT, BINARY -> {
                    if (getInt(i) != var.getInt(i)) {
                        return false;
                    }
                }
                case LONG -> {
                    if (getLong(i) != var.getLong(i)) {
                        return false;
                    }
                }
                case INSTANT -> {
                    if (!getInstant(i).equals(var.getInstant(i))) {
                        return false;
                    }
                }
                default -> {
                    if (!Objects.equals(getLabel(i), var.getLabel(i))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
