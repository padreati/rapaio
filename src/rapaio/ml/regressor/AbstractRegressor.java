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

package rapaio.ml.regressor;

import rapaio.core.sample.Sampler;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract class needed to implement prerequisites for all regression algorithms.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/20/14.
 */
public abstract class AbstractRegressor implements Regressor {

    private String[] inputNames;
    private String[] targetNames;
    private Sampler sampler = new Sampler.Identity();

    @Override
    public String[] inputNames() {
        return inputNames;
    }

    @Override
    public String[] targetNames() {
        return targetNames;
    }

    @Override
    public Sampler sampler() {
        return sampler;
    }

    @Override
    public AbstractRegressor withSampler(Sampler sampler) {
        this.sampler = sampler;
        return this;
    }


    public void prepareLearning(Frame df, Var weights, String... targetVarNames) {
        List<String> targetVarsList = new VarRange(targetVarNames).parseVarNames(df);
        this.targetNames = targetVarsList.toArray(new String[targetVarsList.size()]);

        HashSet<String> targets = new HashSet<>(targetVarsList);
        List<String> inputs = Arrays.stream(df.varNames()).filter(varName -> !targets.contains(varName)).collect(Collectors.toList());
        this.inputNames = inputs.toArray(new String[inputs.size()]);
    }
}
