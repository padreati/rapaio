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

package rapaio.experiment.core;

import java.util.function.BiFunction;

import rapaio.core.tools.GridData;
import rapaio.data.VarDouble;
import rapaio.graphics.opt.ColorGradient;
import rapaio.graphics.plot.Plot;
import rapaio.math.linear.DVector;
import rapaio.sys.Experimental;

/**
 * Two dimension grid of vector data.
 */
@Experimental
public class VectorDataGrid {

    private final int channels;
    private final VarDouble xRange;
    private final VarDouble yRange;

    private DVector[] values;

    public VectorDataGrid(int channels, double xMin, double xMax, double yMin, double yMax, double step) {
        this.channels = channels;
        xRange = VarDouble.seq(xMin, xMax, step);
        yRange = VarDouble.seq(yMin, yMax, step);

        values = new DVector[xRange.size() * yRange.size()];
    }

    public VarDouble getXRange() {
        return xRange;
    }

    public VarDouble getYRange() {
        return yRange;
    }

    public void fillValues(BiFunction<Double, Double, DVector> function) {
        int pos = 0;
        for (int i = 0; i < xRange.size(); i++) {
            for (int j = 0; j < yRange.size(); j++) {
                values[pos++] = function.apply(xRange.getDouble(i), yRange.getDouble(j));
            }
        }
    }

    public void set(int i, int j, DVector value) {
        values[i * yRange.size() + j] = value;
    }

    public void plot(Plot p, double min, double max, int steps) {
        float[] hues = new float[channels];
        for (int i = 0; i < channels; i++) {
            hues[i] = (float) i / channels;
        }
        GridData[] grids = new GridData[channels];
        for (int ch = 0; ch < channels; ch++) {
            GridData grid = new GridData(xRange, yRange);
            grid.fill(Double.NaN);
            grids[ch] = grid;
        }


        int pos = 0;
        // collect data from channel
        for (int i = 0; i < xRange.size(); i++) {
            for (int j = 0; j < yRange.size(); j++) {
                DVector vec = values[pos++];
                grids[vec.argmax()].setValue(i, j, vec.get(vec.argmax()));
            }
        }

        for (int ch = 0; ch < channels; ch++) {
            GridData grid = grids[ch];

            double[] aminmax = grid.quantiles(0, 1);
            min = Double.isNaN(min) ? aminmax[0] : min;
            max = Double.isNaN(max) ? aminmax[1] : max;

            grid.fillNan(min - 1);

            double[] q = new double[steps + 1];
            for (int i = 0; i < steps + 1; i++) {
                q[i] = min + i * (max - min) / steps;
            }
            q[q.length-1] *= 1.0001;

            p.isoBands(grid, ColorGradient.newMonoHueGradient(hues[ch], 0f, 1f, 0.8f, q), q);
//            p.isoLines(grid, () -> {
//                Color[] colors = new Color[100];
//                for (int i = 0; i < colors.length; i++) {
//                    colors[i] = Color.BLACK;
//                }
//                return colors;
//            }, q);
        }

    }
}
