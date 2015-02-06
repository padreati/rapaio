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

import rapaio.data.Frame;
import rapaio.data.Var;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/6/15.
 */
public final class MV {

    /**
     * Builds a new 0 filled matrix with given rows and cols
     *
     * @param rowCount number of rows
     * @param colCount number of columns
     * @return new matrix object
     */
    public static M newMEmpty(int rowCount, int colCount) {
        return new SolidM(rowCount, colCount);
    }

    /**
     * Builds a new matrix with given rows and cols, fillen with given value
     *
     * @param rowCount number of rows
     * @param colCount number of columns
     * @param fill     initial value for all matrix cells
     * @return new matrix object
     */
    public static M newMFill(int rowCount, int colCount, double fill) {
        if (fill == 0) {
            return newMEmpty(rowCount, colCount);
        }
        M m = new SolidM(rowCount, colCount);
        for (int i = 0; i < m.rowCount(); i++) {
            for (int j = 0; j < m.colCount(); j++) {
                m.set(i, j, fill);
            }
        }
        return m;
    }

    public static M newMCopyOf(Frame df) {
        M m = new SolidM(df.rowCount(), df.varCount());
        for (int i = 0; i < df.rowCount(); i++) {
            for (int j = 0; j < df.varCount(); j++) {
                m.set(i, j, df.value(i, j));
            }
        }
        return m;
    }

    public static V newVCopyOf(Var var) {
        return new SolidV(var);
    }

    public static V newVEmpty(int rows) {
        return new SolidV(rows);
    }
}
