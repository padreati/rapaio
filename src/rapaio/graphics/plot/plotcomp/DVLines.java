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

package rapaio.graphics.plot.plotcomp;

import rapaio.core.stat.Maximum;
import rapaio.core.stat.Minimum;
import rapaio.data.Var;
import rapaio.graphics.base.Range;
import rapaio.graphics.opt.GOption;
import rapaio.graphics.plot.PlotComponent;

import java.awt.*;
import java.awt.geom.Line2D;

/**
 * Plot component which display discrete vertical lines.
 * The lines starts at 0 and goes to the height given by the first
 * parameter.
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/29/17.
 */
public class DVLines extends PlotComponent {

    private static final long serialVersionUID = -7666732702588432021L;
    private final Var values;
    private final Var indexes;

    public DVLines(Var values, Var indexes, GOption...opts) {
        this.values = values;
        this.indexes = indexes;

        this.options.bind(opts);
    }

    @Override
    protected Range buildRange() {
        return new Range(
                Minimum.of(indexes).value(), Minimum.of(values).value(),
                Maximum.of(indexes).value(), Maximum.of(values).value());
    }

    @Override
    public void paint(Graphics2D g2d) {

        double x1 = parent.xScale(indexes.getDouble(0));
        double y1 = parent.yScale(0);
        double x2 = parent.xScale(indexes.getDouble(indexes.rowCount()-1));
        double y2 = parent.yScale(0);
        g2d.setColor(Color.BLACK);
        Stroke oldStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(options.getLwd()));
        g2d.draw(new Line2D.Double(x1, y1, x2, y2));
        g2d.setStroke(oldStroke);

        for (int i = 0; i < indexes.rowCount(); i++) {
            g2d.setColor(options.getColor(i));
            x1 = parent.xScale(indexes.getDouble(i));
            y1 = parent.yScale(0);
            x2 = parent.xScale(indexes.getDouble(i));
            y2 = parent.yScale(values.getDouble(i));
            oldStroke = g2d.getStroke();
            g2d.setStroke(new BasicStroke(options.getLwd()));
            g2d.draw(new Line2D.Double(x1, y1, x2, y2));
            g2d.setStroke(oldStroke);
        }

    }
}
