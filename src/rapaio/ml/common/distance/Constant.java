/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.common.distance;

import static rapaio.printer.Format.floatFlex;

import rapaio.data.Frame;
import rapaio.math.linear.DVector;

public class Constant implements Distance {

    public static Constant one() {
        return new Constant(1);
    }

    public static Constant constant(int constant) {
        return new Constant(constant);
    }

    private final double constant;

    private Constant(double constant) {
        this.constant = constant;
    }

    @Override
    public String name() {
        return "Constant(c=" + floatFlex(constant) + ")";
    }

    @Override
    public double compute(DVector x, DVector y) {
        return reduced(x, y);
    }

    @Override
    public double compute(Frame df1, int row1, Frame df2, int row2) {
        return reduced(df1, row1, df2, row2);
    }

    @Override
    public double reduced(DVector x, DVector y) {
        return constant;
    }

    @Override
    public double reduced(Frame df1, int row1, Frame df2, int row2) {
        return constant;
    }
}
