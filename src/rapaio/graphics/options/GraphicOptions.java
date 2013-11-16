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

package rapaio.graphics.options;

import rapaio.data.OneIndexVector;
import rapaio.data.OneNumericVector;
import rapaio.data.Vector;
import rapaio.graphics.colors.ColorPalette;

import java.awt.*;

/**
 * @author tutuianu
 */
public class GraphicOptions {

    public static double SIZE_SCALE = 2;
    private final GraphicOptions parent;
    private float lwd = 1.2f;
    private ColorPalette colorPalette = ColorPalette.STANDARD;
    private Vector sizeIndex = new OneNumericVector(2.5);
    private Vector colorIndex = new OneIndexVector(0);
    private Vector pchIndex = new OneIndexVector(0);
    private double x1 = Double.NaN;
    private double x2 = Double.NaN;
    private double y1 = Double.NaN;
    private double y2 = Double.NaN;

    public GraphicOptions() {
        this(null);
    }

    public GraphicOptions(GraphicOptions parent) {
        this.parent = parent;
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

    public void setLwd(float lwd) {
        this.lwd = lwd;
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

    public void setSizeIndex(Vector sizeIndex) {
        this.sizeIndex = sizeIndex;
    }

    public void setSizeIndex(double size) {
        this.sizeIndex = new OneNumericVector(size);
    }

    public double getSize(int row) {
        Vector index = getSizeIndex();
        if (row >= index.getRowCount()) {
            row %= index.getRowCount();
        }
        return index.getValue(row);
    }

    private boolean isDefaultColorPalette() {
        return colorPalette == ColorPalette.STANDARD;
    }

    public ColorPalette getColorPalette() {
        if (parent != null && isDefaultColorPalette()) {
            return parent.getColorPalette();
        }
        return colorPalette;
    }

    public void setColorPalette(ColorPalette colorPalette) {
        this.colorPalette = colorPalette;
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

    public void setColorIndex(Vector colorIndex) {
        this.colorIndex = colorIndex;
    }

    public void setColorIndex(int colorIndex) {
        this.colorIndex = new OneIndexVector(colorIndex);
    }

    public Color getColor(int row) {
        if (parent != null && isDefaultColorIndex()) {
            return parent.getColor(row);
        }
        Vector index = getColorIndex();
        if (row >= index.getRowCount()) {
            row %= index.getRowCount();
        }
        return getColorPalette().getColor(index.getIndex(row));
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

    public void setPchIndex(Vector pchIndex) {
        this.pchIndex = pchIndex;
    }

    public void setPchIndex(int pch) {
        this.pchIndex = new OneIndexVector(pch);
    }

    public int getPch(int row) {
        Vector index = getPchIndex();
        if (row >= index.getRowCount()) {
            row %= index.getRowCount();
        }
        return index.getIndex(row);
    }

    public double getXRangeStart() {
        if (parent != null && x1 != x1 && x2 != x2) {
            return parent.getXRangeStart();
        }
        return x1;
    }

    public double getXRangeEnd() {
        if (parent != null && x1 != x1 && x2 != x2) {
            return parent.getXRangeEnd();
        }
        return x2;
    }

    public void setXRange(double start, double end) {
        this.x1 = start;
        this.x2 = end;
    }

    public double getYRangeStart() {
        if (parent != null && x1 != x1 && x2 != x2) {
            return parent.getYRangeStart();
        }
        return y1;
    }

    public double getYRangeEnd() {
        if (parent != null && x1 != x1 && x2 != x2) {
            return parent.getYRangeEnd();
        }
        return y2;
    }

    public void setYRange(double start, double end) {
        this.y1 = start;
        this.y2 = end;
    }
}
