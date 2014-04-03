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

package rapaio.graphics.base;

import rapaio.data.Index;
import rapaio.data.Vector;
import rapaio.data.Vectors;
import rapaio.graphics.colors.ColorPalette;
import rapaio.graphics.colors.StandardColorPalette;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public abstract class BaseFigure implements Figure {
    //
    private Rectangle viewport;

    //
    private boolean leftThicker;
    private boolean bottomThicker;
    private boolean leftMarkers;
    private boolean bottomMarkers;
    private final ArrayList<String> bottomMarkersMsg = new ArrayList<>();
    private final ArrayList<Double> bottomMarkersPos = new ArrayList<>();
    private final ArrayList<String> leftMarkersMsg = new ArrayList<>();
    private final ArrayList<Double> leftMarkersPos = new ArrayList<>();
    private String title;
    private String leftLabel;
    private String bottomLabel;
    private double thickerMinSpace = DEFAULT_THICKER_MIN_SPACE;

    //
    private double x1 = Double.NaN;
    private double x2 = Double.NaN;
    private double y1 = Double.NaN;
    private double y2 = Double.NaN;

    //
    protected static final Font TITLE_FONT = new Font("Verdana", Font.BOLD, 18);
    protected static final Font MARKERS_FONT = new Font("Verdana", Font.PLAIN, 13);
    protected static final Font LABELS_FONT = new Font("Verdana", Font.BOLD, 16);
    protected static final double DEFAULT_THICKER_MIN_SPACE = 50.;
    protected static final int THICKER_PAD = 7;
    protected static final int MARKER_PAD = 15;
    protected static final int LABEL_PAD = 30;
    protected static final int TITLE_PAD = 40;
    protected static final int MINIMUM_PAD = 20;

    //
    protected Color[] colors;
    protected Float lwd;
    protected Vector sizeIndex;
    protected Vector pchIndex = Vectors.newIdxOne(0);

    //
    private Range range;

    protected abstract Range buildRange();

    public Range getRange() {
        if (range == null) {
            range = buildRange();
        }
        return range;
    }

    protected void setRange(Range range) {
        this.range = range;
    }

    // color

    protected Color[] getDefaultCol() {
        return new Color[]{Color.BLACK};
    }

    public BaseFigure setCol(int index) {
        colors = new Color[]{new StandardColorPalette().getColor(index)};
        return this;
    }

    public BaseFigure setCol(Color color) {
        colors = new Color[]{color};
        return this;
    }

    public BaseFigure setCol(Vector color) {
        colors = new Color[color.rowCount()];
        for (int i = 0; i < color.rowCount(); i++) {
            colors[i] = new StandardColorPalette().getColor(color.getIndex(i));
//            colors[i] = new GrayColorPallete().getColor(color.getIndex(i));
        }
        return this;
    }

    public Color getCol(int row) {
        if (colors == null) {
            return getDefaultCol()[row % getDefaultCol().length];
        }
        return colors[row % colors.length];
    }

    // lwd

    protected float getDefaultLwd() {
        return 1.2f;
    }

    protected boolean isDefaultLwd() {
        return lwd == null;
    }

    public float getLwd() {
        if (lwd == null) {
            return getDefaultLwd();
        }
        return lwd;
    }

    public BaseFigure setLwd(float lwd) {
        this.lwd = lwd;
        return this;
    }

    // size

    protected Vector getDefaultSize() {
        return Vectors.newNum(1, 2.5);
    }

    public BaseFigure setSize(Vector sizeIndex) {
        this.sizeIndex = sizeIndex;
        return this;
    }

    public BaseFigure setSize(double size) {
        this.sizeIndex = Vectors.newNumOne(size);
        return this;
    }

    public double getSize(int row) {
        if (sizeIndex == null) {
            return getDefaultSize().getValue(row % getDefaultSize().rowCount());
        }
        return sizeIndex.getValue(row % sizeIndex.rowCount());
    }

    // pch

    protected Index getDefaultPch() {
        return Vectors.newIdx(1, 0);
    }

    public BaseFigure setPch(Vector pchIndex) {
        this.pchIndex = pchIndex;
        return this;
    }

    public BaseFigure setPch(int pch) {
        this.pchIndex = Vectors.newIdxOne(pch);
        return this;
    }

    public int getPch(int row) {
        if (pchIndex == null) {
            return getDefaultPch().getIndex(row % getDefaultPch().rowCount());
        }
        return pchIndex.getIndex(row % pchIndex.rowCount());
    }

    public BaseFigure setXLim(double start, double end) {
        this.x1 = start;
        this.x2 = end;
        return this;
    }

    public BaseFigure setYLim(double start, double end) {
        this.y1 = start;
        this.y2 = end;
        return this;
    }

    protected double getXRangeStart() {
        return x1;
    }

    protected double getXRangeEnd() {
        return x2;
    }

    protected double getYRangeStart() {
        return y1;
    }

    protected double getYRangeEnd() {
        return y2;
    }

    protected void buildViewport(Rectangle rectangle) {
        viewport = new Rectangle(rectangle);
        viewport.x += 2 * THICKER_PAD;
        if (leftMarkers) {
            viewport.x += MARKER_PAD;
        }
        if (leftLabel != null) {
            viewport.x += LABEL_PAD;
        }
        viewport.x += MINIMUM_PAD;
        if (title != null) {
            viewport.y += TITLE_PAD;
        }
        viewport.y += MINIMUM_PAD;
        viewport.width = rectangle.width - viewport.x - MINIMUM_PAD;

        int height = 0;
        height += 2 * THICKER_PAD;
        if (bottomMarkers) {
            height += MARKER_PAD;
        }
        if (bottomLabel != null) {
            height += LABEL_PAD;
        }
        height += MINIMUM_PAD;

        viewport.height = rectangle.height - viewport.y - height;
    }

    public double xScale(double x) {
        return viewport.x + viewport.width * (x - getRange().getX1()) / (getRange().getX2() - getRange().getX1());
    }

    public double yScale(double y) {
        return viewport.y + viewport.height * (1. - (y - getRange().getY1()) / (getRange().getY2() - getRange().getY1()));
    }

    protected ArrayList<String> getBottomMarkersMsg() {
        return bottomMarkersMsg;
    }

    protected ArrayList<Double> getBottomMarkersPos() {
        return bottomMarkersPos;
    }

    protected ArrayList<String> getLeftMarkersMsg() {
        return leftMarkersMsg;
    }

    protected ArrayList<Double> getLeftMarkersPos() {
        return leftMarkersPos;
    }

    protected Rectangle getViewport() {
        return viewport;
    }

    protected boolean isLeftThicker() {
        return leftThicker;
    }

    public BaseFigure setLeftThicker(boolean leftThicker) {
        this.leftThicker = leftThicker;
        return this;
    }

    public boolean isBottomThicker() {
        return bottomThicker;
    }

    public BaseFigure setBottomThicker(boolean bottomThicker) {
        this.bottomThicker = bottomThicker;
        return this;
    }

    public boolean isLeftMarkers() {
        return leftMarkers;
    }

    public BaseFigure setLeftMarkers(boolean leftMarkers) {
        this.leftMarkers = leftMarkers;
        return this;
    }

    public boolean isBottomMarkers() {
        return bottomMarkers;
    }

    public BaseFigure setBottomMarkers(boolean bottomMarkers) {
        this.bottomMarkers = bottomMarkers;
        return this;
    }


    protected double getThickerMinSpace() {
        return thickerMinSpace;
    }

    public BaseFigure setThickerMinSpace(double minSpace) {
        thickerMinSpace = minSpace;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public BaseFigure setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getYLabel() {
        return leftLabel;
    }

    public BaseFigure setYLab(String leftLabel) {
        this.leftLabel = leftLabel;
        return this;
    }

    public String getXLabel() {
        return bottomLabel;
    }

    public BaseFigure setXLab(String bottomLabel) {
        this.bottomLabel = bottomLabel;
        return this;
    }

    @Override
    public void paint(Graphics2D g2d, Rectangle rect) {
        buildViewport(rect);
        setRange(buildRange());

        g2d.setColor(ColorPalette.STANDARD.getColor(255));
        g2d.fill(rect);

        g2d.setBackground(ColorPalette.STANDARD.getColor(255));
        g2d.setColor(ColorPalette.STANDARD.getColor(0));

        if (title != null) {
            g2d.setFont(TITLE_FONT);
            double titleWidth = g2d.getFontMetrics().getStringBounds(title, g2d).getWidth();
            g2d.drawString(title, (int) (rect.width - titleWidth) / 2, TITLE_PAD);
        }

        // left part
        buildLeftMarkers();

        g2d.setFont(MARKERS_FONT);
        g2d.drawLine(viewport.x - THICKER_PAD,
                viewport.y,
                viewport.x - THICKER_PAD,
                viewport.y + viewport.height);

        for (int i = 0; i < leftMarkersPos.size(); i++) {
            if (leftThicker) {
                g2d.drawLine(
                        viewport.x - 2 * THICKER_PAD,
                        (int) (viewport.y + leftMarkersPos.get(i)),
                        viewport.x - THICKER_PAD,
                        (int) (viewport.y + leftMarkersPos.get(i)));
            }
            if (leftMarkers) {

                int xx = viewport.x - 3 * THICKER_PAD;
                int yy = (int) (viewport.y + viewport.height - leftMarkersPos.get(i)
                        + g2d.getFontMetrics().getStringBounds(leftMarkersMsg.get(i), g2d).getWidth() / 2);
                g2d.translate(xx, yy);
                g2d.rotate(-Math.PI / 2);
                g2d.drawString(leftMarkersMsg.get(i), 0, 0);
                g2d.rotate(Math.PI / 2);
                g2d.translate(-xx, -yy);
            }
        }
        if (leftLabel != null) {
            g2d.setFont(LABELS_FONT);
            double ywidth = g2d.getFontMetrics().getStringBounds(leftLabel, g2d).getWidth();
            int xx = viewport.x - 5 * THICKER_PAD - MARKER_PAD;
            int yy = (int) ((rect.height + ywidth) / 2);
            g2d.translate(xx, yy);
            g2d.rotate(-Math.PI / 2);
            g2d.drawString(leftLabel, 0, 0);
            g2d.rotate(Math.PI / 2);
            g2d.translate(-xx, -yy);
        }

        // bottom part
        buildBottomMarkers();

        g2d.setFont(MARKERS_FONT);
        g2d.drawLine(viewport.x,
                viewport.y + viewport.height + THICKER_PAD,
                viewport.x + viewport.width,
                viewport.y + viewport.height + THICKER_PAD);

        for (int i = 0; i < bottomMarkersPos.size(); i++) {
            if (bottomThicker) {
                g2d.drawLine(
                        (int) (viewport.x + bottomMarkersPos.get(i)),
                        viewport.y + viewport.height + THICKER_PAD,
                        (int) (viewport.x + bottomMarkersPos.get(i)),
                        viewport.y + viewport.height + 2 * THICKER_PAD);
            }
            if (bottomMarkers) {
                g2d.drawString(
                        bottomMarkersMsg.get(i),
                        (int) (viewport.x + bottomMarkersPos.get(i)
                                - g2d.getFontMetrics().getStringBounds(bottomMarkersMsg.get(i), g2d).getWidth() / 2),
                        viewport.y + viewport.height + 2 * THICKER_PAD + MARKER_PAD
                );
            }
        }

        if (bottomLabel != null) {
            g2d.setFont(LABELS_FONT);
            double xwidth = g2d.getFontMetrics().getStringBounds(bottomLabel, g2d).getWidth();
            g2d.drawString(bottomLabel,
                    (int) ((rect.width - xwidth) / 2),
                    viewport.y + viewport.height + 2 * THICKER_PAD + MARKER_PAD + LABEL_PAD);
        }
    }

    protected void buildNumericBottomMarkers() {
        bottomMarkersPos.clear();
        bottomMarkersMsg.clear();

        int xspots = (int) Math.floor(viewport.width / getThickerMinSpace());
        double xspotwidth = viewport.width / xspots;

        for (int i = 0; i <= xspots; i++) {
            bottomMarkersPos.add(i * xspotwidth);
            bottomMarkersMsg.add(String.format("%." + getRange().getProperDecimalsX() + "f", getRange().getX1() + getRange().getWidth() * i / xspots));
        }
    }

    protected void buildNumericLeftMarkers() {
        leftMarkersPos.clear();
        leftMarkersMsg.clear();

        int yspots = (int) Math.floor(viewport.height / getThickerMinSpace());
        double yspotwidth = viewport.height / yspots;

        for (int i = 0; i <= yspots; i++) {
            leftMarkersPos.add(i * yspotwidth);
            leftMarkersMsg.add(String.format("%." + getRange().getProperDecimalsY() + "f", getRange().getY1() + getRange().getHeight() * i / yspots));
        }
    }

    protected void buildLeftMarkers() {
    }

    protected void buildBottomMarkers() {
    }
}
