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

package rapaio.data.varop;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import rapaio.data.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/10/19.
 */
public class DefaultVarOp<V extends Var> implements VarOp<V> {

    private final V v;

    public DefaultVarOp(V v) {
        this.v = v;
    }

    @Override
    public V apply(Double2DoubleFunction fun) {
        for (int i = 0; i < v.rowCount(); i++) {
            if (!v.isMissing(i)) {
                v.setDouble(i, fun.applyAsDouble(v.getDouble(i)));
            }
        }
        return v;
    }

    @Override
    public VarDouble capply(Double2DoubleFunction fun) {
        double[] data = new double[v.rowCount()];
        for (int i = 0; i < v.rowCount(); i++) {
            if (v.isMissing(i)) {
                data[i] = Double.NaN;
            } else {
                data[i] = fun.applyAsDouble(v.getDouble(i));
            }
        }
        return VarDouble.wrap(data).withName(v.name());
    }

    @Override
    public V plus(double a) {
        for (int i = 0; i < v.rowCount(); i++) {
            v.setDouble(i, v.getDouble(i) + a);
        }
        return v;
    }

    @Override
    public V plus(Var x) {
        for (int i = 0; i < v.rowCount(); i++) {
            v.setDouble(i, v.getDouble(i) + x.getDouble(i));
        }
        return v;
    }
}
