/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

import java.awt.*;
import java.io.Serializable;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public enum ColorPalette implements Serializable {

    STANDARD(new StandardColorPalette()),
    GRAY(new GrayColorPalette()),
    HUE(new HueColorPalette()),
    RED_BLUE_GRADIENT(new RedBlueGradient());
    //
    private final Mapping palette;

    ColorPalette(Mapping palette) {
        this.palette = palette;
    }

    public Color getColor(int index) {
        return palette.getColor(index);
    }

    public interface Mapping extends Serializable {

        Color getColor(int index);
    }
}

class StandardColorPalette implements ColorPalette.Mapping {

    private static final long serialVersionUID = -7776446499900459739L;
    private static final Color[] colors;

    static {
        colors = new Color[256];
        for (int i = 0; i < 256; i++) {
            int index = i;
            int r = 0;
            int g = 0;
            int b = 0;
            r = 2 * r + (index & 1);
            index >>= 1;
            g = 2 * g + (index & 1);
            index >>= 1;
            b = 2 * b + (index & 1);
            index >>= 1;
            r = 2 * r + (index & 1);
            index >>= 1;
            g = 2 * g + (index & 1);
            index >>= 1;
            b = 2 * b + (index & 1);
            index >>= 1;
            r = 2 * r + (index & 1);
            index >>= 1;
            g = 2 * g + (index & 1);
            colors[i] = new Color((r + 1) * 32 - 1, (g + 1) * 32 - 1, (b + 1) * 64 - 1);
        }
        colors[0] = Color.BLACK;
        colors[1] = Color.RED;
        colors[2] = Color.BLUE;
        colors[3] = Color.GREEN;
        colors[4] = Color.ORANGE;
    }

    @Override
    public Color getColor(int index) {
        if (index < 0) {
            index *= -1;
        }
        if (index >= colors.length) {
            return colors[index % colors.length];
        }
        return colors[index];
    }
}

class GrayColorPalette implements ColorPalette.Mapping {

    private static final long serialVersionUID = 1322632809893188876L;

    @Override
    public Color getColor(int index) {
        index %= 256;
        return new Color(index, index, index);
    }
}

class HueColorPalette implements ColorPalette.Mapping {

    private static final long serialVersionUID = -677234648694278016L;

    @Override
    public Color getColor(int index) {
        return new Color(Color.HSBtoRGB((float) (index / 360.0), 1f, 1f));
    }
}

class RedBlueGradient implements ColorPalette.Mapping {

    private static final long serialVersionUID = -8475382197708592744L;

    @Override
    public Color getColor(int index) {
        Color start = Color.RED;
        Color end = Color.BLUE;
        double pp = index / 360.;
        int r = (int) (start.getRed() * pp + end.getRed() * (1 - pp));
        int g = (int) (start.getGreen() * pp + end.getGreen() * (1 - pp));
        int b = (int) (start.getBlue() * pp + end.getBlue() * (1 - pp));
        int a = (int) (start.getAlpha() * pp + end.getAlpha() * (1 - pp));
        return new Color(r, g, b, a);
    }
}
