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

package rapaio.ml.rule;

import org.junit.Assert;
import org.junit.Test;
import rapaio.data.*;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class OneRuleTest {

    private static final int SIZE = 6;

    private final Vector classVector;
    private final Vector heightVector;

    public OneRuleTest() {
        classVector = new Nominal(SIZE, new String[]{"False", "True"});
        classVector.setLabel(0, "True");
        classVector.setLabel(1, "True");
        classVector.setLabel(2, "True");
        classVector.setLabel(3, "False");
        classVector.setLabel(4, "False");
        classVector.setLabel(5, "False");

        heightVector = new Numeric(new double[]{0.1, 0.3, 0.5, 10, 10.3, 10.5});
    }

    @Test
    public void testSimpleNumeric() {
        Frame df = new SolidFrame(SIZE, new Vector[]{heightVector, classVector}, new String[]{"height", "class"});

        String[] labels;
        OneRule oneRule = new OneRule();

        oneRule = oneRule.setMinCount(1);
        oneRule.learn(df, "class");
        oneRule.predict(df);
        labels = new String[]{"True", "True", "True", "False", "False", "False"};
        for (int i = 0; i < SIZE; i++) {
            Assert.assertEquals(labels[i], oneRule.prediction().getLabel(i));
        }

        oneRule.setMinCount(2);
        oneRule.learn(df, "class");
        oneRule.predict(df);
        labels = new String[]{"True", "True", "TrueFalse", "TrueFalse", "False", "False"};
        for (int i = 0; i < SIZE; i++) {
            Assert.assertTrue(labels[i].contains(oneRule.prediction().getLabel(i)));
        }

        oneRule.setMinCount(3);
        oneRule.learn(df, "class");
        oneRule.predict(df);
        labels = new String[]{"True", "True", "True", "False", "False", "False"};
        for (int i = 0; i < SIZE; i++) {
            Assert.assertTrue(labels[i].equals(oneRule.prediction().getLabel(i)));
        }

        oneRule.setMinCount(4);
        oneRule.learn(df, "class");
        oneRule.predict(df);
        for (int i = 1; i < SIZE; i++) {
            Assert.assertTrue(oneRule.prediction().getLabel(i).equals(oneRule.prediction().getLabel(0)));
        }
    }
}
