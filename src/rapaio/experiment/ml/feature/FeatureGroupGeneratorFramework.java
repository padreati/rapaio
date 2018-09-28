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

package rapaio.experiment.ml.feature;

import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.Var;
import rapaio.data.VType;
import rapaio.experiment.ml.feature.generator.FeatureGroupGenerator;
import rapaio.experiment.ml.feature.generator.NumValueSummary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/28/18.
 */
public class FeatureGroupGeneratorFramework {

    private final Frame source;
    private final List<String> keys;

    private List<FeatureGroupGenerator> generators = new ArrayList<>();

    public FeatureGroupGeneratorFramework(Frame source, String... keys) {
        this.source = source;
        this.keys = Arrays.asList(keys);
    }

    public void useDefaultGenerators() {

        generators.clear();
        generators.add(FeatureGroupGenerator.nomLabelCount(VRange.onlyTypes(VType.NOMINAL), true));
        generators.add(FeatureGroupGenerator.numVarStat(
                VRange.onlyTypes(VType.DOUBLE, VType.INT),
                Arrays.asList(NumValueSummary.count, NumValueSummary.sum, NumValueSummary.mean, NumValueSummary.variance)
        ));
    }

    public void clearGenerators() {
        generators.clear();
    }

    public FeatureGroupGeneratorFramework withGenerator(FeatureGroupGenerator generator) {
        this.generators.add(generator);
        return this;
    }

    public Frame decorate(Frame df) {
        for (FeatureGroupGenerator generator : generators) {
            generator.fit(source, keys);
        }

        List<Var> features = new ArrayList<>();
        for (FeatureGroupGenerator generator : generators) {
            features.addAll(generator.generate(df, keys));
        }
        return df.bindVars(features.toArray(new Var[0]));
    }
}
