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

package rapaio.graphics.plot.artist;

import static java.lang.StrictMath.*;

import static rapaio.graphics.Plotter.*;
import static rapaio.graphics.opt.Palette.*;
import static rapaio.graphics.opt.GOpts.*;

import java.io.IOException;
import java.util.function.BiFunction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.tools.Grid2D;
import rapaio.data.VarDouble;
import rapaio.graphics.opt.NColor;
import rapaio.graphics.plot.GridLayer;
import rapaio.printer.ImageTools;

public class IsoCurvesTest extends AbstractArtistTest {

    @BeforeEach
    void setUp() throws Exception {
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testIsoCurves() throws IOException {

        GridLayer grid = new GridLayer(2, 2);

        BiFunction<Double, Double, Double> fun = (x, y) -> pow(x * x + y - 11, 2) + pow(x + y * y - 7, 2);

        Grid2D gd = Grid2D.fromFunction(fun, -3, 3, -3, 3, 256);
        int levelCount = 30;
        double[] p = VarDouble.seq(0, 1, 1. / levelCount).elements();
        double[] levels = gd.quantiles(p);

        grid.add(isoCurves(gd, levels, palette(bicolor(NColor.darkred, NColor.dodgerblue, gd.minValue(), gd.maxValue()))));
        grid.add(isoBands(gd, levels, palette(hue(0, 240, gd.minValue(), gd.maxValue()))));

        grid.add(isoLines(gd, levels, palette(hue())));
        grid.add(isoCurves(gd, levels, palette(hue())).xLim(-2, 2).yLim(-4, 4));

        assertTest(grid, "isocurves-test");
    }
}
