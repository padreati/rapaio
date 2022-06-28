/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.preprocessing;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/3/18.
 */
public class TransformTestUtil {

    public static Frame allDoubles(int n, int k) {
        Var[] vars = new Var[k];
        for (int i = 0; i < k; i++) {
            vars[i] = VarDouble.from(n, row -> RandomSource.nextDouble() - 0.5).name("V" + (i + 1));
        }
        return SolidFrame.byVars(vars);
    }

    public static Frame allDoubleNominal(int n, int dCount, int nomCunt) {
        int len = dCount + nomCunt;
        Var[] vars = new Var[len];

        String[] words = new String[]{
                "a", "factor", "base", "spectrum", "glance", "point", "shuffle", "bias"
        };

        for (int i = 0; i < len; i++) {
            if (i < dCount) {
                vars[i] = VarDouble.from(n, row -> RandomSource.nextDouble() - 0.5)
                        .name("v" + (i + 1));
            } else {
                vars[i] = VarNominal.from(n, row -> words[RandomSource.nextInt(words.length)])
                        .name("v" + (i + 1));
            }
        }
        return SolidFrame.byVars(vars);
    }
}
