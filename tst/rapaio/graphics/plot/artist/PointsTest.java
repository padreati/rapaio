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

import static rapaio.graphics.Plotter.points;
import static rapaio.graphics.Plotter.*;
import static rapaio.sys.With.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.datasets.Datasets;
import rapaio.graphics.Figure;
import rapaio.graphics.opt.Palette;
import rapaio.image.ImageTools;

public class PointsTest extends AbstractArtistTest {

    private Frame df;

    @BeforeEach
    void setUp() throws Exception {
        df = Datasets.loadLifeScience().mapRows(Mapping.range(1000));
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testPoints() throws IOException {

        Var x = df.rvar(0).dv().add(11).log1p().dv();
        Var y = df.rvar(1).dv().add(11).log1p().dv();
        Var h = VarDouble.from(x.size(), row -> Math.pow(Math.hypot(x.getDouble(row), y.getDouble(row)), 1.5));

        Figure fig = gridLayer(2, 2)
                .add(points(x))
                .add(points(x, y, pch(2), fill(2), color(1)))
                .add(points(x, y, pch(2), fill(h), sz(4), palette(Palette.hue(0, 240, h.dv().min(), h.dv().max()))))
                .add(points(x, pch(2), fill(y), sz(3), palette(Palette.hue(0, 120, y.dv().min(), y.dv().max()))));
        assertTest(fig, "points-test");
    }
}
