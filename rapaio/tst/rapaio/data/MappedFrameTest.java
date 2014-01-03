/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */
package rapaio.data;

import org.junit.Test;
import rapaio.datasets.Datasets;
import rapaio.filters.RowFilters;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class MappedFrameTest {

    @Test
    public void colsSortedTest() throws IOException, URISyntaxException {
        Frame orig = Datasets.loadIrisDataset();
        Frame sort = RowFilters.sort(orig, RowComparators.numericComparator(orig.getCol(1), true));
        sort = RowFilters.sort(sort, RowComparators.numericComparator(orig.getCol(2), true));
        for (int i = 0; i < sort.getRowCount(); i++) {
            assertEquals(sort.getValue(i, 0), sort.getCol(0).getValue(i), 1e-10);
        }
    }
}
