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
public class SandBoxM {

    public static void main(String[] args) {

        M m = M.newFill(10, 10, 1);
        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.cols(); j++) {
                m.set(i, j, Math.pow(i, Math.sqrt(j)));
            }
        }
        m.summary();

        M mm = m.mapRows(2, 4, 6).mapCols(6, 9, 1, 2).t();

        mm.summary();

        LUDecomposition lu = new LUDecomposition(mm);

        lu.getL().summary();
        lu.getU().summary();
        lu.getDoublePivot();
    }
}
