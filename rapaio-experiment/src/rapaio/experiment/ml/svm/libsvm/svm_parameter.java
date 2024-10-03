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

package rapaio.experiment.ml.svm.libsvm;

import java.io.Serializable;

public class svm_parameter implements Cloneable, Serializable {
    /* svm_type */
    public static final int C_SVC = 0;
    public static final int NU_SVC = 1;
    public static final int ONE_CLASS = 2;
    public static final int EPSILON_SVR = 3;
    public static final int NU_SVR = 4;

    /* kernel_type */
    public static final int LINEAR = 0;
    public static final int POLY = 1;
    public static final int RBF = 2;
    public static final int SIGMOID = 3;
    public static final int PRECOMPUTED = 4;

    public int svm_type;
    public int kernel_type;
    public int degree;    // for poly
    public double gamma;    // for poly/rbf/sigmoid
    public double coef0;    // for poly/sigmoid

    // these are for training only
    public double cache_size; // in MB
    public double eps;    // stopping criteria
    public double C;    // for C_SVC, EPSILON_SVR and NU_SVR
    public int nr_weight;        // for C_SVC
    public int[] weight_label;    // for C_SVC
    public double[] weight;        // for C_SVC
    public double nu;    // for NU_SVC, ONE_CLASS, and NU_SVR
    public double p;    // for EPSILON_SVR
    public int shrinking;    // use the shrinking heuristics
    public int probability; // do probability estimates

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

}