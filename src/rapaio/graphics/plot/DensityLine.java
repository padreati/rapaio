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

package rapaio.graphics.plot;

import rapaio.core.BaseMath;
import rapaio.data.NumericVector;
import rapaio.data.Vector;
import rapaio.distributions.empirical.KernelDensityEstimator;
import rapaio.distributions.empirical.KernelFunction;
import rapaio.distributions.empirical.KernelFunctionGaussian;
import rapaio.graphics.Plot;
import rapaio.graphics.base.Range;

import java.awt.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class DensityLine extends PlotComponent {

    private final KernelDensityEstimator kde;
    private final int points;
    private final Vector vector;
    private final double bandwidth;

    public DensityLine(Plot plot, Vector vector) {
        this(
                plot,
                vector,
                new KernelFunctionGaussian(),
                new KernelDensityEstimator(vector).getSilvermanBandwidth(vector),
                256);
    }

    public DensityLine(Plot plot, Vector vector, double bandwidth) {
        this(
                plot,
                vector,
                new KernelFunctionGaussian(),
                bandwidth,
                256);
    }

    public DensityLine(Plot plot, Vector vector, KernelFunction kf, double bandwidth, int points) {
        super(plot);
        this.kde = new KernelDensityEstimator(vector, kf, bandwidth);
        this.points = points;
        this.vector = vector;
        this.bandwidth = bandwidth;
    }

    @Override
    public Range getComponentDataRange() {
        double xmin = Double.NaN;
        double xmax = Double.NaN;
        double ymin = 0;
        double ymax = Double.NaN;

        for (int i = 0; i < vector.getRowCount(); i++) {
            if (vector.isMissing(i)) continue;
            if (xmin != xmin) {
                xmin = kde.getKernel().getMinValue(vector.getValue(i), bandwidth);
            } else {
                xmin = BaseMath.min(xmin, kde.getKernel().getMinValue(vector.getValue(i), bandwidth));
            }
            if (xmax != xmax) {
                xmax = kde.getKernel().getMaxValue(vector.getValue(i), bandwidth);
            } else {
                xmax = BaseMath.min(xmax, kde.getKernel().getMaxValue(vector.getValue(i), bandwidth));
            }
            if (ymax != ymax) {
                ymax = kde.getPdfFunction().eval(vector.getValue(i));
            } else {
                ymax = BaseMath.min(ymax, kde.getPdfFunction().eval(vector.getValue(i)));
            }
        }
        // give some space
        ymax *= 1.05;
        Range range = new Range();
        range.setX1(xmin);
        range.setX2(xmax);
        range.setY1(ymin);
        range.setY2(ymax);
        return range;
    }

    @Override
    public void paint(Graphics2D g2d) {
        Range range = plot.getRange();
        Vector x = new NumericVector("", points + 1);
        Vector y = new NumericVector("", points + 1);
        double xstep = (range.getX2() - range.getX1()) / points;
        for (int i = 0; i < x.getRowCount(); i++) {
            x.setValue(i, range.getX1() + i * xstep);
            y.setValue(i, kde.getPdfFunction().eval(x.getValue(i)));
        }

        for (int i = 1; i < x.getRowCount(); i++) {
            if (range.contains(x.getValue(i - 1), y.getValue(i - 1)) && range.contains(x.getValue(i), y.getValue(i))) {
                g2d.setColor(opt().getColor(i));
                g2d.setStroke(new BasicStroke(opt().getLwd()));
                g2d.drawLine(
                        (int) xscale(x.getValue(i - 1)),
                        (int) yscale(y.getValue(i - 1)),
                        (int) xscale(x.getValue(i)),
                        (int) yscale(y.getValue(i)));


            }
        }
    }
}
