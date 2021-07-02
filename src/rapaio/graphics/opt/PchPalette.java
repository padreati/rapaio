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

package rapaio.graphics.opt;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
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

        // 0 is a circle
        pchs.add((g2d, x, y, sz, lwd, color, fill) -> {
            g2d.setStroke(new BasicStroke(lwd));
            g2d.setColor(color);
            g2d.draw(new Ellipse2D.Double(x - sz, y - sz, sz * 2, sz * 2));
        });

        // 1 is a filled circle
        pchs.add((g2d, x, y, sz, lwd, color, fill) -> {
            g2d.setStroke(new BasicStroke());
            g2d.setColor(fill);
            g2d.fill(new Ellipse2D.Double(x - sz, y - sz, sz * 2, sz * 2));
        });

        // 2 is a filled circle with a black surrounding
        pchs.add((g2d, x, y, sz, lwd, color, fill) -> {

            g2d.setColor(fill);
            g2d.setStroke(new BasicStroke(1f));
            g2d.fill(new Ellipse2D.Double(x - sz, y - sz, sz * 2, sz * 2));

            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(lwd));
            g2d.draw(new Ellipse2D.Double(x - sz, y - sz, sz * 2, sz * 2));
        });

        // 3 is a cross
        pchs.add((g2d, x, y, sz, lwd, color, fill) -> {
            g2d.setStroke(new BasicStroke(lwd));
            g2d.setColor(fill);
            g2d.draw(new Line2D.Double(x - sz, y - sz, x + sz, y + sz));
            g2d.fill(new Line2D.Double(x - sz, y - sz, x + sz, y + sz));
            g2d.draw(new Line2D.Double(x + sz, y - sz, x - sz, y + sz));
            g2d.fill(new Line2D.Double(x + sz, y - sz, x - sz, y + sz));
        });
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

interface Drawer extends Serializable {

    void draw(Graphics2D g2d, double x, double y, double size, float lwd, Color color, Color fill);
}
