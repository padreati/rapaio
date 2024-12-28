/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.graphics.plot.artist;

import static rapaio.graphics.Plotter.gridLayer;
import static rapaio.graphics.Plotter.hist;
import static rapaio.graphics.Plotter.hist2d;
import static rapaio.graphics.Plotter.lines;
import static rapaio.graphics.Plotter.points;
import static rapaio.graphics.opt.GOpts.bins;
import static rapaio.graphics.opt.GOpts.fill;
import static rapaio.graphics.opt.GOpts.sz;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.datasets.Datasets;
import rapaio.printer.Figure;
import rapaio.printer.ImageTools;

public class GridLayerTest extends AbstractArtistTest {

    private Frame df;

    @BeforeEach
    void setUp() throws Exception {
        df = Datasets.loadLifeScience().mapRows(Mapping.range(2000));
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testGridLayer() throws IOException {

        Var x = df.rvar(0).name("x");
        Var y = df.rvar(1).name("y");

        Figure fig = gridLayer(3, 3)
                .add(0, 0, 2, 2, points(x, y, sz(2)))
                .add(2, 1, 2, 1, hist2d(x, y, fill(2)))
                .add(lines(x))
                .add(hist(x, bins(20)))
                .add(hist(y, bins(20)));

        assertTest(fig, "grid-test");
    }
}
