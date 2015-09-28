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

package rapaio.ml.classifier.rule;

import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.data.*;
import rapaio.datasets.Datasets;
import rapaio.io.JavaIO;
import rapaio.ml.classifier.CFit;
import rapaio.sys.WS;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class OneRuleTest {

    private static final int SIZE = 6;

    private final Var classVar;
    private final Var heightVar;

    public OneRuleTest() {
        classVar = Nominal.newEmpty(SIZE, "False", "True").withName("class");
        classVar.setLabel(0, "True");
        classVar.setLabel(1, "True");
        classVar.setLabel(2, "True");
        classVar.setLabel(3, "False");
        classVar.setLabel(4, "False");
        classVar.setLabel(5, "False");

        heightVar = Numeric.newCopyOf(0.1, 0.3, 0.5, 10, 10.3, 10.5).withName("height");
    }

    @Test
    public void testSimpleNumeric() {
        Frame df = SolidFrame.newWrapOf(SIZE, heightVar, classVar);

        String[] labels;
        OneRule oneRule = new OneRule();

        oneRule = oneRule.withMinCount(1);
        oneRule.learn(df, "class");
        CFit pred = oneRule.fit(df);
        labels = new String[]{"True", "True", "True", "False", "False", "False"};
        for (int i = 0; i < SIZE; i++) {
            assertEquals(labels[i], pred.firstClasses().label(i));
        }

        oneRule.withMinCount(2);
        oneRule.learn(df, "class");
        pred = oneRule.fit(df);
        labels = new String[]{"True", "True", "TrueFalse", "TrueFalse", "False", "False"};
        for (int i = 0; i < SIZE; i++) {
            assertTrue(labels[i].contains(pred.firstClasses().label(i)));
        }

        oneRule.withMinCount(3);
        oneRule.learn(df, "class");
        pred = oneRule.fit(df);
        labels = new String[]{"True", "True", "True", "False", "False", "False"};
        for (int i = 0; i < SIZE; i++) {
            assertTrue(labels[i].equals(pred.firstClasses().label(i)));
        }

        oneRule.withMinCount(4);
        oneRule.learn(df, "class");
        pred = oneRule.fit(df);
        for (int i = 1; i < SIZE; i++) {
            assertTrue(pred.firstClasses().label(i).equals(pred.firstClasses().label(0)));
        }
    }

    @Test
    public void testSummary() throws IOException, URISyntaxException {
        Frame df1 = Datasets.loadIrisDataset();
        OneRule oneRule1 = new OneRule();
        oneRule1.learn(df1, "class");

        assertEquals("OneRule model\n" +
                "================\n" +
                "\n" +
                "Description:\n" +
                "OneRule (minCount=6)\n" +
                "\n" +
                "Capabilities:\n" +
                "learning type: MULTICLASS_CLASSIFIER\n" +
                "inputTypes: BINARY,INDEX,NOMINAL,NUMERIC,ORDINAL,STAMP\n" +
                "minInputCount: 1, maxInputCount: 1000000\n" +
                "allowMissingInputValues: true\n" +
                "targetTypes: NOMINAL\n" +
                "minTargetCount: 1, maxTargetCount: 1\n" +
                "allowMissingTargetValues: false\n" +
                "\n" +
                "Learned model:\n" +
                "input vars: \n" +
                "> sepal-length : NUMERIC\n" +
                "> sepal-width : NUMERIC\n" +
                "> petal-length : NUMERIC\n" +
                "> petal-width : NUMERIC\n" +
                "target vars:\n" +
                "> class : NOMINAL [?,Iris-setosa,Iris-versicolor,Iris-virginica]\n" +
                "BestRuleSet {var=petal-length, acc=0.9533333}\n" +
                "> NumericRule {min=-Infinity, max=2.45, class=Iris-setosa, errors=0, total=50, acc=1 }\n" +
                "> NumericRule {min=2.45, max=4.75, class=Iris-versicolor, errors=1, total=45, acc=0.9777778 }\n" +
                "> NumericRule {min=4.75, max=Infinity, class=Iris-virginica, errors=6, total=55, acc=0.8909091 }\n" +
                "\n", oneRule1.summary());

        oneRule1.printSummary();

        Frame df2 = Datasets.loadMushrooms();

        RandomSource.setSeed(1);
        OneRule oneRule2 = new OneRule();
        oneRule2.learn(df2, "classes");

        oneRule2.printSummary();

        assertEquals("OneRule model\n" +
                "================\n" +
                "\n" +
                "Description:\n" +
                "OneRule (minCount=6)\n" +
                "\n" +
                "Capabilities:\n" +
                "learning type: MULTICLASS_CLASSIFIER\n" +
                "inputTypes: BINARY,INDEX,NOMINAL,NUMERIC,ORDINAL,STAMP\n" +
                "minInputCount: 1, maxInputCount: 1000000\n" +
                "allowMissingInputValues: true\n" +
                "targetTypes: NOMINAL\n" +
                "minTargetCount: 1, maxTargetCount: 1\n" +
                "allowMissingTargetValues: false\n" +
                "\n" +
                "Learned model:\n" +
                "input vars: \n" +
                "> cap-shape : NOMINAL\n" +
                "> cap-surface : NOMINAL\n" +
                "> cap-color : NOMINAL\n" +
                "> bruises : NOMINAL\n" +
                "> odor : NOMINAL\n" +
                "> gill-attachment : NOMINAL\n" +
                "> gill-spacing : NOMINAL\n" +
                "> gill-size : NOMINAL\n" +
                "> gill-color : NOMINAL\n" +
                "> stalk-shape : NOMINAL\n" +
                "> stalk-root : NOMINAL\n" +
                "> stalk-surface-above-ring : NOMINAL\n" +
                "> stalk-surface-below-ring : NOMINAL\n" +
                "> stalk-color-above-ring : NOMINAL\n" +
                "> stalk-color-below-ring : NOMINAL\n" +
                "> veil-type : NOMINAL\n" +
                "> veil-color : NOMINAL\n" +
                "> ring-number : NOMINAL\n" +
                "> ring-type : NOMINAL\n" +
                "> spore-print-color : NOMINAL\n" +
                "> population : NOMINAL\n" +
                "> habitat : NOMINAL\n" +
                "target vars:\n" +
                "> classes : NOMINAL [?,p,e]\n" +
                "BestRuleSet {var=odor, acc=0.985229}\n" +
                "> NominalRule {value=?, class=e, errors=0, total=0, acc=0}\n" +
                "> NominalRule {value=p, class=p, errors=0, total=256, acc=1}\n" +
                "> NominalRule {value=a, class=e, errors=0, total=400, acc=1}\n" +
                "> NominalRule {value=l, class=e, errors=0, total=400, acc=1}\n" +
                "> NominalRule {value=n, class=e, errors=120, total=3,528, acc=0.9659864}\n" +
                "> NominalRule {value=f, class=p, errors=0, total=2,160, acc=1}\n" +
                "> NominalRule {value=c, class=p, errors=0, total=192, acc=1}\n" +
                "> NominalRule {value=y, class=p, errors=0, total=576, acc=1}\n" +
                "> NominalRule {value=s, class=p, errors=0, total=576, acc=1}\n" +
                "> NominalRule {value=m, class=p, errors=0, total=36, acc=1}\n" +
                "\n", oneRule2.summary());
    }

    @Test
    public void testFit() throws IOException, URISyntaxException {

        Frame df1 = Datasets.loadMushrooms();
        OneRule oneRule1 = new OneRule();
        oneRule1.learn(df1, "classes");

        oneRule1.printSummary();
        CFit fit1 = oneRule1.fit(df1, true, true);
        fit1.printSummary();


        Frame df2 = Datasets.loadIrisDataset();
        OneRule oneRule2 = new OneRule();
        oneRule2.learn(df2, "class");

        oneRule2.printSummary();
        CFit fit2 = oneRule2.fit(df2, true, true);
        fit2.printSummary();


    }

}
