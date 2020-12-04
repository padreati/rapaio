/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.graphics.opt.GOption;
import rapaio.graphics.plot.Artist;
import rapaio.graphics.plot.Axis;
import rapaio.util.function.Double2DoubleFunction;

import java.awt.*;
import java.awt.geom.Line2D;

import static rapaio.graphics.Plotter.points;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class FunctionLine extends Artist {

    private static final long serialVersionUID = 8388944194915495215L;
    private final Double2DoubleFunction f;

    public FunctionLine(Double2DoubleFunction f, GOption<?>... opts) {
        this.f = f;
        // apply default values for function line
        this.options.bind(points(1024 * 64));
        this.options.bind(opts);
    }

    @Override
    public Axis.Type xAxisType() {
        return Axis.Type.NUMERIC;
    }

    @Override
    public Axis.Type yAxisType() {
        return Axis.Type.NUMERIC;
    }

    @Override
    public void updateDataRange(Graphics2D g2d) {
    }

    @Override
    public void paint(Graphics2D g2d) {
        Var x = VarDouble.fill(options.getPoints() + 1, 0);
        Var y = VarDouble.fill(options.getPoints() + 1, 0);
        double xstep = (plot.xAxis().length()) / options.getPoints();
        for (int i = 0; i < x.size(); i++) {
            x.setDouble(i, plot.xAxis().min() + i * xstep);
            y.setDouble(i, f.applyAsDouble(x.getDouble(i)));
        }

        Composite old = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, options.getAlpha()));
        for (int i = 1; i < x.size(); i++) {
            if (contains(x.getDouble(i - 1), y.getDouble(i - 1)) && contains(x.getDouble(i), y.getDouble(i))) {
                g2d.setColor(options.getColor(i));
                g2d.setStroke(new BasicStroke(options.getLwd()));
                g2d.draw(new Line2D.Double(
                        xScale(x.getDouble(i - 1)),
                        yScale(y.getDouble(i - 1)),
                        xScale(x.getDouble(i)),
                        yScale(y.getDouble(i))));

            }
        }
        g2d.setComposite(old);
    }
}
