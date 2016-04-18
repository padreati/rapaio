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

package rapaio.data.sample;

import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.Index;
import rapaio.data.SolidFrame;
import rapaio.ml.common.VarSelector;

import java.util.HashMap;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
@Deprecated
public class UniformRandomSelectorTest {

    @Test
    public void testNaive() {
        Frame df = SolidFrame.newByVars(
                0,
                Index.scalar(1).withName("a"),
                Index.scalar(1).withName("b"),
                Index.scalar(1).withName("c"),
                Index.scalar(1).withName("d"),
                Index.scalar(1).withName("e"),
                Index.scalar(1).withName("f"),
                Index.scalar(1).withName("g"),
                Index.scalar(1).withName("h"),
                Index.scalar(1).withName("class"));
        String classColName = "class";
        int mcols = 4;

        VarSelector colSelector = new VarSelector(mcols);
        colSelector.withVarNames("a", "b", "c", "d", "e", "f", "g", "h");

        final int TESTS = 10_000;
        HashMap<String, Integer> counter = new HashMap<>();
        for (int i = 0; i < TESTS; i++) {
            String[] selection = colSelector.nextVarNames();
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
