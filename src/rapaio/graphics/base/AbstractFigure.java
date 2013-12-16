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

import rapaio.graphics.colors.ColorPalette;

import java.awt.*;
import java.util.ArrayList;
import rapaio.data.OneIndexVector;
import rapaio.data.OneNumericVector;
import rapaio.data.Vector;

/**
 * @author tutuianu
 */
public abstract class AbstractFigure implements Figure {

    protected static final Font TITLE_FONT = new Font("Verdana", Font.BOLD, 18);
    protected static final Font MARKERS_FONT = new Font("Verdana", Font.PLAIN, 13);
    protected static final Font LABELS_FONT = new Font("Verdana", Font.BOLD, 16);
    protected static final double THICKER_MIN_SPACE = 50.;
    protected static final int THICKER_PAD = 7;
    protected static final int MARKER_PAD = 15;
    protected static final int LABEL_PAD = 30;
    protected static final int TITLE_PAD = 40;
    protected static final int MINIMUM_PAD = 20;
    ;
    private AbstractFigure parent;
    ;
    private Rectangle viewport;
    private boolean leftThicker;
    private boolean bottomThicker;
    private boolean leftMarkers;
    private boolean bottomMarkers;
    private Range range;
    private final ArrayList<String> bottomMarkersMsg = new ArrayList<>();
    private final ArrayList<Double> bottomMarkersPos = new ArrayList<>();
    private final ArrayList<String> leftMarkersMsg = new ArrayList<>();
    private final ArrayList<Double> leftMarkersPos = new ArrayList<>();
    private String title;
    private String leftLabel;
    private String bottomLabel;
    ;
    private float lwd = 1.2f;
    private Vector sizeIndex = new OneNumericVector(2.5);
    private Vector colorIndex = new OneIndexVector(0);
    private Vector pchIndex = new OneIndexVector(0);
    private double x1 = Double.NaN;
    private double x2 = Double.NaN;
    private double y1 = Double.NaN;
    private double y2 = Double.NaN;

    @Override
    public void initialize(Rectangle rect) {
        buildViewport(rect);
        range = buildRange();
    }

    public AbstractFigure getParent() {
        return parent;
    }

    public ArrayList<String> getBottomMarkersMsg() {
        return bottomMarkersMsg;
    }

    public ArrayList<Double> getBottomMarkersPos() {
        return bottomMarkersPos;
    }

    public ArrayList<String> getLeftMarkersMsg() {
        return leftMarkersMsg;
    }

    public ArrayList<Double> getLeftMarkersPos() {
        return leftMarkersPos;
    }

    public void setParent(AbstractFigure parent) {
        this.parent = parent;
    }

    public Rectangle getViewport() {
        return viewport;
    }

    public boolean isLeftThicker() {
        return leftThicker;
    }

    public AbstractFigure setLeftThicker(boolean leftThicker) {
        this.leftThicker = leftThicker;
        return this;
    }

    public boolean isBottomThicker() {
        return bottomThicker;
    }

    public AbstractFigure setBottomThicker(boolean bottomThicker) {
        this.bottomThicker = bottomThicker;
        return this;
    }

    public boolean isLeftMarkers() {
        return leftMarkers;
    }

    public AbstractFigure setLeftMarkers(boolean leftMarkers) {
        this.leftMarkers = leftMarkers;
        return this;
    }

    public boolean isBottomMarkers() {
        return bottomMarkers;
    }

    public AbstractFigure setBottomMarkers(boolean bottomMarkers) {
        this.bottomMarkers = bottomMarkers;
        return this;
    }

    public abstract Range buildRange();

    public Range getRange() {
        if (range == null) {
            range = buildRange();
        }
        return range;
    }

    public String getTitle() {
        return title;
    }

    public AbstractFigure setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getLeftLabel() {
        return leftLabel;
    }

    public AbstractFigure setLeftLabel(String leftLabel) {
        this.leftLabel = leftLabel;
        return this;
    }

    public String getBottomLabel() {
        return bottomLabel;
    }

    public AbstractFigure setBottomLabel(String bottomLabel) {
        this.bottomLabel = bottomLabel;
        return this;
    }

    public void buildViewport(Rectangle rectangle) {
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

    public double xscale(double x) {
        return viewport.x + viewport.width * (x - range.getX1()) / (range.getX2() - range.getX1());
    }

    public double yscale(double y) {
        return viewport.y + viewport.height * (1. - (y - range.getY1()) / (range.getY2() - range.getY1()));
    }

    private boolean isDefaultLwd() {
        return lwd == 1.2;
    }

    public float getLwd() {
        if (parent != null && isDefaultLwd()) {
            return parent.getLwd();
        }
        return lwd;
    }

    public AbstractFigure setLwd(float lwd) {
        this.lwd = lwd;
        return this;
    }

    private boolean isDefaultSize() {
        return sizeIndex.getRowCount() == 1 && sizeIndex.getValue(0) == 2.5;
    }

    public Vector getSizeIndex() {
        if (parent != null && isDefaultSize()) {
            return parent.getSizeIndex();
        }
        return sizeIndex;
    }

    public AbstractFigure setSizeIndex(Vector sizeIndex) {
        this.sizeIndex = sizeIndex;
        return this;
    }

    public AbstractFigure setSizeIndex(double size) {
        this.sizeIndex = new OneNumericVector(size);
        return this;
    }

    public double getSize(int row) {
        Vector index = getSizeIndex();
        if (row >= index.getRowCount()) {
            row %= index.getRowCount();
        }
        return index.getValue(row);
    }

    private boolean isDefaultColorIndex() {
        return colorIndex.getRowCount() == 1 && colorIndex.getIndex(0) == 0;
    }

    public Vector getColorIndex() {
        if (parent != null && isDefaultColorIndex()) {
            return parent.getColorIndex();
        }
        return colorIndex;
    }

    public AbstractFigure setColorIndex(Vector colorIndex) {
        this.colorIndex = colorIndex;
        return this;
    }

    public AbstractFigure setColorIndex(int colorIndex) {
        this.colorIndex = new OneIndexVector(colorIndex);
        return this;
    }

    public Color getColor(int row) {
        if (parent != null && isDefaultColorIndex()) {
            return parent.getColor(row);
        }
        Vector index = getColorIndex();
        if (row >= index.getRowCount()) {
            row %= index.getRowCount();
        }
        return ColorPalette.STANDARD.getColor(index.getIndex(row));
    }

    private boolean isDefaultPchIndex() {
        return pchIndex.getIndex(0) == 0 && pchIndex.getRowCount() == 1;
    }

    public Vector getPchIndex() {
        if (parent != null && isDefaultPchIndex()) {
            return parent.getPchIndex();
        }
        return pchIndex;
    }

    public AbstractFigure setPchIndex(Vector pchIndex) {
        this.pchIndex = pchIndex;
        return this;
    }

    public AbstractFigure setPchIndex(int pch) {
        this.pchIndex = new OneIndexVector(pch);
        return this;
    }

    public int getPch(int row) {
        Vector index = getPchIndex();
        if (row >= index.getRowCount()) {
            row %= index.getRowCount();
        }
        return index.getIndex(row);
    }

    public double getXRangeStart() {
        if (parent != null && x1 != x1) {
            return parent.getXRangeStart();
        }
        return x1;
    }

    public double getXRangeEnd() {
        if (parent != null && x2 != x2) {
            return parent.getXRangeEnd();
        }
        return x2;
    }

    public AbstractFigure setXRange(double start, double end) {
        this.x1 = start;
        this.x2 = end;
        return this;
    }

    public double getYRangeStart() {
        if (parent != null && y1 != y1) {
            return parent.getYRangeStart();
        }
        return y1;
    }

    public double getYRangeEnd() {
        if (parent != null && y2 != y2) {
            return parent.getYRangeEnd();
        }
        return y2;
    }

    public AbstractFigure setYRange(double start, double end) {
        this.y1 = start;
        this.y2 = end;
        return this;
    }

    @Override
    public void paint(Graphics2D g2d, Rectangle rect) {
        initialize(rect);

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
                        viewport.y + viewport.height + 2 * THICKER_PAD + MARKER_PAD);
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

        int xspots = (int) Math.floor(viewport.width / THICKER_MIN_SPACE);
        double xspotwidth = viewport.width / xspots;

        for (int i = 0; i <= xspots; i++) {
            bottomMarkersPos.add(i * xspotwidth);
            bottomMarkersMsg.add(String.format("%." + range.getProperDecimalsX() + "f", range.getX1() + range.getWidth() * i / xspots));
        }
    }

    protected void buildNumericLeftMarkers() {
        leftMarkersPos.clear();
        leftMarkersMsg.clear();

        int yspots = (int) Math.floor(viewport.height / THICKER_MIN_SPACE);
        double yspotwidth = viewport.height / yspots;

        for (int i = 0; i <= yspots; i++) {
            leftMarkersPos.add(i * yspotwidth);
            leftMarkersMsg.add(String.format("%." + range.getProperDecimalsY() + "f", range.getY1() + range.getHeight() * i / yspots));
        }
    }

    public void buildLeftMarkers() {
    }

    public void buildBottomMarkers() {
    }
}
