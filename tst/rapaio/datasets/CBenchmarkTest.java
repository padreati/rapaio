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

import org.junit.Test;
import rapaio.core.stat.ConfusionMatrix;
import rapaio.data.*;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.tree.ForestClassifier;
import rapaio.ml.classifier.tree.TreeClassifier;
import rapaio.ws.Summary;

import java.io.IOException;
import java.util.*;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class CBenchmarkTest {

    @Test
    public void testAll() throws IOException {

        CBenchmark bench = new CBenchmark();
        Map<String, Classifier> classifiers = new LinkedHashMap<>();

        // add classifiers to test

        classifiers.put("DecisionStump", TreeClassifier.buildDecisionStump());
        classifiers.put("C45", TreeClassifier.buildC45());
        classifiers.put("RF(100)", ForestClassifier.buildRandomForest(100, 2, 0.9, TreeClassifier.buildC45()));
        classifiers.put("RF(1000)", ForestClassifier.buildRandomForest(100, 2, 0.9, TreeClassifier.buildC45()));

        List<String> cNames = new ArrayList<>(classifiers.keySet());
        List<CTask> tasks = bench.getDefaultTasks();

        List<Var> resultCols = new ArrayList<>();
        List<String> resultNames = new ArrayList<>();

        resultCols.add(new Nominal(tasks.size(), Collections.<String>emptyList()));
        resultNames.add("data set");

        for(String cName : cNames) {
            resultCols.add(new Numeric(tasks.size()));
            resultNames.add(cName);
        }

        Frame results = new SolidFrame(tasks.size(), resultCols, resultNames, null);

        for (int i = 0; i < tasks.size(); i++) {
            CTask task = tasks.get(i);
            results.setLabel(i, "data set", task.getName());

            task.reSample(0.7, false);

            for (String cName : cNames) {
                Classifier c = classifiers.get(cName);
                c.learn(task.getTrainFrame(), task.getTargetName());
                c.predict(task.getTestFrame());

                results.setValue(i, cName,
                        new ConfusionMatrix(
                                task.getTestFrame().col(task.getTargetName()),
                                c.pred()).getAccuracy());
            }
        }

        Summary.lines(results);
    }
}
