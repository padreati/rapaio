/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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
import rapaio.data.Numeric;
import rapaio.data.Var;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.DoublePredicate;
import java.util.stream.DoubleStream;

/**
 * Nominal distribution vector.
 * <p>
 * Vector tool class to facilitate various operations on nominal variables regarding frequencies.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class DVector implements Serializable {

    private static final long serialVersionUID = -546802690694348698L;

    private final String[] labels;
    private final double[] values;
    private double total;

    /**
     * Builds a distribution vector with given labels
     *
     * @param labels used to name values
     * @return new empty distribution vector
     */
    public static DVector newEmpty(String... labels) {
        return new DVector(labels);
    }

    /**
     * Builds a distribution vector with given dimention. Names are generated automatically.
     *
     * @param rows size of the distribution vector
     * @return new empty distribution vector
     */
    public static DVector newEmpty(int rows) {
        String[] labels = new String[rows];
        for (int i = 0; i < labels.length; i++) {
            if (i == 0) {
                labels[i] = "?";
            } else {
                labels[i] = "v" + i;
            }
        }
        return new DVector(labels);
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
    public static DVector newFromCount(Var var) {
        Var weights = Numeric.newFill(var.rowCount(), 1);
        return new DVector(var.levels(), var, weights);
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
    public static DVector newFromWeights(Var var, Var weights) {
        return new DVector(var.levels(), var, weights);
    }

    /**
     * Builds a new distribution vector, with given names, grouped by
     * the nominal variable and with values as sums on numeric weights
     *
     * @param labels  labels used for names
     * @param var     defines nominal grouping
     * @param weights weights used to compute sums for each cell
     * @return new distribution vector
     */
    public static DVector newFromWeights(Var var, Var weights, String... labels) {
        return new DVector(labels, var, weights);
    }

    private DVector(String[] labels) {
        for (int i = 1; i < labels.length; i++) {
            if ("?".equals(labels[i])) {
                throw new IllegalArgumentException("labels are not allowed to have name '?' on a position other than 0");
            }
        }
        if (!labels[0].equals("?")) {
            this.labels = new String[labels.length + 1];
            this.labels[0] = "?";
            if (labels.length > 0) {
                System.arraycopy(labels, 0, this.labels, 1, labels.length);
            }
        } else {
            this.labels = labels;
        }
        this.values = new double[this.labels.length];
    }

    private DVector(String[] labels, Var var, Var weights) {
        this(labels);
        var.stream().forEach(spot -> values[spot.index()] += weights.value(spot.row()));
        total = Arrays.stream(values).sum();
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

    public String label(int pos) {
        return labels[pos];
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
        values[pos] = value;
    }

    /**
     * Find the index of the greatest value from all cells, including eventual missing label.
     * If there are multiple maximal values, one at random is chosen
     *
     * @param useMissing true if the missing column is used, if exists, false otherwise
     * @return index of the greatest value
     */
    public int findBestIndex(boolean useMissing) {
        int start = getStart(useMissing);
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
     *
     * @param useMissing true if missing cell is used, if exists, false otherwise
     */
    public void normalize(boolean useMissing) {
        total = 0.0;
        for (int i = getStart(useMissing); i < values.length; i++) {
            total += values[i];
        }
        if (total == 0) return;
        for (int i = getStart(useMissing); i < values.length; i++) {
            values[i] /= total;
        }
        total = 1.0;
    }

    /**
     * Computes the sum of all cells.
     * Missing cell might be used or not,
     *
     * @param useMissing true if missing cell is used, if exists, false otherwise
     * @return sum of elements
     */
    public double sum(boolean useMissing) {
        if (!useMissing)
            return total - values[0];
        return total;
    }

    /**
     * Computes the sum of all cells except a given one and eventually the missing value cell.
     *
     * @param except     the cell excepted from computation
     * @param useMissing true is missing value cell is used, false otherwise
     * @return partial sum of cells
     */
    public double sumExcept(int except, boolean useMissing) {
        return sum(useMissing) - values[except];
    }

    /**
     * Count values which respects the condition given by the predicate.
     *
     * @param pred       condition used to filter the values
     * @param useMissing true if missing is included in filtering, false if not
     * @return count of filtered values
     */
    public int countValues(DoublePredicate pred, boolean useMissing) {
        int count = 0;
        for (int i = getStart(useMissing); i < values.length; i++) {
            if (pred.test(values[i])) {
                count++;
            }
        }
        return count;
    }

    public int rowCount() {
        return labels.length;
    }

    /**
     * Builds a solid copy of the distribution vector
     *
     * @return a solid copy of distribution vector
     */
    public DVector solidCopy() {
        DVector d = new DVector(labels);
        System.arraycopy(values, 0, d.values, 0, labels.length);
        d.total = total;
        return d;
    }

    /**
     * @param useMissing true if the missing cell is used, if exists, false otherwise
     * @return index of the first cell, is 1 if missing cell exists and {@param useMissing} exists, 0 otherwise
     */
    private int getStart(boolean useMissing) {
        if (useMissing)
            return 0;
        return ("?".equals(labels[0])) ? 1 : 0;
    }

    public DoubleStream streamValues() {
        return Arrays.stream(values);
    }

    @Override
    public String toString() {
        return "DVector{" +
                "labels=" + Arrays.toString(labels) +
                ", values=" + Arrays.toString(values) +
                ", total=" + total +
                '}';
    }

    public boolean equalsFull(DVector o) {
        if (labels.length != o.labels.length) {
            return false;
        }
        if (values.length != o.values.length) {
            return false;
        }
        for (int i = 0; i < labels.length; i++) {
            if (!labels[i].equals(o.labels[i]))
                return false;
        }
        for (int i = 0; i < values.length; i++) {
            if (Math.abs(values[i] - o.values[i]) > 1e-30) {
                return false;
            }
        }
        return true;
    }

}
