/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

import static java.lang.StrictMath.abs;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.function.BiFunction;

import rapaio.core.stat.Maximum;
import rapaio.core.stat.Minimum;
import rapaio.core.stat.Quantiles;
import rapaio.darray.DArray;
import rapaio.darray.DArrays;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.ml.model.ClassifierModel;
import rapaio.ml.model.ClassifierResult;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/22/15.
 */
public class Grid2D implements Serializable {

    public static Grid2D fromPrediction(ClassifierModel<?, ?, ?> c, Frame df, String xName, String yName, String labelName, int bins) {
        return fromPrediction(c, df, xName, yName, labelName, bins, 0.05);
    }

    public static Grid2D fromPrediction(ClassifierModel<?, ?, ?> c, Frame df, String xName, String yName,
            String labelName, int bins, double margin) {

        double xMin = Minimum.of(df.rvar(xName)).value();
        double xMax = Maximum.of(df.rvar(xName)).value();
        double yMin = Minimum.of(df.rvar(yName)).value();
        double yMax = Maximum.of(df.rvar(yName)).value();

        if (margin > 0) {
            double xDelta = abs(xMax - xMin);
            double yDelta = abs(yMax - yMin);

            xMin -= margin * xDelta;
            xMax += margin * xDelta;

            yMin -= margin * yDelta;
            yMax += margin * yDelta;
        }

        VarDouble x = axis(xMin, xMax, bins).name(xName);
        VarDouble y = axis(yMin, yMax, bins).name(yName);

        Grid2D mg = new Grid2D(x, y);

        VarDouble f1 = VarDouble.empty().name(xName);
        VarDouble f2 = VarDouble.empty().name(yName);

        for (int i = 0; i < x.size(); i++) {
            for (int j = 0; j < y.size(); j++) {
                f1.addDouble(x.getDouble(i));
                f2.addDouble(y.getDouble(j));
            }
        }
        ClassifierResult pred = c.predict(SolidFrame.byVars(f1, f2));
        int pos = 0;
        Var density = pred.firstDensity().rvar(1);
        for (int i = 0; i < x.size(); i++) {
            for (int j = 0; j < y.size(); j++) {
                mg.setValue(i, j, density.getDouble(pos++));
            }
        }
        return mg;
    }

    public static Grid2D fromFunction(BiFunction<Double, Double, Double> f, double xMin, double xMax, double yMin, double yMax,
            int bins) {

        VarDouble x = axis(xMin, xMax, bins);
        VarDouble y = axis(yMin, yMax, bins);

        Grid2D mg = new Grid2D(x, y);
        for (int i = 0; i < x.size(); i++) {
            for (int j = 0; j < y.size(); j++) {
                mg.setValue(i, j, f.apply(x.getDouble(i), y.getDouble(j)));
            }
        }
        return mg;
    }

    public static Grid2D fromVars(Var x, Var y) {
        return new Grid2D(x, y);
    }

    @Serial
    private static final long serialVersionUID = 779676910310235832L;
    private final Var x;
    private final Var y;

    private final DArray<Double> values;

    public Grid2D(Var x, Var y) {
        this.x = x;
        this.y = y;
        this.values = DArrays.zeros(Shape.of(x.size(), y.size()));
    }

    public Var x() {
        return x;
    }

    public Var y() {
        return y;
    }

    public double value(int i, int j) {
        return values.getDouble(i, j);
    }

    public void setValue(int i, int j, double value) {
        values.setDouble(value, i, j);
    }

    /**
     * Computes the quantiles of stored grid values.
     *
     * @param qs desired quantiles to be computed
     * @return array of corresponding quantile values
     */
    public double[] quantiles(double... qs) {
        return Quantiles.of(values.ravel(Order.C).dv(), qs).values();
    }

    /**
     * Fills all grid values with the given value.
     *
     * @param fill fill value
     */
    public void fill(double fill) {
        values.fill_(fill);
    }

    /**
     * Fills all {@link Double#NaN} values with fill value.
     *
     * @param fill fill value
     */
    public void fillNan(double fill) {
        values.fillNan_(fill);
    }

    /**
     * Returns the minimum value stored in grid.
     *
     * @return minimum value
     */
    public double minValue() {
        return values.amin();
    }

    /**
     * Returns the maximum value stored in grid.
     *
     * @return maximum value
     */
    public double maxValue() {
        return values.amax();
    }

    /**
     * Generates an array of equally distanced values starting with minimum value, ending with maximum value, and
     * with a given number of intervals. The intervals have the same size.
     *
     * @param intervals number of intervals
     * @return array of double values with increasing values in sequence
     */
    public double[] seq(int intervals) {
        return seq(intervals, 0.0);
    }

    /**
     * Generates an array of equally distanced values which form intervals. The starting and ending values are
     * enlarged with given extension. Thus, the start value is {@code start - |start|*ext} and end value is
     * {@code end + |end|*ext}, which enlarges the range also when a bound is negative.
     * The intervals have the same size, and the result has exactly {@code intervals + 1} values, or a single
     * value when the range is a single point.
     * <p>
     * An usual extension is a small positive factor like {@code 0.01}, which is equivalent with one percent extension
     * of the whole interval.
     *
     * @param intervals number of intervals
     * @param ext       extension to the intervals
     * @return array of double values with extended increasing values in sequence
     */
    public double[] seq(int intervals, double ext) {
        // the extension moves each end away from the center by a share of its own magnitude; for a negative
        // bound a signed product would move it inwards instead
        double start = minValue();
        double end = maxValue();
        start -= abs(start) * ext;
        end += abs(end) * ext;
        VarDouble axis = axis(start, end, intervals);
        return Arrays.copyOf(axis.elements(), axis.size());
    }

    /**
     * Builds the equally spaced points of one axis, {@code bins + 1} of them from {@code start} to {@code end}.
     * A range which is a single point gives that single point, since any step would repeat it forever.
     *
     * @throws IllegalArgumentException if the number of bins is not positive, or the range is not a finite
     *                                  increasing or constant one
     */
    private static VarDouble axis(double start, double end, int bins) {
        if (bins < 1) {
            throw new IllegalArgumentException("Number of bins/intervals must be at least 1, but was " + bins + ".");
        }
        if (!Double.isFinite(start) || !Double.isFinite(end) || start > end) {
            throw new IllegalArgumentException("Invalid range [" + start + ", " + end + "], expected finite values with start <= end.");
        }
        if (start == end) {
            return VarDouble.wrap(start);
        }
        double step = (end - start) / bins;
        if (!(step > 0)) {
            throw new IllegalArgumentException("Range [" + start + ", " + end + "] is too small to be split into " + bins + " bins.");
        }
        return VarDouble.seq(start, end, step);
    }
}
