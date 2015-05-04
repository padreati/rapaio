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

package rapaio.graphics.plot;

import rapaio.graphics.base.Figure;
import rapaio.graphics.base.HostFigure;
import rapaio.graphics.base.Range;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/5/14.
 */
@Deprecated
public class GridLayer extends HostFigure {

    final int rows;
    final int cols;

    public GridLayer(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
    }

    private java.util.List<G> list = new ArrayList<>();

    @Override
    protected Range buildRange() {
        return null;
    }

    /**
     * Add a grid layer component to the given cell. Cells are indexed
     * starting with 1.
     *
     * @param x      horizontal index of the cell
     * @param y      vertical index of the cell
     * @param figure figure to be drawn
     * @return self reference
     */
    public GridLayer add(int x, int y, HostFigure figure) {
        list.add(new G(y, x, 1, 1, figure));
        return this;
    }

//    public Grid add(int x, int y, int width, int height, HostFigure figure) {
//        list.add(new G(x, y, width, height, figure));
//        return this;
//    }

    @Override
    public void paint(Graphics2D g2d, Rectangle r) {
        super.paint(g2d, r);

        double h = r.getHeight() / rows;
        double w = r.getWidth() / cols;

        for (G g : list) {
            Rectangle rect = new Rectangle(
                    (int) (r.x + (g.x - 1) * w),
                    (int) (r.y + (g.y - 1) * h),
                    (int) (w * g.width),
                    (int) (h * g.height)
            );
            g.fig.paint(g2d, rect);
        }
    }
}

@Deprecated
final class G implements Serializable {
    int x;
    int y;
    int width;
    int height;
    Figure fig;

    public G(int x, int y, int width, int height, Figure fig) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.fig = fig;
    }
}