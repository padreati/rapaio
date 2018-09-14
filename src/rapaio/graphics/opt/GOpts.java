/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
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

import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;

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

    private static GOpts defaults;

    static {
        defaults = new GOpts();
        defaults.palette = new GOptionPalette(ColorPalette.STANDARD);
        defaults.color = new GOptionColor(Color.BLACK);
        defaults.lwd = new GOptionLwd(1.0f);
        defaults.sz = new GOptionSz(VarDouble.scalar(3));
        defaults.pch = new GOptionPch(VarInt.scalar(0));
        defaults.alpha = new GOptionAlpha(1.0f);
        defaults.bins = new GOptionBins(-1);
        defaults.prob = new GOptionProb(false);
        defaults.points = new GOptionPoints(256);
        defaults.labels = new GOptionLabels(new String[]{""});
    }

    GOpts parent;

    GOptionPalette palette;
    GOptionColor color;
    GOptionLwd lwd;
    GOptionSz sz;
    GOptionPch pch;
    GOptionAlpha alpha;
    GOptionBins bins;
    GOptionProb prob;
    GOptionPoints points;
    GOptionLabels labels;

    public GOpts bind(GOption... options) {
        Arrays.stream(options).forEach(o -> o.bind(this));
        return this;
    }

    public GOption[] toArray() {
        return new GOption[]{
                palette,
                color,
                lwd,
                sz,
                pch,
                alpha,
                bins,
                prob,
                points,
                labels
        };
    }

    // getters

    public GOpts getParent() {
        return parent;
    }

    public void setParent(GOpts parent) {
        this.parent = parent;
    }

    /*
     * Color palette
     */

    public ColorPalette getPalette() {
        if (palette == null) {
            return parent != null ? parent.getPalette() : defaults.palette.apply(this);
        }
        return palette.apply(this);
    }

    public void setPalette(GOptionPalette palette) {
        this.palette = palette;
    }

    /*
     * Color
     */

    public Color getColor(int row) {
        if (color == null) {
            if (parent != null) {
                return parent.getColor(row);
            } else {
                Color[] _color = defaults.color.apply(this);
                return _color[row % _color.length];
            }
        }
        Color[] _color = color.apply(this);
        return _color[row % _color.length];
    }

    public void setColor(GOptionColor color) {
        this.color = color;
    }

    /*
     * Line width
     */

    public float getLwd() {
        if (lwd == null) {
            return parent != null ? parent.getLwd() : defaults.lwd.apply(this);
        }
        return lwd.apply(this);
    }

    public void setLwd(GOptionLwd lwd) {
        this.lwd = lwd;
    }

    /*
     * Item size
     */

    public double getSz(int row) {
        if (sz == null) {
            if (parent != null) {
                return parent.getSz(row);
            } else {
                Var _sz = defaults.sz.apply(this);
                return _sz.getDouble(row % _sz.rowCount());
            }
        }
        Var _sz = sz.apply(this);
        return _sz.getDouble(row % _sz.rowCount());
    }

    public void setSz(GOptionSz sz) {
        this.sz = sz;
    }

    /*
     * Point character
     */

    public int getPch(int row) {
        if (pch == null) {
            if (parent != null) {
                return parent.getPch(row);
            } else {
                Var _pch = defaults.pch.apply(this);
                return _pch.getInt(row % _pch.rowCount());
            }
        }
        Var _pch = pch.apply(this);
        return _pch.getInt(row % _pch.rowCount());
    }

    public void setPch(GOptionPch pch) {
        this.pch = pch;
    }

    /*
     * Alpha
     */

    public float getAlpha() {
        if (alpha == null) {
            return parent != null ? parent.getAlpha() : defaults.alpha.apply(this);
        }
        return alpha.apply(this);
    }

    public void setAlpha(GOptionAlpha alpha) {
        this.alpha = alpha;
    }

    public int getBins() {
        if (bins == null) {
            return parent != null ? parent.getBins() : defaults.bins.apply(this);
        }
        return bins.apply(this);
    }

    public void setBins(GOptionBins bins) {
        this.bins = bins;
    }

    public boolean getProb() {
        if (prob == null) {
            return parent != null ? parent.getProb() : defaults.prob.apply(this);
        }
        return prob.apply(this);
    }

    public void setProb(GOptionProb prob) {
        this.prob = prob;
    }

    public int getPoints() {
        if (points == null) {
            return parent != null ? parent.getPoints() : defaults.points.apply(this);
        }
        return points.apply(this);
    }

    public void setPoints(GOptionPoints points) {
        this.points = points;
    }

    public String[] getLabels() {
        if (labels == null) {
            return parent != null ? parent.getLabels() : defaults.labels.apply(this);
        }
        return labels.apply(this);
    }

    public void setLabels(GOptionLabels labels) {
        this.labels = labels;
    }
}
