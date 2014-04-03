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

package rapaio.core.stat;

import rapaio.core.Printable;
import rapaio.data.Vector;

import java.util.Arrays;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Mode implements Printable {

    private final Vector vector;
    private final boolean includeMissing;
    private final String[] modes;

    public Mode(Vector vector, boolean includeMissing) {
        this.vector = vector;
        this.includeMissing = includeMissing;
        this.modes = compute();
    }

    private String[] compute() {
        if (!vector.type().isNominal()) {
            throw new IllegalArgumentException("Can't compute mode for other than nominal vectors");
        }
        int[] freq = new int[vector.getDictionary().length];
        for (int i = 0; i < vector.rowCount(); i++) {
            if (vector.isMissing(i)) {
                continue;
            }
            freq[vector.getIndex(i)]++;
        }
        int max = 0;
        int start = includeMissing ? 0 : 1;
        for (int i = start; i < freq.length; i++) {
            max = Math.max(max, freq[i]);
        }
        int count = 0;
        for (int i = start; i < freq.length; i++) {
            if (freq[i] == max) {
                count++;
            }
        }
        int pos = 0;
        String[] modes = new String[count];
        for (int i = start; i < freq.length; i++) {
            if (freq[i] == max) {
                modes[pos++] = vector.getDictionary()[i];
            }
        }
        return modes;
    }

    public String[] getModes() {
        return modes;
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        sb.append(String.format("mode\n%s", Arrays.deepToString(modes)));
    }
}
