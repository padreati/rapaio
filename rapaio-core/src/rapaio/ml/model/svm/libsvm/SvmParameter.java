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

package rapaio.ml.model.svm.libsvm;

import rapaio.ml.common.kernel.Kernel;

public class SvmParameter implements java.io.Serializable {
    /* svm_type */
    public static final int C_SVC = 0;
    public static final int NU_SVC = 1;
    public static final int ONE_CLASS = 2;
    public static final int EPSILON_SVR = 3;
    public static final int NU_SVR = 4;

    public int svmType;

    public Kernel kernel;

    // these are for training only
    public long cacheSize; // in MB
    public double eps;    // stopping criteria
    public double c;    // for C_SVC, EPSILON_SVR and NU_SVR
    public int nrWeight;        // for C_SVC
    public int[] weightLabel;    // for C_SVC
    public double[] weight;        // for C_SVC
    public double nu;    // for NU_SVC, ONE_CLASS, and NU_SVR
    public double p;    // for EPSILON_SVR
    public int shrinking;    // use the shrinking heuristics
    public int probability; // do probability estimates

    public SvmParameter copy() {
        SvmParameter copy = new SvmParameter();
        copy.svmType = svmType;
        copy.kernel = kernel;
        copy.cacheSize = cacheSize;
        copy.eps = eps;
        copy.c = c;
        copy.nrWeight = nrWeight;
        copy.weightLabel = weightLabel;
        copy.weight = weight;
        copy.nu = nu;
        copy.p = p;
        copy.shrinking = shrinking;
        copy.probability = probability;
        return copy;
    }
}