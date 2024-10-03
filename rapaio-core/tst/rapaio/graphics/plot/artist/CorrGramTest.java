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

import static rapaio.graphics.Plotter.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import rapaio.core.correlation.CorrSpearman;
import rapaio.core.tools.DistanceMatrix;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;

public class CorrGramTest extends AbstractArtistTest {

    @Test
    void testCorrGram() throws IOException {
        Frame sel = Datasets.loadHousing();
        DistanceMatrix d = CorrSpearman.of(sel).matrix();
        assertTest(corrGram(d), "corrgram-test");
    }

}
