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

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.data.filter.FFilter;
import rapaio.data.sample.FrameSampler;
import rapaio.printer.format.TextTable;

import java.util.*;
import java.util.function.BiConsumer;
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
    private List<FFilter> inputFilters = new ArrayList<>();
    private String[] inputNames;
    private VarType[] inputTypes;
    private String[] targetNames;
    private VarType[] targetTypes;
    private Map<String, String[]> dict;
    private FrameSampler sampler = new FrameSampler.Identity();
    private boolean learned = false;
    private int poolSize = Runtime.getRuntime().availableProcessors();
    private int runs = 1;
    private BiConsumer<Classifier, Integer> runningHook;

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
    public List<FFilter> inputFilters() {
        return inputFilters;
    }

    @Override
    public Classifier withInputFilters(FFilter... filters) {
        inputFilters.clear();
        Collections.addAll(inputFilters, filters);
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
    public Map<String, String[]> targetLevels() {
        return dict;
    }

    public boolean hasLearned() {
        return learned;
    }

    /**
     * This method is prepares learning phase. It is a generic method which works
     * for all learners. It's taks includes initialization of target names,
     * input names, check the capabilities at learning phase, etc.
     *
     * @param dfOld      data frame
     * @param weights    weights of instances
     * @param targetVars target variable names
     */
    public Frame prepareLearning(Frame dfOld, final Var weights, final String... targetVars) {

        if (targetVars.length == 0) {
            throw new IllegalArgumentException("At least a target var name should be specified at learning time.");
        }
        Frame df = dfOld;
        for (FFilter filter : inputFilters) {
            df = filter.filter(df);
        }
        Frame result = df;
        List<String> targets = new VarRange(targetVars).parseVarNames(result);
        this.targetNames = targets.stream().toArray(String[]::new);
        this.targetTypes = targets.stream().map(name -> result.var(name).type()).toArray(VarType[]::new);
        this.dict = new HashMap<>();
        this.dict.put(firstTargetName(), result.var(firstTargetName()).levels());

        HashSet<String> targetSet = new HashSet<>(targets);
        List<String> inputs = Arrays.stream(result.varNames()).filter(varName -> !targetSet.contains(varName)).collect(Collectors.toList());
        this.inputNames = inputs.stream().toArray(String[]::new);
        this.inputTypes = inputs.stream().map(name -> result.var(name).type()).toArray(VarType[]::new);

        capabilities().checkAtLearnPhase(result, weights, targetVars);
        learned = true;
        return result;
    }

    public Frame prepareFit(Frame df) {
        Frame result = df;
        for (FFilter filter : inputFilters) {
            result = filter.apply(result);
        }
        return result;
    }

    @Override
    public String summary() {
        return "not implemented";
    }

    public String baseSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("input vars: \n");

        int varCount = inputNames.length;
        TextTable tt = TextTable.newEmpty(varCount, 5);
        for (int i = 0; i < varCount; i++) {
            tt.set(i, 0, i + ".", 1);
            tt.set(i, 1, inputNames[i], 1);
            tt.set(i, 2, ":", -1);
            tt.set(i, 3, inputTypes[i].name(), -1);
            tt.set(i, 4, " |", 1);
        }
        tt.withMerge();
        sb.append("\n").append(tt.summary()).append("\n");

        sb.append("target vars:\n");
        IntStream.range(0, targetNames().length).forEach(i -> sb.append("> ")
                .append(targetName(i)).append(" : ")
                .append(targetType(i))
                .append(" [").append(Arrays.stream(targetLevels(targetName(i))).collect(joining(","))).append("]")
                .append("\n"));
        return sb.toString();
    }

    @Override
    public AbstractClassifier withPoolSize(int poolSize) {
        this.poolSize = poolSize < 0 ? Runtime.getRuntime().availableProcessors() : poolSize;
        return this;
    }

    @Override
    public int poolSize() {
        return poolSize;
    }

    @Override
    public int runs() {
        return runs;
    }

    @Override
    public Classifier withRuns(int runs) {
        this.runs = runs;
        return this;
    }

    @Override
    public BiConsumer<Classifier, Integer> runningHook() {
        return runningHook;
    }

    @Override
    public Classifier withRunningHook(BiConsumer<Classifier, Integer> runningHook) {
        this.runningHook = runningHook;
        return this;
    }
}
