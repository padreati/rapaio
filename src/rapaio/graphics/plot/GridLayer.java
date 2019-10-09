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

package rapaio.graphics.plot;

import rapaio.graphics.base.*;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/5/14.
 */
public class GridLayer extends HostFigure {

    private static final long serialVersionUID = 4476430187955007744L;

    final int rows;
    final int cols;
    G[][] assign;
    private java.util.List<G> list = new ArrayList<>();

    public GridLayer(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.assign = new G[rows][cols];
    }

    @Override
    protected Range buildRange() {
        return null;
    }

    public GridLayer add(Figure fig) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (assign[i][j] == null) {
                    return add(i + 1, j + 1, fig);
                }
            }
        }
        return this;
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
    public GridLayer add(int x, int y, Figure figure) {
        return add(x, y, 1, 1, figure);
    }

    public GridLayer add(int row, int col, int w, int h, Figure figure) {
        G g = new G(row - 1, col - 1, w, h, figure);
        list.add(g);
        for (int i = row - 1; i < row - 1 + h; i++) {
            for (int j = col - 1; j < col - 1 + w; j++) {
                assign[i][j] = g;
            }
        }
        return this;
    }

    @Override
    public void paint(Graphics2D g2d, Rectangle r) {
        super.paint(g2d, r);

        double h = r.getHeight() / rows;
        double w = r.getWidth() / cols;

        for (G g : list) {
            Rectangle rect = new Rectangle(
                    (int) (r.x + g.col * w),
                    (int) (r.y + g.row * h),
                    (int) (w * g.width),
                    (int) (h * g.height)
            );
            g.fig.paint(g2d, rect);
        }
    }
}

final class G implements Serializable {

    private static final long serialVersionUID = -2763424578024274986L;

    int row;
    int col;
    int width;
    int height;
    Figure fig;

    public G(int row, int col, int width, int height, Figure fig) {
        this.row = row;
        this.col = col;
        this.width = width;
        this.height = height;
        this.fig = fig;
    }
}