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

package rapaio.experiment.ml.feature.generator;

import rapaio.core.stat.Mean;
import rapaio.core.stat.Sum;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.Var;
import rapaio.data.VarDouble;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/28/18.
 */
public class NumValueSummary extends AbstractFeatureGroupGenerator {

    private final List<String> varNames = new ArrayList<>();
    private final List<Aggregate> aggregates;
    private final HashMap<String, HashMap<Key, VarDouble>> collector = new HashMap<>();

    protected NumValueSummary(VRange range, List<Aggregate> aggregates) {
        super(range);
        this.aggregates = aggregates;
    }

    @Override
    public void fit(Frame source, List<String> keys) {

        Frame applyVars = source.mapVars(range);
        Set<String> selection = new HashSet<>(Arrays.asList(applyVars.varNames()));
        for(String key : keys) {
            if(selection.contains(key)) {
                applyVars = applyVars.removeVars(VRange.of(key));
            }
        }
        for (Var var : applyVars.varList()) {
            varNames.add(var.name());
            collector.put(var.name(), new HashMap<>());
            for (int row = 0; row < var.rowCount(); row++) {
                Key key = Key.from(row, source, keys);
                if (!collector.get(var.name()).containsKey(key)) {
                    collector.get(var.name()).put(key, VarDouble.empty());
                }
                collector.get(var.name()).get(key).addDouble(var.getDouble(row));
            }
        }
    }

    @Override
    public List<Var> generate(Frame df, List<String> keys) {
        List<Var> features = new ArrayList<>();
        for (String varName : varNames) {
            for (Aggregate aggregate : aggregates) {
                VarDouble var = VarDouble.empty().withName(varName + "_" + aggregate.name);
                for (int i = 0; i < df.rowCount(); i++) {
                    Key key = Key.from(i, df, keys);
                    VarDouble from = collector.get(varName).getOrDefault(key, null);
                    var.addDouble(from == null ? Double.NaN : aggregate.fun.apply(from));
                }
                boolean same = true;
                for (int i = 0; i < var.rowCount(); i++) {
                    if (i > 0 && var.getDouble(i) != var.getDouble(i - 1)) {
                        same = false;
                        break;
                    }
                }
                if (!same) {
                    features.add(var);
                }
            }
        }
        return features;
    }

    public static class Aggregate {

        private final String name;
        private final Function<VarDouble, Double> fun;

        public Aggregate(String name, Function<VarDouble, Double> fun) {
            this.name = name;
            this.fun = fun;
        }
    }

    public static Aggregate sum = new Aggregate("sum", v -> Sum.of(v).value());
    public static Aggregate mean = new Aggregate("mean", v -> Mean.of(v).value());
    public static Aggregate variance = new Aggregate("variance", v -> Variance.of(v).value());
    public static Aggregate count = new Aggregate("count", v -> (double) v.rowCount());
}
