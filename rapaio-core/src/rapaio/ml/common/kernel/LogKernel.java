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
import rapaio.printer.Format;

/**
 * Log Kernel
 * <p>
 * The Log kernel seems to be particularly interesting for images, but is only
 * conditionally positive definite.
 * <p>
 * k(x,y) = - log (\lVert x-y \rVert ^d + 1)
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/19/15.
 */
public class LogKernel extends AbstractKernel {

    @Serial
    private static final long serialVersionUID = 6198322741512752359L;

    private final double degree;

    public LogKernel(double degree) {
        this.degree = degree;
    }

    @Override
    public double eval(Frame df1, int row1, Frame df2, int row2) {
        return -Math.log1p(Math.pow(deltaSumSquares(df1, row1, df2, row2), degree));
    }

    @Override
    public double compute(DVector v, DVector u) {
        return -Math.log1p(Math.pow(deltaSumSquares(v, u), degree));
    }

    @Override
    public Kernel newInstance() {
        return new LogKernel(degree);
    }

    @Override
    public String name() {
        return "Log(degree=" + Format.floatFlex(degree) + ")";
    }
}
