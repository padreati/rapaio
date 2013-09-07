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

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Vector;
import static rapaio.filters.NominalFilters.*;
import rapaio.explore.Summary;
import static rapaio.explore.Workspace.closePrinter;
import static rapaio.filters.BaseFilters.renameVector;
import static rapaio.filters.ColFilters.removeCols;
import rapaio.io.CsvPersistence;
import rapaio.ml.supervised.ClassifierResult;
import rapaio.ml.supervised.CrossValidation;
import rapaio.ml.supervised.rule.OneRule;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class OneRuleModel {

    public static void main(String[] args) throws IOException {
        RandomSource.setSeed(1);

        Frame train = Utils.read("train.csv");
        Frame test = Utils.read("test.csv");
        List<Frame> frames = consolidate(Arrays.asList(train, test));
        train = frames.get(0);
        test = frames.get(1);

        Frame tr = removeCols(train, "PassengerId,Name,Ticket,Cabin");
//        Frame tr = retainCols(train, "Survived,Sex");
        Summary.summary(tr);
        CrossValidation cv = new CrossValidation();
        cv.cv(tr, tr.getColIndex("Survived"), new OneRule(4), 10);

        OneRule oneRule = new OneRule(4);
        oneRule.learn(tr, tr.getColIndex("Survived"));
        ClassifierResult cr = oneRule.predict(test);
//        oneRule.printModelSummary();

        Frame submit = new SolidFrame("submit", test.getRowCount(), new Vector[]{test.getCol("PassengerId"), renameVector(cr.getClassification(), "Survived")});
        CsvPersistence persist = new CsvPersistence();
        persist.setColSeparator(',');
        persist.setHasQuotas(false);
        persist.setHeader(true);
        persist.write(submit, "/home/ati/work/incubator.rapaio/RapaioKaggle/src/titanic/submit.csv");

        closePrinter();
    }
}
