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
package rapaio.tutorial.pages;

import rapaio.core.RandomSource;
import rapaio.core.stat.ROC;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;

import static rapaio.explore.Workspace.*;
import static rapaio.sample.StatSampling.*;

import static rapaio.explore.Summary.*;

import rapaio.explore.Summary;
import rapaio.filters.ColFilters;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.ROCCurve;
import rapaio.ml.classification.Classifier;
import rapaio.ml.classification.boost.AdaBoostM1;
import rapaio.ml.classification.rule.OneRule;
import rapaio.ml.classification.tree.DecisionStump;
import rapaio.ml.classification.tree.RandomForest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class ROCCurvesPage implements TutorialPage {

    @Override
    public String getPageName() {
        return "ROCCurves";
    }

    @Override
    public String getPageTitle() {
        return "ROC Curves";
    }

    @Override
    public void render() throws IOException, URISyntaxException {
        heading(2, "Introduction");

        p("For exemplification we will use the classical spam data set. We load the data set and "
                + "we split randomly in two pieces. The first sample will be used for training purposes "
                + "and it will have ~ 0.66 of the data, the second sample will be used for "
                + "testing our model. ");

        p("In order to get train and test samples the following code could be used:");

        RandomSource.setSeed(2718);
        final Frame spam = ColFilters.retainCols(Datasets.loadSpamBase(), "0-4,spam");
        List<Frame> samples = randomSample(spam, new int[]{(int) (spam.getRowCount() * 0.6)});
        final Frame train = samples.get(0);
        final Frame test = samples.get(1);

        code("        RandomSource.setSeed(2718);\n"
                + "        final Frame spam = Datasets.loadSpamBase();\n"
                + "        List<Frame> samples = randomSample(spam, new int[]{(int) (spam.getRowCount() * 0.6)});\n"
                + "        final Frame train = samples.get(0);\n"
                + "        final Frame test = samples.get(1);\n");

        p("If you are not aware how the data for spam data looks like that what you will have to know is "
                + "that it consists of many numerical attributes used to predict a nominal "
                + "attribute called \"spam\"");

        summary(spam.getCol("spam"));

        p("Thus we know there are 2788 instances classified as ham, codified by value 0 (not spam), "
                + "and 1813 instances codified by 1, which denotes spam emails.");

        p("There are a lot of numeric features in this data set. We use "
                + "only the first 5 numerical features for prediction.");

        summary(spam);

        final List<String> clsLabels = new ArrayList<>();
        final List<ROC> clsRocs = new ArrayList<>();

        final OneRule oneRule = new OneRule();
        oneRule.learn(train, "spam");
        oneRule.predict(test);
        clsLabels.add("oneRule");
        clsRocs.add(new ROC(oneRule.getPrediction(), test.getCol("spam"), "1"));

        final RandomForest rf = new RandomForest() {
            {
                setMtrees(50);
            }
        };
        rf.learn(train, "spam");
        rf.predict(test);
        clsLabels.add("rf");
        clsRocs.add(new ROC(rf.getDistribution().getCol("1"), test.getCol("spam"), "1"));

        final AdaBoostM1 ab = new AdaBoostM1(new DecisionStump(), 50);
        ab.learn(train, "spam");
        ab.predict(test);
        clsLabels.add("ab");
        clsRocs.add(new ROC(ab.getDistribution().getCol("1"), test.getCol("spam"), "1"));

        draw(new Plot() {
            {
                for (int i = 0; i < clsLabels.size(); i++) {
                    final int color = i + 1;
                    new ROCCurve(this, clsRocs.get(i)) {
                        {
                            opt().setColorIndex(color);
                        }
                    };
                }
            }
        }, 600, 400);

        for (int i = 0; i < clsLabels.size(); i++) {
//            System.out.println(clsLabels.get(i));
            clsRocs.get(i).summary();
//            Summary.head(clsRocs.get(i).getData().getRowCount(), clsRocs.get(i).getData());
        }
    }
}
