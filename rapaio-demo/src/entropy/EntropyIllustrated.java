/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package entropy;

import static rapaio.graphics.Plotter.*;
import static rapaio.math.MathTools.*;
import static rapaio.sys.With.*;

import java.util.function.BiFunction;

import rapaio.core.tools.Grid2D;
import rapaio.data.VarDouble;
import rapaio.graphics.plot.GridLayer;
import rapaio.sys.WS;
import rapaio.util.function.Double2DoubleFunction;

public class EntropyIllustrated {

    public static void main(String[] args) {

        Double2DoubleFunction f = x -> x == 0 ? 0 : -x * log2(x);
        BiFunction<Double, Double, Double> entropy = (x, y) -> f.apply(x) + f.apply(y);
        Grid2D gd = Grid2D.fromFunction(entropy, 0, 1, 0, 1, 256);
        Double2DoubleFunction binaryEntropy = x -> entropy.apply(x, 1 - x);

        Grid2D gd3d = Grid2D.fromFunction((p1, p2) -> {
            if (p1 + p2 > 1) {
                return 0.0;
            }
            double p3 = 1 - p1 - p2;
            return f.apply(p1) + f.apply(p2) + f.apply(p3);
        }, 0, 1, 0, 1, 256);

        VarDouble x = VarDouble.seq(0, 1, 0.01).name("x");
        VarDouble y = VarDouble.from(x, f).name("y");

        GridLayer gridLayer = GridLayer.of(1, 3);
        gridLayer.add(lines(x, y, lwd(1))
                .funLine(v -> f.apply(1 - v), lwd(1))
                .funLine(binaryEntropy, lwd(2))
                .xLim(0, 1).yLim(0, 1));
        gridLayer.add(isoBands(gd, gd.seq(128, 0.01), pch.circleFull(), palette.hue(0, 240, gd)).abLine(-1, 1));
        gridLayer.add(isoBands(gd3d, gd3d.seq(128, 0.01), palette.hue(0, 240, gd3d)));
        WS.draw(gridLayer);
    }
}
