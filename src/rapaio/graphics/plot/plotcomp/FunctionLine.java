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

package rapaio.graphics.plot.plotcomp;

import rapaio.data.NumericVar;
import rapaio.data.Var;
import rapaio.graphics.base.Range;
import rapaio.graphics.opt.GOpt;
import rapaio.graphics.plot.PlotComponent;
import rapaio.util.func.SFunction;

import java.awt.*;
import java.awt.geom.Line2D;

import static rapaio.graphics.Plotter.points;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class FunctionLine extends PlotComponent {

    private static final long serialVersionUID = 8388944194915495215L;
    private final SFunction<Double, Double> f;

    public FunctionLine(SFunction<Double, Double> f, GOpt... opts) {
        this.f = f;
        // apply default values for function line
        this.options.apply(points(1024 * 10));
        this.options.apply(opts);
    }

    @Override
    public Range buildRange() {
        return null;
    }

    @Override
    public void paint(Graphics2D g2d) {
        Range range = parent.getRange();
        Var x = NumericVar.fill(options.getPoints() + 1, 0);
        Var y = NumericVar.fill(options.getPoints() + 1, 0);
        double xstep = (range.x2() - range.x1()) / options.getPoints();
        for (int i = 0; i < x.getRowCount(); i++) {
            x.setValue(i, range.x1() + i * xstep);
            y.setValue(i, f.apply(x.getValue(i)));
        }

        for (int i = 1; i < x.getRowCount(); i++) {
            if (range.contains(x.getValue(i - 1), y.getValue(i - 1)) && range.contains(x.getValue(i), y.getValue(i))) {
                g2d.setColor(options.getColor(i));
                g2d.setStroke(new BasicStroke(options.getLwd()));
                g2d.draw(new Line2D.Double(
                        parent.xScale(x.getValue(i - 1)),
                        parent.yScale(y.getValue(i - 1)),
                        parent.xScale(x.getValue(i)),
                        parent.yScale(y.getValue(i))));

            }
        }
    }
}
