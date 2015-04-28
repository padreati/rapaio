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

package rapaio.ml.classifier.tree;

import rapaio.data.Frame;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public interface CTreeTestCounter extends Serializable {

    String name();

    void initialize(Frame df, String... inputNames);

    boolean canUse(String name);

    void markUse(String name);

    class MNominalMNumeric implements CTreeTestCounter {

        private Map<String, Integer> counters = new ConcurrentHashMap<>();

        @Override
        public String name() {
            return "MNominalMNumeric";
        }

        public void initialize(Frame df, String... inputNames) {
            counters = new ConcurrentHashMap<>();
            Arrays.stream(inputNames).forEach(colName -> counters.put(colName, -1));
        }

        public boolean canUse(String name) {
            return counters.containsKey(name);
        }

        public void markUse(String name) {
            if (!counters.containsKey(name))
                throw new IllegalArgumentException("can't mark a column for use as a test if it was not initialized");
        }
    }

    class OneNominalMNumeric implements CTreeTestCounter {

        private Map<String, Integer> counters = new HashMap<>();

        @Override
        public String name() {
            return "OneNominalMNumeric";
        }

        @Override
        public void initialize(Frame df, String... inputNames) {
            counters = new HashMap<>();
            Arrays.stream(inputNames).forEach(colName -> {
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
    }

    class OneNominalOneNumeric implements CTreeTestCounter {

        private Map<String, Integer> counters = new HashMap<>();

        @Override
        public String name() {
            return "OneNominalOneNumeric";
        }

        @Override
        public void initialize(Frame df, String... inputNames) {
            counters = new HashMap<>();
            Arrays.stream(inputNames).forEach(colName -> counters.put(colName, 1));
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
    }
}

