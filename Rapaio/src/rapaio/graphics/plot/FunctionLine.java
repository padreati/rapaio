/*
 * Copyright 2013 Aurelian Tutuianu
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

import rapaio.data.NumericVector;
import rapaio.data.Vector;
import rapaio.functions.UnivariateFunction;
import rapaio.graphics.Plot;
import rapaio.graphics.base.Range;

import java.awt.*;

import static rapaio.core.BaseMath.validNumber;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class FunctionLine extends PlotComponent {

    private final UnivariateFunction f;
    private final int points;

    public FunctionLine(Plot plot, UnivariateFunction f) {
        this(plot, f, 1024);
    }

    public FunctionLine(Plot plot, UnivariateFunction f, int points) {
        super(plot);
        this.f = f;
        this.points = points;
    }

    @Override
    public Range getComponentDataRange() {
        Range range = new Range();
        range.setX1(opt().getXRangeStart());
        range.setX2(opt().getXRangeEnd());
        range.setY1(opt().getYRangeStart());
        range.setY2(opt().getYRangeEnd());

        if (validNumber(range.getX1()) && validNumber(range.getX2())) {
            return range;
        }
        if (validNumber(range.getY1()) && validNumber(range.getY2())) {
            return range;
        }
        return null;
    }

    @Override
    public void paint(Graphics2D g2d) {
        Range range = plot.getRange();
        Vector x = new NumericVector("", points + 1);
        Vector y = new NumericVector("", points + 1);
        double xstep = (range.getX2() - range.getX1()) / points;
        for (int i = 0; i < x.getRowCount(); i++) {
            x.setValue(i, range.getX1() + i * xstep);
            y.setValue(i, f.eval(x.getValue(i)));
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
