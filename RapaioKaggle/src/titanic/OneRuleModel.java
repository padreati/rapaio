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

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Vector;
import rapaio.data.util.NominalConsolidator;
import rapaio.filters.FilterRemoveColumns;
import rapaio.io.CsvPersistence;
import rapaio.supervised.ClassifierResult;
import rapaio.supervised.CrossValidation;
import rapaio.supervised.rule.OneRule;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static rapaio.core.BaseFilters.rename;
import static rapaio.core.BaseMath.getRandomSource;
import static rapaio.explore.Workspace.closePrinter;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class OneRuleModel {

    public static void main(String[] args) throws IOException {
        getRandomSource().setSeed(1);

        Frame train = Explore.read("train.csv");
        Frame test = Explore.read("test.csv");
        List<Frame> frames = NominalConsolidator.consolidate(Arrays.asList(new Frame[]{train, test}));
        train = frames.get(0);
        test = frames.get(1);

        Frame tr = new FilterRemoveColumns("PassengerId,Name,Ticket,Cabin").filter(train);
//        Frame tr = new FilterRetainColumns("Survived,Sex").filter(train);
//        Summary.summary(tr);
        CrossValidation cv = new CrossValidation();
        cv.cv(tr, tr.getColIndex("Survived"), new OneRule(4), 10);

        OneRule oneRule = new OneRule(4);
        oneRule.learn(tr, tr.getColIndex("Survived"));
        ClassifierResult cr = oneRule.predict(test);
//        oneRule.printModelSummary();

        Frame submit = new SolidFrame("submit", test.getRowCount(), new Vector[]{test.getCol("PassengerId"), rename(cr.getClassification(), "Survived")});
        CsvPersistence persist = new CsvPersistence();
        persist.setColSeparator(',');
        persist.setHasQuotas(false);
        persist.setHeader(true);
        persist.write(submit, "/home/ati/work/incubator.rapaio/RapaioKaggle/src/titanic/submit.csv");

        closePrinter();
    }
}
