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

import rapaio.ml.classifier.boost.AdaBoostSAMMEClassifier;
import rapaio.ml.classifier.rule.OneRule;
import rapaio.core.RandomSource;
import rapaio.core.stat.ConfusionMatrix;
import rapaio.core.stat.ROC;
import rapaio.data.Frame;
import rapaio.data.filters.BaseFilters;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Legend;
import rapaio.graphics.plot.ROCCurve;
//import rapaio.ml.tree.RandomForestClassifier;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static rapaio.core.sample.StatSampling.randomSample;
import static rapaio.workspace.Summary.summary;
import static rapaio.workspace.W.*;

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
        RandomSource.setSeed(1234);

        heading(2, "Data set preparation");

        p("For exemplification I will use the classical spam data set. We load the data set and "
                + "we split randomly in two pieces. The first sample will be used for training purposes "
                + "and it will have ~ 0.66 of the data, the second sample will be used for "
                + "testing our model. ");

        RandomSource.setSeed(2718);
        final Frame spam = BaseFilters.retainCols(Datasets.loadSpamBase(), "0-4,spam");
        List<Frame> samples = randomSample(spam, new int[]{(int) (spam.rowCount() * 0.6)});
        final Frame train = samples.get(0);
        final Frame test = samples.get(1);

        code("        RandomSource.setSeed(2718);\n" +
                "        final Frame spam = ColFilters.retainCols(Datasets.loadSpamBase(), \"0-4,spam\");\n" +
                "        List<Frame> samples = randomSample(spam, new int[]{(int) (spam.getRowCount() * 0.6)});\n" +
                "        final Frame train = samples.get(0);\n" +
                "        final Frame test = samples.get(1);\n");

        p("If you are not aware how the data for spam data looks like that what you will have to know is "
                + "that it consists of many numerical attributes used to predict a nominal "
                + "attribute called \\(spam\\)");

        p("Thus we know there are 2788 instances classified as \\(ham\\), codified by value 0 (\\(not spam\\)), "
                + "and 1813 instances codified by 1, which denotes spam emails. There are a lot of "
                + "numeric features in this data set. We use "
                + "only the first 5 numerical features for prediction.");

        summary(spam);

        p("Now we can do some predictions.");

        heading(2, "Binary classification");

        p("We will learn 3 models for prediction. We will use the train test which consists "
                + "of 66% percents of our initial data. For testing how well the model predicts "
                + "we use the remaining data.");

        heading(4, "OneRule");
        p("This first model is one of the simplest model possible. It basically learn a decision tree "
                + "with a single level. For documentation obout this algorithm you can "
                + "check the original paper <a href=\"http://link.springer.com/article/10.1023/A:1022631118932\">"
                + "Holte, R.C. Very Simple Classification Rules Perform Well on Most Commonly Used Datasets. "
                + "Machine Learning 11, 63-91 (1993).</a>");

        code("        OneRule oneRule = new OneRule();\n" +
                "        oneRule.learn(train, \"spam\");\n" +
                "        oneRule.predict(test);\n");

        OneRule oneRule = new OneRule();
        oneRule.learn(train, "spam");
        oneRule.predict(test);

        p("One of the most used ways to check the performance of a classifier is the accuracy. "
                + "Accuracy is the percentage of cases with correct prediction from total number of cases. "
                + "With rapaio library one way to see the accuracy is to summarize the confusion rapaio.data.matrix.");

        code("        new ConfusionMatrix(test.getCol(\"spam\"), oneRule.getPrediction()).summary();\n");

        new ConfusionMatrix(test.col("spam"), oneRule.pred()).summary();

        heading(4, "Random Forest");

        p("The second prediction model is a random forest with 200 random trees. ");

//        RandomForestClassifier rf = new RandomForestClassifier().setMtrees(200);
//        rf.learn(train, "spam");
//        rf.predict(test);
//
//        code("        RandomForestClassifier rf = new RandomForestClassifier().setMtrees(200);\n" +
//                "        rf.learn(train, \"spam\");\n" +
//                "        rf.predict(test);\n");
//
//        new ConfusionMatrix(test.col("spam"), rf.prediction()).summary();

        heading(4, "AdaBoost.SAMME");

        p("The third prediction model is a boosting algorithm called AdaBoost.SAMME. " +
                "This model uses decision stumps as a weak learners, " +
                "and builds 200 boosting iterations. "
                + "The following code shows how one can achieve that using rapaio.");

        AdaBoostSAMMEClassifier ab = new AdaBoostSAMMEClassifier().withRuns(200);
        ab.learn(train, "spam");
        ab.predict(test);

        code("        AdaBoostSAMME ab = new AdaBoostSAMME(new DecisionStumpClassifier(), 200);\n" +
                "        ab.learn(train, \"spam\");\n" +
                "        ab.predict(test);\n");

        new ConfusionMatrix(test.col("spam"), ab.pred()).summary();

        heading(2, "ROC Curves");

        p("When accuracy is used to compare the performance of some classifiers it is very often "
                + "the case that the comparison is misleading. That happens because accuracy "
                + "is a measure which depends on many factors which pose some assumptions "
                + "which are not always true. ");

        p("I will not explain what a ROC graph is. There is enought literature on this topic. "
                + "Among many useful documents, I found one which gives crystal clear details "
                + "and explanations on ROC curves: <a href=\"http://binf.gmu.edu/mmasso/ROC101.pdf\">"
                + "Fawcett, T. (2004). ROC graphs: Notes and practical considerations for researchers. "
                + "Machine Learning.</a>");

        p("In order to draw ROC graphs for the previous models with rapaio you can use the "
                + "ROCCurve plot component which builds and draws a curve according with "
                + "a given computed ROC object. The following code does this.");

        ROC rocOR = new ROC(oneRule.pred(), test.col("spam"), "1");
//        ROC rocRF = new ROC(rf.distribution().col("1"), test.col("spam"), "1");
        ROC rocAB = new ROC(ab.dist().col("1"), test.col("spam"), "1");
        draw(new Plot()
                        .add(new ROCCurve(rocOR).setCol(1))
//                        .add(new ROCCurve(rocRF).setCol(2))
                        .add(new ROCCurve(rocAB).setCol(3))
                        .add(new Legend(0.6, 0.33,
                                new String[]{"onerule", "rf", "adaboost.m1"},
                                new int[]{1, 2, 3})),
                600, 400
        );

        code("        ROC rocOR = new ROC(oneRule.getPrediction(), test.getCol(\"spam\"), \"1\");\n" +
                "        ROC rocRF = new ROC(rf.getDistribution().getCol(\"1\"), test.getCol(\"spam\"), \"1\");\n" +
                "        ROC rocAB = new ROC(ab.getDistribution().getCol(\"1\"), test.getCol(\"spam\"), \"1\");\n" +
                "        draw(new Plot()\n" +
                "                .add(new ROCCurve(rocOR).setColorIndex(1))\n" +
                "                .add(new ROCCurve(rocRF).setColorIndex(2))\n" +
                "                .add(new ROCCurve(rocAB).setColorIndex(3))\n" +
                "                .add(new Legend(0.6, 0.33,\n" +
                "                        new String[]{\"onerule\", \"rf\", \"adaboost.m1\"},\n" +
                "                        new int[]{1, 2, 3})),\n" +
                "                600, 400);\n");

        p("As you can see, ROC objects are used to compute values for ROC curves, and "
                + "ROCCurve plot is used to add these on a plot graphic. ");

        p("Note however, that Random Forst model used exhibits a ROC graph which is "
                + "better than adaboost model most of the times in the conservative "
                + "area of the graph. AdaBoost tends to be a little better in the "
                + "liberal area, but in the extreme liberal area, again the random "
                + "forest model exhibits better performance. ");
        p("OneRule behaves sub-optimal, as it was expected in this specific case. ");

        p(">>>This tutorial is generated with Rapaio document printer facilities.<<<");
    }
}
