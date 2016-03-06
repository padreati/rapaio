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

package rapaio.data.filter.var;

import rapaio.core.CoreTools;
import rapaio.data.Nominal;
import rapaio.data.Var;
import rapaio.sys.WS;
import rapaio.util.func.SPredicate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Make a numerical variable a nominal one with intervals specified by quantiles.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/18/16.
 */
public class VFQuantileDiscrete extends AbstractVF {

    private static final long serialVersionUID = -6702714518094848749L;

    private final int k;
    List<String> dict = new ArrayList<>();
    Map<String, SPredicate<Double>> predicates = new HashMap<>();
    double[] qv;

    public VFQuantileDiscrete(int k) {
        this.k = k;
        if (k <= 1) {
            throw new IllegalArgumentException(String.format("k=%d should be greater than 1", k));
        }
    }

    @Override
    public void fit(Var... vars) {
        super.checkSingleVar(vars);

        double len = 1.0 / k;
        double[] q = new double[k - 1];
        for (int i = 0; i < q.length; i++) {
            q[i] = len * (i + 1);
        }
        Var original = vars[0];

        qv = CoreTools.quantiles(original, q).values();

        // first interval

        dict.add("-Inf~" + WS.formatFlexShort(qv[0]));
        predicates.put("-Inf~" + WS.formatFlexShort(qv[0]), x -> x <= qv[0]);

        // mid intervals

        for (int i = 1; i < qv.length; i++) {
            int index = i;
            dict.add(WS.formatFlexShort(qv[i - 1]) + "~" + WS.formatFlexShort(qv[i]));
            predicates.put(WS.formatFlexShort(qv[i - 1]) + "~" + WS.formatFlexShort(qv[i]), x -> x > qv[index - 1] && x <= qv[index]);
        }

        // last interval

        dict.add(WS.formatFlexShort(qv[qv.length - 1]) + "~Inf");
        predicates.put(WS.formatFlexShort(qv[qv.length - 1]) + "~Inf", x -> x > qv[qv.length - 1]);
    }

    @Override
    public Var apply(Var... vars) {
        super.checkSingleVar(vars);

        Var original = vars[0];

        Nominal result = Nominal.empty(0, dict).withName(original.name());
        for (int i = 0; i < original.rowCount(); i++) {
            if (original.missing(i))
                result.addMissing();
            for (Map.Entry<String, SPredicate<Double>> e : predicates.entrySet()) {
                if (e.getValue().test(original.value(i))) {
                    result.addLabel(e.getKey());
                }
            }
        }
        return result;
    }
}
