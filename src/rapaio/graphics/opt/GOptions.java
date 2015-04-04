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

public class GOptions {
    public static GraphicalOption palette(ColorPalette colorPalette) {
        return opt -> opt.palette = colorPalette;
    }

    public static GraphicalOption color(int index) {
        return opt -> opt.colors = new Color[]{opt.palette.getColor(index)};
    }

    public static GraphicalOption color(Color color) {
        return opt -> opt.colors = new Color[]{color};
    }

    public static GraphicalOption color(Var color) {
        return opt -> opt.colors = new Color[color.rowCount()];
    }

    public static GraphicalOption lwd(float lwd) {
        return opt -> opt.lwd = lwd;
    }

    public static GraphicalOption sz(Var sizeIndex) {
        return opt -> opt.sizeIndex = sizeIndex;
    }

    public static GraphicalOption sz(double size) {
        return opt -> opt.sizeIndex = Numeric.newScalar(size);
    }

    public static GraphicalOption pch(Var pchIndex) {
        return opt -> opt.pchIndex = pchIndex;
    }

    public static GraphicalOption pch(int pch) {
        return opt -> opt.pchIndex = Index.newScalar(pch);
    }

    public static GraphicalOption alpha(float alpha) {
        return opt -> opt.alpha = alpha;
    }

    public static GraphicalOption bins(int bins) {
        return opt -> opt.bins = bins;
    }

    public static GraphicalOption prob(boolean prob) {
        return opt -> opt.prob = prob;
    }

    public static GraphicalOption points(int points) {
        return opt -> opt.points = points;
    }
}
