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
package rapaio.core;

import org.junit.Test;
import rapaio.core.stat.ConfusionMatrix;
import rapaio.data.NominalVector;
import rapaio.data.Vector;

/**
 *
 * @author tutuianu
 */
public class ConfusionMatrixTest {

    @Test
    public void hello() {
        Vector actual = new NominalVector(11, new String[]{"cat", "dog", "mouse"});
        Vector predict = new NominalVector(11, new String[]{"cat", "dog", "mouse"});

        actual.setLabel(0, "cat");
        predict.setLabel(0, "cat");
        
        actual.setLabel(1, "cat");
        predict.setLabel(1, "dog");
        
        actual.setLabel(2, "dog");
        predict.setLabel(2, "dog");
        
//        actual.setLabel(3, "dog");
//        predict.setLabel(3, "mouse");
//
        actual.setLabel(4, "mouse");
        predict.setLabel(4, "mouse");

        actual.setLabel(5, "mouse");
        predict.setLabel(5, "mouse");

        actual.setLabel(6, "mouse");
        predict.setLabel(6, "dog");

        actual.setLabel(7, "cat");
        predict.setLabel(7, "cat");

        actual.setLabel(8, "cat");
        predict.setLabel(8, "mouse");

        actual.setLabel(9, "mouse");
        predict.setLabel(9, "mouse");

        actual.setLabel(10, "mouse");
        predict.setLabel(10, "mouse");

        
        ConfusionMatrix cm = new ConfusionMatrix(actual, predict);
        cm.summary();
    }
}
