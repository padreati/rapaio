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

package rapaio.ml.tree;

import org.junit.Assert;
import org.junit.Test;
import rapaio.data.Frame;
import rapaio.io.ArffPersistence;
import rapaio.io.Csv;
import rapaio.ml.tools.ModelEvaluation;
import rapaio.workspace.Summary;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class RandomForestClassifierTest {

    public static Frame loadFrame(String name) throws IOException {
        final String path = "/UCI/" + name + ".arff";
        ArffPersistence arff = new ArffPersistence();
        return arff.read(name, RandomForestClassifierTest.class.getResourceAsStream(path));
    }

    @Test
    public void testDummy() {
        Assert.assertTrue(true);
    }

    public double test(String name) throws IOException {
        Frame df = loadFrame(name);
        String className = df.colNames()[df.colCount() - 1];
        RandomForestClassifier rf = new RandomForestClassifier() {{
            setMtrees(100);
        }};
        ModelEvaluation cv = new ModelEvaluation();
        return cv.cv(df, className, rf, 10);
    }

    //        @Test
    public void allCompareTest() throws IOException, URISyntaxException {
        Frame tests = new Csv().read(getClass(), "tests.csv");
        for (int i = 0; i < tests.rowCount(); i++) {
            if (tests.label(i, 0).startsWith("#")) {
                continue;
            }
            System.out.println("test for " + tests.label(i, 0));
            tests.setValue(i, 3, test(tests.label(i, 0)));
        }
        Summary.head(tests.rowCount(), tests);
    }
}
