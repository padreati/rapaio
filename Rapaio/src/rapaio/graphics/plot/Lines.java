/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

import rapaio.data.Vector;
import rapaio.graphics.Plot;
import rapaio.graphics.base.Range;
import rapaio.graphics.colors.ColorPalette;

import java.awt.*;

/**
 * @author Aurelian Tutuianu
 */
public class Lines extends PlotComponent {

    private final Vector x;
    private final Vector y;

    public Lines(Plot parent, Vector x, Vector y) {
        super(parent);
        this.x = x;
        this.y = y;
    }

    @Override
    public Range getComponentDataRange() {
        if (x.getRowCount() == 0) {
            return null;
        }
        Range range = new Range();
        for (int i = 0; i < x.getRowCount(); i++) {
            if (x.isMissing(i) || y.isMissing(i)) {
                continue;
            }
            range.union(x.getValue(i), y.getValue(i));
        }
        return range;
    }

    @Override
    public void paint(Graphics2D g2d) {

        int lwd = opt().getLwd();
        g2d.setStroke(new BasicStroke(lwd));
        g2d.setBackground(ColorPalette.STANDARD.getColor(255));

        for (int i = 1; i < x.getRowCount(); i++) {
            g2d.setColor(opt().getColor(i));
            int x1 = (int) (plot.xscale(x.getValue(i - 1)));
            int y1 = (int) (plot.yscale(y.getValue(i - 1)));
            int x2 = (int) (plot.xscale(x.getValue(i)));
            int y2 = (int) (plot.yscale(y.getValue(i)));

            //TODO improve this crap to clip only parts of lines outside of the data range
            if (plot.getRange().contains(x.getValue(i - 1), y.getValue(i - 1))
                    && plot.getRange().contains(x.getValue(i), y.getValue(i))) {
                g2d.drawLine(x1, y1, x2, y2);
            }
        }
    }
}
