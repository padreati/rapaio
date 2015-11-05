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

package rapaio.ml.regressor;

import rapaio.data.VarType;
import rapaio.data.sample.FrameSampler;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.ml.classifier.Classifier;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Abstract class needed to implement prerequisites for all regression algorithms.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/20/14.
 */
public abstract class AbstractRegression implements Regression {

    private static final long serialVersionUID = 5544999078321108408L;

    private String[] inputNames;
    private VarType[] inputTypes;
    private String[] targetNames;
    private VarType[] targetTypes;
    private FrameSampler sampler = new FrameSampler.Identity();
    private boolean hasLearned;
    private int poolSize = Runtime.getRuntime().availableProcessors();
    private int runs = 1;
    private BiConsumer<Regression, Integer> runningHook;

    @Override
    public String[] inputNames() {
        return inputNames;
    }

    @Override
    public String[] targetNames() {
        return targetNames;
    }

    @Override
    public FrameSampler sampler() {
        return sampler;
    }

    @Override
    public AbstractRegression withSampler(FrameSampler sampler) {
        this.sampler = sampler;
        return this;
    }

    @Override
    public int runs() {
        return runs;
    }

    public Regression withRuns(int runs) {
        this.runs = runs;
        return this;
    }

    public void prepareTraining(Frame df, Var weights, String... targetVarNames) {
        List<String> targetVarsList = new VarRange(targetVarNames).parseVarNames(df);
        this.targetNames = targetVarsList.toArray(new String[targetVarsList.size()]);
        this.targetTypes = targetVarsList.stream().map(varName -> df.var(varName).type()).toArray(VarType[]::new);

        HashSet<String> targets = new HashSet<>(targetVarsList);
        List<String> inputs = Arrays.stream(df.varNames()).filter(varName -> !targets.contains(varName)).collect(Collectors.toList());
        this.inputNames = inputs.stream().toArray(String[]::new);
        this.inputTypes = inputs.stream().map(varName -> df.var(varName).type()).toArray(VarType[]::new);

        hasLearned = true;
    }

    @Override
    public boolean hasLearned() {
        return hasLearned;
    }

    @Override
    public VarType[] inputTypes() {
        return inputTypes;
    }

    @Override
    public VarType[] targetTypes() {
        return targetTypes;
    }

    @Override
    public Regression withPoolSize(int poolSize) {
        this.poolSize = poolSize < 0 ? Runtime.getRuntime().availableProcessors() : poolSize;
        return this;
    }

    @Override
    public int poolSize() {
        return poolSize;
    }

    @Override
    public BiConsumer<Regression, Integer> runningHook() {
        return runningHook;
    }

    @Override
    public Regression withRunningHook(BiConsumer<Regression, Integer> runningHook) {
        this.runningHook = runningHook;
        return this;
    }
}
