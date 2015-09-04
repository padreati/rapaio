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

import rapaio.data.sample.FrameSampler;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstract base class for all classifiers.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public abstract class AbstractClassifier implements Classifier {

    private static final long serialVersionUID = -6866948033065091047L;
    protected String[] inputNames;
    protected String[] targetNames;
    protected Map<String, String[]> dict;
    protected FrameSampler sampler = new FrameSampler.Identity();
    protected boolean debug = false;

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
    public Classifier withDebug(boolean debug) {
        this.debug = debug;
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

    /**
     * This method is prepares learning phase. It is a generic method which works
     * for all learners. It's taks includes initialization of target names,
     * input names, check the capabilities at learning phase, etc.
     *
     * @param df         data frame
     * @param weights    weights of instances
     * @param targetVars target variable names
     */
    public void prepareLearning(Frame df, Var weights, String... targetVars) {

        if (targetVars.length == 0) {
            throw new IllegalArgumentException("At least a target var name should be specified at learning time.");
        }
        List<String> targetVarsList = new VarRange(targetVars).parseVarNames(df);
        this.targetNames = targetVarsList.toArray(new String[targetVarsList.size()]);
        this.dict = new HashMap<>();
        this.dict.put(firstTargetName(), df.var(firstTargetName()).dictionary());

        HashSet<String> targets = new HashSet<>(targetVarsList);
        List<String> inputs = Arrays.stream(df.varNames()).filter(varName -> !targets.contains(varName)).collect(Collectors.toList());
        this.inputNames = inputs.toArray(new String[inputs.size()]);

        capabilities().checkAtLearnPhase(df, weights, targetVars);
    }

    @Override
    public String summary() {
        return "not implemented";
    }
}
