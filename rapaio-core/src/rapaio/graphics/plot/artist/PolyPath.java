/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.graphics.plot.artist;

import rapaio.data.VarDouble;
import rapaio.util.Pair;

public final class PolyPath {

    private final VarDouble xs = VarDouble.empty();
    private final VarDouble ys = VarDouble.empty();

    public PolyPath addPoints(VarDouble x, VarDouble y, boolean reverse) {
        int n = Math.min(x.size(), y.size());
        if (reverse) {
            for (int i = n - 1; i >= 0; i--) {
                xs.addDouble(x.getDouble(i));
                ys.addDouble(y.getDouble(i));
            }
        } else {
            for (int i = 0; i < n; i++) {
                xs.addDouble(x.getDouble(i));
                ys.addDouble(y.getDouble(i));
            }
        }
        return this;
    }

    public Pair<VarDouble, VarDouble> getPath() {
        return Pair.from(xs, ys);
    }
}
