/*
 * Copyright 2013 Aurelian Tutuianu
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

package titanic;

import static rapaio.core.BaseMath.E;
import static rapaio.core.BaseMath.log;
import rapaio.core.UnivariateFunction;
import rapaio.data.Frame;
import rapaio.data.NominalVector;
import rapaio.data.SolidFrame;
import rapaio.data.Vector;
import rapaio.filters.BaseFilters;
import static rapaio.filters.NominalFilters.fillMissingValues;
import static rapaio.filters.NumericFilters.applyFunction;
import rapaio.io.CsvPersistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Utils {
    public static Frame read(String name) throws IOException {
        CsvPersistence csv = new CsvPersistence();
        csv.setHasHeader(true);
        csv.setColSeparator(',');
        Frame df = csv.read(name, Explore.class.getResourceAsStream(name));

        HashSet<String> numericColumns = new HashSet<>();
        numericColumns.add("Age");
        numericColumns.add("Fare");
//        numericColumns.add("SibSp");
//        numericColumns.add("Parch");
        List<Vector> vectors = new ArrayList<>();

        for (int i = 0; i < df.getColCount(); i++) {
            if (!numericColumns.contains(df.getCol(i).getName())) {
                vectors.add(fillMissingValues(df.getCol(i), new String[]{"", " "}));
            } else {
                vectors.add(BaseFilters.toNumeric(df.getCol(i).getName(), df.getCol(i)));
            }
        }
        vectors.add(applyFunction(BaseFilters.toNumeric("LogFare", df.getCol("Fare")), new UnivariateFunction() {
            @Override
            public double eval(double value) {
                return log(E + value);
            }
        }));

        HashMap<String, String> titleDict = new HashMap<>();
        titleDict.put("Mrs.", "Mrs");
        titleDict.put("Mr.", "Mr");
        titleDict.put("Master.", "Master");
        titleDict.put("Miss.", "Miss");
        titleDict.put("Ms.", "Miss");
        titleDict.put("Mlle.", "Miss");
        titleDict.put("Dr.", "Dr");
        titleDict.put("Rev.", "Rev");
        titleDict.put("Sir.", "Sir");
        titleDict.put("Major.", "Sir");
        titleDict.put("Don.", "Sir");
        titleDict.put("Mme.", "Mrs");
        titleDict.put("Col.", "Col");
        titleDict.put("Capt.", "Col");
        titleDict.put("Jonkheer.", "Col");
        titleDict.put("Countess.", "Lady");
        titleDict.put("Lady.", "Lady");
        titleDict.put("Dona.", "Lady");
        Vector title = new NominalVector("Title", df.getRowCount(), titleDict.values());
        for (int i = 0; i < df.getRowCount(); i++) {
            for (String term : titleDict.keySet()) {
                if (df.getCol("Name").getLabel(i).contains(term)) {
                    title.setLabel(i, titleDict.get(term));
                    break;
                }
            }
            if (title.isMissing(i)) {
                System.out.println(df.getCol("Name").getLabel(i));
            }
        }
        vectors.add(title);

        HashSet<String> nameDict = new HashSet<>();
        for (int i = 0; i < df.getRowCount(); i++) {
            nameDict.add(df.getLabel(i, df.getColIndex("Name")).split(",")[0]);
        }
        Vector family = new NominalVector("Family", df.getRowCount(), nameDict);
        for (int i = 0; i < df.getRowCount(); i++) {
            family.setLabel(i, df.getLabel(i, df.getColIndex("Name")).split(",")[0]);
        }
        vectors.add(family);

        df = new SolidFrame(df.getName(), df.getRowCount(), vectors);
        return df;
    }
}
