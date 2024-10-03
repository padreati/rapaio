/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import rapaio.core.tools.Grid2D;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.util.function.SFunction;
import rapaio.printer.opt.NamedParamSet;

public class GOpts extends NamedParamSet<GOpts, GOpt<?>> {

    public static final int HALIGN_LEFT = -1;
    public static final int HALIGN_CENTER = 0;
    public static final int HALIGN_RIGHT = 1;

    public static final int PCH_CIRCLE_WIRE = 0;
    public static final int PCH_CIRCLE_FILL = 1;
    public static final int PCH_CIRCLE_FULL = 2;
    public static final int PCH_CROSS_WIRE = 3;
    public static final int PCH_TRIANGLE_WIRE = 4;
    public static final int PCH_TRIANGLE_FILL = 5;
    public static final int PCH_TRIANGLE_FULL = 6;
    public static final int PCH_SQUARE_WIRE = 7;
    public static final int PCH_SQUARE_FILL = 8;
    public static final int PCH_SQUARE_FULL = 9;

    public static final int SORT_DESC = -1;
    public static final int SORT_NONE = 0;
    public static final int SORT_ASC = 1;

    public static final int VALIGN_TOP = -1;
    public static final int VALIGN_CENTER = 0;
    public static final int VALIGN_BOTTOM = 1;


    private static final GOpt<Palette> _palette = new GOpt<>("palette", __ -> Palette.tableau21());
    private static final GOpt<Color[]> _color = new GOpt<>("color", __ -> new Color[] {Color.BLACK});
    private static final GOpt<Color[]> _fill = new GOpt<>("fill", __ -> null);
    private static final GOpt<Float> _lwd = new GOpt<>("lwd", __ -> 1.0f);
    private static final GOpt<Var> _sz = new GOpt<>("sz", __ -> VarDouble.scalar(3));
    private static final GOpt<VarInt> _pch = new GOpt<>("pch", __ -> VarInt.scalar(0));
    private static final GOpt<Float> _alpha = new GOpt<>("alpha", __ -> 1.0f);
    private static final GOpt<Integer> _bins = new GOpt<>("bins", __ -> 30);
    private static final GOpt<Boolean> _prob = new GOpt<>("prob", __ -> false);
    private static final GOpt<Boolean> _stacked = new GOpt<>("stacked", __ -> false);
    private static final GOpt<Integer> _points = new GOpt<>("points", __ -> 256);
    private static final GOpt<Integer> _top = new GOpt<>("top", __ -> Integer.MAX_VALUE);
    private static final GOpt<Integer> _sort = new GOpt<>("sort", __ -> 0);
    private static final GOpt<Boolean> _horizontal = new GOpt<>("horizontal", __ -> false);
    private static final GOpt<Sizes> _widths = new GOpt<>("widths", __ -> new Sizes(false, new double[] {-1}, null));
    private static final GOpt<Sizes> _heights = new GOpt<>("heights", __ -> new Sizes(false, new double[] {-1}, null));
    private static final GOpt<String[]> _labels = new GOpt<>("labels", __ -> new String[] {""});
    private static final GOpt<Integer> _hAlign = new GOpt<>("hAling", __ -> HALIGN_LEFT);
    private static final GOpt<Integer> _vAlign = new GOpt<>("vAlign", __ -> VALIGN_TOP);
    private static final GOpt<Font> _font = new GOpt<>("font", __ -> new Font("DejaVu Sans", Font.PLAIN, 20));
    private static final GOpt<Rectangle2D> _position = new GOpt<>("position", __ -> new Rectangle2D.Double(0, 0, 1, 1));

    // getters

    public interface palette {
        static GOpt<Palette> hue() {
            return new GOpt<>(_palette, s -> Palette.hue());
        }

        static GOpt<Palette> hue(double start, double end) {
            return new GOpt<>(_palette, s -> Palette.hue(start, end));
        }

        static GOpt<Palette> hue(double start, double end, double rangeFrom, double rangeTo) {
            return new GOpt<>(_palette, s -> Palette.hue(start, end, rangeFrom, rangeTo));
        }

        static GOpt<Palette> hue(double start, double end, Grid2D gridData) {
            return new GOpt<>(_palette, s -> Palette.hue(start, end, gridData.minValue(), gridData.maxValue()));
        }

        static GOpt<Palette> redRGB() {
            return new GOpt<>(_palette, s -> Palette.RedRGB());
        }

        static GOpt<Palette> greenRGB() {
            return new GOpt<>(_palette, s -> Palette.GreenRGB());
        }

        static GOpt<Palette> blueRGB() {
            return new GOpt<>(_palette, s -> Palette.BlueRGB());
        }

        static GOpt<Palette> bw(double start, double end) {
            return new GOpt<>(_palette, s -> Palette.gray(start, end));
        }
    }

    public static GOpt<Palette> palette(Palette colorPalette) {
        return new GOpt<>(_palette, s -> colorPalette);
    }

    public static GOpt<Color[]> color(int... index) {
        SFunction<GOpts, Color[]> fun = (index.length == 1 && index[0] == -1)
                ? s -> null
                : s -> Arrays.stream(index).boxed().map(i -> s.getPalette().getColor(i)).toArray(Color[]::new);
        return new GOpt<>(_color, fun);
    }

    public static GOpt<Color[]> color(Color... colors) {
        return new GOpt<>(_color, s -> colors);
    }

    public static GOpt<Color[]> color(Var color) {
        return new GOpt<>(_color, s -> {
            Color[] colors = new Color[color.size()];
            for (int i = 0; i < colors.length; i++) {
                colors[i] = s.getPalette().getColor(color.getDouble(i));
            }
            return colors;
        });
    }

    public static GOpt<Color[]> fill(int... index) {
        SFunction<GOpts, Color[]> fun = (index.length == 1 && index[0] == -1)
                ? s -> null
                : s -> Arrays.stream(index).boxed().map(i -> s.getPalette().getColor(i)).toArray(Color[]::new);
        return new GOpt<>(_fill, fun);
    }

    public static GOpt<Color[]> fill(Color... colors) {
        return new GOpt<>(_fill, s -> colors);
    }

    public static GOpt<Color[]> fill(Var color) {
        return new GOpt<>(_fill, s -> {
            Color[] colors = new Color[color.size()];
            for (int i = 0; i < colors.length; i++) {
                colors[i] = s.getPalette().getColor(color.getDouble(i));
            }
            return colors;
        });
    }

    public static GOpt<Float> lwd(float lwd) {
        return new GOpt<>(_lwd, __ -> lwd);
    }

    public static GOpt<Var> sz(Var sizeIndex) {
        return sz(sizeIndex, 1);
    }

    public static GOpt<Var> sz(Var sizeIndex, double factor) {
        return sz(sizeIndex, factor, 0);
    }

    public static GOpt<Var> sz(Var sizeIndex, double factor, double offset) {
        return new GOpt<>(_sz,
                __ -> sizeIndex.stream().mapToDouble().map(x -> x * factor + offset).boxed().collect(VarDouble.collector()));
    }

    public static GOpt<Var> sz(double size) {
        return new GOpt<>(_sz, __ -> VarDouble.scalar(size));
    }

    public static GOpt<VarInt> pch(Var pchIndex, int... mapping) {
        return new GOpt<>(_pch, __ -> VarInt.from(pchIndex.size(), row -> {
            int i = pchIndex.getInt(row);
            if (i >= 0 && i < mapping.length) {
                return mapping[i];
            }
            return mapping != null && mapping.length > 0 ? mapping[0] : i;
        }));
    }

    public interface pch {
        static GOpt<VarInt> circleWire() {
            return new GOpt<>(_pch, __ -> VarInt.scalar(PCH_CIRCLE_WIRE));
        }

        static GOpt<VarInt> circleFill() {
            return new GOpt<>(_pch, __ -> VarInt.scalar(PCH_CIRCLE_FILL));
        }

        static GOpt<VarInt> circleFull() {
            return new GOpt<>(_pch, __ -> VarInt.scalar(PCH_CIRCLE_FULL));
        }

        static GOpt<VarInt> crossWire() {
            return new GOpt<>(_pch, __ -> VarInt.scalar(PCH_CROSS_WIRE));
        }

        static GOpt<VarInt> triangleWire() {
            return new GOpt<>(_pch, __ -> VarInt.scalar(PCH_TRIANGLE_WIRE));
        }

        static GOpt<VarInt> triangleFill() {
            return new GOpt<>(_pch, __ -> VarInt.scalar(PCH_TRIANGLE_FILL));
        }

        static GOpt<VarInt> triangleFull() {
            return new GOpt<>(_pch, __ -> VarInt.scalar(PCH_TRIANGLE_FULL));
        }

        static GOpt<VarInt> squareWire() {
            return new GOpt<>(_pch, __ -> VarInt.scalar(PCH_SQUARE_WIRE));
        }

        static GOpt<VarInt> squareFill() {
            return new GOpt<>(_pch, __ -> VarInt.scalar(PCH_SQUARE_FILL));
        }

        static GOpt<VarInt> squareFull() {
            return new GOpt<>(_pch, __ -> VarInt.scalar(PCH_SQUARE_FULL));
        }
    }

    public static GOpt<Float> alpha(float alpha) {
        return new GOpt<>(_alpha, __ -> alpha);
    }

    public static GOpt<Integer> bins(int bins) {
        return new GOpt<>(_bins, __ -> bins);
    }

    public static GOpt<Boolean> prob(boolean prob) {
        return new GOpt<>(_prob, __ -> prob);
    }

    public static GOpt<Boolean> stacked(boolean stacked) {
        return new GOpt<>(_stacked, __ -> stacked);
    }

    public static GOpt<Integer> points(int points) {
        return new GOpt<>(_points, __ -> points);
    }

    public static GOpt<Integer> top(int top) {
        return new GOpt<>(_top, __ -> top);
    }

    public interface sort {
        static GOpt<Integer> asc() {
            return new GOpt<>(_sort, __ -> SORT_ASC);
        }

        static GOpt<Integer> none() {
            return new GOpt<>(_sort, __ -> SORT_NONE);
        }

        static GOpt<Integer> desc() {
            return new GOpt<>(_sort, __ -> SORT_DESC);
        }
    }

    public static GOpt<Boolean> horizontal(boolean horizontal) {
        return new GOpt<>(_horizontal, __ -> horizontal);
    }

    public static GOpt<String[]> labels(String... labels) {
        return new GOpt<>(_labels, __ -> labels);
    }

    public static GOpt<Sizes> widths(double... relativeSizes) {
        return new GOpt<>(_widths, __ -> new Sizes(false, relativeSizes, null));
    }

    public static GOpt<Sizes> widths(int... absoluteSizes) {
        return new GOpt<>(_widths, __ -> new Sizes(true, null, absoluteSizes));
    }

    public static GOpt<Sizes> heights(double... relativeSizes) {
        return new GOpt<>(_heights, __ -> new Sizes(false, relativeSizes, null));
    }

    public static GOpt<Sizes> heights(int... absoluteSizes) {
        return new GOpt<>(_heights, __ -> new Sizes(true, null, absoluteSizes));
    }

    public interface halign {
        static GOpt<Integer> left() {
            return new GOpt<>(_hAlign, __ -> HALIGN_LEFT);
        }

        static GOpt<Integer> center() {
            return new GOpt<>(_hAlign, __ -> HALIGN_CENTER);
        }

        static GOpt<Integer> right() {
            return new GOpt<>(_hAlign, __ -> HALIGN_RIGHT);
        }
    }

    public interface valign {
        static GOpt<Integer> top() {
            return new GOpt<>(_vAlign, __ -> VALIGN_TOP);
        }

        static GOpt<Integer> center() {
            return new GOpt<>(_vAlign, __ -> VALIGN_CENTER);
        }

        static GOpt<Integer> bottom() {
            return new GOpt<>(_vAlign, __ -> VALIGN_BOTTOM);
        }
    }

    public static GOpt<Font> font(Font font) {
        return new GOpt<>(_font, __ -> font);
    }

    public static GOpt<Font> font(String fontName) {
        return new GOpt<>(_font, __ -> new Font(fontName, Font.PLAIN, 20));
    }

    public static GOpt<Font> font(String fontName, int style, int size) {
        return new GOpt<>(_font, __ -> new Font(fontName, style, size));
    }

    public static GOpt<Rectangle2D> position(Rectangle2D position) {
        return new GOpt<>(_position, __ -> position);
    }

    public static GOpt<Rectangle2D> position(double x, double y, double width, double height) {
        return position(new Rectangle2D.Double(x, y, width, height));
    }


    // constructors

    public GOpts() {
        this(null);
    }

    protected GOpts(GOpts parent) {
        super(parent);
    }

    @Override
    public GOpts bind(GOpt<?>... parameters) {
        return new GOpts(this).apply(parameters);
    }

    // getters

    public Palette getPalette() {
        return (Palette) getParamValue(_palette);
    }

    public Color getColor(int row) {
        Color[] value = (Color[]) getParamValue(_color);
        return value != null ? value[row % value.length] : null;
    }

    public Color getFill(int row) {
        Color[] value = (Color[]) getParamValue(_fill);
        return value != null ? value[row % value.length] : null;
    }

    public Float getLwd() {
        return (Float) getParamValue(_lwd);
    }

    public double getSz(int row) {
        Var sz = (Var) getParamValue(_sz);
        return sz.getDouble(row % sz.size());
    }

    public int getPch(int row) {
        var pch = (VarInt) getParamValue(_pch);
        return pch.getInt(row % pch.size());
    }

    public Float getAlpha() {
        return (Float) getParamValue(_alpha);
    }

    public Integer getBins() {
        return (Integer) getParamValue(_bins);
    }

    public Boolean getProb() {
        return (Boolean) getParamValue(_prob);
    }

    public Boolean getStacked() {
        return (Boolean) getParamValue(_stacked);
    }

    public Integer getPoints() {
        return (Integer) getParamValue(_points);
    }

    public Integer getTop() {
        return (Integer) getParamValue(_top);
    }

    public Integer getSort() {
        return (Integer) getParamValue(_sort);
    }

    public Boolean getHorizontal() {
        return (Boolean) getParamValue(_horizontal);
    }

    public String[] getLabels() {
        return (String[]) getParamValue(_labels);
    }

    public Sizes getWidths() {
        return (Sizes) getParamValue(_widths);
    }

    public Sizes getHeights() {
        return (Sizes) getParamValue(_heights);
    }

    public Integer getHAlign() {
        return (Integer) getParamValue(_hAlign);
    }

    public Integer getVAlign() {
        return (Integer) getParamValue(_vAlign);
    }

    public Font getFont() {
        return (Font) getParamValue(_font);
    }

    public Rectangle2D getPosition() {
        return (Rectangle2D) getParamValue(_position);
    }
}
