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

import static rapaio.graphics.opt.GOptions.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.graphics.plot.Plot;
import rapaio.printer.ImageTools;

public class SegmentTest extends AbstractArtistTest {

    @BeforeEach
    void setUp() throws Exception {
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testSegment() throws IOException {
        Plot plot = new Plot();
        plot.xLim(0, 1);
        plot.yLim(0, 1);

        plot.segmentLine(0.1, 0.1, 0.7, 0.7, fill(1));
        plot.segmentArrow(0.1, 0.9, 0.9, 0.1, fill(2), lwd(6));

        assertTest(plot, "segment-test");
    }
}
