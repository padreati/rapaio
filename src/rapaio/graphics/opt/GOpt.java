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
import java.io.Serializable;

public interface GOpt extends Serializable {
    void apply(GOpts opt);

    static GOpt palette(ColorPalette colorPalette) {
        return opt -> opt.palette = colorPalette;
    }

    static GOpt color(int index) {
        return opt -> opt.colors = new Color[]{opt.getPalette().getColor(index)};
    }

    static GOpt color(Color color) {
        return opt -> opt.colors = new Color[]{color};
    }

    static GOpt color(Color[] colors) {
        return opt -> opt.colors = colors;
    }

    static GOpt color(Var color) {
        return opt -> {
            opt.colors = new Color[color.rowCount()];
            for (int i = 0; i < opt.colors.length; i++) {
                opt.colors[i] = opt.getPalette().getColor(color.index(i));
            }
        };
    }

    static GOpt lwd(float lwd) {
        return opt -> opt.lwd = lwd;
    }

    static GOpt sz(Var sizeIndex) {
        return opt -> opt.sizeIndex = sizeIndex;
    }

    static GOpt sz(double size) {
        return opt -> opt.sizeIndex = Numeric.newScalar(size);
    }

    static GOpt pch(Var pchIndex) {
        return opt -> opt.pchIndex = pchIndex;
    }

    static GOpt pch(int pch) {
        return opt -> opt.pchIndex = Index.newScalar(pch);
    }

    static GOpt alpha(float alpha) {
        return opt -> opt.alpha = alpha;
    }

    static GOpt bins(int bins) {
        return opt -> opt.bins = bins;
    }

    static GOpt prob(boolean prob) {
        return opt -> opt.prob = prob;
    }

    static GOpt points(int points) {
        return opt -> opt.points = points;
    }
}