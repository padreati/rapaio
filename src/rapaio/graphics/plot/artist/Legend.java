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

import rapaio.graphics.opt.GOption;
import rapaio.graphics.opt.GOptionFill;
import rapaio.graphics.plot.Artist;
import rapaio.graphics.plot.Axis;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.stream.IntStream;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Legend extends Artist {

    private static final long serialVersionUID = 7360504551525942239L;

    public static final int UP_LEFT = 0;

    private final double x;
    private final double y;
    private final int place;

    public Legend(double x, double y, GOption<?>... opts) {
        this.x = x;
        this.y = y;
        this.place = -1;
        options.setFill(new GOptionFill(IntStream.range(1, 256).mapToObj(i -> options.getPalette().getColor(i)).toArray(Color[]::new)));
        this.options.bind(opts);
    }

    public Legend(int place, GOption<?>... opts) {
        this.x = -1;
        this.y = -1;
        this.place = place;
        options.setFill(new GOptionFill(IntStream.range(1, 256).mapToObj(i -> options.getPalette().getColor(i)).toArray(Color[]::new)));
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
    public void paint(Graphics2D g2d) {

        // TODO I've commented that because it seems like it needs a better treatment
//        g2d.setFont(MARKERS_FONT);

        String[] labels = options.getLabels();

        double minHeight = Double.MAX_VALUE;
        for (String string : labels) {
            double height = g2d.getFontMetrics().getStringBounds(string, g2d).getHeight();
            minHeight = Math.min(minHeight, height);
        }
        double size = g2d.getFontMetrics().getStringBounds("aa", g2d).getWidth();
        double xstart = xScale(x);
        double ystart = yScale(y);

        if (place != -1) {
            switch (place) {
                case UP_LEFT:
                    xstart = xScale(plot.xAxis().min());
                    ystart = yScale(plot.yAxis().max());
            }
        }

        for (int i = 0; i < labels.length; i++) {
            g2d.setColor(options.getFill(i));
            g2d.draw(new Rectangle2D.Double(xstart, ystart - minHeight / 3, size, 1));
            g2d.setColor(Color.BLACK);
            g2d.drawString(labels[i], (int) (xstart + size + size / 2), (int) (ystart));
            ystart += minHeight + 1;
        }
    }

    @Override
    public void updateDataRange(Graphics2D g2d) {
    }
}
