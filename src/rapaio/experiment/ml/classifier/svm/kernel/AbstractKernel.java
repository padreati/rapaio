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

package rapaio.experiment.ml.classifier.svm.kernel;

import rapaio.data.Frame;
import rapaio.experiment.ml.classifier.svm.kernel.cache.KernelCache;
import rapaio.experiment.ml.classifier.svm.kernel.cache.MapKernelCache;
import rapaio.experiment.ml.classifier.svm.kernel.cache.SolidKernelCache;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/16/15.
 */
public abstract class AbstractKernel implements Kernel {

    private static final long serialVersionUID = -2216556261751685749L;

    protected String[] varNames;
    private KernelCache cache;

    @Override
    public void buildKernel(String[] varNames, Frame df) {
        this.varNames = varNames;
        if (df.rowCount() <= 10_000) {
            cache = new SolidKernelCache(df);
        } else {
            cache = new MapKernelCache();
        }
    }

    @Override
    public boolean isLinear() {
        return false;
    }

    protected double dotProd(Frame df1, int row1, Frame df2, int row2) {
        double result = 0;
        for (String varName : varNames) {
            result += df1.getDouble(row1, varName) * df2.getDouble(row2, varName);
        }
        return result;
    }

    protected double deltaDotProd(Frame df1, int row1, Frame df2, int row2) {
        double result = 0;
        for (String varName : varNames) {
            result += Math.pow(df1.getDouble(row1, varName) - df2.getDouble(row2, varName), 2);
        }
        return result;
    }

    @Override
    public double compute(Frame df1, int row1, Frame df2, int row2) {
        Double value = cache.retrieve(df1, row1, df2, row2);
        if (value == null) {
            value = eval(df1, row1, df2, row2);
            cache.store(df1, row1, df2, row2, value);
        }
        return value;
    }


    public abstract double eval(Frame df1, int row1, Frame df2, int row2);

    @Override
    public void clean() {
        cache.clear();
    }
}

