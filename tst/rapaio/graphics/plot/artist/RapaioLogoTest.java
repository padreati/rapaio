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

package rapaio.graphics.plot.artist;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static rapaio.graphics.Plotter.*;
import static rapaio.sys.With.HALIGN_CENTER;
import static rapaio.sys.With.VALIGN_CENTER;
import static rapaio.sys.With.alpha;
import static rapaio.sys.With.color;
import static rapaio.sys.With.fill;
import static rapaio.sys.With.font;
import static rapaio.sys.With.hAlign;
import static rapaio.sys.With.heights;
import static rapaio.sys.With.lwd;
import static rapaio.sys.With.vAlign;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Normal;
import rapaio.data.VarDouble;
import rapaio.graphics.Figure;
import rapaio.graphics.plot.Plot;
import rapaio.graphics.plot.artist.AbstractArtistTest;
import rapaio.graphics.plot.artist.PolyFill;
import rapaio.graphics.plot.artist.PolyLine;
import rapaio.graphics.plot.artist.Text;
import rapaio.image.ImageTools;

/**
 * Test some graphics by maintaining some previously generated images.
 * <p>
 * The main idea is that is hard to check if an image is what some might expect.
 * We first generate an image, we check it and agree that it is ok, and we comment
 * out generation of that image again. At test time we need to be sure that the
 * new generated image is the same. When something is changed in graphic system,
 * other images might be generated, with additionally human check.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 12/4/15.
 */

public class RapaioLogoTest extends AbstractArtistTest {

    @BeforeEach
    void setUp() throws Exception {
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testRapaioLogo() throws IOException {
        var x = VarDouble.seq(0, 1, 0.004).name("x");

        var green = Normal.of(0.24, 0.08);
        var blue = Normal.of(0.37, 0.15);
        var orange = Normal.of(0.59, 0.13);
        var red = Normal.of(0.80, 0.06);

        Color cgreen = Color.decode("0x2ca02c");
        Color cblue = Color.decode("0x1f77b4");
        Color corange = Color.decode("0xff7f0e");
        Color cred = Color.decode("0xd62728");

        var ygreen = VarDouble.from(x, green::pdf).name("y");
        var yblue = VarDouble.from(x, blue::pdf).name("y");
        var yorange = VarDouble.from(x, orange::pdf).name("y");
        var yred = VarDouble.from(x, red::pdf).name("y");

        float alpha = 0.5f;
        float lwd = 5f;

        Plot up = plot();

        up.add(new PolyFill(x, yblue, fill(cblue), alpha(alpha)));
        up.add(new PolyFill(x, yorange, fill(corange), alpha(alpha)));
        up.add(new PolyFill(x, ygreen, fill(cgreen), alpha(alpha)));
        up.add(new PolyFill(x, yred, fill(cred), alpha(alpha)));

        up.add(new PolyLine(false, x, yblue, color(cblue), lwd(lwd)));
        up.add(new PolyLine(false, x, yorange, color(corange), lwd(lwd)));
        up.add(new PolyLine(false, x, ygreen, color(cgreen), lwd(lwd)));
        up.add(new PolyLine(false, x, yred, color(cred), lwd(lwd)));

        up.xLim(0, 1);
        up.leftThick(false);
        up.leftMarkers(false);
        up.bottomThick(false);
        up.bottomMarkers(false);

        Plot down = plot();

        down.leftThick(false);
        down.leftMarkers(false);
        down.bottomThick(false);
        down.bottomMarkers(false);

        down.add(new Text(0.5, 0.6, "rapaio", font("DejaVu Sans", Font.BOLD, 110),
                hAlign(HALIGN_CENTER), vAlign(VALIGN_CENTER), color(Color.decode("0x096b87"))));
        down.xLim(0, 1);
        down.yLim(0, 1);

        Figure fig = gridLayer(2, 1, heights(0.7, 0.3)).add(up).add(down);
        assertTest(fig, "rapaio-logo");
    }
}
