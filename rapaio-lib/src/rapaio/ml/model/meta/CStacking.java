/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

package rapaio.ml.model.meta;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import rapaio.core.param.ListParam;
import rapaio.core.param.ValueParam;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.ml.common.Capabilities;
import rapaio.ml.model.ClassifierModel;
import rapaio.ml.model.ClassifierResult;
import rapaio.ml.model.RunInfo;
import rapaio.printer.Printable;

/**
 * Stacking with a stacking classifier
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/30/15.
 */
public class CStacking extends ClassifierModel<CStacking, ClassifierResult, RunInfo<CStacking>> implements Printable {

    public static CStacking newModel() {
        return new CStacking();
    }

    @Serial
    private static final long serialVersionUID = -9087871586729573030L;
    private static final Logger logger = Logger.getLogger(CStacking.class.getName());

    public final ListParam<ClassifierModel<?, ?, ?>, CStacking> learners = new ListParam<>(this, List.of(), "learners", (__, ___) -> true);
    public final ValueParam<ClassifierModel<?, ?, ?>, CStacking> stackModel = new ValueParam<>(this, null, "stacker", Objects::nonNull);

    private List<ClassifierModel<?, ?, ?>> weaks;
    private ClassifierModel<?, ?, ?> stack;

    /**
     * @return the weak learners fitted by the last call to {@code fit}, or null if not fitted
     */
    public List<ClassifierModel<?, ?, ?>> fittedLearners() {
        return weaks;
    }

    /**
     * @return the stack model fitted by the last call to {@code fit}, or null if not fitted
     */
    public ClassifierModel<?, ?, ?> fittedStackModel() {
        return stack;
    }

    @Override
    public CStacking newInstance() {
        return new CStacking().copyParameterValues(this);
    }

    @Override
    public String name() {
        return "CStacking";
    }

    @Override
    public String fullName() {
        return name() + "{stacker=" + stackModel.get().fullName() + ";";
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .inputs(1, 100_000, false, VarType.BINARY, VarType.INT, VarType.DOUBLE)
                .targets(1, 1, false, VarType.NOMINAL);
    }

    private void validateParams() {
        if (stackModel.get() == null) {
            throw new IllegalStateException("Stack model is not configured.");
        }
        if (learners.get().isEmpty()) {
            throw new IllegalStateException("At least a weak learner is needed.");
        }
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {

        validateParams();

        logger.fine("Initialize weak and stack models");
        weaks = new ArrayList<>(learners.get().stream().map(ClassifierModel::newInstance).toList());
        stack = stackModel.get().newInstance();
        learned = false;

        logger.fine("started learning for stacker classifier...");
        for (int i = 0; i < weaks.size(); i++) {
            logger.fine("started fitting weak learner " + i + " from " + weaks.size());
            var weak = weaks.get(i);
            weak.seed.set(seed.get());
            weak.fit(df, weights, targetNames);
        }
        List<Var> vars = stackFeatures(df);
        vars.addAll(df.mapVars(targetNames).copy().varList());
        stack.seed.set(seed.get());
        stack.fit(SolidFrame.byVars(vars), weights, targetNames);
        return true;
    }

    /**
     * Features handed to the stack model: the predicted class densities of every weak learner, one column per
     * target level, named {@code <level>_<learnerIndex>}. Used identically at fit and predict time so the stack
     * model always sees the same columns.
     */
    private List<Var> stackFeatures(Frame df) {
        List<Var> vars = new ArrayList<>();
        int levels = firstTargetLevels().size();
        for (int i = 0; i < weaks.size(); i++) {
            var density = weaks.get(i).predict(df, true, true).firstDensity();
            for (int j = 0; j < levels; j++) {
                vars.add(density.rvar(j).name(density.rvar(j).name() + "_" + i));
            }
        }
        return vars;
    }

    @Override
    protected ClassifierResult corePredict(Frame df, boolean withClasses, boolean withDistributions) {
        logger.fine("predict method called.");
        List<Var> vars = stackFeatures(df);
        return ClassifierResult.copy(this, df, withClasses, withDistributions, stack.predict(SolidFrame.byVars(vars)));
    }
}
