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

import rapaio.graphics.base.Range;
import rapaio.graphics.opt.ColorPalette;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class Legend extends PlotComponent {

    private final double x;
    private final double y;
    private final String[] labels;
    private final int[] colors;

    public Legend(double x, double y, String[] labels, int[] colors) {
        this.x = x;
        this.y = y;
        this.labels = labels;
        this.colors = colors;
    }

    @Override
    public void paint(Graphics2D g2d) {

        // TODO I've commented that because it seems like it needs a better treatment
//        g2d.setFont(MARKERS_FONT);
        double minHeight = Double.MAX_VALUE;
        for (String string : labels) {
            double height = g2d.getFontMetrics().getStringBounds(string, g2d).getHeight();
            minHeight = Math.min(minHeight, height);
        }
        double size = g2d.getFontMetrics().getStringBounds("aa", g2d).getWidth();
        double xstart = parent.xScale(x);
        double ystart = parent.yScale(y);

        for (int i = 0; i < labels.length; i++) {
            g2d.setColor(ColorPalette.STANDARD.getColor(colors[i]));
            g2d.draw(new Rectangle2D.Double(xstart, ystart - minHeight / 3, size, 1));
            g2d.drawString(labels[i], (int) (xstart + size + size / 2), (int) (ystart));
            ystart += minHeight + 1;
        }
    }

    @Override
    public Range buildRange() {
        return null;
    }
}
