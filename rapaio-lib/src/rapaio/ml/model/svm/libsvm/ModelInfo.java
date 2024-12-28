/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.ml.model.svm.libsvm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rapaio.darray.DArray;

public class ModelInfo {

    final ProblemInfo pi;

    /**
     * A map of support vectors, indexes by the position in the training data set.
     */
    final Map<Integer, DArray<Double>> svs;
    final SubModel[] submodels;

    private static final class SubModel {
        public List<SupportVector> svs = new ArrayList<>();
        public double rho;
        public double probA;
        public double probB;
    }

    private record SupportVector(int index, int levelIndex, double coefficient) {
    }

    public ModelInfo(ProblemInfo pi) {
        this.pi = pi;
        this.svs = new HashMap<>();

        int modelCount = pi.levels().size() * (pi.levels().size() - 1) / 2;

        this.submodels = new SubModel[modelCount];
        int pos = 0;
        for (int i = 0; i < modelCount; i++) {
            for (int j = i + 1; j < modelCount; j++) {
                SubModel submodel = new SubModel();
                submodels[pos] = submodel;
            }
        }
    }
}
