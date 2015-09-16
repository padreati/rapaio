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
 *
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
public class CTreeTestCounter implements Serializable {

    private static final long serialVersionUID = 4088420847369668548L;
    private Map<String, Integer> counters = new ConcurrentHashMap<>();
    private final int maxNum;
    private final int maxNom;

    public CTreeTestCounter(int maxNum, int maxNom) {
        this.maxNum = maxNum;
        this.maxNom = maxNom;
    }

    public String name() {
        return "CTreeTestCounter(maxNom=" + maxNom + ",maxNum=" + maxNum + ")";
    }

    void initialize(Frame df, String... inputNames) {
        counters = new HashMap<>();
        Arrays.stream(inputNames).forEach(colName -> {
            if (df.var(colName).type().isNominal()) {
                counters.put(colName, maxNom);
            } else {
                counters.put(colName, maxNum);
            }
        });
    }

    public boolean canUse(String name) {
        return counters.containsKey(name) && counters.get(name) > 0;
    }

    public void use(String name) {
        if (!counters.containsKey(name))
            throw new IllegalArgumentException("can't mark a column for use as a test if it was not initialized");
        counters.put(name, counters.get(name) - 1);
    }

    void free(String name) {
        if (!counters.containsKey(name)) {
            throw new IllegalArgumentException("can't mark a column for use as a test if it was not initialized");
        }
        counters.put(name, counters.get(name) + 1);
    }
}