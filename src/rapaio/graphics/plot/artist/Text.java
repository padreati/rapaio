/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.graphics.plot.artist;

import rapaio.graphics.opt.GOption;
import rapaio.graphics.plot.Artist;
import rapaio.graphics.plot.Axis;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.Serial;
import java.util.Arrays;

import static rapaio.graphics.Plotter.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/27/20.
 */
public class Text extends Artist {

    @Serial
    private static final long serialVersionUID = 1699360902819848697L;

    private final double x;
    private final double y;
    private final String text;

    private Rectangle2D[] rectangles;

    public Text(double x, double y, String text, GOption<?>... opts) {
        this.x = x;
        this.y = y;
        this.text = text;
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

    private String[] getTextLines(String text) {
        if (text == null) {
            return new String[0];
        }
        return Arrays.stream(text.split("\n")).map(String::strip).toArray(String[]::new);
    }

    @Override
    public void updateDataRange(Graphics2D g2d) {
        union(x, y);
    }

    @Override
    public void paint(Graphics2D g2d) {
        int xx = (int) xScale(x);
        int yy = (int) yScale(y);

        Font font = options.getFont();
        FontMetrics fm = g2d.getFontMetrics(font);

        for (String line : text.split("\n")) {
            Rectangle2D r = fm.getStringBounds(line, g2d);

            g2d.setFont(font);
            g2d.setColor(options.getColor(0));
            g2d.setClip(new Rectangle(plot.getViewport()));

            int hoffset = 0;
            int voffset = 0;

            switch (options.getHAlign()) {
                case HALIGN_CENTER -> hoffset = -(int) r.getWidth() / 2;
                case HALIGN_RIGHT -> hoffset = -(int) r.getWidth();
            }

            switch (options.getVAlign()) {
                case VALIGN_TOP -> voffset = fm.getAscent();
                case VALIGN_CENTER -> voffset = fm.getAscent() - (int) r.getHeight() / 2;
                case VALIGN_BOTTOM -> voffset = fm.getAscent() - (int) r.getHeight();
            }
            g2d.drawString(line, xx + hoffset, yy + voffset);

            yy += r.getHeight();
        }
    }
}
