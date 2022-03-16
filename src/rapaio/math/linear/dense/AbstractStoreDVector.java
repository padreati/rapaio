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

package rapaio.math.linear.dense;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import rapaio.math.linear.dense.storage.DVectorStore;

public abstract class AbstractStoreDVector extends AbstractDVector {

    private static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;
    private static final int SPECIES_LEN = SPECIES.length();

    public abstract DVectorStore store();

    public abstract int size();

    @Override
    public double get(int i) {
        return store().get(i);
    }

    @Override
    public void set(int i, double value) {
        store().set(i, value);
    }

    @Override
    public void inc(int i, double value) {
        store().inc(i, value);
    }

    @Override
    public double sum() {
        int i = 0;
        DoubleVector aggr = DoubleVector.zero(SPECIES);
        int bound = SPECIES.loopBound(size());
        for (; i < bound; i += SPECIES_LEN) {
            DoubleVector xv = store().loadVector(i);
            aggr = aggr.add(xv);
        }
        VectorMask<Double> m = store().indexInRange(i);
        aggr = aggr.add(store().loadVector(i, m), m);
        return aggr.reduceLanes(VectorOperators.ADD);
    }

    @Override
    public double nansum() {
        int i = 0;
        VectorMask<Double> m;
        DoubleVector sum = DoubleVector.zero(SPECIES);
        int bound = SPECIES.loopBound(size());
        for (; i < bound; i += SPECIES_LEN) {
            DoubleVector xv = store().loadVector(i);
            m = xv.test(VectorOperators.IS_NAN).not();
            sum = sum.add(xv, m);
        }
        double result = sum.reduceLanes(VectorOperators.ADD);
        for (; i < size(); i++) {
            double value = store().get(i);
            if (!Double.isNaN(value)) {
                result = result + value;
            }
        }
        return result;
    }

    @Override
    public double prod() {
        int i = 0;
        DoubleVector aggr = DoubleVector.broadcast(SPECIES, 1);
        int bound = SPECIES.loopBound(size());
        for (; i < bound; i += SPECIES.length()) {
            DoubleVector xv = store().loadVector(i);
            aggr = aggr.mul(xv);
        }
        double result = aggr.reduceLanes(VectorOperators.MUL);
        for (; i < size(); i++) {
            result = result * store().get(i);
        }
        return result;
    }
}
