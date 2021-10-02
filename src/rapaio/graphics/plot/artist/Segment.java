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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.io.Serial;

import rapaio.graphics.opt.GOption;
import rapaio.graphics.plot.Artist;
import rapaio.graphics.plot.Axis;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/5/16.
 */
public class Segment extends Artist {

    @Serial
    private static final long serialVersionUID = 6358307433520540622L;

    private final double x1;
    private final double y1;
    private final double x2;
    private final double y2;
    private final Type type;

    public enum Type {
        LINE,
        ARROW
    }

    public Segment(Type type, double x1, double y1, double x2, double y2, GOption<?>... opts) {
        this.type = type;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
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
        union(x1, y1);
        union(x2, y2);
    }

    @Override
    public void paint(Graphics2D g2d) {

        Composite old = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, options.getAlpha()));

        g2d.setColor(options.getFill(0));
        g2d.setStroke(new BasicStroke(options.getLwd(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

        drawLine(g2d, xScale(x1), yScale(y1), xScale(x2), yScale(y2));

        g2d.setComposite(old);
    }

    void drawLine(Graphics g1, double x1, double y1, double x2, double y2) {
        Graphics2D g = (Graphics2D) g1.create();

        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx * dx + dy * dy);

        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.transform(at);

        int diff = (int) options.getLwd();
        // Draw horizontal arrow starting in (0, 0)
        g.drawLine(0, 0, len - diff, 0);
        if (type.equals(Type.ARROW)) {
            diff = 2 * diff;
            g.fillPolygon(
                    new int[]{len, len - diff, len - diff, len},
                    new int[]{0, -diff, diff, 0}, 4);
        }
    }
}
