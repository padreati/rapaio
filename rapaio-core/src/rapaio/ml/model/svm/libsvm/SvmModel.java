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

import java.io.Serializable;

import rapaio.math.tensor.Tensor;

public class SvmModel implements Serializable {

    public SvmParameter param;    // parameter
    public int nr_class;        // number of classes, = 2 in regression/one class svm
    public int l;            // total #SV
    public Tensor<Double>[] SV;    // SVs (SV[l])
    public double[][] svCoef;    // coefficients for SVs in decision functions (sv_coef[k-1][l])
    public double[] rho;        // constants in decision functions (rho[k*(k-1)/2])
    public double[] probA;         // pariwise probability information
    public double[] probB;
    public int[] sv_indices;       // sv_indices[0,...,nSV-1] are values in [1,...,num_traning_data] to indicate SVs in the training set

    // for classification only

    public int[] label;        // label of each class (label[k])
    public int[] nSV;        // number of SVs for each class (nSV[k])
    // nSV[0] + nSV[1] + ... + nSV[k-1] = l
}