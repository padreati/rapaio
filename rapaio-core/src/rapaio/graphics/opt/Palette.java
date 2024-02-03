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
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

import java.awt.Color;
import java.io.Serial;
import java.io.Serializable;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public abstract class Palette implements Serializable {

    public static Palette standard() {
        return new StandardColorPalette(Palette.emptyRange);
    }

    public static Palette tableau11() {
        return new TableauClassic11ColorPalette(Palette.emptyRange);
    }

    public static Palette tableau21() {
        return new Tableau21ColorPalette(Palette.emptyRange);
    }

    public static Palette millerStone() {
        return new MillerStonePalette(Palette.emptyRange);
    }

    public static Palette gray() {
        return new GrayColorPalette(Palette.emptyRange);
    }

    public static Palette gray(double start, double end) {
        return new GrayColorPalette(new Range(start, end));
    }

    public static Palette redBlue() {
        return new RedBlueGradient(Palette.emptyRange);
    }

    public static Palette redBlue(double start, double end) {
        return new RedBlueGradient(new Range(start, end));
    }

    public static Palette rgb() {
        return new RedGreenBluePalette(Palette.emptyRange);
    }

    public static Palette rgb(double start, double end) {
        return new RedGreenBluePalette(new Range(start, end));
    }

    public static Palette hueBlueRed() {
        return new HueBlueRed(Palette.emptyRange);
    }

    public static Palette hueBlueRed(double start, double end) {
        return new HueBlueRed(new Range(start, end));
    }

    public static Palette bicolor(Color start, Color end) {
        return new BiColorGradient(start, end, new Range(0, 1));
    }

    public static Palette bicolor(Color start, Color end, double rangeStart, double rangeEnd) {
        return new BiColorGradient(start, end, new Range(rangeStart, rangeEnd));
    }

    public static Palette hue() {
        return hue(0, 360);
    }

    public static Palette hue(double hueStart, double hueEnd) {
        return hue(0, 360, hueStart, hueEnd);
    }

    public static Palette hue(double hueStart, double hueEnd, double rangeFrom, double rangeTo) {
        return new HueGradient(new Range(rangeFrom, rangeTo), new Range(hueStart, hueEnd));
    }

    public static Palette hueMono(float hue, float minSat, float maxSat, float brightness, double rangeStart, double rangeEnd) {
        return new MonoHueGradient(hue, minSat, maxSat, brightness, new Range(rangeStart, rangeEnd));
    }

    public static Palette RedRGB() {
        return new RedRGB();
    }

    public static Palette GreenRGB() {
        return new GreenRGB();
    }

    public static Palette BlueRGB() {
        return new BlueRGB();
    }


    protected static final Range emptyRange = new Range(Double.NaN, Double.NaN);

    protected final Range inputRange;
    protected final Range internalRange;

    protected Palette(Range inputRange, Range internalRange) {
        this.inputRange = inputRange;
        this.internalRange = internalRange;
    }

    public abstract Color getColor(double index);

    protected record Range(double start, double end) {

        public double cut(double value) {
            return Math.max(min(), Math.min(max(), value));
        }

        public double proportion(double value) {
            if (Double.isNaN(start) || Double.isNaN(end)) {
                return value;
            }
            value = cut(value);
            return (value - start) / (end - start);
        }

        public double fromProportion(double proportion) {
            return start + proportion * (end - start);
        }

        public double min() {
            return Math.min(start, end);
        }

        public double max() {
            return Math.max(start, end);
        }
    }
}

final class StandardColorPalette extends IndexedPalette {

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

    public StandardColorPalette(Range inputRange) {
        super(inputRange, colors);
    }
}

final class GrayColorPalette extends Palette {

    @Serial
    private static final long serialVersionUID = 1322632809893188876L;

    public GrayColorPalette(Range inputRange) {
        super(inputRange, new Range(0, 256));
    }

    @Override
    public Color getColor(double value) {
        value = inputRange.proportion(value);
        value = internalRange.fromProportion(value);
        int index = (int) Math.floor(value);
        while (index < 0) {
            index += 256;
        }
        index %= 256;
        return new Color(index, index, index);
    }
}

final class HueBlueRed extends Palette {

    @Serial
    private static final long serialVersionUID = -1363705251691599652L;

    public HueBlueRed(Range inputRange) {
        super(inputRange, new Range(0, 240));
    }

    @Override
    public Color getColor(double value) {
        return new Color(Color.HSBtoRGB((float) ((240. - value) / 360.), 1f, 1f));
    }
}

final class RedBlueGradient extends Palette {

    @Serial
    private static final long serialVersionUID = -8475382197708592744L;

    public RedBlueGradient(Range inputRange) {
        super(inputRange, new Range(0, 360));
    }

    @Override
    public Color getColor(double value) {
        Color start = Color.RED;
        Color end = Color.BLUE;
        double pp = value / 360.;
        int r = (int) (start.getRed() * pp + end.getRed() * (1 - pp));
        int g = (int) (start.getGreen() * pp + end.getGreen() * (1 - pp));
        int b = (int) (start.getBlue() * pp + end.getBlue() * (1 - pp));
        int a = (int) (start.getAlpha() * pp + end.getAlpha() * (1 - pp));
        return new Color(r, g, b, a);
    }
}

final class RedGreenBluePalette extends Palette {

    @Serial
    private static final long serialVersionUID = 7373521590860220143L;

    public RedGreenBluePalette(Range inputRange) {
        super(inputRange, new Range(0, 510));
    }

    @Override
    public Color getColor(double value) {
        int index = (int) Math.floor(value);
        if (index < 256) {
            return new Color(255 - index, index, 0);
        }
        return new Color(0, 510 - index, index - 255);
    }
}

final class TableauClassic11ColorPalette extends IndexedPalette {

    @Serial
    private static final long serialVersionUID = -7776446499900459739L;

    public TableauClassic11ColorPalette(Range inputRange) {
        super(inputRange, new Color[] {
                new Color(0, 0, 0),
                new Color(0xd62728),
                new Color(0x1f77b4),
                new Color(0x2ca02c),
                new Color(0xff7f0e),
                new Color(0x9467bd),
                new Color(0x8c564b),
                new Color(0xe377c2),
                new Color(0x7f7f7f),
                new Color(0xbcbd22),
                new Color(0x17becf)
        });
    }
}

final class Tableau21ColorPalette extends IndexedPalette {

    @Serial
    private static final long serialVersionUID = -7776446499900459739L;

    public Tableau21ColorPalette(Range inputRange) {
        super(inputRange, new Color[] {
                new Color(0, 0, 0),
                new Color(0xd62728),
                new Color(0x2ca02c),
                new Color(0x1f77b4),
                new Color(0xff7f0e),
                new Color(0x9467bd),
                new Color(0x8c564b),
                new Color(0xbcbd22),
                new Color(0x17becf),
                new Color(0xe377c2),
                new Color(0x7f7f7f),
                new Color(0xff9896),
                new Color(0x98df8a),
                new Color(0xaec7e8),
                new Color(0xffbb78),
                new Color(0xc5b0d5),
                new Color(0xc49c94),
                new Color(0xdbdb8d),
                new Color(0xc7c7c7),
                new Color(0x9edae5),
                new Color(0xf7b6d2)
        });
    }
}

final class MillerStonePalette extends IndexedPalette {

    @Serial
    private static final long serialVersionUID = -7776446499900459739L;

    public MillerStonePalette(Range inputRange) {
        super(inputRange, new Color[] {
                new Color(0, 0, 0),
                new Color(0xb66353),
                new Color(0x638b66),
                new Color(0x4f6980),
                new Color(0xf47942),
                new Color(0xb9aa97),
                new Color(0xbfbb60),
                new Color(0x849db1),
                new Color(0xd7ce9f),
                new Color(0xfbb04e),
                new Color(0xa2ceaa),
                new Color(0x7e756d)
        });
    }
}

class IndexedPalette extends Palette {
    private final Color[] colors;

    public IndexedPalette(Range inputRange, Color[] colors) {
        super(inputRange, new Range(0, colors.length - 1));
        this.colors = colors;
    }

    @Override
    public Color getColor(double value) {
        int index = (int) Math.floor(value);
        while (index < 0) {
            index += colors.length;
        }
        if (index >= colors.length) {
            return colors[index % colors.length];
        }
        return colors[index];
    }
}

class BiColorGradient extends Palette {

    private final Color start;
    private final Color end;

    public BiColorGradient(Color start, Color end, Range inputRange) {
        super(inputRange, inputRange);
        this.start = start;
        this.end = end;
    }

    @Override
    public Color getColor(double value) {
        double p = inputRange.proportion(value);
        int r = (int) (start.getRed() * p + end.getRed() * (1 - p));
        int g = (int) (start.getGreen() * p + end.getGreen() * (1 - p));
        int b = (int) (start.getBlue() * p + end.getBlue() * (1 - p));
        return new Color(r, g, b, 255);
    }
}

class HueGradient extends Palette {

    public HueGradient(Range inputRange, Range internalRange) {
        super(inputRange, internalRange);
    }

    @Override
    public Color getColor(double value) {
        double p = inputRange.proportion(value);
        return new Color(Color.HSBtoRGB((float) (internalRange.fromProportion(p) / 360.0), 1f, 1f));
    }
}

class MonoHueGradient extends Palette {

    private final float hue;
    private final float brightness;

    public MonoHueGradient(float hue, float minSat, float maxSat, float brightness, Range inputRange) {
        super(new Range(minSat, maxSat), inputRange);
        this.hue = hue;
        this.brightness = brightness;
    }

    @Override
    public Color getColor(double value) {
        double p = inputRange.proportion(value);
        float sat = (float) (internalRange.fromProportion(p));
        return new Color(Color.HSBtoRGB(hue, sat, brightness));
    }
}

class RedRGB extends Palette {

    protected RedRGB() {
        super(new Range(0, 255), new Range(0, 255));
    }

    @Override
    public Color getColor(double index) {
        return new Color((int) inputRange.cut(index), 0, 0);
    }
}

class GreenRGB extends Palette {

    protected GreenRGB() {
        super(new Range(0, 255), new Range(0, 255));
    }

    @Override
    public Color getColor(double index) {
        return new Color(0, (int) inputRange.cut(index), 0);
    }
}

class BlueRGB extends Palette {

    protected BlueRGB() {
        super(new Range(0, 255), new Range(0, 255));
    }

    @Override
    public Color getColor(double index) {
        return new Color(0, 0, (int) inputRange.cut(index));
    }
}