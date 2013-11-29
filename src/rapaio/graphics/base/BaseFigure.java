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
import rapaio.graphics.options.GraphicOptions;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author tutuianu
 */
public abstract class BaseFigure implements Figure {

    protected final GraphicOptions options = new GraphicOptions();
    protected static final Font titleFont = new Font("Verdana", Font.BOLD, 18);
    protected static final Font markersFont = new Font("Verdana", Font.PLAIN, 13);
    protected static final Font labelsFont = new Font("Verdana", Font.BOLD, 16);
    protected static final double THICKER_MIN_SPACE = 50.;
    protected static final int THICKER_PAD = 7;
    protected static final int MARKER_PAD = 15;
    protected static final int LABEL_PAD = 30;
    protected static final int TITLE_PAD = 40;
    protected static final int MINIMUM_PAD = 20;
    protected Rectangle viewport;
    protected boolean leftThicker;
    protected boolean bottomThicker;
    protected boolean leftMarkers;
    protected boolean bottomMarkers;
    private Range range;
    protected ArrayList<String> bottomMarkersMsg = new ArrayList<>();
    protected ArrayList<Double> bottomMarkersPos = new ArrayList<>();
    protected ArrayList<String> leftMarkersMsg = new ArrayList<>();
    protected ArrayList<Double> leftMarkersPos = new ArrayList<>();
    protected String title;
    protected String leftLabel;
    protected String bottomLabel;

    @Override
    public GraphicOptions opt() {
        return options;
    }

    public static Font getMarkersFont() {
        return markersFont;
    }

    public static Font getLabelsFont() {
        return labelsFont;
    }

    public Rectangle getViewport() {
        return viewport;
    }

    public boolean isLeftThicker() {
        return leftThicker;
    }

    public void setLeftThicker(boolean leftThicker) {
        this.leftThicker = leftThicker;
    }

    public boolean isBottomThicker() {
        return bottomThicker;
    }

    public void setBottomThicker(boolean bottomThicker) {
        this.bottomThicker = bottomThicker;
    }

    public boolean isLeftMarkers() {
        return leftMarkers;
    }

    public void setLeftMarkers(boolean leftMarkers) {
        this.leftMarkers = leftMarkers;
    }

    public boolean isBottomMarkers() {
        return bottomMarkers;
    }

    public void setBottomMarkers(boolean bottomMarkers) {
        this.bottomMarkers = bottomMarkers;
    }

    public Range getRange() {
        if (range == null) {
            this.range = buildRange();
        }
        return range;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLeftLabel() {
        return leftLabel;
    }

    public void setLeftLabel(String leftLabel) {
        this.leftLabel = leftLabel;
    }

    public String getBottomLabel() {
        return bottomLabel;
    }

    public void setBottomLabel(String bottomLabel) {
        this.bottomLabel = bottomLabel;
    }

    public abstract Range buildRange();

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

    public int xscale(double x) {
        return (int) (viewport.x + viewport.width * (x - range.getX1()) / (range.getX2() - range.getX1()));
    }

    public int yscale(double y) {
        return (int) (viewport.y + viewport.height * (1. - (y - range.getY1()) / (range.getY2() - range.getY1())));
    }

    public double xscaledbl(double x) {
        return viewport.x + viewport.width * (x - range.getX1()) / (range.getX2() - range.getX1());
    }

    public double yscaledbl(double y) {
        return viewport.y + viewport.height * (1. - (y - range.getY1()) / (range.getY2() - range.getY1()));
    }

    @Override
    public void paint(Graphics2D g2d, Rectangle rect) {
        buildViewport(rect);
        if (range == null) {
            range = buildRange();
        }

        g2d.setColor(ColorPalette.STANDARD.getColor(255));
        g2d.fill(rect);

        g2d.setBackground(ColorPalette.STANDARD.getColor(255));
        g2d.setColor(ColorPalette.STANDARD.getColor(0));

        if (title != null) {
            g2d.setFont(titleFont);
            double titleWidth = g2d.getFontMetrics().getStringBounds(title, g2d).getWidth();
            g2d.drawString(title, (int) (rect.width - titleWidth) / 2, TITLE_PAD);
        }

        // left part
        buildLeftMarkers();

        g2d.setFont(markersFont);
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
            g2d.setFont(labelsFont);
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

        g2d.setFont(markersFont);
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
            g2d.setFont(labelsFont);
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

    public abstract void buildLeftMarkers();

    public abstract void buildBottomMarkers();
}
