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
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/4/15.
 */
public class MappedM implements M {

    private final M ref;
    private final int[] rowIndexes;
    private final int[] colIndexes;

    MappedM(M ref, boolean byRow, int... indexes) {
        if (byRow) {
            this.ref = ref;
            this.rowIndexes = indexes;
            this.colIndexes = new int[ref.cols()];
            for (int i = 0; i < ref.cols(); i++) {
                this.colIndexes[i] = i;
            }
        } else {
            this.ref = ref;
            this.rowIndexes = new int[ref.rows()];
            for (int i = 0; i < ref.rows(); i++) {
                this.rowIndexes[i] = i;
            }
            this.colIndexes = indexes;
        }
    }

    @Override
    public int rows() {
        return rowIndexes.length;
    }

    @Override
    public int cols() {
        return colIndexes.length;
    }

    @Override
    public double get(int i, int j) {
        return ref.get(rowIndexes[i], colIndexes[j]);
    }

    @Override
    public void set(int i, int j, double value) {
        ref.set(rowIndexes[i], colIndexes[j], value);
    }
}
