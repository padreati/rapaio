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

package rapaio.experiment.math.linear.dense;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

public abstract class AbstractDVectorStore extends AbstractDVector {

    public static final VectorSpecies<Double> species = DoubleVector.SPECIES_PREFERRED;
    public static final int speciesLen = species.length();

    public final int loopBound;

    public AbstractDVectorStore(int size) {
        loopBound = species.loopBound(size);
    }

    /**
     * @return double array where elements are stored.
     */
    public abstract double[] array();

    /**
     * Loads a vector with values where the first value is at the given position.
     * <p>
     * The number of lanes from the vector depends on the implementation.
     * <p>
     * All the elements stored in this vector are from consecutive positions,
     * starting with position {@param i}.
     *
     * @param i position of the first element from the store
     * @return vector of elements from store
     */
    public abstract DoubleVector loadVector(int i);

    /**
     * Loads a vector with values where the first value is at the given position and
     * the loaded elements is dictated by the mask.
     * <p>
     * The number of lanes depends on the implementation.
     * <p>
     * All the elements from this vector are from consecutive positions, but only the
     * elements specified by the mask are loaded. The mask contains flags for each lane
     * and if the flag is set to {@code true} the element will be loaded into vector,
     * otherwise the element will not be loaded into the vector.
     *
     * @param i position of the first element from the vector
     * @param m mask for element loading
     * @return vector of elements from store
     */
    public abstract DoubleVector loadVector(int i, VectorMask<Double> m);

    /**
     * Stores elements from the vector into store with the first element to the
     * given positions.
     * <p>
     * All the other elements will be stored at consecutive positions, with the
     * number of elements depending on the number of lanes in the vector.
     *
     * @param v vector of elements to be stored.
     * @param i position in store of the first element
     */
    public abstract void storeVector(DoubleVector v, int i);

    /**
     * Stores elements from the vector into store with the first element to the
     * given positions.
     * <p>
     * All the other elements will be stored at consecutive positions, with the
     * number of elements depending on the number of lanes in the vector.
     * The elements which are not enabled by the given mask will not be stored.
     *
     * @param v vector of elements to be stored.
     * @param i position in store of the first element
     */
    public abstract void storeVector(DoubleVector v, int i, VectorMask<Double> m);

    /**
     * Creates a new array of values which contains a copy of the stored values.
     * <p>
     * The new copy array have the dimension equal with the number of elements and
     * the offset is 0 and stride is 1. This copy of the elements might be different
     * in layout than how the elements are stored.
     * <p>
     * The purpose of this operation is to create a copy array of elements which is better suited
     * for fast vector operations than the original store, where the layout of the elements
     * depends on the implementation.
     *
     * @return new dense solid copy array of values
     */
    public abstract double[] solidArrayCopy();

    @Override
    public double sum() {
        DoubleVector aggr = DoubleVector.zero(species);
        int i = 0;
        for (; i < loopBound; i += speciesLen) {
            DoubleVector xv = loadVector(i);
            aggr = aggr.add(xv);
        }
        double sum = aggr.reduceLanes(VectorOperators.ADD);
        for (; i < size(); i++) {
            sum += get(i);
        }
        return sum;
    }

    @Override
    public double nansum() {
        int i = 0;
        VectorMask<Double> m;
        DoubleVector sum = DoubleVector.zero(species);
        for (; i < loopBound; i += speciesLen) {
            DoubleVector xv = loadVector(i);
            m = xv.test(VectorOperators.IS_NAN).not();
            sum = sum.add(xv, m);
        }
        double result = sum.reduceLanes(VectorOperators.ADD);
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
        int i = 0;
        DoubleVector aggr = DoubleVector.broadcast(species, 1);
        for (; i < loopBound; i += speciesLen) {
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
