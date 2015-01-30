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

package rapaio.ml.common;

import rapaio.core.sample.SamplingTool;
import rapaio.data.Frame;
import rapaio.data.VarRange;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public interface VarSelector extends Serializable {

    String name();

    VarSelector newInstance();

    void initialize(Frame df, VarRange except);

    String[] nextVarNames();

    // built-in instances

    public static final class Standard implements VarSelector {

        private String[] selection;

        public String name() {
            return "Std";
        }

        @Override
        public VarSelector newInstance() {
            return new Standard();
        }

        @Override
        public synchronized void initialize(Frame df, VarRange except) {

            String[] all = df.varNames();
            List<Integer> ex = except == null ? new ArrayList<>() : except.parseVarIndexes(df);
            selection = new String[all.length - ex.size()];
            int p = 0;
            int s = 0;
            for (int i = 0; i < all.length; i++) {
                if (p < ex.size() && i == ex.get(p)) {
                    p++;
                    continue;
                }
                selection[s++] = all[i];
            }
        }

        @Override
        public String[] nextVarNames() {
            return selection;
        }
    }

    public static final class Random implements VarSelector {

        private final int mVars;
        private String[] candidates;

        public Random() {
            this.mVars = -1;
        }

        public Random(int mVars) {
            this.mVars = mVars;
        }

        @Override
        public VarSelector newInstance() {
            return new Random();
        }

        public String name() {
            return "Random[" + mVars + "]";
        }

        public synchronized void initialize(Frame df, VarRange except) {
            List<Integer> exceptColumns = except == null ? new ArrayList<>() : except.parseVarIndexes(df);
            candidates = new String[df.varCount() - exceptColumns.size()];
            int pos = 0;
            int expos = 0;
            for (int i = 0; i < df.varCount(); i++) {
                if (expos < exceptColumns.size() && i == exceptColumns.get(expos)) {
                    expos++;
                    continue;
                }
                candidates[pos++] = df.varNames()[i];
            }
        }

        @Override
        public synchronized String[] nextVarNames() {
            int m = (mVars < 1) ? Math.max((int) Math.sqrt(candidates.length), 1) : mVars;
            int[] indexes = SamplingTool.sampleWR(m, candidates.length);
            String[] result = new String[m];
            for (int i = 0; i < indexes.length; i++) {
                result[i] = candidates[indexes[i]];
            }
            return result;
        }
    }
}
