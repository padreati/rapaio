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

import rapaio.data.*;
import rapaio.util.serializable.*;

import java.awt.*;
import java.util.Arrays;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/14/17.
 */
public class GOptionColor implements GOption<Color[]> {

    private static final long serialVersionUID = 7534853593877383832L;
    private final SFunction<GOpts, Color[]> function;

    public GOptionColor(int... index) {
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
            Color[] colors = new Color[color.rowCount()];
            for (int i = 0; i < colors.length; i++) {
                colors[i] = opt.getPalette().getColor(color.getInt(i));
            }
            return colors;
        };
    }

    @Override
    public void bind(GOpts opts) {
        opts.setColor(this);
    }

    @Override
    public Color[] apply(GOpts opts) {
        return function.apply(opts);
    }
}
