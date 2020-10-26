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

package rapaio.graphics.plot.artist;

import rapaio.graphics.base.Range;
import rapaio.graphics.opt.GOption;
import rapaio.graphics.plot.Artist;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/5/16.
 */
public class Segment2D extends Artist {

    private static final long serialVersionUID = 6358307433520540622L;

    private final double x1;
    private final double y1;
    private final double x2;
    private final double y2;

    public Segment2D(double x1, double y1, double x2, double y2, GOption<?>... opts) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.options.bind(opts);
    }

    @Override
    public void updateDataRange(Range range) {
        range.union(x1, y1);
        range.union(x2, y2);
    }

    @Override
    public void paint(Graphics2D g2d) {

        Composite old = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, options.getAlpha()));
        g2d.setColor(options.getColor(0));

        g2d.setStroke(new BasicStroke(options.getLwd(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

//        g2d.draw(new Line2D.Double(xScale(x1), yScale(y1), xScale(x2), yScale(y2)));
        drawArrow(g2d, xScale(x1), yScale(y1), xScale(x2), yScale(y2));

        g2d.setComposite(old);
    }

    void drawArrow(Graphics g1, double x1, double y1, double x2, double y2) {
        Graphics2D g = (Graphics2D) g1.create();

        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx * dx + dy * dy);
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.transform(at);

        // Draw horizontal arrow starting in (0, 0)
        g.drawLine(0, 0, len, 0);
        g.fillPolygon(new int[]{len, (int) (len - options.getSz(0)), (int) (len - options.getSz(0)), len},
                new int[]{0, (int) -options.getSz(0), (int) options.getSz(0), 0}, 4);
    }
}
