/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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

package rapaio.math.linear;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/6/15.
 */
public class MappedV implements V {

    private final M ref;
    private final boolean isRow;
    private final int index;

    MappedV(M ref, boolean isRow, int index) {
        this.ref = ref;
        this.isRow = isRow;
        this.index = index;
    }

    @Override
    public int rowCount() {
        return isRow ? 1 : ref.rowCount();
    }

    @Override
    public int colCount() {
        return isRow ? ref.colCount() : 1;
    }

    @Override
    public double get(int i, int j) {
        if (isRow && i == 0) {
            return ref.get(index, j);
        }
        if (!isRow && j == 0) {
            return ref.get(i, index);
        }
        throw new IllegalArgumentException("This operation is valid only for mapped vectors");
    }

    @Override
    public void set(int i, int j, double value) {
        if (isRow && i == 0) {
            ref.set(index, j, value);
        }
        if (!isRow && j == 0) {
            ref.set(i, index, value);
        }
        throw new IllegalArgumentException("This operation is valid only for mapped vectors");
    }
}
