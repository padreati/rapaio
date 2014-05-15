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

package rapaio.ml.classifier.mi;

import rapaio.data.Frame;
import rapaio.data.Frames;
import rapaio.data.Nominal;
import rapaio.data.filters.BaseFilters;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.bayes.NaiveBayesClassifier;
import rapaio.ml.classifier.colselect.ColSelector;
import rapaio.ml.classifier.tools.DensityVector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class MIClassifier extends AbstractClassifier {

    String groupCol = "";
    Classifier c = new NaiveBayesClassifier();

    @Override
    public ColSelector getColSelector() {
        return super.getColSelector();
    }

    @Override
    public MIClassifier withColSelector(ColSelector colSelector) {
        return (MIClassifier) super.withColSelector(colSelector);
    }

    public String getGroupCol() {
        return groupCol;
    }

    public MIClassifier withGroupCol(String groupCol) {
        this.groupCol = groupCol;
        return this;
    }

    public Classifier getClassifier() {
        return c;
    }

    public MIClassifier withClassifier(Classifier c) {
        this.c = c;
        return this;
    }

    @Override
    public Classifier newInstance() {
        return new MIClassifier()
                .withColSelector(colSelector)
                .withGroupCol(groupCol)
                .withClassifier(c.newInstance());
    }

    @Override
    public String name() {
        return "MIClassifier";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append("MIClassifier(");
        sb.append("colSelector=").append(colSelector.name()).append(",");
        sb.append("groupCol=").append(groupCol).append(",");
        sb.append("c=").append(c.fullName()).append(",");
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void learn(Frame df, String targetCol) {
        this.targetCol = targetCol;
        this.dict = df.col(targetCol).getDictionary();

        if (!groupCol.isEmpty()) {
            df = BaseFilters.removeCols(df, groupCol);
        }
        c.learn(df, targetCol);
    }

    @Override
    public void predict(Frame df) {
        String[] groups = df.col(groupCol).getDictionary();
        Map<String, DensityVector> dvs = new HashMap<>();
        Arrays.stream(groups).forEach(groupLabel -> dvs.put(groupLabel, new DensityVector(dict)));

        c.predict(df);
        c.pred().stream().forEach(s -> {
            String groupLabel = df.getLabel(s.row(), groupCol);
            DensityVector dv = dvs.get(groupLabel);
            int i = s.getIndex();
            dv.update(i, 1);
        });

        Map<String, String> predictions = new HashMap<>();
        dvs.forEach((groupLabel, dv) -> {
            predictions.put(groupLabel, dict[dv.findBestIndex()]);
        });

        pred = new Nominal(df.rowCount(), dict);

        c.pred().stream().forEach(s -> pred.setLabel(s.row(), predictions.get(df.getLabel(s.row(), groupCol))));
        dist = c.dist();
    }

    @Override
    public void buildSummary(StringBuilder sb) {

    }
}
