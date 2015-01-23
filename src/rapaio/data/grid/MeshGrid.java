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

package rapaio.data.grid;

import rapaio.core.stat.Maximum;
import rapaio.core.stat.Minimum;
import rapaio.core.stat.Quantiles;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.util.Pair;

import java.util.function.BiFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/22/15.
 */
public class MeshGrid {

    private Var x;
    private Var y;

    private double xStart;
    private double xEnd;

    private double yStart;
    private double yEnd;

    private double[][] grid;

    public MeshGrid(Var x, Var y) {
        this.x = x;
        this.y = y;

        this.xStart = new Minimum(x).value();
        this.xEnd = new Maximum(x).value();
        this.yStart = new Minimum(y).value();
        this.yEnd = new Maximum(y).value();

        this.grid = new double[x.rowCount()][y.rowCount()];
    }

    public Var getX() {
        return x;
    }

    public Var getY() {
        return y;
    }

    public Pair<Double, Double> valueRange() {
        double min = grid[0][0];
        double max = grid[0][0];
        for (int i = 0; i < x.rowCount(); i++) {
            for (int j = 0; j < y.rowCount(); j++) {
                min = Math.min(min, grid[i][j]);
                max = Math.max(max, grid[i][j]);
            }
        }
        return new Pair<>(min, max);
    }

    public double value(int i, int j) {
        return grid[i][j];
    }

    public void setValue(int i, int j, double value) {
        grid[i][j] = value;
    }

    public double[] quantiles(double... qs) {
        Numeric values = Numeric.newEmpty();
        for (int i = 0; i < x.rowCount(); i++) {
            for (int j = 0; j < y.rowCount(); j++) {
                values.addValue(grid[i][j]);
            }
        }
        return new Quantiles(values, qs).values();
    }

    public void fillWithFunction(BiFunction<Double, Double, Double> f) {
        for (int i = 0; i < x.rowCount(); i++) {
            for (int j = 0; j < y.rowCount(); j++) {
                grid[i][j] = f.apply(x.value(i), y.value(j));
            }
        }
    }
}
