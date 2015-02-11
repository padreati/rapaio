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
 */

package rapaio.ml.classifier;

import rapaio.core.sample.Sampler;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public abstract class AbstractClassifier implements Classifier {

    private String[] inputNames;
    private String[] targetNames;
    private Map<String, String[]> dict;
    private Sampler sampler = new Sampler.Identity();

    @Override
    public Sampler sampler() {
        return sampler;
    }

    @Override
    public AbstractClassifier withSampler(Sampler sampler) {
        this.sampler = sampler;
        return this;
    }

    @Override
    public String[] inputNames() {
        return inputNames;
    }

    @Override
    public String[] targetNames() {
        return targetNames;
    }

    @Override
    public Map<String, String[]> dictionaries() {
        return dict;
    }

    public void prepareLearning(Frame df, Var weights, String... targetVarNames) {

        if (targetVarNames.length == 0) {
            throw new IllegalArgumentException("At least a target var name should be specified at learning time.");
        }
        List<String> targetVarsList = new VarRange(targetVarNames).parseVarNames(df);
        this.targetNames = targetVarsList.toArray(new String[targetVarsList.size()]);
        this.dict = new HashMap<>();
        this.dict.put(firstTargetName(), df.var(firstTargetName()).dictionary());

        HashSet<String> targets = new HashSet<>(targetVarsList);
        List<String> inputs = Arrays.stream(df.varNames()).filter(varName -> !targets.contains(varName)).collect(Collectors.toList());
        this.inputNames = inputs.toArray(new String[inputs.size()]);
    }
}
