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

package rapaio.ml.common.kernel;

import java.io.Serial;

import rapaio.data.Frame;
import rapaio.math.linear.DVector;
import rapaio.math.tensor.Tensor;

/**
 * The Histogram Intersection Kernel is also known as the Min Kernel
 * and has been proven useful in image classification.
 * <p>
 * k(x,y) = \sum_{i=1}^n \min(x_i,y_i)
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/21/15.
 */
public class MinKernel extends AbstractKernel {

    @Serial
    private static final long serialVersionUID = -2388704255494979581L;

    @Override
    public double eval(Frame df1, int row1, Frame df2, int row2) {
        double sum = 0;
        for (String varName : varNames) {
            sum += Math.min(df1.getDouble(row1, varName), df2.getDouble(row2, varName));
        }
        return sum;
    }

    @Override
    public double compute(DVector v, DVector u) {
        double sum = 0;
        for (int i = 0; i < v.size(); i++) {
            sum += Math.min(v.get(i), u.get(i));
        }
        return sum;
    }

    @Override
    public double compute(Tensor<Double> v, Tensor<Double> u) {
        double sum = 0;
        for (int i = 0; i < v.size(); i++) {
            sum += Math.min(v.get(i), u.get(i));
        }
        return sum;
    }

    @Override
    public Kernel newInstance() {
        return new MinKernel();
    }

    @Override
    public String name() {
        return "Min";
    }
}
