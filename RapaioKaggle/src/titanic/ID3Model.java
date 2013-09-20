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

package titanic;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Vector;
import rapaio.explore.Summary;
import static rapaio.explore.Workspace.closePrinter;
import static rapaio.filters.BaseFilters.renameVector;
import rapaio.filters.ColFilters;
import static rapaio.filters.ColFilters.removeCols;
import rapaio.filters.NominalFilters;
import static rapaio.filters.NominalFilters.consolidate;
import rapaio.io.CsvPersistence;
import rapaio.ml.supervised.ClassifierResult;
import rapaio.ml.supervised.CrossValidation;
import rapaio.ml.supervised.rule.OneRule;
import rapaio.ml.supervised.tree.ID3;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ID3Model {
    public static void main(String[] args) throws IOException {
//        RandomSource.setSeed(1);

        Frame train = Utils.read("train.csv");
        Frame test = Utils.read("test.csv");
        List<Frame> frames = consolidate(Arrays.asList(train, test));
        train = frames.get(0);
        test = frames.get(1);

        Frame tr = null;
        tr = removeCols(train, "PassengerId,Name,Ticket,Cabin");//0.789 - kaggle 0.784
//        tr = removeCols(train, "PassengerId,Name,Ticket,Cabin,Embarked");//0.789 - kaggle 0.784
//        tr = removeCols(train, "PassengerId,Name,Ticket,Cabin");//0.823 - kaggle 0.784
//        tr = removeCols(train, "PassengerId,Name");//0.802 - kaggle 0.746
        tr = ColFilters.retainNominal(tr);
        Summary.summary(tr);

        ID3 id3 = new ID3();
        id3.setMetricType(new ID3.InfoGainMetricType());
        id3.setMetricType(new ID3.EntropyMetricType());
        CrossValidation cv = new CrossValidation();
        cv.cv(tr, "Survived", id3, tr.getRowCount() - 1);

        id3.learn(tr, "Survived");
//        Summary.summary(id3);
        ClassifierResult cr = id3.predict(test);


        Frame submit = new SolidFrame("submit", test.getRowCount(), new Vector[]{
                test.getCol("PassengerId"),
                renameVector(cr.getClassification(), "Survived")
        });
        CsvPersistence persist = new CsvPersistence();
        persist.setColSeparator(',');
        persist.setHasQuotas(true);
        persist.setHasHeader(true);
        persist.write(submit, "/home/ati/work/rapaio/RapaioKaggle/src/titanic/submit.csv");

        closePrinter();
    }
}
