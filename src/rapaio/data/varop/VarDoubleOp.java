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
public class VarDoubleOp implements VarOp<VarDouble> {

    private final VarDouble v;
    private final double[] array;
    private final int rowCount;

    public VarDoubleOp(VarDouble v) {
        this.v = v;
        this.array = v.array();
        this.rowCount = v.rowCount();
    }

    @Override
    public VarDouble apply(Double2DoubleFunction fun) {
        for (int i = 0; i < rowCount; i++) {
            array[i] = fun.applyAsDouble(array[i]);
        }
        return v;
    }

    @Override
    public VarDouble capply(Double2DoubleFunction fun) {
        double[] copy = new double[rowCount];
        for (int i = 0; i < rowCount; i++) {
            copy[i] = fun.applyAsDouble(array[i]);
        }
        return VarDouble.wrap(copy).withName(v.name());
    }

    @Override
    public VarDouble plus(double a) {
        for (int i = 0; i < rowCount; i++) {
            array[i] += a;
        }
        return v;
    }

    @Override
    public VarDouble plus(Var x) {
        if(x instanceof VarDouble) {
            VarDouble xd = (VarDouble)x;
            double[] xdarray = xd.array();
            for (int i = 0; i < rowCount; i++) {
                array[i] += xdarray[i];
            }
        }
        return v;
    }
}
