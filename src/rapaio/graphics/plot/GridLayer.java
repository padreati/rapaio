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

import lombok.AllArgsConstructor;
import lombok.Getter;
import rapaio.graphics.Figure;
import rapaio.graphics.opt.ColorPalette;
import rapaio.graphics.opt.GOptions;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/5/14.
 */
public class GridLayer implements Figure {

    private static final long serialVersionUID = 4476430187955007744L;

    protected static final Font TITLE_FONT = new Font("Verdana", Font.BOLD, 18);
    protected static final Font MARKERS_FONT = new Font("Verdana", Font.PLAIN, 13);
    protected static final Font LABELS_FONT = new Font("Verdana", Font.BOLD, 16);
    protected static final int TITLE_PAD = 40;
    protected static final int MINIMUM_PAD = 20;

    @Getter
    protected final GOptions options = new GOptions();

    protected Rectangle viewport;
    protected String title;


    final int rows;
    final int cols;
    final G[][] assign;
    private final java.util.List<G> list = new ArrayList<>();

    public GridLayer(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.assign = new G[rows][cols];
    }

    protected int sizeTitle;

    protected void buildViewport(Rectangle rectangle) {
        viewport = new Rectangle(rectangle);

        viewport.x += MINIMUM_PAD;
        viewport.width -= 2 * MINIMUM_PAD;

        viewport.y += MINIMUM_PAD;
        viewport.height -= 2 * MINIMUM_PAD;

        if (title != null) {
            sizeTitle = TITLE_PAD;
        }

        viewport.y += sizeTitle;
        viewport.height -= sizeTitle;
    }

    protected Rectangle getViewport() {
        return viewport;
    }

    public GridLayer title(String title) {
        this.title = title;
        return this;
    }

    public GridLayer add(Plot plot) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (assign[i][j] == null) {
                    return add(i + 1, j + 1, plot);
                }
            }
        }
        return this;
    }

    /**
     * Add a grid layer component to the given cell. Cells are indexed
     * starting with 1.
     *
     * @param x    horizontal index of the cell
     * @param y    vertical index of the cell
     * @param plot plot to be drawn
     * @return self reference
     */
    public GridLayer add(int x, int y, Plot plot) {
        return add(x, y, 1, 1, plot);
    }

    public GridLayer add(int row, int col, int w, int h, Plot plot) {
        G g = new G(row - 1, col - 1, w, h, plot);
        list.add(g);
        for (int i = row - 1; i < row - 1 + h; i++) {
            for (int j = col - 1; j < col - 1 + w; j++) {
                assign[i][j] = g;
            }
        }
        return this;
    }

    @Override
    public void prepare(Rectangle r) {
        buildViewport(r);
        double h = r.getHeight() / rows;
        double w = r.getWidth() / cols;

        for (G g : list) {
            Rectangle rect = new Rectangle(
                    (int) (r.x + g.col * w),
                    (int) (r.y + g.row * h),
                    (int) (w * g.width),
                    (int) (h * g.height)
            );
            g.plot.prepare(rect);
        }
    }

    @Override
    public void paint(Graphics2D g2d, Rectangle r) {

        g2d.setColor(ColorPalette.STANDARD.getColor(255));
        g2d.fill(r);

        g2d.setBackground(ColorPalette.STANDARD.getColor(255));
        g2d.setColor(ColorPalette.STANDARD.getColor(0));

        if (title != null) {
            g2d.setFont(TITLE_FONT);
            double titleWidth = g2d.getFontMetrics().getStringBounds(title, g2d).getWidth();
            g2d.drawString(title, (int) (r.x + (r.width - titleWidth) / 2), r.y + TITLE_PAD);
        }

        double h = r.getHeight() / rows;
        double w = r.getWidth() / cols;

        for (G g : list) {
            Rectangle rect = new Rectangle(
                    (int) (r.x + g.col * w),
                    (int) (r.y + g.row * h),
                    (int) (w * g.width),
                    (int) (h * g.height)
            );
            g.plot.paint(g2d, rect);
        }
    }

    @AllArgsConstructor
    static class G implements Serializable {
        private static final long serialVersionUID = -2763424578024274986L;
        final int row;
        final int col;
        final int width;
        final int height;
        final Plot plot;
    }
}
