/*
 *
 *  * Apache License
 *  * Version 2.0, January 2004
 *  * http://www.apache.org/licenses/
 *  *
 *  * Copyright 2013 - 2022 Aurelian Tutuianu
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *  http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package rapaio.graphics.plot.artist;

import static rapaio.graphics.Plotter.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.datasets.Datasets;
import rapaio.graphics.Figure;
import rapaio.graphics.plot.artist.AbstractArtistTest;
import rapaio.image.ImageTools;
import rapaio.ml.eval.metric.ROC;

public class ROCCurveTest extends AbstractArtistTest {

    private Frame df;

    @BeforeEach
    void setUp() throws Exception {
        RandomSource.setSeed(1234);
        df = Datasets.loadLifeScience().mapRows(Mapping.range(2000));
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testRocCurve() throws IOException {

        ROC roc = ROC.from(df.rvar(0), df.rvar("class"), 2);
        Figure fig = rocCurve(roc);
        assertTest(fig, "roc-test");
    }
}
