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

import rapaio.data.mapping.Mapping;
import rapaio.data.stream.VSpot;
import rapaio.data.stream.VSpots;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Random access list of observed values (observations) for a specific variable.
 *
 * @author Aurelian Tutuianu
 */
@Deprecated
public interface Var extends Serializable {

    /**
     * @return variable type
     */
    VarType type();

    /**
     * A variable implementation might be solid os mapped.
     * A mapped variable is a variable which hold only row indexes and maps over a solid variable.
     *
     * @return true if it is a mapped variable
     */
    boolean isMapped();

    /**
     * Solid variable source
     *
     * @return solid variable source, self if it is not mapped
     */
    Var source();

    /**
     * @return mapping of rows, null is variable is not mapped
     */
    Mapping mapping();

    /**
     * Number of observations contained by the var.
     *
     * @return size of var
     */
    int rowCount();

    /**
     * Returns numeric value for the observation specified by row.
     * <p>
     * Returns valid values for numerical var types, otherwise the method
     * returns unspecified value.
     *
     * @param row position of the observation
     * @return numerical setValue
     */
    double value(int row);

    /**
     * Set numeric setValue for the observation specified by {@param row} to {@param setValue}.
     * <p>
     * Returns valid values for numerical var types, otherwise the method
     * returns unspeified values.
     *
     * @param row   position of the observation
     * @param value numeric setValue from position {@param row}
     */
    void setValue(int row, double value);


    void addValue(double value);

    /**
     * Returns index setValue for the observation specified by {@param row}
     *
     * @param row position of the observation
     * @return index setValue
     */
    int index(int row);

    /**
     * Set index setValue for the observation specified by {@param row}.
     *
     * @param row   position of the observation
     * @param value index setValue for the observation
     */
    void setIndex(int row, int value);

    void addIndex(int value);

    /**
     * Returns nominal label for the observation specified by {@param row}.
     *
     * @param row position of the observation
     * @return label setValue for the observation
     */
    String label(int row);

    /**
     * Set nominal label for the observation specified by {@param row}.
     *
     * @param row   position of the observation
     * @param value label setValue of the observation
     */
    void setLabel(int row, String value);

    void addLabel(String value);

    /**
     * Returns the term dictionary used by the nominal values.
     * <p>
     * Term dictionary contains all the nominal labels used by
     * observations and might contain also additional nominal labels.
     * Term dictionary defines the domain of the definition for the nominal var.
     * <p>
     * The term dictionary contains nominal labels sorted in lexicografical order,
     * so binary search techniques may be used on this var.
     * <p>
     * For other var types like numerical ones this method returns nothing.
     *
     * @return term dictionary defined by the nominal var.
     */
    String[] dictionary();

    void setDictionary(String[] dict);

    /**
     * @param row position of the observation
     * @return boolean binary value
     */
    boolean binary(int row);

    /**
     * Set a binary/boolean value
     *
     * @param row position of the observation
     * @param value boolean binary value
     */
    void setBinary(int row, boolean value);

    /**
     * Adds a binary/boolean value
     *
     * @param value boolean binary value to be added
     */
    void addBinary(boolean value);

    /**
     * Gets long integer (stamp) value.
     *
     * @param row position of the observation
     * @return long integer value
     */
    long stamp(int row);

    /**
     * Set long integer (stamp) value
     *
     * @param row position of the observation
     * @param value long integer value to be set
     */
    void setStamp(int row, long value);

    /**
     * Adds a long integer (stump) value
     *
     * @param value long integer value to be added
     */
    void addStamp(long value);

    /**
     * Returns true if the setValue for the observation specified by {@param row} is missing, not available.
     * <p>
     * A missing setValue for the observation means taht the measurement
     * was not completed or the result of the measurement was not documented,
     * thus the setValue is not available for analysis.
     *
     * @param row position of the observation
     * @return true if the observation measurement is not specified
     */
    boolean missing(int row);

    /**
     * Set the setValue of the observation specified by {@param row} as missing, not available for analysis.
     *
     * @param row position of the observation.
     */
    void setMissing(int row);

    void addMissing();

    void remove(int row);

    void clear();

    Var solidCopy();

    default VSpots stream() {
        List<VSpot> instances = new LinkedList<>();
        for (int i = 0; i < this.rowCount(); i++) {
            instances.add(new VSpot(i, this));
        }
        return new VSpots(instances.stream());
    }

    public static Collector<Double, Numeric, Numeric> numericCollector() {
        return new Collector<Double, Numeric, Numeric>() {
            @Override
            public Supplier<Numeric> supplier() {
                return Numeric::newEmpty;
            }

            @Override
            public BiConsumer<Numeric, Double> accumulator() {
                return Numeric::addValue;
            }

            @Override
            public BinaryOperator<Numeric> combiner() {
                return (x, y) -> {
                    y.stream().forEach(s -> y.addValue(s.value()));
                    return x;
                };
            }

            @Override
            public Function<Numeric, Numeric> finisher() {
                return x -> x;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return new HashSet<>();
            }
        };
    }

    public static Collector<Integer, Index, Index> indexCollector() {
        return new Collector<Integer, Index, Index>() {
            @Override
            public Supplier<Index> supplier() {
                return Index::newEmpty;
            }

            @Override
            public BiConsumer<Index, Integer> accumulator() {
                return Index::addIndex;
            }

            @Override
            public BinaryOperator<Index> combiner() {
                return (x, y) -> {
                    y.stream().forEach(s -> y.addValue(s.value()));
                    return x;
                };
            }

            @Override
            public Function<Index, Index> finisher() {
                return x -> x;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return new HashSet<>();
            }
        };
    }

}
