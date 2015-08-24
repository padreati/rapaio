/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.core.tools;

import org.junit.Test;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.ws.Summary;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class DTableTest {

    @Test
    public void testPlayNoMissing() throws IOException {

        Frame df = Datasets.loadPlay();

        DTable id = DTable.newFromCounts(df.var("outlook"), df.var("class"));
        assertEquals(0.940, id.getTargetEntropy(false), 1e-3);
        assertEquals(0.694, id.getSplitEntropy(false), 1e-3);
        assertEquals(0.246, id.getInfoGain(false), 1e-3);

        assertEquals(1.577, id.getSplitInfo(false), 1e-3);
        assertEquals(0.156, id.getGainRatio(false), 1e-3);

        id = DTable.newFromCounts(df.var("windy"), df.var("class"));
        assertEquals(0.940, id.getTargetEntropy(false), 1e-3);
        assertEquals(0.892, id.getSplitEntropy(false), 1e-3);
        assertEquals(0.048, id.getInfoGain(false), 1e-3);

        assertEquals(0.985, id.getSplitInfo(false), 1e-3);
        assertEquals(0.048, id.getGainRatio(false), 1e-3);
    }

    @Test
    public void testPlayWithMissing() throws IOException {

        Frame df = Datasets.loadPlay();
        df.var("outlook").setMissing(5);
        Summary.head(10000, df);

        DTable id = DTable.newFromCounts(df.var("outlook"), df.var("class"));

        assertEquals(0.892, id.getTargetEntropy(true), 1e-3);
        assertEquals(0.693, id.getSplitEntropy(true), 1e-3);
        assertEquals(0.199, id.getInfoGain(true), 1e-3);

        assertEquals(1.809, id.getSplitInfo(true), 1e-3);
        assertEquals(0.110, id.getGainRatio(true), 1e-3);
    }
}
