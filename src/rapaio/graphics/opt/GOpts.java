/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.graphics.opt;

import rapaio.data.Index;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.graphics.Plotter2D;

import java.awt.*;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Graphical aspect options.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/31/15.
 */
public class GOpts implements Serializable {

    private static final long serialVersionUID = -8407683729055712796L;
    GOpts parent;
    public static final GOpts DEFAULTS;

    static {
        DEFAULTS = new GOpts();
        DEFAULTS.palette = ColorPalette.STANDARD;
        DEFAULTS.colors = new Color[]{Color.black};
        DEFAULTS.lwd = 1.2f;
        DEFAULTS.sizeIndex = Numeric.newScalar(3);
        DEFAULTS.pchIndex = Index.newScalar(0);
        DEFAULTS.alpha = 1.0f;
        DEFAULTS.bins = -1;
        DEFAULTS.prob = false;
        DEFAULTS.points = 512;
    }

    //
    ColorPalette palette;
    Color[] colors;
    Float lwd;
    Var sizeIndex;
    Var pchIndex;
    Float alpha;
    Integer bins;
    Boolean prob;
    Integer points;

    public GOpts apply(GOpt... options) {
        Arrays.stream(options).forEach(o -> o.apply(this));
        return this;
    }

    public GOpt[] toArray() {
        return new GOpt[]{
                Plotter2D.palette(palette),
                Plotter2D.color(colors),
                Plotter2D.lwd(lwd),
                Plotter2D.sz(sizeIndex),
                Plotter2D.pch(pchIndex),
                Plotter2D.alpha(alpha),
                Plotter2D.bins(bins),
                Plotter2D.prob(prob),
                Plotter2D.points(points)
        };
    }

    public GOpts parent(GOpts parent) {
        this.parent = parent;
        return this;
    }

    public GOpts defaults() {
        if (parent != null)
            return parent.defaults();
        return DEFAULTS;
    }

    // getters

    public ColorPalette getPalette() {
        if (palette == null) {
            return parent != null ? parent.getPalette() : DEFAULTS.palette;
        }
        return palette;
    }

    public Color getColor(int row) {
        if (colors == null) {
            return parent != null ? parent.getColor(row) : DEFAULTS.colors[row % DEFAULTS.colors.length];
        }
        return colors[row % colors.length];
    }

    public float getLwd() {
        if (lwd == null) {
            return parent != null ? parent.getLwd() : DEFAULTS.lwd;
        }
        return lwd;
    }

    public double getSize(int row) {
        if (sizeIndex == null) {
            return parent != null ? parent.getSize(row) : DEFAULTS.sizeIndex.value(row % DEFAULTS.sizeIndex.rowCount());
        }
        return sizeIndex.value(row % sizeIndex.rowCount());
    }

    public int getPch(int row) {
        if (pchIndex == null) {
            return parent != null ? parent.getPch(row) : DEFAULTS.pchIndex.index(row % DEFAULTS.pchIndex.rowCount());
        }
        return pchIndex.index(row % pchIndex.rowCount());
    }

    public float getAlpha() {
        if (alpha == null) {
            return parent != null ? parent.getAlpha() : DEFAULTS.alpha;
        }
        return alpha;
    }

    public int getBins() {
        if (bins == null) {
            return parent != null ? parent.getBins() : DEFAULTS.bins;
        }
        return bins;
    }

    public boolean getProb() {
        if (prob == null) {
            return parent != null ? parent.getProb() : DEFAULTS.prob;
        }
        return prob;
    }

    public int getPoints() {
        if (points == null)
            return parent != null ? parent.getPoints() : DEFAULTS.points;
        return points;
    }

    public void setParent(GOpts parent) {
        this.parent = parent;
    }

    public void setPalette(ColorPalette palette) {
        this.palette = palette;
    }

    public void setColors(Color[] colors) {
        this.colors = colors;
    }

    public void setLwd(Float lwd) {
        this.lwd = lwd;
    }

    public void setSizeIndex(Var sizeIndex) {
        this.sizeIndex = sizeIndex;
    }

    public void setPchIndex(Var pchIndex) {
        this.pchIndex = pchIndex;
    }

    public void setAlpha(Float alpha) {
        this.alpha = alpha;
    }

    public void setBins(Integer bins) {
        this.bins = bins;
    }

    public void setProb(Boolean prob) {
        this.prob = prob;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }
}
