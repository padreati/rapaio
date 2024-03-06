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
import static rapaio.graphics.opt.GOpts.color;
import static rapaio.graphics.opt.GOpts.points;

import java.awt.Color;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.graphics.plot.Plot;
import rapaio.printer.ImageTools;

public class FunLineTest extends AbstractArtistTest {

    @BeforeEach
    void setUp() throws Exception {
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testFunLine() throws IOException {

        Plot plot = funLine(x -> x * x, color(1))
                .funLine(Math::log1p, color(2))
                .funLine(x -> Math.sin(Math.pow(x, 3)) + 5, color(3), points(10_000))
                .hLine(5, color(Color.LIGHT_GRAY))
                .xLim(0, 10)
                .yLim(0, 10);

        assertTest(plot, "funline-test");
    }
}
