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
import rapaio.graphics.colors.StandardColorPalette;

import java.awt.*;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public abstract class AbstractFigure implements Figure {

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

    public abstract Range buildRange();

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

    protected Color[] getDefaultColor() {
        return new Color[]{Color.BLACK};
    }

    public AbstractFigure setColor(int index) {
        colors = new Color[]{new StandardColorPalette().getColor(index)};
        return this;
    }

    public AbstractFigure setColor(Color color) {
        colors = new Color[]{color};
        return this;
    }

    public AbstractFigure setColor(Vector color) {
        colors = new Color[color.rowCount()];
        for (int i = 0; i < color.rowCount(); i++) {
            colors[i] = new StandardColorPalette().getColor(color.getIndex(i));
        }
        return this;
    }

    public Color getColor(int row) {
        if (colors == null) {
            return getDefaultColor()[row % getDefaultColor().length];
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

    public AbstractFigure setLwd(float lwd) {
        this.lwd = lwd;
        return this;
    }

    // size

    protected Vector getDefaultSize() {
        return Vectors.newNum(1, 2.5);
    }

    public AbstractFigure setSize(Vector sizeIndex) {
        this.sizeIndex = sizeIndex;
        return this;
    }

    public AbstractFigure setSize(double size) {
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

    public AbstractFigure setPch(Vector pchIndex) {
        this.pchIndex = pchIndex;
        return this;
    }

    public AbstractFigure setPch(int pch) {
        this.pchIndex = Vectors.newIdxOne(pch);
        return this;
    }

    public int getPch(int row) {
        if (pchIndex == null) {
            return getDefaultPch().getIndex(row % getDefaultPch().rowCount());
        }
        return pchIndex.getIndex(row % pchIndex.rowCount());
    }
}
