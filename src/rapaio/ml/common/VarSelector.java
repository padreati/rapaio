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

package rapaio.ml.common;

import rapaio.core.sample.SamplingTool;

import java.io.Serializable;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public interface VarSelector extends Serializable {

    String name();

    VarSelector newInstance();

    void initialize(String... varNames);

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
        public void initialize(String... varNames) {
            selection = varNames;
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
            Random vs = new Random(mVars);
            vs.initialize(candidates);
            return vs;
        }

        public String name() {
            return "Random[" + mVars + "]";
        }

        public void initialize(final String... varNames) {
            candidates = varNames;
        }

        @Override
        public String[] nextVarNames() {
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
