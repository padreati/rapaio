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
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

package rapaio.graphics.plot.artist;

import rapaio.core.distributions.empirical.KDE;
import rapaio.core.distributions.empirical.KFunc;
import rapaio.core.distributions.empirical.KFuncGaussian;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.graphics.opt.GOption;
import rapaio.graphics.plot.Artist;
import rapaio.graphics.plot.Axis;

import java.awt.*;
import java.awt.geom.Line2D;

/**
 * Artist which draws a KDE density estimator function.
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class DensityLine extends Artist {

    private static final long serialVersionUID = -9207144655129877629L;
    private final Var var;
    private final double bandwidth;
    private final KDE kde;

    public DensityLine(Var var, GOption<?>... opts) {
        this(var, new KFuncGaussian(), KDE.silvermanBandwidth(var), opts);
    }

    public DensityLine(Var var, double bandwidth, GOption<?>... opts) {
        this(var, new KFuncGaussian(), bandwidth, opts);
    }

    public DensityLine(Var var, KFunc kfunc, GOption<?>... opts) {
        this(var, kfunc, KDE.silvermanBandwidth(var), opts);
    }

    public DensityLine(Var var, KFunc kfunc, double bandwidth, GOption<?>... opts) {
        this.var = var;
        this.bandwidth = bandwidth;
        this.kde = KDE.of(var, kfunc, bandwidth);
        this.options.bind(opts);
    }

    @Override
    public Axis newXAxis() {
        return Axis.numeric(plot);
    }

    @Override
    public Axis newYAxis() {
        return Axis.numeric(plot);
    }

    @Override
    public void updateDataRange() {
        var.stream().complete().forEach(s -> {
            union(kde.kernel().minValue(s.getDouble(), bandwidth), 0);
            union(kde.kernel().maxValue(s.getDouble(), bandwidth), kde.pdf(s.getDouble()));
        });
    }

    @Override
    public void paint(Graphics2D g2d) {
        Var x = VarDouble.fill(options.getPoints() + 1, 0);
        Var y = VarDouble.fill(options.getPoints() + 1, 0);
        double xstep = plot.xAxis().length() / options.getPoints();
        for (int i = 0; i < x.rowCount(); i++) {
            x.setDouble(i, plot.xAxis().min() + i * xstep);
            y.setDouble(i, kde.pdf(x.getDouble(i)));
        }

        Composite oldComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, options.getAlpha()));
        g2d.setStroke(new BasicStroke(options.getLwd()));

        for (int i = 1; i < x.rowCount(); i++) {
            if (contains(x.getDouble(i - 1), y.getDouble(i - 1)) && contains(x.getDouble(i), y.getDouble(i))) {
                g2d.setColor(options.getColor(i));
                g2d.draw(new Line2D.Double(
                        xScale(x.getDouble(i - 1)),
                        yScale(y.getDouble(i - 1)),
                        xScale(x.getDouble(i)),
                        yScale(y.getDouble(i))));

            }
        }

        g2d.setComposite(oldComposite);
    }
}
