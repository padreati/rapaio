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

package rapaio.printer.opt;

import java.io.Serializable;
import java.text.DecimalFormat;

import rapaio.printer.Format;
import rapaio.printer.nparam.NamedParamSet;

/**
 * Printing options.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/25/20.
 */
public class POpts extends NamedParamSet<POpts, POpt<?>> implements Serializable {

    // parameter declarations with default values

    private static final POpt<Integer> pTextWidth = new POpt<>("textWidth", __ -> 120);
    private static final POpt<DecimalFormat> pFloatFormat = new POpt<>("floatFormat", __ -> Format.floatFlexLong());
    private static final POpt<Integer> pGraphicWidth = new POpt<>("graphicWidth", __ -> 1200);
    private static final POpt<Integer> pGraphicHeight = new POpt<>("graphicHeight", __ -> 600);

    // named parameters methods

    public static POpt<Integer> textWidth(int textWidth) {
        return new POpt<>(pTextWidth, __ -> textWidth);
    }

    public static POpt<DecimalFormat> floatFormat(DecimalFormat format) {
        return new POpt<>(pFloatFormat, __ -> format);
    }

    public static POpt<Integer> graphicWidth(int width) {
        return new POpt<>(pGraphicWidth, __ -> width);
    }

    public static POpt<Integer> graphicHeight(int height) {
        return new POpt<>(pGraphicHeight, __ -> height);
    }

    // constructor

    public POpts() {
        super(null);
    }

    private POpts(POpts parent) {
        super(parent);
    }

    public POpts bind(POpt<?>... options) {
        return new POpts(this).apply(options);
    }

    public Integer getTextWidth() {
        return (Integer) getParamValue(pTextWidth);
    }

    public DecimalFormat getFloatFormat() {
        return (DecimalFormat) getParamValue(pFloatFormat);
    }

    public Integer getGraphicWidth() {
        return (Integer) getParamValue(pGraphicWidth);
    }

    public Integer getGraphicHeight() {
        return (Integer) getParamValue(pGraphicHeight);
    }
}
