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

package rapaio.ml.supervised;

import rapaio.data.Frame;
import rapaio.data.MappedFrame;
import rapaio.data.Mapping;
import static rapaio.explore.Workspace.code;
import static rapaio.filters.RowFilters.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CrossValidation {

    public void cv(Frame df, String classColName, Classifier c, int folds) {
        StringBuilder sb = new StringBuilder();
        sb.append("CrossValidation with ").append(folds).append(" folds\n");
        Frame f = shuffle(df);
        ClassifierModel[] results = new ClassifierModel[folds];

        for (int i = 0; i < folds; i++) {
            List<Integer> trainMapping = new ArrayList<>();
            List<Integer> testMapping = new ArrayList<>();
            if (folds >= df.getRowCount() - 1) {
                testMapping.add(i);
                for (int j = 0; j < f.getRowCount(); j++) {
                    if (j != i) {
                        trainMapping.add(j);
                    }
                }
            } else {
                for (int j = 0; j < f.getRowCount(); j++) {
                    if (j % folds == i) {
                        testMapping.add(j);
                    } else {
                        trainMapping.add(j);
                    }
                }
            }
            Frame train = new MappedFrame(f, new Mapping(trainMapping));
            Frame test = new MappedFrame(f, new Mapping(testMapping));

            c.learn(train, classColName);
            results[i] = c.predict(test);
        }

        double tacc = 0;
        for (int i = 0; i < folds; i++) {
            ClassifierModel cr = results[i];
            double acc = 0;
            for (int j = 0; j < cr.getClassification().getRowCount(); j++) {
                if (cr.getClassification().getIndex(j) == cr.getTestFrame().getCol(classColName).getIndex(j)) {
                    acc++;
                }
            }
            acc /= (1. * cr.getClassification().getRowCount());
            tacc += acc;
            sb.append(String.format("CV %d, accuracy:%.6f\n", i + 1, acc));
        }
        tacc /= (1. * folds);
        sb.append(String.format("Mean accuracy:%.6f\n", tacc));
        code(sb.toString());
    }
}
