/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

package rapaio.ml.supervised.tree;

import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Vector;
import rapaio.explore.Summary;
import rapaio.filters.BaseFilters;
import rapaio.filters.ColFilters;
import rapaio.io.ArffPersistence;
import rapaio.io.CsvPersistence;
import rapaio.ml.supervised.ClassifierModel;
import rapaio.ml.supervised.CrossValidation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class RandomForestTest {


    public static Frame loadFrame(String name) throws IOException {
        final String path = "/UCI/" + name + ".arff";
        ArffPersistence arff = new ArffPersistence();
        return arff.read(name, RandomForestTest.class.getResourceAsStream(path));
    }

    public double test(String name) throws IOException {
        Frame df = loadFrame(name);
        String className = df.getCol(df.getColCount() - 1).getName();
        RandomForest rf = new RandomForest(100);
        CrossValidation cv = new CrossValidation();
        return cv.cv(df, className, rf, 10);
    }

    @Test
    public void allCompareTest() throws IOException {
        CsvPersistence csv = new CsvPersistence();
        csv.setHasHeader(true);
        csv.setDecimalPoint('.');
        Frame tests = csv.read("test", getClass().getResourceAsStream("tests.csv"));
        List<Vector> vectors = new ArrayList<>();
        vectors.add(tests.getCol(0));
        for (int i = 1; i <= 3; i++) {
            vectors.add(BaseFilters.toNumeric(tests.getCol(i).getName(), tests.getCol(i)));
        }
        tests = new SolidFrame("tests", tests.getRowCount(), vectors);

        for (int i = 0; i < tests.getRowCount(); i++) {
            System.out.println("test for " + tests.getLabel(i, 0));
            tests.setValue(i, 3, test(tests.getLabel(i, 0)));
        }

        Summary.head(tests.getRowCount(), tests);
    }

    //    @Test
    public void testSomeTests() throws IOException {
        test("anneal");
    }
}
