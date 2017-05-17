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

package rapaio.core.stat;

import rapaio.data.Var;
import rapaio.printer.Printable;

import java.util.Arrays;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Modes implements Printable {

    public static Modes from(Var var, boolean includeMissing) {
        return new Modes(var, includeMissing);
    }

    private final String varName;
    private final boolean includeMissing;
    private final String[] modes;
    private int completeCount;
    private int missingCount;

    private Modes(Var var, boolean includeMissing) {
        this.varName = var.getName();
        this.includeMissing = includeMissing;
        this.modes = compute(var);
    }

    private String[] compute(Var var) {
        if (!var.getType().isNominal()) {
            throw new IllegalArgumentException("Can't compute mode for other than nominal vectors");
        }
        int[] freq = new int[var.getLevels().length];
        for (int i = 0; i < var.getRowCount(); i++) {
            if (var.isMissing(i)) {
                missingCount++;
                continue;
            }
            freq[var.getIndex(i)]++;
        }
        completeCount = var.getRowCount() - missingCount;
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
                modes[pos++] = var.getLevels()[i];
            }
        }
        return modes;
    }

    public String[] getValues() {
        return modes;
    }

    @Override
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\n > modes[%s]\n", varName));
        sb.append(String.format("total rows: %d (complete: %d, missing: %d)\n", completeCount + missingCount, completeCount, missingCount));
        sb.append(String.format("modes: %s\n", Arrays.deepToString(modes)));
        return sb.toString();
    }
}
