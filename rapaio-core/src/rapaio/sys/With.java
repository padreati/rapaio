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

package rapaio.sys;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

import rapaio.core.tools.Grid2D;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.graphics.opt.GOptionAlpha;
import rapaio.graphics.opt.GOptionBins;
import rapaio.graphics.opt.GOptionColor;
import rapaio.graphics.opt.GOptionFill;
import rapaio.graphics.opt.GOptionFont;
import rapaio.graphics.opt.GOptionHAlign;
import rapaio.graphics.opt.GOptionHeights;
import rapaio.graphics.opt.GOptionHorizontal;
import rapaio.graphics.opt.GOptionLabels;
import rapaio.graphics.opt.GOptionLwd;
import rapaio.graphics.opt.GOptionPalette;
import rapaio.graphics.opt.GOptionPch;
import rapaio.graphics.opt.GOptionPoints;
import rapaio.graphics.opt.GOptionPosition;
import rapaio.graphics.opt.GOptionProb;
import rapaio.graphics.opt.GOptionSort;
import rapaio.graphics.opt.GOptionStacked;
import rapaio.graphics.opt.GOptionSz;
import rapaio.graphics.opt.GOptionTop;
import rapaio.graphics.opt.GOptionVAlign;
import rapaio.graphics.opt.GOptionWidths;
import rapaio.graphics.opt.Palette;
import rapaio.printer.opt.POptionTextWidth;
import rapaio.printer.opt.POtpionFloatFormat;

/**
 * Utility class which provides shortcuts to various named options.
 */
public final class With {

    public static POptionTextWidth textWidth(int textWidth) {
        return new POptionTextWidth(textWidth);
    }

    public static POtpionFloatFormat floatFormat(DecimalFormat format) {
        return new POtpionFloatFormat(format);
    }

    // Graphical options

    public interface palette {
        static GOptionPalette hue() {
            return new GOptionPalette(Palette.hue());
        }

        static GOptionPalette hue(double start, double end) {
            return new GOptionPalette(Palette.hue(start, end));
        }

        static GOptionPalette hue(double start, double end, double rangeFrom, double rangeTo) {
            return new GOptionPalette(Palette.hue(start, end, rangeFrom, rangeTo));
        }

        static GOptionPalette hue(double start, double end, Grid2D gridData) {
            return new GOptionPalette(Palette.hue(start, end, gridData.minValue(), gridData.maxValue()));
        }
    }

    public static GOptionPalette palette(Palette colorPalette) {
        return new GOptionPalette(colorPalette);
    }

    public static GOptionColor color(int... index) {
        return new GOptionColor(index);
    }

    public static GOptionColor color(Color... colors) {
        return new GOptionColor(colors);
    }

    public static GOptionColor color(Var color) {
        return new GOptionColor(color);
    }

    public static GOptionFill fill(int... index) {
        return new GOptionFill(index);
    }

    public static GOptionFill fill(Color... colors) {
        return new GOptionFill(colors);
    }

    public static GOptionFill fill(Var color) {
        return new GOptionFill(color);
    }

    public static GOptionLwd lwd(float lwd) {
        return new GOptionLwd(lwd);
    }

    public static GOptionSz sz(Var sizeIndex) {
        return sz(sizeIndex, 1);
    }

    public static GOptionSz sz(Var sizeIndex, double factor) {
        return sz(sizeIndex, factor, 0);
    }

    public static GOptionSz sz(Var sizeIndex, double factor, double offset) {
        VarDouble size = sizeIndex
                .stream()
                .mapToDouble()
                .map(x -> x * factor + offset)
                .boxed()
                .collect(VarDouble.collector());
        return new GOptionSz(size);
    }

    public static GOptionSz sz(double size) {
        return new GOptionSz(VarDouble.scalar(size));
    }

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

    public static GOptionPch pch(Var pchIndex, int... mapping) {
        VarInt pch = VarInt.from(pchIndex.size(), row -> {
            int i = pchIndex.getInt(row);
            if (i >= 0 && i < mapping.length) {
                return mapping[i];
            }
            return mapping[0];
        });
        return new GOptionPch(pch);
    }

    public interface pch {
        static GOptionPch circleWire() {
            return new GOptionPch(VarInt.scalar(PCH_CIRCLE_WIRE));
        }

        static GOptionPch circleFill() {
            return new GOptionPch(VarInt.scalar(PCH_CIRCLE_FILL));
        }

        static GOptionPch circleFull() {
            return new GOptionPch(VarInt.scalar(PCH_CIRCLE_FULL));
        }

        static GOptionPch crossWire() {
            return new GOptionPch(VarInt.scalar(PCH_CROSS_WIRE));
        }

        static GOptionPch triangleWire() {
            return new GOptionPch(VarInt.scalar(PCH_TRIANGLE_WIRE));
        }

        static GOptionPch triangleFill() {
            return new GOptionPch(VarInt.scalar(PCH_TRIANGLE_FILL));
        }

        static GOptionPch triangleFull() {
            return new GOptionPch(VarInt.scalar(PCH_TRIANGLE_FULL));
        }

        static GOptionPch squareWire() {
            return new GOptionPch(VarInt.scalar(PCH_SQUARE_WIRE));
        }

        static GOptionPch squareFill() {
            return new GOptionPch(VarInt.scalar(PCH_SQUARE_FILL));
        }

        static GOptionPch squareFull() {
            return new GOptionPch(VarInt.scalar(PCH_SQUARE_FULL));
        }
    }

    public static GOptionPch pch(int pch) {
        return new GOptionPch(VarInt.scalar(pch));
    }

    public static GOptionAlpha alpha(float alpha) {
        return new GOptionAlpha(alpha);
    }

    public static GOptionBins bins(int bins) {
        return new GOptionBins(bins);
    }

    public static GOptionProb prob(boolean prob) {
        return new GOptionProb(prob);
    }

    public static GOptionStacked stacked(boolean stacked) {
        return new GOptionStacked(stacked);
    }

    public static GOptionPoints points(int points) {
        return new GOptionPoints(points);
    }

    public static GOptionTop top(int top) {
        return new GOptionTop(top);
    }

    public static final int SORT_DESC = -1;
    public static final int SORT_NONE = 0;
    public static final int SORT_ASC = 1;

    public static GOptionSort sort(int sort) {
        return new GOptionSort(sort);
    }

    public interface sort {
        static GOptionSort asc() {
            return new GOptionSort(SORT_ASC);
        }

        static GOptionSort none() {
            return new GOptionSort(SORT_NONE);
        }

        static GOptionSort desc() {
            return new GOptionSort(SORT_DESC);
        }
    }

    public static GOptionHorizontal horizontal(boolean horizontal) {
        return new GOptionHorizontal(horizontal);
    }

    public static GOptionLabels labels(String... labels) {
        return new GOptionLabels(labels);
    }

    public static GOptionWidths widths(double... relativeSizes) {
        return new GOptionWidths(relativeSizes);
    }

    public static GOptionWidths widths(int... absoluteSizes) {
        return new GOptionWidths(absoluteSizes);
    }

    public static GOptionHeights heights(double... relativeSizes) {
        return new GOptionHeights(relativeSizes);
    }

    public static GOptionHeights heights(int... absoluteSizes) {
        return new GOptionHeights(absoluteSizes);
    }

    public static final int HALIGN_LEFT = -1;
    public static final int HALIGN_CENTER = 0;
    public static final int HALIGN_RIGHT = 1;

    public static GOptionHAlign halign(int hAlign) {
        return new GOptionHAlign(hAlign);
    }

    public interface halign {
        static GOptionHAlign left() {
            return new GOptionHAlign(HALIGN_LEFT);
        }

        static GOptionHAlign center() {
            return new GOptionHAlign(HALIGN_CENTER);
        }

        static GOptionHAlign right() {
            return new GOptionHAlign(HALIGN_RIGHT);
        }
    }

    public static final int VALIGN_TOP = -1;
    public static final int VALIGN_CENTER = 0;
    public static final int VALIGN_BOTTOM = 1;

    public static GOptionVAlign valign(int vAlign) {
        return new GOptionVAlign(vAlign);
    }

    public interface valign {
        static GOptionVAlign top() {
            return new GOptionVAlign(VALIGN_TOP);
        }
        static GOptionVAlign center() {
            return new GOptionVAlign(VALIGN_CENTER);
        }
        static GOptionVAlign bottom() {
            return new GOptionVAlign(VALIGN_BOTTOM);
        }
    }

    public static GOptionFont font(Font font) {
        return new GOptionFont(font);
    }

    public static GOptionFont font(String fontName) {
        return new GOptionFont(fontName);
    }

    public static GOptionFont font(String fontName, int style, int size) {
        return new GOptionFont(new Font(fontName, style, size));
    }

    public static GOptionPosition position(Rectangle2D position) {
        return new GOptionPosition(position);
    }

    public static GOptionPosition position(double x, double y, double width, double height) {
        return position(new Rectangle2D.Double(x, y, width, height));
    }
}
