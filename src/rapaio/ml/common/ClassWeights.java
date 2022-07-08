/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.common;

import java.util.HashMap;
import java.util.Map;

import rapaio.core.tools.DensityVector;
import rapaio.data.Var;

/**
 * Helper class for class weights used in some classifiers
 */
public final class ClassWeights {

    public static ClassWeights of(String c1, double w1) {
        return new ClassWeights(Map.of(c1, w1));
    }

    public static ClassWeights of(String c1, double w1, String c2, double w2) {
        return new ClassWeights(Map.of(c1, w1, c2, w2));
    }

    public static ClassWeights of(String c1, double w1, String c2, double w2, String c3, double w3) {
        return new ClassWeights(Map.of(c1, w1, c2, w2, c3, w3));
    }

    public static ClassWeights of(String c1, double w1, String c2, double w2, String c3, double w3, String c4, double w4) {
        return new ClassWeights(Map.of(c1, w1, c2, w2, c3, w3, c4, w4));
    }

    public static ClassWeights of(String c1, double w1, String c2, double w2, String c3, double w3, String c4, double w4, String c5,
            double w5) {
        return new ClassWeights(Map.of(c1, w1, c2, w2, c3, w3, c4, w4, c5, w5));
    }

    public static ClassWeights instanceCount(Var target) {
        var dv = DensityVector.fromLevelCounts(false, target);
        HashMap<String, Double> map = new HashMap<>();
        for(String label : dv.index().getValues()) {
            map.put(label, dv.get(label));
        }
        return new ClassWeights(map);
    }

    private final Map<String, Double> classWeights;

    private ClassWeights(Map<String, Double> classWeights) {
        this.classWeights = classWeights;
    }

    public Map<String, Double> getWeights() {
        return classWeights;
    }
}
