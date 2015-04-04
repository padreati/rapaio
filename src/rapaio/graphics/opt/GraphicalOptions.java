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
 */

package rapaio.graphics.opt;

import rapaio.data.Index;
import rapaio.data.Numeric;
import rapaio.data.Var;

import java.awt.*;
import java.util.Arrays;

/**
 * Graphical aspect options.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/31/15.
 */
public class GraphicalOptions {

    private static final ColorPalette DEFAULT_PALETTE = ColorPalette.STANDARD;
    private static final Color[] DEFAULT_COLORS = new Color[]{Color.BLACK};
    private static final Float DEFAULT_LWD = 1.2f;
    private static final Var DEFAULT_SIZE = Numeric.newScalar(3);
    private static final Var DEFAULT_PCH = Index.newScalar(0);
    private static final Float DEFAULT_ALPHA = 1.0f;
    private static final Integer DEFAULT_BINS = -1;
    private static final boolean DEFAULT_PROB = false;
    private static final int DEFAULT_POINTS = 256;

    GraphicalOptions parent;
    ColorPalette palette;
    Color[] colors;
    Float lwd;
    Var sizeIndex;
    Var pchIndex;
    Float alpha;
    Integer bins;
    Boolean prob;
    Integer points;

    public GraphicalOptions(GraphicalOption... options) {
        Arrays.stream(options).forEach(o -> o.apply(this));
    }

    public GraphicalOptions parent(GraphicalOptions parent) {
        this.parent = parent;
        return this;
    }

    // getters

    public ColorPalette getPalette() {
        if (palette == null) {
            if (parent != null) {
                return parent.getPalette();
            } else {
                return DEFAULT_PALETTE;
            }
        }
        return palette;
    }

    public Color getColor(int row) {
        if (colors == null) {
            if (parent != null) {
                parent.getColor(row);
            } else {
                return DEFAULT_COLORS[row % DEFAULT_COLORS.length];
            }
        }
        return colors[row % colors.length];
    }

    public float getLwd() {
        if (lwd == null) {
            if (parent != null) {
                return parent.getLwd();
            } else {
                return DEFAULT_LWD;
            }
        }
        return lwd;
    }

    public double getSize(int row) {
        if (sizeIndex == null) {
            if (parent != null) {
                return parent.getSize(row);
            } else {
                return DEFAULT_SIZE.value(row % DEFAULT_SIZE.rowCount());
            }
        }
        return sizeIndex.value(row % sizeIndex.rowCount());
    }

    public int getPch(int row) {
        if (pchIndex == null) {
            if (parent != null) {
                return parent.getPch(row);
            } else {
                return DEFAULT_PCH.index(row % DEFAULT_PCH.rowCount());
            }
        }
        return pchIndex.index(row % pchIndex.rowCount());
    }

    public float getAlpha() {
        if (alpha == null) {
            if (parent != null) {
                return parent.getAlpha();
            } else {
                return DEFAULT_ALPHA;
            }
        }
        return alpha;
    }

    public int getBins() {
        if (bins == null) {
            return (parent != null) ? parent.getBins() : DEFAULT_BINS;
        }
        return bins;
    }

    public boolean getProb() {
        if (prob == null) {
            if (parent != null) {
                return parent.getProb();
            } else {
                return DEFAULT_PROB;
            }
        }
        return prob;
    }

    public int getPoints() {
        if (points == null)
            return parent != null ? parent.getPoints() : DEFAULT_POINTS;
        return points;
    }
}
