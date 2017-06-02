/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.ml.classifier.svm.kernel;

import rapaio.data.Frame;

/**
 * The Spline kernel is given as a piece-wise cubic polynomial, as derived in the works by Gunn (1998).
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/21/15.
 */
public class SplineKernel extends AbstractKernel {

    private static final long serialVersionUID = -4985948375658836441L;

    @Override
    public double eval(Frame df1, int row1, Frame df2, int row2) {

        double value = 1;
        for (String varName : varNames) {
            double x_i = df1.getValue(row1, varName);
            double y_i = df2.getValue(row2, varName);
            double min = Math.min(x_i, y_i);
            value *= 1 + x_i * y_i + x_i * y_i * min - (x_i + y_i) * Math.pow(min, 2) / 2.0 + Math.pow(min, 3) / 3;
        }
        return value;
    }

    @Override
    public Kernel newInstance() {
        return new SplineKernel();
    }

    @Override
    public String name() {
        return "Spline";
    }
}
