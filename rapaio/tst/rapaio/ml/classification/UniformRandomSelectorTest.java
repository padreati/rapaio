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
package rapaio.ml.classification;

import org.junit.Test;
import rapaio.core.ColRange;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Vector;
import rapaio.data.Vectors;
import rapaio.ml.classification.colselect.ColSelector;
import rapaio.ml.classification.colselect.RandomColSelector;

import java.util.HashMap;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class UniformRandomSelectorTest {

    @Test
    public void testNaive() {
        Frame df = new SolidFrame(
                0,
                new Vector[]{
                        Vectors.newIdxOne(1),
                        Vectors.newIdxOne(1),
                        Vectors.newIdxOne(1),
                        Vectors.newIdxOne(1),
                        Vectors.newIdxOne(1),
                        Vectors.newIdxOne(1),
                        Vectors.newIdxOne(1),
                        Vectors.newIdxOne(1),
                        Vectors.newIdxOne(1)},
                new String[]{
                        "a", "b", "c", "d", "e", "f", "g", "h", "class"
                });
        String classColName = "class";
        int mcols = 4;

        ColSelector colSelector = new RandomColSelector(df, new ColRange(classColName), mcols);

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
