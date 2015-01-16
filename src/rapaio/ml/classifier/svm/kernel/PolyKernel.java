/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.ml.classifier.svm.kernel;

import rapaio.core.MathBase;
import rapaio.data.Frame;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/16/15.
 */
public class PolyKernel extends AbstractKernel {

    private final boolean lowerOrder;
    private final double exponent;

    @Override
    public boolean isLinear() {
        return MathBase.eq(exponent, 1);
    }

    public PolyKernel(double exponent, boolean lowerOrder) {
        this.exponent = exponent;
        this.lowerOrder = lowerOrder;
    }

    @Override
    public double eval(Frame df1, int row1, Frame df2, int row2) {

        if (varNames == null) {
            throw new IllegalArgumentException("This kernel is not build with var names");
        }

        double result = dotProd(df1, row1, df2, row2);
        if (lowerOrder) {
            result += 1;
        }
        if (exponent != 1.0) {
            result = Math.pow(result, exponent);
        }
        return result;
    }
}