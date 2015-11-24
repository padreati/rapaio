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
import rapaio.ml.classifier.CFit;

import java.io.IOException;
import java.net.URISyntaxException;

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
        classVar = Nominal.empty(SIZE, "False", "True").withName("class");
        classVar.setLabel(0, "True");
        classVar.setLabel(1, "True");
        classVar.setLabel(2, "True");
        classVar.setLabel(3, "False");
        classVar.setLabel(4, "False");
        classVar.setLabel(5, "False");

        heightVar = Numeric.copy(0.1, 0.3, 0.5, 10, 10.3, 10.5).withName("height");
    }

    @Test
    public void testSimpleNumeric() {
        Frame df = SolidFrame.newWrapOf(SIZE, heightVar, classVar);

        String[] labels;
        OneRule oneRule = new OneRule();

        oneRule = oneRule.withMinCount(1);
        oneRule.train(df, "class");
        CFit pred = oneRule.fit(df);
        labels = new String[]{"True", "True", "True", "False", "False", "False"};
        for (int i = 0; i < SIZE; i++) {
            assertEquals(labels[i], pred.firstClasses().label(i));
        }

        oneRule.withMinCount(2);
        oneRule.train(df, "class");
        pred = oneRule.fit(df);
        labels = new String[]{"True", "True", "TrueFalse", "TrueFalse", "False", "False"};
        for (int i = 0; i < SIZE; i++) {
            assertTrue(labels[i].contains(pred.firstClasses().label(i)));
        }

        oneRule.withMinCount(3);
        oneRule.train(df, "class");
        pred = oneRule.fit(df);
        labels = new String[]{"True", "True", "True", "False", "False", "False"};
        for (int i = 0; i < SIZE; i++) {
            assertTrue(labels[i].equals(pred.firstClasses().label(i)));
        }

        oneRule.withMinCount(4);
        oneRule.train(df, "class");
        pred = oneRule.fit(df);
        for (int i = 1; i < SIZE; i++) {
            assertTrue(pred.firstClasses().label(i).equals(pred.firstClasses().label(0)));
        }
    }

    @Test
    public void testSummary() throws IOException, URISyntaxException {
        Frame df1 = Datasets.loadIrisDataset();
        OneRule oneRule1 = new OneRule();
        oneRule1.train(df1, "class");

        oneRule1.printSummary();
        assertEquals("OneRule model\n" +
                "================\n" +
                "\n" +
                "Description:\n" +
                "OneRule (minCount=6)\n" +
                "\n" +
                "Capabilities:\n" +
                "learning: MULTICLASS_CLASSIFIER\n" +
                "types inputs/targets: BINARY,INDEX,NOMINAL,NUMERIC,ORDINAL,STAMP/NOMINAL\n" +
                "counts inputs/targets: [1,1000000] / [1,1]\n" +
                "missing inputs/targets: true/false\n" +
                "\n" +
                "Learned model:\n" +
                "input vars: \n" +
                "\n" +
                " 0. sepal-length : NUMERIC  | 1. sepal-width : NUMERIC  | 2. petal-length : NUMERIC  | 3. petal-width : NUMERIC  |\n" +
                "\n" +
                "target vars:\n" +
                "> class : NOMINAL [?,setosa,versicolor,virginica]\n" +
                "BestRuleSet {var=petal-length, acc=0.9533333}\n" +
                "> NumericRule {min=-Infinity, max=2.45, class=setosa, errors=0, total=50, acc=1 }\n" +
                "> NumericRule {min=2.45, max=4.75, class=versicolor, errors=1, total=45, acc=0.9777778 }\n" +
                "> NumericRule {min=4.75, max=Infinity, class=virginica, errors=6, total=55, acc=0.8909091 }\n" +
                "\n", oneRule1.summary());

        oneRule1.printSummary();

        Frame df2 = Datasets.loadMushrooms();

        RandomSource.setSeed(1);
        OneRule oneRule2 = new OneRule();
        oneRule2.train(df2, "classes");

        oneRule2.printSummary();

        assertEquals("OneRule model\n" +
                "================\n" +
                "\n" +
                "Description:\n" +
                "OneRule (minCount=6)\n" +
                "\n" +
                "Capabilities:\n" +
                "learning: MULTICLASS_CLASSIFIER\n" +
                "types inputs/targets: BINARY,INDEX,NOMINAL,NUMERIC,ORDINAL,STAMP/NOMINAL\n" +
                "counts inputs/targets: [1,1000000] / [1,1]\n" +
                "missing inputs/targets: true/false\n" +
                "\n" +
                "Learned model:\n" +
                "input vars: \n" +
                "\n" +
                " 0.   cap-shape : NOMINAL  | 5. gill-attachment : NOMINAL  | 10.               stalk-root : NOMINAL  | 15.         veil-type : NOMINAL  | 20. population : NOMINAL  |\n" +
                " 1. cap-surface : NOMINAL  | 6.    gill-spacing : NOMINAL  | 11. stalk-surface-above-ring : NOMINAL  | 16.        veil-color : NOMINAL  | 21.    habitat : NOMINAL  |\n" +
                " 2.   cap-color : NOMINAL  | 7.       gill-size : NOMINAL  | 12. stalk-surface-below-ring : NOMINAL  | 17.       ring-number : NOMINAL  |                            \n" +
                " 3.     bruises : NOMINAL  | 8.      gill-color : NOMINAL  | 13.   stalk-color-above-ring : NOMINAL  | 18.         ring-type : NOMINAL  |                            \n" +
                " 4.        odor : NOMINAL  | 9.     stalk-shape : NOMINAL  | 14.   stalk-color-below-ring : NOMINAL  | 19. spore-print-color : NOMINAL  |                            \n" +
                "\n" +
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
        oneRule1.train(df1, "classes");

        oneRule1.printSummary();
        CFit fit1 = oneRule1.fit(df1, true, true);
        fit1.printSummary();


        Frame df2 = Datasets.loadIrisDataset();
        OneRule oneRule2 = new OneRule();
        oneRule2.train(df2, "class");

        oneRule2.printSummary();
        CFit fit2 = oneRule2.fit(df2, true, true);
        fit2.printSummary();


    }

}
