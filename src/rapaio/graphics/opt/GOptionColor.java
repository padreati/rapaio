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

import rapaio.data.Var;
import rapaio.util.function.SFunction;

import java.awt.*;
import java.io.Serial;
import java.util.Arrays;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/14/17.
 */
public class GOptionColor implements GOption<Color[]> {

    @Serial
    private static final long serialVersionUID = 8113363057332129656L;
    private final SFunction<GOptions, Color[]> function;

    public GOptionColor(String... names) {
        function = gOpts -> Arrays.stream(names).map(NamedColors.getInstance()::getColor).toArray(Color[]::new);
    }

    public GOptionColor(char... names) {
        String[] strNames = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            strNames[i] = String.valueOf(names[i]);
        }
        function = gOpts -> Arrays.stream(strNames).map(NamedColors.getInstance()::getColor).toArray(Color[]::new);
    }

    public GOptionColor(int... index) {
        if (index.length == 1 && index[0] == -1) {
            function = gOpts -> null;
            return;
        }
        function = gOpts -> Arrays.stream(index).boxed().map(i -> gOpts.getPalette().getColor(i)).toArray(Color[]::new);
    }

    public GOptionColor(Color color) {
        function = gOpts -> new Color[]{color};
    }

    public GOptionColor(Color[] colors) {
        function = gOpts -> colors;
    }

    public GOptionColor(Var color) {
        function = opt -> {
            Color[] colors = new Color[color.size()];
            for (int i = 0; i < colors.length; i++) {
                colors[i] = opt.getPalette().getColor(color.getInt(i));
            }
            return colors;
        };
    }

    @Override
    public void bind(GOptions opts) {
        opts.setColor(this);
    }

    @Override
    public Color[] apply(GOptions opts) {
        return function.apply(opts);
    }
}
