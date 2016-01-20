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

package rapaio.ml.classifier.svm.kernel;

import rapaio.data.Frame;

/**
 * The Histogram Intersection Kernel is also known as the Min Kernel
 * and has been proven useful in image classification.
 * <p>
 * k(x,y) = \sum_{i=1}^n \min(x_i,y_i)
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/21/15.
 */
@Deprecated
public class MinKernel extends AbstractKernel {

    @Override
    public double eval(Frame df1, int row1, Frame df2, int row2) {
        double sum = 0;
        for (String varName : varNames) {
            sum += Math.min(df1.value(row1, varName), df2.value(row2, varName));
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
