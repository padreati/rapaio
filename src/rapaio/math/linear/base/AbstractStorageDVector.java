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

package rapaio.math.linear.base;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import rapaio.math.linear.DVector;
import rapaio.math.linear.option.AlgebraOption;
import rapaio.math.linear.option.AlgebraOptions;

public abstract class AbstractStorageDVector extends AbstractDVector {

    protected static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;

    public abstract int size();

    public abstract DoubleVector loadVector(int i);

    public abstract void storeVector(DoubleVector v, int i);

    @Override
    public DVector add(double x, AlgebraOption<?>... opts) {
        int bound = SPECIES.loopBound(size());
        DoubleVector va = DoubleVector.broadcast(SPECIES, x);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size()];
            int i = 0;
            for (; i < bound; i += SPECIES.length()) {
                loadVector(i).add(va).intoArray(copy, i);
            }
            for(;i<size(); i++) {
                copy[i] = get(i) + x;
            }
            return DVector.wrap(copy);
        }
        int i = 0;
        for (; i < bound; i += SPECIES.length()) {
            DoubleVector vx = loadVector(i).add(va);
            storeVector(vx, i);
        }
        for (; i < size(); i++) {
            inc(i, x);
        }
        return this;
    }

    @Override
    public DVector sub(double x, AlgebraOption<?>... opts) {
        int bound = SPECIES.loopBound(size());
        DoubleVector va = DoubleVector.broadcast(SPECIES, x);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size()];
            int i = 0;
            for (; i < bound; i += SPECIES.length()) {
                loadVector(i).sub(va).intoArray(copy, i);
            }
            for (; i < size(); i++) {
                copy[i] = get(i) - x;
            }
            return DVector.wrap(copy);
        }
        int i = 0;
        for (; i < bound; i += SPECIES.length()) {
            DoubleVector vx = loadVector(i).sub(va);
            storeVector(vx, i);
        }
        for (; i < size(); i++) {
            inc(i, -x);
        }
        return this;
    }


    @Override
    public double sum() {
        int bound = SPECIES.loopBound(size());
        int i = 0;
        DoubleVector aggr = DoubleVector.zero(SPECIES);
        for (; i < bound; i += SPECIES.length()) {
            DoubleVector xv = loadVector(i);
            aggr = aggr.add(xv);
        }
        double result = aggr.reduceLanes(VectorOperators.ADD);
        for (; i < size(); i++) {
            result = result + get(i);
        }
        return result;
    }

    @Override
    public double nansum() {
        int bound = SPECIES.loopBound(size());
        int i = 0;
        VectorMask<Double> m;
        DoubleVector aggr = DoubleVector.zero(SPECIES);
        for (; i < bound; i += SPECIES.length()) {
            DoubleVector xv = loadVector(i);
            m = xv.test(VectorOperators.IS_NAN).not();
            aggr = aggr.add(xv, m);
        }
        m = aggr.test(VectorOperators.IS_NAN).not();
        double result = aggr.reduceLanes(VectorOperators.ADD, m);
        for (; i < size(); i++) {
            double value = get(i);
            if (!Double.isNaN(value)) {
                result = result + value;
            }
        }
        return result;
    }

    @Override
    public double prod() {
        int bound = SPECIES.loopBound(size());
        int i = 0;
        DoubleVector aggr = DoubleVector.broadcast(SPECIES, 1);
        for (; i < bound; i += SPECIES.length()) {
            DoubleVector xv = loadVector(i);
            aggr = aggr.mul(xv);
        }
        double result = aggr.reduceLanes(VectorOperators.MUL);
        for (; i < size(); i++) {
            result = result * get(i);
        }
        return result;
    }
}
