/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.ml.classifier.bayes.estimator;

import rapaio.data.Frame;
import rapaio.data.Var;

import java.util.HashMap;
import java.util.Map;

/**
 * Weighted multinomial pmf estimator.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/18/15.
 */
public class MultinomialPmf implements NominalEstimator {

    private static final long serialVersionUID = 3019563706421891472L;
    private double[][] density;
    private Map<String, Integer> invTreeTarget;
    private Map<String, Integer> invTreeTest;

    @Override
    public String name() {
        return "MultinomialPmf";
    }

    @Override
    public String learningInfo() {
        return "MultinomialPmf";
    }

    @Override
    public void learn(Frame df, Var weights, String targetVar, String testVar, String... targetLabels) {

        String[] targetDict = df.var(targetVar).dictionary();
        String[] testDict = df.var(testVar).dictionary();

        invTreeTarget = new HashMap<>();
        invTreeTest = new HashMap<>();

        for (int i = 0; i < targetDict.length; i++) {
            invTreeTarget.put(targetDict[i], i);
        }
        for (int i = 0; i < testDict.length; i++) {
            invTreeTest.put(testDict[i], i);
        }

        density = new double[targetDict.length][testDict.length];
        for (int i = 0; i < targetDict.length; i++) {
            for (int j = 0; j < testDict.length; j++) {
                density[i][j] = 1.0;
            }
        }
        df.stream().forEach(s -> density[s.index(targetVar)][s.index(testVar)]++);
        for (int i = 0; i < targetDict.length; i++) {
            double t = 0;
            for (int j = 0; j < testDict.length; j++) {
                t += density[i][j];
            }
            for (int j = 0; j < testDict.length; j++) {
                density[i][j] /= t;
            }
        }
    }

    @Override
    public double cpValue(String testLabel, String targetLabel) {
        if (!invTreeTarget.containsKey(targetLabel)) {
            return 1e-10;
        }
        if (!invTreeTest.containsKey(testLabel)) {
            return 1e-10;
        }
        return density[invTreeTarget.get(targetLabel)][invTreeTest.get(testLabel)];
    }

    @Override
    public NominalEstimator newInstance() {
        return new MultinomialPmf();
    }
}
