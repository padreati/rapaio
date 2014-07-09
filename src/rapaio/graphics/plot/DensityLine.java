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

import rapaio.core.distributions.empirical.KDE;
import rapaio.core.distributions.empirical.KFunc;
import rapaio.core.distributions.empirical.KFuncGaussian;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.graphics.base.Range;
import rapaio.util.Pin;

import java.awt.*;
import java.awt.geom.Line2D;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class DensityLine extends PlotComponent {

    private final Var var;
    private int points;
    private double bandwidth;
    private KFunc kfunc;
    private KDE kde;

    public DensityLine(Var var) {
        this.var = var;
        this.points = 256;
        this.bandwidth = KDE.getSilvermanBandwidth(var);
        this.kfunc = new KFuncGaussian();
        this.kde = new KDE(var, kfunc, bandwidth);
    }

    public DensityLine points(int points) {
        this.points = points;
        return this;
    }

    public DensityLine kfunc(KFunc kfunc) {
        this.kfunc = kfunc;
        this.kde = new KDE(var, kfunc, bandwidth);
        return this;
    }

    public DensityLine bandwidth(double bandwidth) {
        this.bandwidth = bandwidth;
        this.kde = new KDE(var, kfunc, bandwidth);
        return this;
    }

    @Override
    public Range buildRange() {
        Pin<Double> xmin = new Pin<>(Double.NaN);
        Pin<Double> xmax = new Pin<>(Double.NaN);
        Pin<Double> ymin = new Pin<>(0.0);
        Pin<Double> ymax = new Pin<>(Double.NaN);

        var.stream().filter(s -> !s.missing()).forEach(s -> {
            double xMin = kde.getKernel().getMinValue(s.value(), bandwidth);
            double xMax = kde.getKernel().getMaxValue(s.value(), bandwidth);
            double yMax = kde.getPdf().apply(s.value());
            xmin.set(Double.isNaN(xmin.get()) ? xMin : Math.min(xmin.get(), xMin));
            xmax.set(Double.isNaN(xmax.get()) ? xMax : Math.max(xmax.get(), xMax));
            ymax.set(Double.isNaN(ymax.get()) ? yMax : Math.max(ymax.get(), yMax));
        });
        // give some space
        ymax.set(ymax.get() * 1.05);
        Range range = new Range();
        range.setX1(xmin.get());
        range.setX2(xmax.get());
        range.setY1(ymin.get());
        range.setY2(ymax.get());
        return range;
    }

    @Override
    public void paint(Graphics2D g2d) {
        buildRange();
        Range range = getParent().getRange();
        Var x = Numeric.newFill(points + 1, 0);
        Var y = Numeric.newFill(points + 1, 0);
        double xstep = (range.getX2() - range.getX1()) / points;
        for (int i = 0; i < x.rowCount(); i++) {
            x.setValue(i, range.getX1() + i * xstep);
            y.setValue(i, kde.getPdf().apply(x.value(i)));
        }

        for (int i = 1; i < x.rowCount(); i++) {
            if (range.contains(x.value(i - 1), y.value(i - 1)) && range.contains(x.value(i), y.value(i))) {
                g2d.setColor(getCol(i));
                g2d.setStroke(new BasicStroke(getLwd()));
                g2d.draw(new Line2D.Double(
                        getParent().xScale(x.value(i - 1)),
                        getParent().yScale(y.value(i - 1)),
                        getParent().xScale(x.value(i)),
                        getParent().yScale(y.value(i))));

            }
        }
    }
}
