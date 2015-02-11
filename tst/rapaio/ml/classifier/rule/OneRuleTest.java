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
 */

package rapaio.ml.classifier.rule;

import org.junit.Assert;
import org.junit.Test;
import rapaio.data.*;
import rapaio.ml.classifier.CResult;

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
        CResult pred = oneRule.predict(df);
        labels = new String[]{"True", "True", "True", "False", "False", "False"};
        for (int i = 0; i < SIZE; i++) {
            Assert.assertEquals(labels[i], pred.firstClasses().label(i));
        }

        oneRule.withMinCount(2);
        oneRule.learn(df, "class");
        pred = oneRule.predict(df);
        labels = new String[]{"True", "True", "TrueFalse", "TrueFalse", "False", "False"};
        for (int i = 0; i < SIZE; i++) {
            Assert.assertTrue(labels[i].contains(pred.firstClasses().label(i)));
        }

        oneRule.withMinCount(3);
        oneRule.learn(df, "class");
        pred = oneRule.predict(df);
        labels = new String[]{"True", "True", "True", "False", "False", "False"};
        for (int i = 0; i < SIZE; i++) {
            Assert.assertTrue(labels[i].equals(pred.firstClasses().label(i)));
        }

        oneRule.withMinCount(4);
        oneRule.learn(df, "class");
        pred = oneRule.predict(df);
        for (int i = 1; i < SIZE; i++) {
            Assert.assertTrue(pred.firstClasses().label(i).equals(pred.firstClasses().label(0)));
        }
    }
}
