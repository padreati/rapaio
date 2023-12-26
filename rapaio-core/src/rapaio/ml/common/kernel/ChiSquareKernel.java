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

/**
 * The Chi-Square kernel comes from the Chi-Square distribution.
 * <p>
 * k(x,y) = 1 - \sum_{i=1}^n \frac{(x_i-y_i)^2}{\frac{1}{2}(x_i+y_i)}
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/21/15.
 */
public class ChiSquareKernel extends AbstractKernel {

    @Serial
    private static final long serialVersionUID = -3301596992870913061L;

    @Override
    public double eval(Frame df1, int row1, Frame df2, int row2) {
        double result = 0;
        for (String varName : varNames) {
            double sum = df1.getDouble(row1, varName) + df2.getDouble(row2, varName);
            double diff = df1.getDouble(row1, varName) - df2.getDouble(row2, varName);
            result = 2 * diff * diff / sum;
        }
        return 1 - result;
    }

    @Override
    public double compute(DVector v, DVector u) {
        double result = 0;
        for (int i = 0; i < u.size(); i++) {
            double sum = u.get(i) + v.get(i);
            double diff = u.get(i) - v.get(i);
            result = 2 * diff * diff / sum;
        }
        return 1 - result;
    }

    @Override
    public Kernel newInstance() {
        return new ChiSquareKernel();
    }

    @Override
    public String name() {
        return "ChiSquare";
    }
}
