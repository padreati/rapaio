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

package rapaio.ml.regressor.tree.rtree;

import rapaio.data.Frame;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/24/14.
 */
public interface RTreeTestCounter {

    String name();

    void initialize(Frame df, String targetName);

    boolean canUse(String name);

    void markUse(String name);

    public RTreeTestCounter M_NOMINAL_M_NUMERIC = new RTreeTestCounter() {

        private Map<String, Integer> counters = new HashMap<>();

        @Override
        public String name() {
            return "M_NOMINAL_M_NUMERIC";
        }

        public void initialize(Frame df, String targetName) {
            counters = new HashMap<>();
            Arrays.stream(df.varNames()).forEach(colName -> {
                if (targetName.equals(colName))
                    return;
                counters.put(colName, -1);
            });
        }

        public boolean canUse(String name) {
            return counters.containsKey(name);
        }

        public void markUse(String name) {
            if (!counters.containsKey(name))
                throw new IllegalArgumentException("can't mark a column for use as a test if it was not initialized");
        }
    };

    public RTreeTestCounter ONE_NOMINAL_M_NUMERIC = new RTreeTestCounter() {

        private Map<String, Integer> counters = new HashMap<>();

        @Override
        public String name() {
            return "ONE_NOMINAL_M_NUMERIC";
        }

        @Override
        public void initialize(Frame df, String targetName) {
            counters = new HashMap<>();
            Arrays.stream(df.varNames()).forEach(colName -> {
                if (targetName.equals(colName))
                    return;
                if (df.var(colName).type().isNominal()) {
                    counters.put(colName, 1);
                } else {
                    counters.put(colName, -1);
                }
            });
        }

        @Override
        public boolean canUse(String name) {
            return counters.containsKey(name) && counters.get(name) != 0;
        }

        @Override
        public void markUse(String name) {
            if (!counters.containsKey(name))
                throw new IllegalArgumentException("can't mark a column for use as a test if it was not initialized");
            int count = counters.get(name);
            if (count <= 0) return;
            counters.put(name, count - 1);
        }
    };

    RTreeTestCounter ONE_NOMINAL_ONE_NUMERIC = new RTreeTestCounter() {

        private Map<String, Integer> counters = new HashMap<>();

        @Override
        public String name() {
            return "ONE_NOMINAL_ONE_NUMERIC";
        }

        @Override
        public void initialize(Frame df, String targetName) {
            counters = new HashMap<>();
            Arrays.stream(df.varNames()).forEach(colName -> {
                if (targetName.equals(colName))
                    return;
                counters.put(colName, 1);
            });
        }

        @Override
        public boolean canUse(String name) {
            return counters.containsKey(name) && counters.get(name) != 0;
        }

        @Override
        public void markUse(String name) {
            if (!counters.containsKey(name))
                throw new IllegalArgumentException("can't mark a column for use as a test if it was not initialized");
            int count = counters.get(name);
            if (count <= 0) return;
            counters.put(name, count - 1);
        }
    };
}
