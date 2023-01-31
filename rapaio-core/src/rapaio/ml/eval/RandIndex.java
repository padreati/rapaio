/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.ml.eval;

import rapaio.data.Var;

public class RandIndex {

    public static RandIndex from(Var trueLabels, Var clusterLabels) {
        return new RandIndex(trueLabels, clusterLabels);
    }

    private final Var trueLabels;
    private final Var clusterLabels;

    private double randIndex;

    private RandIndex(Var trueLabels, Var clusterLabels) {
        this.trueLabels = trueLabels;
        this.clusterLabels = clusterLabels;
        compute();
    }

    public double getRandIndex() {
        return randIndex;
    }

    private void compute() {
        if (trueLabels.size() != clusterLabels.size()) {
            throw new IllegalStateException("Assignments must have the same size.");
        }
        double a = 0;
        double b = 0;
        double c = 0;
        double d = 0;

        for (int i = 0; i < trueLabels.size(); i++) {
            for (int j = i + 1; j < trueLabels.size(); j++) {
                boolean sameTrue = trueLabels.getLabel(i).equals(trueLabels.getLabel(j));
                boolean sameCluster = clusterLabels.getLabel(i).equals(clusterLabels.getLabel(j));

                if (sameTrue && sameCluster) {
                    a++;
                }
                if (!sameTrue && !sameCluster) {
                    b++;
                }
                if (sameTrue && !sameCluster) {
                    c++;
                }
                if (!sameTrue && sameCluster) {
                    d++;
                }
            }
        }
        randIndex = (a + b) / (a + b + c + d);
    }
}
