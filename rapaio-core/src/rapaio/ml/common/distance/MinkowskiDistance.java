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

package rapaio.ml.common.distance;

import static java.lang.StrictMath.pow;

import rapaio.data.Frame;
import rapaio.math.tensor.Tensor;

public class MinkowskiDistance implements Distance {

    private final double p;

    public MinkowskiDistance(double p) {
        this.p = p;
    }

    @Override
    public String name() {
        return "Euclidean";
    }

    @Override
    public double compute(Tensor<Double> x, Tensor<Double> y) {
        return pow(reduced(x, y), 1 / p);
    }

    @Override
    public double compute(Frame df1, int row1, Frame df2, int row2) {
        return pow(reduced(df1, row1, df2, row2), 1 / p);
    }

    @Override
    public double reduced(Tensor<Double> x, Tensor<Double> y) {
        return x.sub(y).apply_(v -> pow(v, p)).sum();
    }

    @Override
    public double reduced(Frame df1, int row1, Frame df2, int row2) {
        double sum = 0;
        String[] names = df1.varNames();
        for (String name : names) {
            double delta = df1.getDouble(row1, name) - df2.getDouble(row2, name);
            sum += pow(delta, p);
        }
        return sum;
    }

    @Override
    public boolean equalOnParams(Distance d) {
        if (d instanceof MinkowskiDistance md) {
            return p == md.p;
        }
        return false;
    }
}
