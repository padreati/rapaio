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

import junit.framework.Assert;
import org.junit.Test;
import rapaio.datasets.Datasets;
import rapaio.io.JavaIO;
import rapaio.sys.WS;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/17/15.
 */
public class SerializationTest {

    @Test
    public void testFrames() throws IOException, ClassNotFoundException, URISyntaxException {
        testFrame(Datasets.loadIrisDataset(), "iris", 7243);
        testFrame(Datasets.loadCarMpgDataset(), "car", 48884);
        testFrame(Datasets.loadMushrooms(), "mushrooms", 1121870);
        testFrame(Datasets.loadSpamBase(), "spam", 2874777);
        testFrame(Datasets.loadLifeScience(), "life", 2640020);
        testFrame(Datasets.loadRandom(), "random", 311112);
    }

    private void testFrame(Frame df, String name, long initialSize) throws IOException, ClassNotFoundException {
        WS.println("Testing " + name + " dataset serialization");
        df.printSummary();

        File tmp = File.createTempFile("test-", "ser");
        JavaIO.storeToFile(df, tmp);

        Frame restore = (Frame) JavaIO.restoreFromFile(tmp);
        WS.println("dataset " + name + " serialization size: " + tmp.length());
        WS.println("achievement " + (initialSize - tmp.length()) + " ( " + WS.formatFlex((initialSize - tmp.length()) * 1.0 / initialSize) + "%)");
        WS.println();

        Assert.assertTrue(df.deepEquals(restore));
    }
}
