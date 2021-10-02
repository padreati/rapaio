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

package rapaio.graphics.plot;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import rapaio.graphics.Figure;
import rapaio.graphics.opt.ColorPalette;
import rapaio.graphics.opt.GOption;
import rapaio.graphics.opt.GOptions;
import rapaio.util.collection.DoubleArrays;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/5/14.
 */
public class GridLayer implements Figure {

    public static GridLayer of(int rows, int cols, GOption<?>... options) {
        return new GridLayer(rows, cols, options);
    }

    @Serial
    private static final long serialVersionUID = 4476430187955007744L;

    protected static final Font TITLE_FONT = new Font("Verdana", Font.BOLD, 18);
    protected static final Font MARKERS_FONT = new Font("Verdana", Font.PLAIN, 13);
    protected static final Font LABELS_FONT = new Font("Verdana", Font.BOLD, 16);
    protected static final int TITLE_PAD = 40;
    protected static final int MINIMUM_PAD = 20;

    protected final GOptions options = new GOptions();

    protected Rectangle viewport;
    protected String title;


    private final int rows;
    private final int cols;
    private final G[][] assign;
    private final List<G> list = new ArrayList<>();

    protected int sizeTitle;

    public GridLayer(int rows, int cols, GOption<?>... options) {
        this.rows = rows;
        this.cols = cols;
        this.assign = new G[rows][cols];
        this.options.bind(options);
    }

    public GOptions getOptions() {
        return options;
    }

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
                    return add(i, j, plot);
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
        G g = new G(row, col, w, h, plot);
        list.add(g);
        for (int i = row; i < row + h; i++) {
            for (int j = col; j < col + w; j++) {
                assign[i][j] = g;
            }
        }
        return this;
    }

    @Override
    public void prepare(Graphics2D g2d, Rectangle r) {
        buildViewport(r);


        double[] h = options.getHeights().computeSizes(rows, r.getHeight());
        double[] w = options.getWidths().computeSizes(cols, r.getWidth());

        for (G g : list) {
            Rectangle rect = new Rectangle(
                    (int) (r.x + DoubleArrays.sum(w, 0, g.col)),
                    (int) (r.y + DoubleArrays.sum(h, 0, g.row)),
                    (int) (DoubleArrays.sum(w, g.col, g.width)),
                    (int) (DoubleArrays.sum(h, g.row, g.height)));
            g.plot.prepare(g2d, rect);
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

        double[] h = options.getHeights().computeSizes(rows, r.getHeight());
        double[] w = options.getWidths().computeSizes(cols, r.getWidth());

        for (G g : list) {
            Rectangle rect = new Rectangle(
                    (int) (r.x + DoubleArrays.sum(w, 0, g.col)),
                    (int) (r.y + DoubleArrays.sum(h, 0, g.row)),
                    (int) (DoubleArrays.sum(w, g.col, g.width)),
                    (int) (DoubleArrays.sum(h, g.row, g.height)));
            g.plot.paint((Graphics2D) g2d.create(), rect);
        }
    }

    static record G(int row, int col, int width, int height, Plot plot) implements Serializable {
        @Serial
        private static final long serialVersionUID = -2763424578024274986L;
    }
}
