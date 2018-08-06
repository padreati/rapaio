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

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import rapaio.data.BoundFrame;
import rapaio.data.Frame;
import rapaio.data.NumVar;
import rapaio.data.VRange;
import rapaio.data.Var;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/28/18.
 */
public class NomLabelCount extends AbstractFeatureGroupGenerator {

    boolean normalize;
    private List<String> labelKeys = new ArrayList<>();
    private HashMap<String, List<String>> groups = new HashMap<>();
    private HashMap<String, Object2DoubleOpenHashMap<Key>> counts = new HashMap<>();

    protected NomLabelCount(VRange range, boolean normalize) {
        super(range);
        this.normalize = normalize;
    }

    @Override
    public void fit(Frame source, List<String> keys) {
        Frame applyVars = source.mapVars(range);
        Set<String> varNames = new HashSet<>(Arrays.asList(applyVars.varNames()));
        for (String key : keys) {
            if (varNames.contains(key)) {
                applyVars = applyVars.removeVars(key);
            }
        }
        for (Var var : applyVars.varList()) {
            groups.put(var.name(), new ArrayList<>());
            for (String level : var.levels()) {
                String labelKey = var.name() + "_" + level;
                labelKeys.add(labelKey);
                groups.get(var.name()).add(labelKey);
                if (!counts.containsKey(labelKey)) {
                    counts.put(labelKey, new Object2DoubleOpenHashMap<>());
                }
            }
            for (int row = 0; row < var.rowCount(); row++) {
                String labelKey = var.name() + "_" + var.label(row);
                Key key = Key.from(row, source, keys);
                if (!counts.get(labelKey).containsKey(key)) {
                    counts.get(labelKey).put(key, 0);
                }
                counts.get(labelKey).put(key, counts.get(labelKey).getDouble(key) + 1);
            }
        }
    }

    @Override
    public List<Var> generate(Frame df, List<String> keys) {
        List<Var> features = new ArrayList<>();
        for (String labelKey : labelKeys) {
            NumVar index = NumVar.empty().withName(labelKey);
            for (int row = 0; row < df.rowCount(); row++) {
                Key key = Key.from(row, df, keys);
                index.addValue(counts.get(labelKey).getOrDefault(key, 0));
            }
            boolean empty = true;
            for (int i = 0; i < index.rowCount(); i++) {
                if (index.index(i) > 0) {
                    empty = false;
                    break;
                }
            }
            if (!empty) {
                features.add(index);
            }
        }

        if (normalize) {
            Frame bound = BoundFrame.byVars(features);
            for (String group : groups.keySet()) {
                List<Var> vars = new ArrayList<>();
                // collect vars from group
                for (String name : groups.get(group)) {
                    try {
                        vars.add(bound.rvar(name));
                    } catch (Throwable ignored) {
                    }
                }
                // normalize each row
                for (int row = 0; row < bound.rowCount(); row++) {
                    double sum = 0.0;
                    for (int i = 0; i < vars.size(); i++) {
                        sum += vars.get(i).value(row);
                    }
                    // normalize
                    for (int i = 0; i < vars.size(); i++) {
                        vars.get(i).setValue(row, vars.get(i).value(row) / sum);
                    }
                }
            }
        }

        return features;
    }
}
