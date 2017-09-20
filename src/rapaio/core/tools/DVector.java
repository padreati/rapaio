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

package rapaio.core.tools;

import rapaio.core.RandomSource;
import rapaio.data.NumericVar;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.printer.Printable;
import rapaio.printer.format.TextTable;
import rapaio.sys.WS;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoublePredicate;
import java.util.stream.DoubleStream;

/**
 * Nominal distribution vector.
 * <p>
 * Vector tool class to facilitate various operations on nominal variables regarding frequencies.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class DVector implements Printable, Serializable {

    /**
     * Builds a distribution vector with given levels
     *
     * @param labels used to name values
     * @return new empty distribution vector
     */
    public static DVector empty(boolean useFirst, String... labels) {
        return new DVector(useFirst, labels);
    }

    /**
     * Builds a distribution vector with given dimestion. Names are generated automatically.
     *
     * @param rows size of the distribution vector
     * @return new empty distribution vector
     */
    public static DVector empty(boolean useFirst, int rows) {
        String[] labels = new String[rows];
        for (int i = 0; i < labels.length; i++) {
            if (i == 0) {
                labels[i] = useFirst ? "v0" : "?";
            } else {
                labels[i] = "v" + i;
            }
        }
        return new DVector(useFirst, labels);
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
    public static DVector fromCount(boolean useFirst, Var var) {
        Var weights = NumericVar.fill(var.rowCount(), 1);
        return new DVector(useFirst, var.levels(), var, weights);
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
    public static DVector fromWeights(boolean useFirst, Var var, Var weights) {
        return new DVector(useFirst, var.levels(), var, weights);
    }

    /**
     * Builds a new distribution vector, with given names, grouped by
     * the nominal variable and with values as sums on numeric weights
     *
     * @param labels  levels used for names
     * @param var     defines nominal grouping
     * @param weights weights used to compute sums for each cell
     * @return new distribution vector
     */
    public static DVector fromWeights(boolean useFirst, Var var, Var weights, String... labels) {
        return new DVector(useFirst, labels, var, weights);
    }

    private static final long serialVersionUID = -546802690694348698L;
    private final String[] levels;
    private final Map<String, Integer> reverse = new HashMap<>();
    private final double[] values;
    private boolean useFirst;
    private int start;
    private double total;

    private DVector(boolean useFirst, String[] labels) {
        this.useFirst = useFirst;
        this.start = useFirst ? 0 : 1;

        this.levels = labels;
        for (int i = 0; i < labels.length; i++) {
            reverse.put(labels[i], i);
        }
        this.values = new double[this.levels.length];
    }

    private DVector(boolean useFirst, String[] labels, Var var, Var weights) {
        this(useFirst, labels);
        int off = var.type().equals(VarType.BINARY) ? 1 : 0;
        var.stream().forEach(s -> values[s.getIndex() + off] += weights.value(s.getRow()));
        total = Arrays.stream(values).sum();
    }

    public boolean isFirstUsed() {
        return useFirst;
    }

    public DVector withFirst(boolean useFirst) {
        this.useFirst = useFirst;
        this.start = useFirst ? 0 : 1;
        return this;
    }

    public String[] levels() {
        return levels;
    }

    /**
     * Getter for the value from a given position
     *
     * @param pos position of the value
     * @return value from the give position
     */
    public double get(int pos) {
        return values[pos];
    }

    public double get(String name) {
        return get(reverse.get(name));
    }

    public String label(int pos) {
        return levels[pos];
    }

    /**
     * Updates the value from the given position {@param pos} by adding the {@param value}
     *
     * @param pos   position of the denity vector to be updated
     * @param value value to be added to given cell
     */
    public void increment(int pos, double value) {
        values[pos] += value;
        total += value;
    }

    /**
     * Updates the value from the given position {@param pos} by adding the {@param value}
     *
     * @param dv     density vector which will be added
     * @param factor the factor used to multiply added density vector with
     */
    public void increment(DVector dv, double factor) {
        for (int i = 0; i < values.length; i++) {
            values[i] += dv.get(i) * factor;
            total += dv.get(i) * factor;
        }
    }

    public void increment(String name, double value) {
        increment(reverse.get(name), value);
    }

    public void increment(DVector dv) {
        if (values.length != dv.values.length)
            throw new IllegalArgumentException("Cannot update density vector, row count is different");
        for (int i = 0; i < values.length; i++) {
            values[i] += dv.values[i];
        }
        total += dv.total;
    }

    /**
     * Setter for the value from a given position
     *
     * @param pos   position of the value
     * @param value value to be set at the given position
     */
    public void set(int pos, double value) {
        total += value - values[pos];
        values[pos] = value;
    }

    public void set(String name, double value) {
        set(reverse.get(name), value);
    }

    /**
     * Find the index of the greatest value from all cells, including eventual missing label.
     * If there are multiple maximal values, one at random is chosen
     *
     * @return index of the greatest value
     */
    public int findBestIndex() {
        double n = 1;
        int bestIndex = start;
        double best = values[start];
        for (int i = start + 1; i < values.length; i++) {
            if (values[i] > best) {
                best = values[i];
                bestIndex = i;
                n = 1;
                continue;
            }
            if (values[i] == best) {
                if (RandomSource.nextDouble() > n / (n + 1)) {
                    best = values[i];
                    bestIndex = i;
                }
                n++;
            }
        }
        return bestIndex;
    }

    /**
     * Normalize values from density vector
     */
    public DVector normalize() {
        total = 0.0;
        for (int i = start; i < values.length; i++) {
            total += values[i];
        }
        if (total == 0)
            return this;
        for (int i = start; i < values.length; i++) {
            values[i] /= total;
        }
        total = 1.0;
        return this;
    }

    /**
     * Computes the sum of all cells.
     * Missing cell might be used or not,
     *
     * @return sum of elements
     */
    public double sum() {
        return useFirst ? total : total - values[0];
    }

    /**
     * Computes the sum of all cells except a given one and eventually the missing value cell.
     *
     * @param except the cell excepted from computation
     * @return partial sum of cells
     */
    public double sumExcept(int except) {
        return sum() - values[except];
    }

    /**
     * Count values which respects the condition given by the predicate.
     *
     * @param pred condition used to filter the values
     * @return count of filtered values
     */
    public int countValues(DoublePredicate pred) {
        int count = 0;
        for (int i = start; i < values.length; i++) {
            if (pred.test(values[i])) {
                count++;
            }
        }
        return count;
    }

    public int getRowCount() {
        return levels.length;
    }

    /**
     * Builds a solid copy of the distribution vector
     *
     * @return a solid copy of distribution vector
     */
    public DVector solidCopy() {
        DVector d = new DVector(useFirst, levels);
        System.arraycopy(values, 0, d.values, 0, levels.length);
        d.total = total;
        return d;
    }

    /**
     * @return index of the first cell, is 1 if missing cell exists and {@param useMissing} exists, 0 otherwise
     */
    public int start() {
        return start;
    }

    public DoubleStream streamValues() {
        return Arrays.stream(values);
    }

    @Override
    public String toString() {
        return "DVector{" +
                "levels=" + Arrays.toString(levels) +
                ", values=" + Arrays.toString(values) +
                ", total=" + total +
                '}';
    }

    public boolean equalsFull(DVector o) {
        if (levels.length - start != o.levels.length - o.start) {
            return false;
        }
        if (values.length - start != o.values.length - o.start) {
            return false;
        }
        for (int i = 0; i < levels.length - start; i++) {
            if (!levels[i + start].equals(o.levels[i + o.start]))
                return false;
        }
        for (int i = 0; i < values.length - start; i++) {
            if (Math.abs(values[i + start] - o.values[i + o.start]) > 1e-30) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String summary() {
        TextTable tt = TextTable.newEmpty(3, levels.length);
        for (int i = start; i < levels.length; i++) {
            tt.set(0, i, levels[i], 1);
            tt.set(1, i, repeat(levels[i].length(), '-'), 1);
            tt.set(2, i, WS.formatFlex(values[i]), 1);
        }
        return tt.summary();
    }

    private String repeat(int len, char ch) {
        char[] lineChars = new char[len];
        for (int i = 0; i < len; i++) {
            lineChars[i] = ch;
        }
        return String.valueOf(lineChars);
    }

}
