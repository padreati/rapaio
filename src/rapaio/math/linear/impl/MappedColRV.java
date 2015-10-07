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
 *
 */

package rapaio.math.linear.impl;

import rapaio.math.linear.RM;
import rapaio.math.linear.RV;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/6/15.
 */
@Deprecated
public class MappedColRV implements RV {

    private final RM ref;
    private final int col;

    public MappedColRV(RM ref, int col) {
        this.ref = ref;
        this.col = col;
    }

    @Override
    public int rowCount() {
        return ref.rowCount();
    }

    @Override
    public int colCount() {
        return 1;
    }

    @Override
    public double get(int i, int j) {
        if (j == 0) {
            return ref.get(i, col);
        }
        throw new IllegalArgumentException("This operation is valid only for mapped vectors");
    }

    @Override
    public void set(int i, int j, double value) {
        if (j == 0) {
            ref.set(i, col, value);
            return;
        }
        throw new IllegalArgumentException("This operation is valid only for mapped vectors");
    }
}
