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

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static rapaio.graphics.Plotter.plot;
import static rapaio.graphics.opt.GOpts.color;
import static rapaio.graphics.opt.GOpts.halign;

public class TextTest extends AbstractArtistTest {

    @Test
    void testText() throws IOException {
        var plot = plot().xLim(0, 1).yLim(0, 1);
        plot.text(0.1, 0.9, "Ana\nAre\nMere", halign.left());
        plot.text(0.5, 0.9, "Ana\nAre\nMere", halign.center(), color(2));
        plot.text(0.8, 0.9, "Ana\nAre\nMere", halign.right(), color(4));

        assertTest(plot, "text-test");
    }
}
