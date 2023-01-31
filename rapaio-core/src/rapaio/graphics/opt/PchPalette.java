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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.graphics.opt;

import static java.lang.StrictMath.sqrt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public enum PchPalette implements Serializable {

    STANDARD(new StandardPchPalette());

    private final Mapping mapping;

    PchPalette(Mapping mapping) {
        this.mapping = mapping;
    }

    public void draw(Graphics2D g2d, double x, double y, double size, int pch, float lwd, Color color, Color fill) {
        mapping.draw(g2d, x, y, size, pch, lwd, color, fill);
    }

}

interface Mapping {
    void draw(Graphics2D g2d, double x, double y, double size, int pch, float lwd, Color color, Color fill);
}

final class StandardPchPalette implements Mapping {

    private final java.util.List<Drawer> pchs;

    public StandardPchPalette() {
        pchs = new ArrayList<>();

        pchs.add(Drawer.CIRCLE_WIRE);
        pchs.add(Drawer.CIRCLE_FILL);
        pchs.add(Drawer.CIRCLE_FULL);
        pchs.add(Drawer.CROSS_WIRE);
        pchs.add(Drawer.TRIANGLE_WIRE);
        pchs.add(Drawer.TRIANGLE_FILL);
        pchs.add(Drawer.TRIANGLE_FULL);
        pchs.add(Drawer.SQUARE_WIRE);
        pchs.add(Drawer.SQUARE_FILL);
        pchs.add(Drawer.SQUARE_FULL);
    }

    @Override
    public void draw(Graphics2D g2d, double x, double y, double size, int pch, float lwd, Color color, Color fill) {
        if (pch < 0) {
            pch = 0;
        }
        if (pch >= pchs.size()) {
            pch %= pchs.size();
        }
        pchs.get(pch).draw(g2d, x, y, size, lwd, color, fill);
    }
}

enum Drawer {

    CIRCLE_WIRE() {
        @Override
        void draw(Graphics2D g2d, double x, double y, double sz, float lwd, Color color, Color fill) {
            g2d.setStroke(new BasicStroke(lwd));
            g2d.setColor(color);
            g2d.draw(new Ellipse2D.Double(x - sz, y - sz, sz * 2, sz * 2));
        }
    },
    CIRCLE_FILL() {
        @Override
        void draw(Graphics2D g2d, double x, double y, double sz, float lwd, Color color, Color fill) {
            g2d.setStroke(new BasicStroke(1f));
            g2d.setColor(fill);
            g2d.fill(new Ellipse2D.Double(x - sz, y - sz, sz * 2, sz * 2));
        }
    },
    CIRCLE_FULL() {
        @Override
        void draw(Graphics2D g2d, double x, double y, double sz, float lwd, Color color, Color fill) {
            g2d.setColor(fill);
            g2d.setStroke(new BasicStroke(1f));
            g2d.fill(new Ellipse2D.Double(x - sz, y - sz, sz * 2, sz * 2));

            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(lwd));
            g2d.draw(new Ellipse2D.Double(x - sz, y - sz, sz * 2, sz * 2));
        }
    },
    CROSS_WIRE() {
        @Override
        void draw(Graphics2D g2d, double x, double y, double sz, float lwd, Color color, Color fill) {
            g2d.setStroke(new BasicStroke(lwd));
            g2d.setColor(color);
            g2d.draw(new Line2D.Double(x - sz, y - sz, x + sz, y + sz));
            g2d.fill(new Line2D.Double(x - sz, y - sz, x + sz, y + sz));
            g2d.draw(new Line2D.Double(x + sz, y - sz, x - sz, y + sz));
            g2d.fill(new Line2D.Double(x + sz, y - sz, x - sz, y + sz));
        }
    },
    TRIANGLE_WIRE() {
        @Override
        void draw(Graphics2D g2d, double x, double y, double sz, float lwd, Color color, Color fill) {
            sz = sz * 1.2;
            Path2D.Double path = new Path2D.Double();
            path.moveTo(x, y - sz);
            path.lineTo(x + sz * sqrt(3) / 2, y + sz * 2 / 3);
            path.lineTo(x - sz * sqrt(3) / 2, y + sz * 2 / 3);
            path.lineTo(x, y - sz);

            g2d.setStroke(new BasicStroke(lwd));
            g2d.setColor(color);
            g2d.draw(path);
        }
    },
    TRIANGLE_FILL() {
        @Override
        void draw(Graphics2D g2d, double x, double y, double sz, float lwd, Color color, Color fill) {
            sz = sz * 1.2;
            Path2D.Double path = new Path2D.Double();
            path.moveTo(x, y - sz);

            path.lineTo(x + sz * sqrt(3) / 2, y + sz * 2 / 3);
            path.lineTo(x - sz * sqrt(3) / 2, y + sz * 2 / 3);
            path.lineTo(x, y - sz);

            g2d.setStroke(new BasicStroke(1f));
            g2d.setColor(fill);
            g2d.fill(path);
        }
    },
    TRIANGLE_FULL() {
        @Override
        void draw(Graphics2D g2d, double x, double y, double sz, float lwd, Color color, Color fill) {
            sz = sz * 1.2;
            Path2D.Double path = new Path2D.Double();
            path.moveTo(x, y - sz);
            path.lineTo(x + sz * sqrt(3) / 2, y + sz * 2 / 3);
            path.lineTo(x - sz * sqrt(3) / 2, y + sz * 2 / 3);
            path.lineTo(x, y - sz);

            g2d.setStroke(new BasicStroke(1f));
            g2d.setColor(fill);
            g2d.fill(path);

            g2d.setStroke(new BasicStroke(lwd));
            g2d.setColor(color);
            g2d.draw(path);
        }
    },
    SQUARE_WIRE() {
        @Override
        void draw(Graphics2D g2d, double x, double y, double sz, float lwd, Color color, Color fill) {
            Path2D.Double path = new Path2D.Double();
            double d = sz;
            path.moveTo(x - d, y - d);
            path.lineTo(x + d, y - d);
            path.lineTo(x + d, y + d);
            path.lineTo(x - d, y + d);
            path.lineTo(x - d, y - d);

            g2d.setStroke(new BasicStroke(lwd));
            g2d.setColor(color);
            g2d.draw(path);
        }
    },
    SQUARE_FILL() {
        @Override
        void draw(Graphics2D g2d, double x, double y, double sz, float lwd, Color color, Color fill) {
            Path2D.Double path = new Path2D.Double();
            double d = sz;
            path.moveTo(x - d, y - d);
            path.lineTo(x + d, y - d);
            path.lineTo(x + d, y + d);
            path.lineTo(x - d, y + d);
            path.lineTo(x - d, y - d);

            g2d.setStroke(new BasicStroke(1f));
            g2d.setColor(fill);
            g2d.fill(path);
        }
    },
    SQUARE_FULL() {
        @Override
        void draw(Graphics2D g2d, double x, double y, double sz, float lwd, Color color, Color fill) {
            Path2D.Double path = new Path2D.Double();
            path.moveTo(x - sz, y - sz);
            path.lineTo(x + sz, y - sz);
            path.lineTo(x + sz, y + sz);
            path.lineTo(x - sz, y + sz);
            path.lineTo(x - sz, y - sz);

            g2d.setStroke(new BasicStroke(1f));
            g2d.setColor(fill);
            g2d.fill(path);

            g2d.setStroke(new BasicStroke(lwd));
            g2d.setColor(color);
            g2d.draw(path);
        }
    };

    abstract void draw(Graphics2D g2d, double x, double y, double sz, float lwd, Color color, Color fill);
}
