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
import rapaio.util.func.SFunction;

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
        DEFAULTS.palette = gOpts -> ColorPalette.STANDARD;
        DEFAULTS.colors = gOpts -> new Color[]{Color.black};
        DEFAULTS.lwd = gOpts -> 1.2f;
        DEFAULTS.sz = gOpts -> Numeric.newScalar(3);
        DEFAULTS.pch = gOpts -> Index.newScalar(0);
        DEFAULTS.alpha = gOpts -> 1.0f;
        DEFAULTS.bins = gOpts -> -1;
        DEFAULTS.prob = gOpts -> false;
        DEFAULTS.points = gOpts -> 256;
    }

    //
    SFunction<GOpts, ColorPalette> palette;
    SFunction<GOpts, Color[]> colors;
    SFunction<GOpts, Float> lwd;
    SFunction<GOpts, Var> sz;
    SFunction<GOpts, Var> pch;
    SFunction<GOpts, Float> alpha;
    SFunction<GOpts, Integer> bins;
    SFunction<GOpts, Boolean> prob;
    SFunction<GOpts, Integer> points;

    public GOpts apply(GOpt... options) {
        Arrays.stream(options).forEach(o -> o.apply(this));
        return this;
    }

    public GOpt[] toArray() {
        return new GOpt[]{
                opt -> opt.setPalette(palette),
                opt -> opt.setColors(colors),
                opt -> opt.setLwd(lwd),
                opt -> opt.setSz(sz),
                opt -> opt.setPch(pch),
                opt -> opt.setAlpha(alpha),
                opt -> opt.setBins(bins),
                opt -> opt.setProb(prob),
                opt -> opt.setPoints(points)
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
            return parent != null ? parent.getPalette() : DEFAULTS.palette.apply(this);
        }
        return palette.apply(this);
    }

    public Color getColor(int row) {
        if (colors == null) {
            return parent != null ? parent.getColor(row) : DEFAULTS.colors.apply(this)[row % DEFAULTS.colors.apply(this).length];
        }
        return colors.apply(this)[row % colors.apply(this).length];
    }

    public float getLwd() {
        if (lwd == null) {
            return parent != null ? parent.getLwd() : DEFAULTS.lwd.apply(this);
        }
        return lwd.apply(this);
    }

    public double getSz(int row) {
        if (sz == null) {
            return parent != null ? parent.getSz(row) :
                    DEFAULTS.sz.apply(this).value(row % DEFAULTS.sz.apply(this).rowCount());
        }
        return sz.apply(this).value(row % sz.apply(this).rowCount());
    }

    public int getPch(int row) {
        if (pch == null) {
            return parent != null ? parent.getPch(row) :
                    DEFAULTS.pch.apply(this).index(row % DEFAULTS.pch.apply(this).rowCount());
        }
        return pch.apply(this).index(row % pch.apply(this).rowCount());
    }

    public float getAlpha() {
        if (alpha == null) {
            return parent != null ? parent.getAlpha() : DEFAULTS.alpha.apply(this);
        }
        return alpha.apply(this);
    }

    public int getBins() {
        if (bins == null) {
            return parent != null ? parent.getBins() : DEFAULTS.bins.apply(this);
        }
        return bins.apply(this);
    }

    public boolean getProb() {
        if (prob == null) {
            return parent != null ? parent.getProb() : DEFAULTS.prob.apply(this);
        }
        return prob.apply(this);
    }

    public int getPoints() {
        if (points == null)
            return parent != null ? parent.getPoints() : DEFAULTS.points.apply(this);
        return points.apply(this);
    }

    public void setParent(GOpts parent) {
        this.parent = parent;
    }

    public void setPalette(SFunction<GOpts, ColorPalette> palette) {
        this.palette = palette;
    }

    public void setColors(SFunction<GOpts, Color[]> colors) {
        this.colors = colors;
    }

    public void setLwd(SFunction<GOpts, Float> lwd) {
        this.lwd = lwd;
    }

    public void setSz(SFunction<GOpts, Var> sizeIndex) {
        this.sz = sizeIndex;
    }

    public void setPch(SFunction<GOpts, Var> pchIndex) {
        this.pch = pchIndex;
    }

    public void setAlpha(SFunction<GOpts, Float> alpha) {
        this.alpha = alpha;
    }

    public void setBins(SFunction<GOpts, Integer> bins) {
        this.bins = bins;
    }

    public void setProb(SFunction<GOpts, Boolean> prob) {
        this.prob = prob;
    }

    public void setPoints(SFunction<GOpts, Integer> points) {
        this.points = points;
    }
}
