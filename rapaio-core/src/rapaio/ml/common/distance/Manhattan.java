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

package rapaio.ml.common.distance;

import static java.lang.StrictMath.abs;

import rapaio.darray.DArray;
import rapaio.data.Frame;

public class Manhattan implements Distance {

    @Override
    public String name() {
        return "Manhattan";
    }

    @Override
    public double compute(DArray<Double> x, DArray<Double> y) {
        return x.sub(y).abs_().sum();
    }

    @Override
    public double compute(Frame df1, int row1, Frame df2, int row2) {
        double sum = 0;
        String[] names = df1.varNames();
        for (String name : names) {
            sum += abs(df1.getDouble(row1, name) - df2.getDouble(row2, name));
        }
        return sum;
    }

    @Override
    public double reduced(DArray<Double> x, DArray<Double> y) {
        return compute(x, y);
    }

    @Override
    public double reduced(Frame df1, int row1, Frame df2, int row2) {
        return compute(df1, row1, df2, row2);
    }

    @Override
    public boolean equalOnParams(Distance d) {
        return d instanceof Manhattan;
    }
}
