/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public enum PchPalette implements Serializable {

    STANDARD(new StandardPchPalette());


    private final Mapping mapping;

    PchPalette(Mapping mapping) {
        this.mapping = mapping;
    }

    public void draw(Graphics2D g2d, double x, double y, double size, int pch) {
        mapping.draw(g2d, x, y, size, pch);
    }

}

@Deprecated
interface Mapping {
    void draw(Graphics2D g2d, double x, double y, double size, int pch);
}

@Deprecated
final class StandardPchPalette implements Mapping {

    private final java.util.List<Drawer> pchs = new ArrayList<Drawer>() {{
        add((g2d, x, y, sz) -> g2d.draw(new Ellipse2D.Double(x - sz, y - sz, sz * 2, sz * 2)));
        add((g2d, x, y, sz) -> g2d.fill(new Ellipse2D.Double(x - sz, y - sz, sz * 2, sz * 2)));
        add((g2d, x, y, sz) -> {
            Color fill = g2d.getColor();
            BasicStroke stroke = null;
            if (g2d.getStroke() instanceof BasicStroke)
                stroke = (BasicStroke) g2d.getStroke();
            g2d.fill(new Ellipse2D.Double(x - sz, y - sz, sz * 2, sz * 2));
            if (g2d.getStroke() instanceof BasicStroke)
                g2d.setStroke(new BasicStroke(1f));
            g2d.setColor(Color.BLACK);
            g2d.draw(new Ellipse2D.Double(x - sz, y - sz, sz * 2, sz * 2));
            g2d.setColor(fill);
            if (g2d.getStroke() instanceof BasicStroke)
                g2d.setStroke(stroke);
        });
    }};

    @Override
    public void draw(Graphics2D g2d, double x, double y, double size, int pch) {
        if (pch < 0) {
            pch = 0;
        }
        if (pch >= pchs.size()) {
            pch %= pchs.size();
        }
        pchs.get(pch).draw(g2d, x, y, size);
    }
}

@Deprecated
interface Drawer extends Serializable {

    void draw(Graphics2D g2d, double x, double y, double size);
}
