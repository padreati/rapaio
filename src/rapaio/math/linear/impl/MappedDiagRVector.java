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

package rapaio.math.linear.impl;

import rapaio.math.linear.RMatrix;
import rapaio.math.linear.RVector;

/**
 * Column vector obtained from the diagonal of a given matrix.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/26/15.
 */
public class MappedDiagRVector implements RVector {
    private final int n;
    private final RMatrix ref;

    public MappedDiagRVector(RMatrix ref) {
        this.ref = ref;
        this.n = Math.min(ref.rowCount(), ref.colCount());
    }

    @Override
    public int rowCount() {
        return n;
    }

    @Override
    public int colCount() {
        return n;
    }

    @Override
    public double get(int i, int j) {
        if (j == 0) {
            return ref.get(i, i);
        }
        throw new IllegalArgumentException("This operation is valid only for mapped vectors");
    }

    @Override
    public void set(int i, int j, double value) {
        if (j == 0) {
            ref.set(i, i, value);
            return;
        }
        throw new IllegalArgumentException("This operation is valid only for mapped vectors");
    }

    @Override
    public double get(int i) {
        return get(i, 0);
    }

    @Override
    public void set(int i, double value) {
        set(i, 0, value);
    }
}
