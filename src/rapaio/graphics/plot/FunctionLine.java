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

import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.graphics.base.Range;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.function.Function;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class FunctionLine extends PlotComponent {

    private final Function<Double, Double> f;
    private final int points;

    public FunctionLine(Function<Double, Double> f) {
        this(f, 1024);
    }

    public FunctionLine(Function<Double, Double> f, int points) {
        this.f = f;
        this.points = points;
    }

    @Override
    public Range buildRange() {
        return null;
    }

    @Override
    public void paint(Graphics2D g2d) {
        Range range = getParent().getRange();
        Var x = Numeric.newFill(points + 1, 0);
        Var y = Numeric.newFill(points + 1, 0);
        double xstep = (range.getX2() - range.getX1()) / points;
        for (int i = 0; i < x.rowCount(); i++) {
            x.setValue(i, range.getX1() + i * xstep);
            y.setValue(i, f.apply(x.value(i)));
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
