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

import rapaio.core.distributions.empirical.KFuncGaussian;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.datasets.Datasets;
import rapaio.printer.ImageTools;

public class DensityTest extends AbstractArtistTest {

    private Frame df;

    @BeforeEach
    void setUp() throws Exception {
        df = Datasets.loadLifeScience().mapRows(Mapping.range(2000));
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testDensity() throws IOException {

        Var x = df.rvar(0).mapRows(Mapping.range(200));
        var up = densityLine(x, new KFuncGaussian(), lwd(30), alpha(0.1f), color(2));
        for (int i = 10; i < 150; i += 5) {
            up.densityLine(x, i / 300.0);
        }
        up.densityLine(x, lwd(2), color(1));

        var down = densityLine(df.rvar(0), fill(13));
        assertTest(gridLayer(1, 2).add(up).add(down), "density-test");
    }
}
