/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.ml.eval.metric;

import org.junit.Assert;
import org.junit.Test;
import rapaio.core.stat.*;
import rapaio.data.*;
import rapaio.datasets.*;
import rapaio.experiment.ml.eval.metric.*;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Test for roc utility.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/26/16.
 */
public class ROCTest {

    @Test
    public void testRoc() throws IOException, URISyntaxException {

        Frame df = Datasets.loadIrisDataset();

        Var score = df.rvar(0);
        Var clazz = df.rvar("class");

        ROC roc = ROC.from(score, clazz, 3);
        Assert.assertEquals("> ROC printSummary\n" +
                        "\n" +
                        "threshold , fpr       , tpr       , acc       \n" +
                        "Infinity  , 0         , 0         , 0.6666667 \n" +
                        "7.6       , 0         , 0.12      , 0.7066667 \n" +
                        "7.1       , 0         , 0.24      , 0.7466667 \n" +
                        "6.8       , 0.03      , 0.34      , 0.76      \n" +
                        "6.4       , 0.11      , 0.62      , 0.8       \n" +
                        "6         , 0.24      , 0.86      , 0.7933333 \n" +
                        "5.7       , 0.37      , 0.96      , 0.74      \n" +
                        "5.3       , 0.56      , 0.98      , 0.62      \n" +
                        "5         , 0.79      , 0.98      , 0.4666667 \n" +
                        "4.6       , 0.95      , 1         , 0.3666667 \n" +
                        "4.3       , 1         , 1         , 0.3333333 \n" +
                        "\n" +
                        "AUC: 0.8871\n",
                roc.summary());

        Assert.assertEquals(0.8871, roc.auc(), 1e-20);


        double midValue = Mean.of(score).value();
        int midRow = roc.findRowForThreshold(midValue);

        Assert.assertEquals(0.3, roc.data().getDouble(midRow, ROC.fpr), 1e-20);
        Assert.assertEquals(0.94, roc.data().getDouble(midRow, ROC.tpr), 1e-20);

        VarNominal pred = VarNominal.from(df.rowCount(), row -> row % 2 == 0 ? "virginica" : "setosa");
        Assert.assertEquals("> ROC printSummary\n" +
                        "\n" +
                        "threshold , fpr       , tpr       , acc       \n" +
                        "Infinity  , 0         , 0         , 0.6666667 \n" +
                        "7.6       , 0.04      , 0.04      , 0.6533333 \n" +
                        "7.1       , 0.08      , 0.08      , 0.64      \n" +
                        "6.8       , 0.14      , 0.12      , 0.6133333 \n" +
                        "6.4       , 0.27      , 0.3       , 0.5866667 \n" +
                        "6         , 0.45      , 0.44      , 0.5133333 \n" +
                        "5.7       , 0.6       , 0.5       , 0.4333333 \n" +
                        "5.3       , 0.77      , 0.56      , 0.34      \n" +
                        "5         , 0.89      , 0.78      , 0.3333333 \n" +
                        "4.6       , 0.97      , 0.96      , 0.34      \n" +
                        "4.3       , 1         , 1         , 0.3333333 \n" +
                        "\n" +
                        "AUC: 0.4445\n",
                ROC.from(score, clazz, pred).summary());

    }
}
