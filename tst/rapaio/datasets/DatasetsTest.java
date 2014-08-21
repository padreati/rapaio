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

package rapaio.datasets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rapaio.data.Frame;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class DatasetsTest {

    public DatasetsTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testIrisDataset() throws IOException, URISyntaxException {
        Frame df = Datasets.loadIrisDataset();

        assertNotNull(df);
        assertEquals(5, df.varCount());
        assertEquals(150, df.rowCount());

        final String[] names = new String[]{"sepal-length", "sepal-width", "petal-length", "petal-width", "class"};
        assertArrayEquals(names, df.varNames());

        int nas = 0;
        for (int i = 0; i < df.varCount(); i++) {
            for (int j = 0; j < df.rowCount(); j++) {
                if (df.var(i).missing(j)) {
                    nas++;
                }
            }
        }
        assertEquals(0, nas);
    }
}