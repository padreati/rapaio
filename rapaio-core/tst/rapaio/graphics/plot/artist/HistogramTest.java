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

import static rapaio.graphics.Plotter.*;
import static rapaio.graphics.opt.GOptions.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.datasets.Datasets;
import rapaio.graphics.opt.NColor;
import rapaio.graphics.plot.GridLayer;
import rapaio.printer.ImageTools;

public class HistogramTest extends AbstractArtistTest {

    private Frame df;

    @BeforeEach
    void setUp() throws Exception {
        df = Datasets.loadLifeScience().mapRows(Mapping.range(2000));
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testHistogram() throws IOException {
        Var x = df.rvar(0).name("x");

        GridLayer grid = new GridLayer(2, 2);

        grid.add(hist(x, -10, -2, bins(30)).xLim(-10, -2));
        grid.add(hist(x));

        grid.add(hist(x, bins(40), horizontal(true)).yLim(-10, -2));
        grid.add(hist(x, bins(40), horizontal(true), prob(true)).yLim(-10, -2).xLim(0, 0.02));

        assertTest(grid, "hist-test");
    }

    @Test
    void testNormedHistogram() throws IOException {
        Frame iris = Datasets.loadIrisDataset();
        var v = iris.rvar("sepal-length");
        assertTest(hist(v, bins(30), prob(true), fill(NColor.tab_purple))
                .densityLine(v, (v.dt().max() - v.dt().min()) / 15), "hist-density-test");
    }
}
