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

import rapaio.graphics.opt.ColorPalette;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public abstract class HostFigure extends BaseFigure {

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
    protected Rectangle viewport;

    //
    protected String title;
    protected String yLabel;
    protected String xLabel;
    protected double thickerMinSpace = DEFAULT_THICKER_MIN_SPACE;

    //
    protected boolean leftThicker;
    protected boolean bottomThicker;
    protected boolean leftMarkers;
    protected boolean bottomMarkers;
    protected final ArrayList<String> bottomMarkersMsg = new ArrayList<>();
    protected final ArrayList<Double> bottomMarkersPos = new ArrayList<>();
    protected final ArrayList<String> leftMarkersMsg = new ArrayList<>();
    protected final ArrayList<Double> leftMarkersPos = new ArrayList<>();

    //
    protected double x1 = Double.NaN;
    protected double x2 = Double.NaN;
    protected double y1 = Double.NaN;
    protected double y2 = Double.NaN;

    public HostFigure xLim(double start, double end) {
        this.x1 = start;
        this.x2 = end;
        return this;
    }

    public HostFigure yLim(double start, double end) {
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

    protected int sizeLeftThicker;
    protected int sizeBottomThicker;
    protected int sizeLeftMarkers;
    protected int sizeBottomMarkers;
    protected int sizeTitle;
    protected int sizeYLabel;
    protected int sizeXLabel;

    protected void buildViewport(Rectangle rectangle) {
        viewport = new Rectangle(rectangle);

        viewport.x += MINIMUM_PAD;
        viewport.width -= 2 * MINIMUM_PAD;

        viewport.y += MINIMUM_PAD;
        viewport.height -= 2 * MINIMUM_PAD;

        if (leftThicker) {
            sizeLeftThicker = 2 * THICKER_PAD;
        }
        if (leftMarkers) {
            sizeLeftMarkers = MARKER_PAD;
        }
        if (yLabel != null) {
            sizeYLabel = LABEL_PAD;
        }
        if (title != null) {
            sizeTitle = TITLE_PAD;
        }
        if (bottomThicker) {
            sizeBottomThicker = 2 * THICKER_PAD;
        }
        if (bottomMarkers) {
            sizeBottomMarkers = MARKER_PAD;
        }
        if (xLabel != null) {
            sizeXLabel = LABEL_PAD;
        }

        viewport.x += sizeLeftThicker;
        viewport.width -= sizeLeftThicker;
        viewport.x += sizeLeftMarkers;
        viewport.width -= sizeLeftMarkers;
        viewport.x += sizeYLabel;
        viewport.width -= sizeYLabel;


        viewport.y += sizeTitle;
        viewport.height -= sizeTitle;

        viewport.height -= sizeBottomThicker;
        viewport.height -= sizeBottomMarkers;
        viewport.height -= sizeXLabel;

    }

    public double xScale(double x) {
        return viewport.x + viewport.width * (x - getRange().x1()) / (getRange().x2() - getRange().x1());
    }

    public double yScale(double y) {
        return viewport.y + viewport.height * (1. - (y - getRange().y1()) / (getRange().y2() - getRange().y1()));
    }

    protected Rectangle getViewport() {
        return viewport;
    }

    public HostFigure leftThick(boolean leftThicker) {
        this.leftThicker = leftThicker;
        return this;
    }

    public HostFigure bottomThick(boolean bottomThicker) {
        this.bottomThicker = bottomThicker;
        return this;
    }

    public HostFigure leftMarkers(boolean leftMarkers) {
        this.leftMarkers = leftMarkers;
        return this;
    }

    public HostFigure bottomMarkers(boolean bottomMarkers) {
        this.bottomMarkers = bottomMarkers;
        return this;
    }

    public HostFigure thickMinSpace(double minSpace) {
        thickerMinSpace = minSpace;
        return this;
    }

    public HostFigure title(String title) {
        this.title = title;
        return this;
    }

    public HostFigure yLab(String yLabel) {
        this.yLabel = yLabel;
        return this;
    }

    public HostFigure xLab(String xLabel) {
        this.xLabel = xLabel;
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
            g2d.drawString(title, (int) (rect.x + (rect.width - titleWidth) / 2), TITLE_PAD);
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
        if (yLabel != null) {
            g2d.setFont(LABELS_FONT);
            double ywidth = g2d.getFontMetrics().getStringBounds(yLabel, g2d).getWidth();
            int xx = viewport.x - 5 * THICKER_PAD - MARKER_PAD;
            int yy = (int) (viewport.y + ywidth + (viewport.height - ywidth) / 2);
            g2d.translate(xx, yy);
            g2d.rotate(-Math.PI / 2);
            g2d.drawString(yLabel, 0, 0);
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

        if (xLabel != null) {
            g2d.setFont(LABELS_FONT);
            double xwidth = g2d.getFontMetrics().getStringBounds(xLabel, g2d).getWidth();
            g2d.drawString(xLabel,
                    (int) (viewport.x + (viewport.width - xwidth) / 2),
                    viewport.y + viewport.height + 2 * THICKER_PAD + MARKER_PAD + LABEL_PAD);
        }
    }

    protected void buildNumericBottomMarkers() {
        bottomMarkersPos.clear();
        bottomMarkersMsg.clear();

        int xspots = (int) Math.floor(viewport.width / thickerMinSpace);
        double xspotwidth = viewport.width / xspots;

        for (int i = 0; i <= xspots; i++) {
            bottomMarkersPos.add(i * xspotwidth);
            bottomMarkersMsg.add(String.format("%." + getRange().getProperDecimalsX() + "f", getRange().x1() + getRange().width() * i / xspots));
        }
    }

    protected void buildNumericLeftMarkers() {
        leftMarkersPos.clear();
        leftMarkersMsg.clear();

        int yspots = (int) Math.floor(viewport.height / thickerMinSpace);
        double yspotwidth = viewport.height / yspots;

        for (int i = 0; i <= yspots; i++) {
            leftMarkersPos.add(i * yspotwidth);
            leftMarkersMsg.add(String.format("%." + getRange().getProperDecimalsY() + "f", getRange().y1() + getRange().height() * i / yspots));
        }
    }

    protected void buildLeftMarkers() {
    }

    protected void buildBottomMarkers() {
    }
}
