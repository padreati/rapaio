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
 */

package rapaio.ml.classifier.svm.kernel;

import rapaio.data.Frame;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/16/15.
 */
public abstract class AbstractKernel implements Kernel {

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
            result += df1.value(row1, varName) * df2.value(row2, varName);
        }
        return result;
    }

    protected double deltaDotProd(Frame df1, int row1, Frame df2, int row2) {
        double result = 0;
        for (String varName : varNames) {
            result += Math.pow(df1.value(row1, varName) - df2.value(row2, varName), 2);
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

interface KernelCache {

    Double retrieve(Frame df1, int row1, Frame df2, int row2);

    void store(Frame df1, int row1, Frame df2, int row2, double value);

    void clear();
}

class MapKernelCache implements KernelCache {

    private Map<Frame, Map<Frame, Map<Long, Double>>> cache = new HashMap<>();

    @Override
    public void store(Frame df1, int row1, Frame df2, int row2, double value) {
        if (!cache.containsKey(df1)) {
            cache.put(df1, new HashMap<>());
        }
        if (!cache.get(df1).containsKey(df2)) {
            cache.get(df1).put(df2, new HashMap<>());
        }
        cache.get(df1).get(df2).put((((long) row1) << 32) | (row2 & 0xffffffffL), value);
    }

    @Override
    public Double retrieve(Frame df1, int row1, Frame df2, int row2) {
        if (cache.containsKey(df1) && cache.get(df1).containsKey(df2)) {
            cache.get(df1).get(df2).get((((long) row1) << 32) | (row2 & 0xffffffffL));
        }
        return null;
    }

    @Override
    public void clear() {
        cache.clear();
    }
}

class SolidKernelCache implements KernelCache {

    private final Frame df;
    private Double[][] cache;

    public SolidKernelCache(Frame df) {
        this.df = df;
        cache = new Double[df.rowCount()][df.rowCount()];
        for (int i = 0; i < df.rowCount(); i++) {
            for (int j = 0; j < df.rowCount(); j++) {
                cache[i][j] = null;
            }
        }
    }

    @Override
    public Double retrieve(Frame df1, int row1, Frame df2, int row2) {
        if (df1 != df2)
            return null;
        if (row1 > row2)
            return retrieve(df1, row2, df2, row1);
        if (df1 == this.df)
            return cache[row1][row2];
        return null;
    }

    @Override
    public void store(Frame df1, int row1, Frame df2, int row2, double value) {
        if (df1 != df2) {
            return;
        }
        if (row1 > row2) {
            store(df1, row2, df2, row1, value);
            return;
        }
        if (df1 == this.df) {
            if (cache == null) {
                throw new IllegalArgumentException();
            }
            cache[row1][row2] = value;
        }
    }

    @Override
    public void clear() {
        cache = null;
    }
}