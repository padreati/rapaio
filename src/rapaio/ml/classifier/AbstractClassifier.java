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

package rapaio.ml.classifier;

import rapaio.data.VarType;
import rapaio.data.sample.FrameSampler;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

/**
 * Abstract base class for all classifiers.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public abstract class AbstractClassifier implements Classifier {

    private static final long serialVersionUID = -6866948033065091047L;
    private String[] inputNames;
    private VarType[] inputTypes;
    private String[] targetNames;
    private VarType[] targetTypes;
    private Map<String, String[]> dict;
    private FrameSampler sampler = new FrameSampler.Identity();
    private boolean debug = false;
    private boolean learned = false;

    @Override
    public FrameSampler sampler() {
        return sampler;
    }

    @Override
    public AbstractClassifier withSampler(FrameSampler sampler) {
        this.sampler = sampler;
        return this;
    }

    @Override
    public boolean debug() {
        return debug;
    }

    @Override
    public Classifier withDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    @Override
    public String[] inputNames() {
        return inputNames;
    }

    @Override
    public VarType[] inputTypes() {
        return inputTypes;
    }

    @Override
    public String[] targetNames() {
        return targetNames;
    }

    @Override
    public VarType[] targetTypes() {
        return targetTypes;
    }

    @Override
    public Map<String, String[]> dictionaries() {
        return dict;
    }

    public boolean isLearned() {
        return learned;
    }

    /**
     * This method is prepares learning phase. It is a generic method which works
     * for all learners. It's taks includes initialization of target names,
     * input names, check the capabilities at learning phase, etc.
     *
     * @param df         data frame
     * @param weights    weights of instances
     * @param targetVars target variable names
     */
    public void prepareLearning(final Frame df, final Var weights, final String... targetVars) {

        if (targetVars.length == 0) {
            throw new IllegalArgumentException("At least a target var name should be specified at learning time.");
        }
        List<String> targets = new VarRange(targetVars).parseVarNames(df);
        this.targetNames = targets.stream().toArray(String[]::new);
        this.targetTypes = targets.stream().map(name -> df.var(name).type()).toArray(VarType[]::new);
        this.dict = new HashMap<>();
        this.dict.put(firstTargetName(), df.var(firstTargetName()).levels());

        HashSet<String> targetSet = new HashSet<>(targets);
        List<String> inputs = Arrays.stream(df.varNames()).filter(varName -> !targetSet.contains(varName)).collect(Collectors.toList());
        this.inputNames = inputs.stream().toArray(String[]::new);
        this.inputTypes = inputs.stream().map(name -> df.var(name).type()).toArray(VarType[]::new);

        capabilities().checkAtLearnPhase(df, weights, targetVars);
        learned = true;
    }

    @Override
    public String summary() {
        return "not implemented";
    }

    public String baseSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("input vars: \n");
        IntStream.range(0, inputNames().length).forEach(i ->
                sb.append("> ").append(inputName(i)).append(" : ").append(inputType(i)).append("\n"));
        sb.append("target vars:\n");
        IntStream.range(0, targetNames().length).forEach(i -> sb.append("> ")
                .append(targetName(i)).append(" : ")
                .append(targetType(i))
                .append(" [").append(Arrays.stream(dictionary(targetName(i))).collect(joining(","))).append("]")
                .append("\n"));
        return sb.toString();
    }
}
