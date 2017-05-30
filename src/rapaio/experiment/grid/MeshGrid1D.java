/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.experiment.grid;

import rapaio.core.stat.Quantiles;
import rapaio.data.NumericVar;
import rapaio.data.Var;

import java.io.Serializable;
import java.util.function.BiFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/22/15.
 */
@Deprecated
public class MeshGrid1D implements Serializable {

    final Var x;
    final Var y;
    final int len;

    NumericVar grid;

    public MeshGrid1D(Var x, Var y) {
        this.x = x;
        this.y = y;
        this.len = y.getRowCount();

        this.grid = NumericVar.empty(x.getRowCount() * y.getRowCount());
    }

    public Var getX() {
        return x;
    }

    public Var getY() {
        return y;
    }

    public double getValue(int i, int j) {
        return grid.getValue(i * len + j);
    }

    public void setValue(int i, int j, double value) {
        if (i * len + j == 4067) {
            System.out.println();
        }
        grid.setValue(i * len + j, value);
    }

    public double[] quantiles(double... qs) {
        return Quantiles.from(grid, qs).getValues();
    }

    public void fillWithFunction(BiFunction<Double, Double, Double> f) {
        for (int i = 0; i < x.getRowCount(); i++) {
            for (int j = 0; j < y.getRowCount(); j++) {
                grid.setValue(i * len + j, f.apply(x.getValue(i), y.getValue(j)));
            }
        }
    }

    public MeshGrid compute(double low, double high) {
        return new MeshGrid1DImpl(this, low, high);
    }
}

@Deprecated
class MeshGrid1DImpl implements MeshGrid {

    private final MeshGrid1D g;
    private final double low;
    private final double high;
    private final int[] sides;

    public MeshGrid1DImpl(MeshGrid1D g, double low, double high) {
        this.g = g;
        this.low = low;
        this.high = high;

        this.sides = new int[g.grid.getRowCount()];
        for (int i = 0; i < g.x.getRowCount(); i++) {
            for (int j = 0; j < g.y.getRowCount(); j++) {
                sides[i * g.len + j] = sideCompute(i, j);
            }
        }
    }

    @Override
    public Var x() {
        return g.x;
    }

    @Override
    public Var y() {
        return g.y;
    }

    @Override
    public int side(int i, int j) {
        return sides[i * g.len + j];
    }

    public int sideCompute(int i, int j) {
        if (g.getValue(i, j) < low) {
            return 0;
        }
        if (g.getValue(i, j) > high) {
            return 2;
        }
        return 1;
    }

    @Override
    public double xLow(int i, int j) {
        if ((side(i, j) == 0 && side(i + 1, j) >= 1) || (side(i, j) >= 1 && side(i + 1, j) == 0)) {
            double value = g.x.getValue(i) + Math.abs(g.x.getValue(i + 1) - g.x.getValue(i)) * Math.abs(low - g.getValue(i, j)) / Math.abs(g.getValue(i + 1, j) - g.getValue(i, j));
            return Math.max(g.x.getValue(i), Math.min(g.x.getValue(i + 1), value));
//            if (value < g.x.getValue(i) || value > g.x.getValue(i + 1)) {
//                throw new RuntimeException("This should not happen");
//            }
//            return value;
        }
        return Double.NaN;
    }

    @Override
    public double xHigh(int i, int j) {
        if ((side(i, j) <= 1 && side(i + 1, j) == 2) || (side(i, j) == 2 && side(i + 1, j) <= 1)) {
            double value = g.x.getValue(i) + Math.abs(g.x.getValue(i + 1) - g.x.getValue(i)) * Math.abs(high - g.getValue(i, j)) / Math.abs(g.getValue(i + 1, j) - g.getValue(i, j));
            return Math.max(g.x.getValue(i), Math.min(g.x.getValue(i + 1), value));
//            if (value < g.x.getValue(i) || value > g.x.getValue(i + 1)) {
//                throw new RuntimeException("This should not happen");
//            }
//            return value;
        }
        return Double.NaN;
    }

    @Override
    public double yLow(int i, int j) {
        if ((side(i, j) == 0 && side(i, j + 1) >= 1) || (side(i, j) >= 1 && side(i, j + 1) == 0)) {
            double value = g.y.getValue(j) + Math.abs(g.y.getValue(j + 1) - g.y.getValue(j)) * Math.abs(g.getValue(i, j) - low) / Math.abs(g.getValue(i, j + 1) - g.getValue(i, j));
            return Math.max(g.y.getValue(j), Math.min(g.y.getValue(j + 1), value));
//            if (value < g.y.getValue(j) || value > g.y.getValue(j + 1)) {
//                throw new RuntimeException("This should not happen");
//            }
//            return value;
        }
        return Double.NaN;
    }

    @Override
    public double yHigh(int i, int j) {
        if ((side(i, j) <= 1 && side(i, j + 1) == 2) || (side(i, j) == 2 && side(i, j + 1) <= 1)) {
            double value = g.y.getValue(j) + Math.abs(g.y.getValue(j + 1) - g.y.getValue(j)) * Math.abs(high - g.getValue(i, j)) / Math.abs(g.getValue(i, j + 1) - g.getValue(i, j));
            return Math.max(g.y.getValue(j), Math.min(g.y.getValue(j + 1), value));
//            if (value < g.y.getValue(j) || value > g.y.getValue(j + 1)) {
//                throw new RuntimeException("This should not happen");
//            }
//            return value;
        }
        return Double.NaN;
    }
}