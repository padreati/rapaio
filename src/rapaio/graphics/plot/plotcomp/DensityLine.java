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
 */

package rapaio.graphics.plot.plotcomp;

import rapaio.core.distributions.empirical.KDE;
import rapaio.core.distributions.empirical.KFunc;
import rapaio.core.distributions.empirical.KFuncGaussian;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.graphics.base.Range;
import rapaio.graphics.opt.GOpt;
import rapaio.graphics.plot.PlotComponent;
import rapaio.util.Pin;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.function.Function;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class DensityLine extends PlotComponent {

    private static final long serialVersionUID = -9207144655129877629L;
    private final Var var;
    private final double bandwidth;
    private final KDE kde;

    public DensityLine(Var var, GOpt... opts) {
        this(var, new KFuncGaussian(), KDE.getSilvermanBandwidth(var), opts);
    }

    public DensityLine(Var var, double bandwidth, GOpt... opts) {
        this(var, new KFuncGaussian(), bandwidth, opts);
    }

    public DensityLine(Var var, KFunc kfunc, GOpt... opts) {
        this(var, kfunc, KDE.getSilvermanBandwidth(var), opts);
    }

    public DensityLine(Var var, KFunc kfunc, double bandwidth, GOpt... opts) {
        this.var = var;
        this.bandwidth = bandwidth;
        this.kde = new KDE(var, kfunc, bandwidth);
        this.options.apply(opts);
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
            double yMax = ((Function<Double, Double>) kde::pdf).apply(s.value());
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
        Range range = parent.getRange();
        Var x = Numeric.newFill(options.getPoints() + 1, 0);
        Var y = Numeric.newFill(options.getPoints() + 1, 0);
        double xstep = (range.x2() - range.x1()) / options.getPoints();
        for (int i = 0; i < x.rowCount(); i++) {
            x.setValue(i, range.x1() + i * xstep);
            y.setValue(i, ((Function<Double, Double>) kde::pdf).apply(x.value(i)));
        }

        for (int i = 1; i < x.rowCount(); i++) {
            if (range.contains(x.value(i - 1), y.value(i - 1)) && range.contains(x.value(i), y.value(i))) {
                g2d.setColor(options.getColor(i));
                g2d.setStroke(new BasicStroke(options.getLwd()));
                g2d.draw(new Line2D.Double(
                        parent.xScale(x.value(i - 1)),
                        parent.yScale(y.value(i - 1)),
                        parent.xScale(x.value(i)),
                        parent.yScale(y.value(i))));

            }
        }
    }
}
