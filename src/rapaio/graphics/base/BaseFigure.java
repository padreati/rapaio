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
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.graphics.colors.ColorPalette;

import java.awt.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/14/14.
 */
public abstract class BaseFigure implements Figure {

    protected static Color[] DEFAULT_COLOR = new Color[]{Color.BLACK};

    //
    private ColorPalette colorPalette = ColorPalette.STANDARD;
    private Color[] colors;
    private Float lwd;
    private Var sizeIndex;
    private Var pchIndex = Index.newScalar(0);
    private Float alpha = 1.0f;
    //
    private Range range;


    // color palette

    public BaseFigure colorPalette(ColorPalette colorPalette) {
        this.colorPalette = colorPalette;
        return this;
    }

    // color

    public BaseFigure color(int index) {
        colors = new Color[]{colorPalette.getColor(index)};
        return this;
    }

    public BaseFigure color(Color color) {
        colors = new Color[]{color};
        return this;
    }

    public BaseFigure color(Var color) {
        colors = new Color[color.rowCount()];
        for (int i = 0; i < color.rowCount(); i++) {
            colors[i] = colorPalette.getColor(color.index(i));
        }
        return this;
    }

    protected Color getCol(int row) {
        if (colors == null) {
            return DEFAULT_COLOR[row % DEFAULT_COLOR.length];
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

    public BaseFigure lwd(float lwd) {
        this.lwd = lwd;
        return this;
    }

    // size

    protected Var getDefaultSize() {
        return Numeric.newScalar(3);
    }

    public BaseFigure sz(Var sizeIndex) {
        this.sizeIndex = sizeIndex;
        return this;
    }

    public BaseFigure sz(double size) {
        this.sizeIndex = Numeric.newScalar(size);
        return this;
    }

    public double getSize(int row) {
        if (sizeIndex == null) {
            return getDefaultSize().value(row % getDefaultSize().rowCount());
        }
        return sizeIndex.value(row % sizeIndex.rowCount());
    }

    // pch

    protected Index getDefaultPch() {
        return Index.newScalar(0);
    }

    public BaseFigure pch(Var pchIndex) {
        this.pchIndex = pchIndex;
        return this;
    }

    public BaseFigure pch(int pch) {
        this.pchIndex = Index.newScalar(pch);
        return this;
    }

    public int getPch(int row) {
        if (pchIndex == null) {
            return getDefaultPch().index(row % getDefaultPch().rowCount());
        }
        return pchIndex.index(row % pchIndex.rowCount());
    }

    public BaseFigure alpha(float alpha) {
        this.alpha = alpha;
        return this;
    }

    public float getAlpha() {
        return alpha;
    }


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


}
