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

package rapaio.ml.classifier.meta;

import rapaio.core.VarRange;
import rapaio.data.Frame;
import rapaio.data.Nominal;
import rapaio.data.Numeric;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.colselect.ColSelector;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
@Deprecated
public class MVClassifier extends AbstractClassifier {

    Classifier c;
    // learning artifacts
    String[] targetCols;
    Map<String, Classifier> models;

    @Override
    public MVClassifier withColSelector(ColSelector colSelector) {
        return (MVClassifier) super.withColSelector(colSelector);
    }

    public Classifier getClassifier() {
        return c;
    }

    public MVClassifier withClassifier(Classifier c) {
        this.c = c;
        return this;
    }

    @Override
    public Classifier newInstance() {
        return new MVClassifier()
                .withColSelector(colSelector)
                .withClassifier(c);
    }

    @Override
    public String name() {
        return "MVClassifier";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append("MVClassifier(");
        sb.append("colSelector=").append(colSelector.name()).append(",");
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void learn(Frame df, Numeric weights, String targetCol) {
        this.targetCol = targetCol;
        List<Integer> colIndexes = new VarRange(targetCol).parseColumnIndexes(df);
        this.targetCols = new String[colIndexes.size()];
        for (int i = 0; i < colIndexes.size(); i++) {
            this.targetCols[i] = df.varNames()[colIndexes.get(i)];
        }

        models = Arrays.stream(this.targetCols).collect(
                toMap(s -> s, s -> {
                    Classifier model = c.newInstance();
                    Frame train = df;
                    for (String col : targetCols) {
                        if (s.equals(col)) continue;
                        train = train.removeVars(new VarRange(col));
                    }
                    model.learn(train, weights, s);
                    return model;
                })
        );
    }

    @Override
    public void predict(Frame df) {
        models.values().forEach(classifier -> classifier.predict(df));
        this.pred = models.get(targetCols[0]).pred();
        this.dist = models.get(targetCols[0]).dist();
    }

    public Nominal pred(String targetCol) {
        return models.get(targetCol).pred();
    }

    public Frame dist(String targetCol) {
        return models.get(targetCol).dist();
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        sb.append("Not yet implemented!");
    }
}
