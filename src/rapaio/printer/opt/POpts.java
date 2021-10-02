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

package rapaio.printer.opt;

import java.io.Serial;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;

import rapaio.printer.Format;

/**
 * Printing options.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/25/20.
 */
public class POpts implements Serializable {

    @Serial
    private static final long serialVersionUID = -2369999674228369814L;

    public static final POpts defaults;

    static {
        defaults = new POpts(null);
        defaults.setTextWidth(new POptionTextWidth(120));
        defaults.setFloatFormat(new POtpionFloatFormat(Format.formatDecFlex));
    }

    public POpts(POpts parent) {
        this.parent = parent;
    }

    public POpts() {
        this(defaults);
    }

    private final POpts parent;
    private POptionTextWidth textWidth;
    private POtpionFloatFormat floatFormat;

    public POpts getParent() {
        return parent;
    }

    public POpts bind(POption<?>... options) {
        Arrays.stream(options).forEach(o -> o.bind(this));
        return this;
    }

    public POption<?>[] toArray() {
        return new POption[]{
                textWidth, floatFormat
        };
    }

    public int textWidth() {
        if (textWidth == null) {
            return parent != null ? parent.textWidth() : defaults.textWidth.apply(this);
        }
        return textWidth.apply(this);
    }

    public void setTextWidth(POptionTextWidth textWidth) {
        this.textWidth = textWidth;
    }

    public DecimalFormat floatFormat() {
        if (floatFormat == null) {
            return parent != null ? parent.floatFormat() : defaults.floatFormat.apply(this);
        }
        return floatFormat.apply(this);
    }

    public void setFloatFormat(POtpionFloatFormat floatFormat) {
        this.floatFormat = floatFormat;
    }
}
