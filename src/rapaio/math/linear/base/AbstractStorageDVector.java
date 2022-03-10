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
import rapaio.math.linear.dense.DVectorStorage;

public abstract class AbstractStorageDVector extends AbstractDVector {

    private static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;
    private static final int SPECIES_LEN = SPECIES.length();

    protected final DVectorStorage storage;

    public AbstractStorageDVector(DVectorStorage storage) {
        this.storage = storage;
    }

    public abstract int size();

    @Override
    public double get(int i) {
        return storage.get(i);
    }

    @Override
    public void set(int i, double value) {
        storage.set(i, value);
    }

    @Override
    public void inc(int i, double value) {
        storage.inc(i, value);
    }

    @Override
    public double sum() {
        int i = 0;
        DoubleVector aggr = DoubleVector.zero(SPECIES);
        int bound = SPECIES.loopBound(size());
        for (; i < bound; i += SPECIES_LEN) {
            DoubleVector xv = storage.loadVector(i);
            aggr = aggr.add(xv);
        }
        double result = aggr.reduceLanes(VectorOperators.ADD);
        for (; i < size(); i++) {
            result = result + storage.get(i);
        }
        return result;
    }

    @Override
    public double nansum() {
        int i = 0;
        VectorMask<Double> m;
        DoubleVector aggr = DoubleVector.zero(SPECIES);
        int bound = SPECIES.loopBound(size());
        for (; i < bound; i += SPECIES_LEN) {
            DoubleVector xv = storage.loadVector(i);
            m = xv.test(VectorOperators.IS_NAN).not();
            aggr = aggr.add(xv, m);
        }
        m = aggr.test(VectorOperators.IS_NAN).not();
        double result = aggr.reduceLanes(VectorOperators.ADD, m);
        for (; i < size(); i++) {
            double value = storage.get(i);
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
            DoubleVector xv = storage.loadVector(i);
            aggr = aggr.mul(xv);
        }
        double result = aggr.reduceLanes(VectorOperators.MUL);
        for (; i < size(); i++) {
            result = result * storage.get(i);
        }
        return result;
    }
}
