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

package rapaio.ml.classifier.tools;

import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.Index;
import rapaio.data.SolidFrame;
import rapaio.data.VarRange;
import rapaio.ml.refactor.colselect.ColSelector;
import rapaio.ml.refactor.colselect.RandomColSelector;

import java.util.HashMap;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class UniformRandomSelectorTest {

    @Test
    public void testNaive() {
        Frame df = SolidFrame.newWrapOf(
                0,
                Index.newScalar(1).withName("a"),
                Index.newScalar(1).withName("b"),
                Index.newScalar(1).withName("c"),
                Index.newScalar(1).withName("d"),
                Index.newScalar(1).withName("e"),
                Index.newScalar(1).withName("f"),
                Index.newScalar(1).withName("g"),
                Index.newScalar(1).withName("h"),
                Index.newScalar(1).withName("class"));
        String classColName = "class";
        int mcols = 4;

        ColSelector colSelector = new RandomColSelector(df, new VarRange(classColName), mcols);

        final int TESTS = 10_000;
        HashMap<String, Integer> counter = new HashMap<>();
        for (int i = 0; i < TESTS; i++) {
            String[] selection = colSelector.nextColNames();
            for (String sel : selection) {
                if (!counter.containsKey(sel)) {
                    counter.put(sel, 0);
                }
                counter.put(sel, counter.get(sel) + 1);
            }
        }
        final double[] freq = new double[counter.size()];
        int pos = 0;
        double total = 0;
        for (int value : counter.values()) {
            freq[pos++] = value;
            total += value;
        }
        for (int i = 0; i < freq.length; i++) {
            freq[i] /= total;
            System.out.println(String.format("%.6f", freq[i]));
        }

    }
}
