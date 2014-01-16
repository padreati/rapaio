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

import rapaio.core.UnivariateFunction;
import rapaio.data.Numeric;
import rapaio.data.Vector;
import rapaio.graphics.base.Range;

import java.awt.*;
import java.awt.geom.Line2D;

import static rapaio.core.BaseMath.validNumber;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class FunctionLine extends PlotComponent {

    private final UnivariateFunction f;
    private final int points;

    public FunctionLine(UnivariateFunction f) {
        this(f, 1024);
    }

    public FunctionLine(UnivariateFunction f, int points) {
        this.f = f;
        this.points = points;
    }

    @Override
    public Range buildRange() {
        Range range = new Range();
        range.setX1(getXRangeStart());
        range.setX2(getXRangeEnd());
        range.setY1(getYRangeStart());
        range.setY2(getYRangeEnd());

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
        Range range = getParent().getRange();
        Vector x = new Numeric(points + 1);
        Vector y = new Numeric(points + 1);
        double xstep = (range.getX2() - range.getX1()) / points;
        for (int i = 0; i < x.rowCount(); i++) {
            x.setValue(i, range.getX1() + i * xstep);
            y.setValue(i, f.eval(x.value(i)));
        }

        for (int i = 1; i < x.rowCount(); i++) {
            if (range.contains(x.value(i - 1), y.value(i - 1)) && range.contains(x.value(i), y.value(i))) {
                g2d.setColor(getColor(i));
                g2d.setStroke(new BasicStroke(getLwd()));
                g2d.draw(new Line2D.Double(
                        getParent().xscale(x.value(i - 1)),
                        getParent().yscale(y.value(i - 1)),
                        getParent().xscale(x.value(i)),
                        getParent().yscale(y.value(i))));

            }
        }
    }
}
