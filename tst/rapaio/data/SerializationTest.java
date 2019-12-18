/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.data;

import org.junit.jupiter.api.Test;
import rapaio.datasets.Datasets;
import rapaio.io.JavaIO;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/17/15.
 */
public class SerializationTest {

    @Test
    void testFrames() throws IOException, ClassNotFoundException {
        testFrame(Datasets.loadIrisDataset(), "iris", 7243);
        testFrame(Datasets.loadCarMpgDataset(), "car", 48884);
        testFrame(Datasets.loadRandom(), "random", 311112);
    }

    void testFrame(Frame df, String name, long initialSize) throws IOException, ClassNotFoundException {
        File tmp = File.createTempFile("test-", "ser");
        JavaIO.storeToFile(df, tmp);

        Frame restore = (Frame) JavaIO.restoreFromFile(tmp);

        assertTrue(df.deepEquals(restore));
    }
}
