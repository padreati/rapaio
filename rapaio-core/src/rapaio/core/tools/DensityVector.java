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

package rapaio.core.tools;

import static java.lang.Math.abs;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoublePredicate;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import rapaio.data.Index;
import rapaio.data.Var;
import rapaio.data.index.IndexLabel;
import rapaio.narray.NArrays;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POpt;

/**
 * Nominal distribution vector.
 * <p>
 * Vector tool class to facilitate various operations on nominal variables regarding frequencies.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class DensityVector<T> implements Printable, Serializable {

    /**
     * Builds a distribution vector with given levels.
     *
     * @param values used to name values
     * @return new empty distribution vector
     */
    public static DensityVector<String> emptyByLabels(boolean useFirst, List<String> values) {
        if (useFirst) {
            return new DensityVector<>(IndexLabel.fromLabelValues(values));
        }
        return new DensityVector<>(IndexLabel.fromLabelValues(values.subList(1, values.size())));
    }

    /**
     * Builds a distribution vector with given levels.
     *
     * @param labels used to name values
     * @return new empty distribution vector
     */
    public static DensityVector<String> emptyByLabels(boolean useMissing, String... labels) {
        if (useMissing) {
            return new DensityVector<>(IndexLabel.fromLabelValues(labels));
        }
        return new DensityVector<>(IndexLabel.fromLabelValues(Arrays.stream(labels).skip(1).collect(Collectors.toList())));
    }

    /**
     * Builds a distribution vector with given dimension. Names are generated automatically.
     *
     * @param rows size of the distribution vector
     * @return new empty distribution vector
     */
    public static DensityVector<String> emptyByLabels(int rows) {
        ArrayList<String> labels = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            labels.add("v" + i);
        }
        return new DensityVector<>(IndexLabel.fromLabelValues(labels));
    }

    /**
     * Builds a distribution vector as a frequency table from a
     * given nominal variable. For each cell value it will hold
     * the number of appearances and will have the cell names
     * from the levels of the nominal value given as input.
     *
     * @param var given nominal value
     * @return new distribution vector filled with counts
     */
    public static DensityVector<String> fromLevelCounts(boolean useMissing, Var var) {
        IndexLabel index = IndexLabel.fromVarLevels(useMissing, var);
        DensityVector<String> dv = new DensityVector<>(index);
        for (int row : index.getIndexList(var)) {
            dv.increment(row, 1);
        }
        return dv;
    }

    /**
     * Builds a distribution vector as a table with one cell for each
     * value in the nominal variable and as value the sum of it's
     * corresponding weights.
     *
     * @param var     given nominal variable
     * @param weights given numeric weights
     * @return new distribution variable
     */
    public static DensityVector<String> fromLevelWeights(boolean useMissing, Var var, Var weights) {
        IndexLabel index = IndexLabel.fromVarLevels(useMissing, var);
        DensityVector<String> dv = new DensityVector<>(index);
        for (int i = 0; i < var.size(); i++) {
            if (index.containsValue(var, i)) {
                dv.increment(index.getIndex(var, i), weights.getDouble(i));
            }
        }
        return dv;
    }

    @Serial
    private static final long serialVersionUID = -546802690694348698L;
    private final Index<T> index;
    private final double[] values;
    private double total;

    private DensityVector(Index<T> index) {
        this.index = index;
        this.values = new double[this.index.size()];
    }

    public DensityVector<T> newInstance() {
        return new DensityVector<>(index);
    }

    public Index<T> index() {
        return index;
    }

    public T getIndexValue(int pos) {
        return index.getValue(pos);
    }

    /**
     * Getter for the value from a given position.
     *
     * @param pos position of the value
     * @return value from the give position
     */
    public double get(int pos) {
        return values[pos];
    }

    public double get(T name) {
        return get(index.getIndex(name));
    }

    /**
     * Updates the value from the given position {@param pos} by adding the {@param value}.
     *
     * @param pos   position of the density vector to be updated
     * @param value value to be added to given cell
     */
    public void increment(int pos, double value) {
        values[pos] += value;
        total += value;
    }

    public void increment(T name, double value) {
        increment(index.getIndex(name), value);
    }

    /**
     * Updates the value from the given position {@param pos} by adding the {@param value}.
     *
     * @param dv     density vector which will be added
     * @param factor the factor used to multiply added density vector with
     */
    public void plus(DensityVector<T> dv, double factor) {
        if (values.length != dv.values.length) {
            throw new IllegalArgumentException("Cannot update density vector, row count is different");
        }
        for (int i = 0; i < values.length; i++) {
            values[i] += dv.get(i) * factor;
            total += dv.get(i) * factor;
        }
    }

    /**
     * Setter for the value from a given position.
     *
     * @param pos   position of the value
     * @param value value to be set at the given position
     */
    public void set(int pos, double value) {
        total += value - values[pos];
        values[pos] = value;
    }

    public void set(T name, double value) {
        set(index.getIndex(name), value);
    }

    /**
     * Find the index of the greatest value from all cells, including eventual missing label.
     * If there are multiple maximal values, one at random is chosen
     *
     * @return index of the greatest value
     */
    public int findBestIndex() {
        return NArrays.stride(values).argmax();
    }

    public String findBestLabel() {
        return index.getValueString(NArrays.stride(values).argmax());
    }

    /**
     * Normalize values from density vector to sum.
     */
    public DensityVector<T> normalize() {
        normalize(1);
        return this;
    }

    /**
     * Normalize values from density vector to sum of powers.
     */
    public DensityVector<T> normalize(double pow) {
        NArrays.stride(values).normalize_(pow);
        return this;
    }

    /**
     * Computes the sum of all cells.
     *
     * @return sum of elements
     */
    public double sum() {
        return total;
    }

    /**
     * Computes the sum of all cells except a given one and eventually the missing value cell.
     *
     * @param except the cell excepted from computation
     * @return partial sum of cells
     */
    public double sumExcept(int except) {
        if (except < 0) {
            throw new IllegalArgumentException("Except index must be greater or equal with 0.");
        }
        return sum() - values[except];
    }

    /**
     * Computes the sum of all cells except a given one and eventually the missing value cell.
     *
     * @param except the cell excepted from computation
     * @return partial sum of cells
     */
    public double sumExcept(String except) {
        int intexcept = -1;
        for (int i = 0; i < index.size(); i++) {
            if (index.getValueString(i).equals(except)) {
                intexcept = i;
                break;
            }
        }
        if (intexcept < 0) {
            throw new IllegalArgumentException("Except value: " + except + " not found.");
        }
        return sum() - values[intexcept];
    }

    /**
     * Count values which respects the condition given by the predicate.
     *
     * @param predicate condition used to filter the values
     * @return count of filtered values
     */
    public int countValues(DoublePredicate predicate) {
        int count = 0;
        for (double value : values) {
            if (predicate.test(value)) {
                count++;
            }
        }
        return count;
    }

    public int rowCount() {
        return index.size();
    }

    /**
     * Builds a solid copy of the distribution vector.
     *
     * @return a solid copy of distribution vector
     */
    public DensityVector<T> copy() {
        DensityVector<T> d = new DensityVector<>(index);
        System.arraycopy(values, 0, d.values, 0, index.size());
        d.total = total;
        return d;
    }

    public DoubleStream streamValues() {
        return Arrays.stream(values);
    }

    public boolean equalsFull(DensityVector<T> o) {
        if (index.size() != o.index.size()) {
            return false;
        }
        if (values.length != o.values.length) {
            return false;
        }
        for (int i = 0; i < index.size(); i++) {
            if (!index.getValue(i).equals(o.index.getValue(i))) {
                return false;
            }
        }
        for (int i = 0; i < values.length; i++) {
            if (abs(values[i] - o.values[i]) > 1e-30) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("DVector{levels=[%s], values=%s, total=%f}",
                String.join(",", index.getValueStrings()), Arrays.toString(values), total);
    }

    @Override
    public String toContent(Printer printer, POpt<?>... options) {
        TextTable tt = TextTable.empty(3, index.size());
        for (int i = 0; i < index.size(); i++) {
            tt.textRight(0, i, index.getValueString(i));
            tt.textRight(1, i, repeat(index.getValueString(i).length()));
            tt.floatFlex(2, i, values[i]);
        }
        return tt.getDynamicText(printer, options);
    }

    private String repeat(int length) {
        char[] buffer = new char[length];
        Arrays.fill(buffer, '-');
        return String.valueOf(buffer);
    }

    @Override
    public String toFullContent(Printer printer, POpt<?>... options) {
        return toContent(printer, options);
    }

    @Override
    public String toSummary(Printer printer, POpt<?>... options) {
        return toContent(printer, options);
    }
}
