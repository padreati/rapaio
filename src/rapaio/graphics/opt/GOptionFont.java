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

import java.awt.Font;
import java.io.Serial;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/14/17.
 */
public class GOptionFont implements GOption<Font> {

    @Serial
    private static final long serialVersionUID = 7534853593877383832L;
    private final Font font;

    public GOptionFont(Font font) {
        this.font = font;
    }

    public GOptionFont(String fontName) {
        this.font = new Font(fontName, Font.PLAIN, 20);
    }

    public GOptionFont(String fontName, int style, int size) {
        this.font = new Font(fontName, style, size);
    }

    @Override
    public void bind(GOptions opts) {
        opts.setFont(this);
    }

    @Override
    public Font apply(GOptions opts) {
        return font;
    }
}
