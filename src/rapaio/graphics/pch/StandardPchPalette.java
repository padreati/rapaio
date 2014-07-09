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

package rapaio.graphics.pch;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class StandardPchPalette implements PchPalette.Mapping {

    private final java.util.List<Drawer> pchs = new ArrayList<Drawer>() {{
        add((g2d, x, y, sz) -> g2d.draw(new Ellipse2D.Double(x - sz, y - sz, sz * 2, sz * 2)));
        add((g2d, x, y, sz) -> g2d.fill(new Ellipse2D.Double(x - sz, y - sz, sz * 2, sz * 2)));
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

interface Drawer {

    void draw(Graphics2D g2d, double x, double y, double size);
}