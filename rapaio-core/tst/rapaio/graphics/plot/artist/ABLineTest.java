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

import static rapaio.graphics.opt.GOpts.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import rapaio.graphics.plot.Plot;

public class ABLineTest extends AbstractArtistTest {

    @Test
    void testABLine() throws IOException {
        Plot plot = new Plot();
        plot.xLim(-10, 10);
        plot.yLim(-10, 10);

        plot.hLine(0, fill(3));
        plot.hLine(1, fill(4));

        plot.vLine(0, fill(5));
        plot.vLine(1.2, fill(6));

        plot.abLine(1, 0, fill(7));
        plot.abLine(-1.2, 0, fill(8));

        assertTest(plot, "abline-test");
    }
}
