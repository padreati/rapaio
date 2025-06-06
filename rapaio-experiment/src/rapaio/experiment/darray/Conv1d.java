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

package rapaio.experiment.darray;

import rapaio.darray.DArrayManager;
import rapaio.darray.DType;
import rapaio.darray.Shape;

public class Conv1d {

    public static void main(String[] args) {

        DArrayManager dm = DArrayManager.base();
        DType<?> dt = DType.FLOAT;

        int n = 2;
        int cIn = 2;
        int l = 10;
        int cOut = 3;

        var x = dm.seq(dt, Shape.of(n, cIn, l));
        x.printString();

        int k = 3;
        int p = 0;
        int s = 2;
        int d = 1;

        var xp = x.pad(2, p, 1);
        xp.printString();

        var xf = xp.unfold(2, k, s, d);
        xf.printString();


        var w = dm.seq(dt, Shape.of(k, cOut, cIn));
        w.printString();
    }
}
