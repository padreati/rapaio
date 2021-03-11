/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

package rapaio.math.linear;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/3/21.
 */
public
enum SOrder {
    R(true, false),
    C(false, true);

    final boolean rowMajor;
    final boolean colMajor;

    SOrder(boolean rowMajor, boolean colMajor) {
        this.rowMajor = rowMajor;
        this.colMajor = colMajor;
    }

    public boolean isRowMajor() {
        return rowMajor;
    }

    public boolean isColMajor() {
        return colMajor;
    }
}
