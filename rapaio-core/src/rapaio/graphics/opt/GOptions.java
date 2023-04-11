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
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import rapaio.core.tools.Grid2D;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.util.function.SFunction;
import rapaio.util.nparam.NamedParamSet;

public class GOptions extends NamedParamSet<GOptions, GOption<?>> {

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


    private static final GOption<Palette> _palette = new GOption<>("palette", __ -> Palette.tableau21());
    private static final GOption<Color[]> _color = new GOption<>("color", __ -> new Color[] {Color.BLACK});
    private static final GOption<Color[]> _fill = new GOption<>("fill", __ -> null);
    private static final GOption<Float> _lwd = new GOption<>("lwd", __ -> 1.0f);
    private static final GOption<Var> _sz = new GOption<>("sz", __ -> VarDouble.scalar(3));
    private static final GOption<VarInt> _pch = new GOption<>("pch", __ -> VarInt.scalar(0));
    private static final GOption<Float> _alpha = new GOption<>("alpha", __ -> 1.0f);
    private static final GOption<Integer> _bins = new GOption<>("bins", __ -> 30);
    private static final GOption<Boolean> _prob = new GOption<>("prob", __ -> false);
    private static final GOption<Boolean> _stacked = new GOption<>("stacked", __ -> false);
    private static final GOption<Integer> _points = new GOption<>("points", __ -> 256);
    private static final GOption<Integer> _top = new GOption<>("top", __ -> Integer.MAX_VALUE);
    private static final GOption<Integer> _sort = new GOption<>("sort", __ -> 0);
    private static final GOption<Boolean> _horizontal = new GOption<>("horizontal", __ -> false);
    private static final GOption<Sizes> _widths = new GOption<>("widths", __ -> new Sizes(false, new double[] {-1}, null));
    private static final GOption<Sizes> _heights = new GOption<>("heights", __ -> new Sizes(false, new double[] {-1}, null));
    private static final GOption<String[]> _labels = new GOption<>("labels", __ -> new String[] {""});
    private static final GOption<Integer> _hAlign = new GOption<>("hAling", __ -> HALIGN_LEFT);
    private static final GOption<Integer> _vAlign = new GOption<>("vAlign", __ -> VALIGN_TOP);
    private static final GOption<Font> _font = new GOption<>("font", __ -> new Font("DejaVu Sans", Font.PLAIN, 20));
    private static final GOption<Rectangle2D> _position = new GOption<>("position", __ -> new Rectangle2D.Double(0, 0, 1, 1));

    // getters

    public interface palette {
        static GOption<Palette> hue() {
            return new GOption<>(_palette, s -> Palette.hue());
        }

        static GOption<Palette> hue(double start, double end) {
            return new GOption<>(_palette, s -> Palette.hue(start, end));
        }

        static GOption<Palette> hue(double start, double end, double rangeFrom, double rangeTo) {
            return new GOption<>(_palette, s -> Palette.hue(start, end, rangeFrom, rangeTo));
        }

        static GOption<Palette> hue(double start, double end, Grid2D gridData) {
            return new GOption<>(_palette, s -> Palette.hue(start, end, gridData.minValue(), gridData.maxValue()));
        }
    }

    public static GOption<Palette> palette(Palette colorPalette) {
        return new GOption<>(_palette, s -> colorPalette);
    }

    public static GOption<Color[]> color(int... index) {
        SFunction<GOptions, Color[]> fun = (index.length == 1 && index[0] == -1)
                ? s -> null
                : s -> Arrays.stream(index).boxed().map(i -> s.getPalette().getColor(i)).toArray(Color[]::new);
        return new GOption<>(_color, fun);
    }

    public static GOption<Color[]> color(Color... colors) {
        return new GOption<>(_color, s -> colors);
    }

    public static GOption<Color[]> color(Var color) {
        return new GOption<>(_color, s -> {
            Color[] colors = new Color[color.size()];
            for (int i = 0; i < colors.length; i++) {
                colors[i] = s.getPalette().getColor(color.getDouble(i));
            }
            return colors;
        });
    }

    public static GOption<Color[]> fill(int... index) {
        SFunction<GOptions, Color[]> fun = (index.length == 1 && index[0] == -1)
                ? s -> null
                : s -> Arrays.stream(index).boxed().map(i -> s.getPalette().getColor(i)).toArray(Color[]::new);
        return new GOption<>(_fill, fun);
    }

    public static GOption<Color[]> fill(Color... colors) {
        return new GOption<>(_fill, s -> colors);
    }

    public static GOption<Color[]> fill(Var color) {
        return new GOption<>(_fill, s -> {
            Color[] colors = new Color[color.size()];
            for (int i = 0; i < colors.length; i++) {
                colors[i] = s.getPalette().getColor(color.getDouble(i));
            }
            return colors;
        });
    }

    public static GOption<Float> lwd(float lwd) {
        return new GOption<>(_lwd, __ -> lwd);
    }

    public static GOption<Var> sz(Var sizeIndex) {
        return sz(sizeIndex, 1);
    }

    public static GOption<Var> sz(Var sizeIndex, double factor) {
        return sz(sizeIndex, factor, 0);
    }

    public static GOption<Var> sz(Var sizeIndex, double factor, double offset) {
        return new GOption<>(_sz,
                __ -> sizeIndex.stream().mapToDouble().map(x -> x * factor + offset).boxed().collect(VarDouble.collector()));
    }

    public static GOption<Var> sz(double size) {
        return new GOption<>(_sz, __ -> VarDouble.scalar(size));
    }

    public static GOption<VarInt> pch(Var pchIndex, int... mapping) {
        return new GOption<>(_pch, __ -> VarInt.from(pchIndex.size(), row -> {
            int i = pchIndex.getInt(row);
            if (i >= 0 && i < mapping.length) {
                return mapping[i];
            }
            return mapping != null && mapping.length>0 ? mapping[0] : i;
        }));
    }

    public interface pch {
        static GOption<VarInt> circleWire() {
            return new GOption<>(_pch, __ -> VarInt.scalar(PCH_CIRCLE_WIRE));
        }

        static GOption<VarInt> circleFill() {
            return new GOption<>(_pch, __ -> VarInt.scalar(PCH_CIRCLE_FILL));
        }

        static GOption<VarInt> circleFull() {
            return new GOption<>(_pch, __ -> VarInt.scalar(PCH_CIRCLE_FULL));
        }

        static GOption<VarInt> crossWire() {
            return new GOption<>(_pch, __ -> VarInt.scalar(PCH_CROSS_WIRE));
        }

        static GOption<VarInt> triangleWire() {
            return new GOption<>(_pch, __ -> VarInt.scalar(PCH_TRIANGLE_WIRE));
        }

        static GOption<VarInt> triangleFill() {
            return new GOption<>(_pch, __ -> VarInt.scalar(PCH_TRIANGLE_FILL));
        }

        static GOption<VarInt> triangleFull() {
            return new GOption<>(_pch, __ -> VarInt.scalar(PCH_TRIANGLE_FULL));
        }

        static GOption<VarInt> squareWire() {
            return new GOption<>(_pch, __ -> VarInt.scalar(PCH_SQUARE_WIRE));
        }

        static GOption<VarInt> squareFill() {
            return new GOption<>(_pch, __ -> VarInt.scalar(PCH_SQUARE_FILL));
        }

        static GOption<VarInt> squareFull() {
            return new GOption<>(_pch, __ -> VarInt.scalar(PCH_SQUARE_FULL));
        }
    }

    public static GOption<VarInt> pch(int pch) {
        return new GOption<>(_pch, __ -> VarInt.scalar(pch));
    }

    public static GOption<Float> alpha(float alpha) {
        return new GOption<>(_alpha, __ -> alpha);
    }

    public static GOption<Integer> bins(int bins) {
        return new GOption<>(_bins, __ -> bins);
    }

    public static GOption<Boolean> prob(boolean prob) {
        return new GOption<>(_prob, __ -> prob);
    }

    public static GOption<Boolean> stacked(boolean stacked) {
        return new GOption<>(_stacked, __ -> stacked);
    }

    public static GOption<Integer> points(int points) {
        return new GOption<>(_points, __ -> points);
    }

    public static GOption<Integer> top(int top) {
        return new GOption<>(_top, __ -> top);
    }

    public static GOption<Integer> sort(int sort) {
        return new GOption<>(_sort, __ -> sort);
    }

    public interface sort {
        static GOption<Integer> asc() {
            return new GOption<>(_sort, __ -> SORT_ASC);
        }

        static GOption<Integer> none() {
            return new GOption<>(_sort, __ -> SORT_NONE);
        }

        static GOption<Integer> desc() {
            return new GOption<>(_sort, __ -> SORT_DESC);
        }
    }

    public static GOption<Boolean> horizontal(boolean horizontal) {
        return new GOption<>(_horizontal, __ -> horizontal);
    }

    public static GOption<String[]> labels(String... labels) {
        return new GOption<>(_labels, __ -> labels);
    }

    public static GOption<Sizes> widths(double... relativeSizes) {
        return new GOption<>(_widths, __ -> new Sizes(false, relativeSizes, null));
    }

    public static GOption<Sizes> widths(int... absoluteSizes) {
        return new GOption<>(_widths, __ -> new Sizes(true, null, absoluteSizes));
    }

    public static GOption<Sizes> heights(double... relativeSizes) {
        return new GOption<>(_heights, __ -> new Sizes(false, relativeSizes, null));
    }

    public static GOption<Sizes> heights(int... absoluteSizes) {
        return new GOption<>(_heights, __ -> new Sizes(true, null, absoluteSizes));
    }

    public static GOption<Integer> halign(int hAlign) {
        return new GOption<>(_hAlign, __ -> hAlign);
    }

    public interface halign {
        static GOption<Integer> left() {
            return new GOption<>(_hAlign, __ -> HALIGN_LEFT);
        }

        static GOption<Integer> center() {
            return new GOption<>(_hAlign, __ -> HALIGN_CENTER);
        }

        static GOption<Integer> right() {
            return new GOption<>(_hAlign, __ -> HALIGN_RIGHT);
        }
    }

    public static GOption<Integer> valign(int vAlign) {
        return new GOption<>(_vAlign, __ -> vAlign);
    }

    public interface valign {
        static GOption<Integer> top() {
            return new GOption<>(_vAlign, __ -> VALIGN_TOP);
        }

        static GOption<Integer> center() {
            return new GOption<>(_vAlign, __ -> VALIGN_CENTER);
        }

        static GOption<Integer> bottom() {
            return new GOption<>(_vAlign, __ -> VALIGN_BOTTOM);
        }
    }

    public static GOption<Font> font(Font font) {
        return new GOption<>(_font, __ -> font);
    }

    public static GOption<Font> font(String fontName) {
        return new GOption<>(_font, __ -> new Font(fontName, Font.PLAIN, 20));
    }

    public static GOption<Font> font(String fontName, int style, int size) {
        return new GOption<>(_font, __ -> new Font(fontName, style, size));
    }

    public static GOption<Rectangle2D> position(Rectangle2D position) {
        return new GOption<>(_position, __ -> position);
    }

    public static GOption<Rectangle2D> position(double x, double y, double width, double height) {
        return position(new Rectangle2D.Double(x, y, width, height));
    }


    // constructors

    public GOptions() {
        this(null);
    }

    protected GOptions(GOptions parent) {
        super(parent);
    }

    @Override
    public GOptions bind(GOption<?>... parameters) {
        return new GOptions(this).apply(parameters);
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
