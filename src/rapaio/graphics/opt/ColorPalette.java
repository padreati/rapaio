/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.graphics.opt;

import java.awt.Color;
import java.io.Serial;
import java.io.Serializable;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public enum ColorPalette implements Serializable {

    STANDARD(new StandardColorPalette()),
    TABLEAU11(new TableauClassic11ColorPalette()),
    TABLEAU21(new Tableau21ColorPalette()),
    MILLER_STONE(new MillerStonePalette()),
    GRAY(new GrayColorPalette()),
    HUE(new HueColorPalette()),
    RED_BLUE_GRADIENT(new RedBlueGradient()),
    RGB_GRADIENT(new RedGreenBluePalette()),
    HUE_BLUE_RED(new HueBlueRed());
    //
    private final Mapping palette;

    ColorPalette(Mapping palette) {
        this.palette = palette;
    }

    public Color getColor(int index) {
        return palette.getColor(index);
    }

    public int getSize() {
        return palette.getSize();
    }

    public interface Mapping extends Serializable {

        Color getColor(int index);

        int getSize();
    }
}

class StandardColorPalette implements ColorPalette.Mapping {

    @Serial
    private static final long serialVersionUID = -7776446499900459739L;
    private static final Color[] colors;

    static {
        colors = new Color[256];
        for (int i = 0; i < 256; i++) {
            int index = i;
            int r = index & 1;
            index >>= 1;
            int g = index & 1;
            index >>= 1;
            int b = index & 1;
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

    @Override
    public int getSize() {
        return 256;
    }
}

class GrayColorPalette implements ColorPalette.Mapping {

    @Serial
    private static final long serialVersionUID = 1322632809893188876L;

    @Override
    public Color getColor(int index) {
        index %= 256;
        return new Color(index, index, index);
    }

    @Override
    public int getSize() {
        return 256;
    }
}

class HueColorPalette implements ColorPalette.Mapping {

    @Serial
    private static final long serialVersionUID = -677234648694278016L;

    @Override
    public Color getColor(int index) {
        return new Color(Color.HSBtoRGB((float) (index / 360.0), 1f, 1f));
    }

    @Override
    public int getSize() {
        return 360;
    }
}

class HueBlueRed implements ColorPalette.Mapping {

    @Serial
    private static final long serialVersionUID = -1363705251691599652L;

    @Override
    public Color getColor(int index) {
        return new Color(Color.HSBtoRGB((float) ((250. - index) / 360.), 1f, 1f));
    }

    @Override
    public int getSize() {
        return 250;
    }
}

class RedBlueGradient implements ColorPalette.Mapping {

    @Serial
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

    @Override
    public int getSize() {
        return 360;
    }
}

class RedGreenBluePalette implements ColorPalette.Mapping {

    @Serial
    private static final long serialVersionUID = 7373521590860220143L;

    @Override
    public Color getColor(int index) {
        if (index < 256) {
            return new Color(255 - index, index, 0);
        }
        return new Color(0, 510 - index, index - 255);
    }

    @Override
    public int getSize() {
        return 510;
    }
}

class TableauClassic11ColorPalette implements ColorPalette.Mapping {

    @Serial
    private static final long serialVersionUID = -7776446499900459739L;
    private static final Color[] colors;

    static {
        colors = new Color[11];
        colors[0] = Color.BLACK;
        colors[1] = new Color(0xd62728);
        colors[2] = new Color(0x1f77b4);
        colors[3] = new Color(0x2ca02c);
        colors[4] = new Color(0xff7f0e);
        colors[5] = new Color(0x9467bd);
        colors[6] = new Color(0x8c564b);
        colors[7] = new Color(0xe377c2);
        colors[8] = new Color(0x7f7f7f);
        colors[9] = new Color(0xbcbd22);
        colors[10] = new Color(0x17becf);
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

    @Override
    public int getSize() {
        return colors.length;
    }
}

class Tableau21ColorPalette implements ColorPalette.Mapping {

    @Serial
    private static final long serialVersionUID = -7776446499900459739L;
    private static final Color[] colors;

    static {
        colors = new Color[21];
        colors[0] = Color.BLACK;

        colors[1] = new Color(0xd62728);
        colors[2] = new Color(0x2ca02c);
        colors[3] = new Color(0x1f77b4);
        colors[4] = new Color(0xff7f0e);
        colors[5] = new Color(0x9467bd);
        colors[6] = new Color(0x8c564b);
        colors[7] = new Color(0xbcbd22);
        colors[8] = new Color(0x17becf);
        colors[9] = new Color(0xe377c2);
        colors[10] = new Color(0x7f7f7f);
        colors[11] = new Color(0xff9896);
        colors[12] = new Color(0x98df8a);
        colors[13] = new Color(0xaec7e8);
        colors[14] = new Color(0xffbb78);
        colors[15] = new Color(0xc5b0d5);
        colors[16] = new Color(0xc49c94);
        colors[17] = new Color(0xdbdb8d);
        colors[18] = new Color(0xc7c7c7);
        colors[19] = new Color(0x9edae5);
        colors[20] = new Color(0xf7b6d2);
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

    @Override
    public int getSize() {
        return colors.length;
    }
}

class MillerStonePalette implements ColorPalette.Mapping {

    @Serial
    private static final long serialVersionUID = -7776446499900459739L;
    private static final Color[] colors;

    static {
        colors = new Color[12];
        colors[0] = Color.BLACK;

        colors[1] = new Color(0xb66353);
        colors[2] = new Color(0x638b66);
        colors[3] = new Color(0x4f6980);
        colors[4] = new Color(0xf47942);
        colors[5] = new Color(0xb9aa97);
        colors[6] = new Color(0xbfbb60);
        colors[7] = new Color(0x849db1);
        colors[8] = new Color(0xd7ce9f);
        colors[9] = new Color(0xfbb04e);
        colors[10] = new Color(0xa2ceaa);
        colors[11] = new Color(0x7e756d);
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

    @Override
    public int getSize() {
        return colors.length;
    }
}