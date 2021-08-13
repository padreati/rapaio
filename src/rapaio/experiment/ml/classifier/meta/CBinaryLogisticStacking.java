/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.experiment.ml.classifier.meta;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.data.filter.VApply;
import rapaio.ml.classifier.ClassifierModel;
import rapaio.ml.classifier.ClassifierResult;
import rapaio.ml.classifier.DefaultHookInfo;
import rapaio.ml.classifier.linear.BinaryLogistic;
import rapaio.ml.common.Capabilities;
import rapaio.printer.Printable;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toList;

/**
 * Stacking with Binary Logistic as stacking classifier
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/30/15.
 */
public class CBinaryLogisticStacking extends ClassifierModel<CBinaryLogisticStacking, ClassifierResult, DefaultHookInfo> implements Printable {

    @Serial
    private static final long serialVersionUID = -9087871586729573030L;

    private static final Logger logger = Logger.getLogger(CBinaryLogisticStacking.class.getName());

    private final List<ClassifierModel<?, ?, ?>> weaks = new ArrayList<>();
    private final BinaryLogistic log = BinaryLogistic.newModel();
    private double tol = 1e-5;
    private int maxRuns = 1_000_000;

    public CBinaryLogisticStacking withLearners(ClassifierModel<?, ?, ?>... learners) {
        weaks.clear();
        Collections.addAll(weaks, learners);
        return this;
    }

    public CBinaryLogisticStacking withTol(double tol) {
        this.tol = tol;
        return this;
    }

    public CBinaryLogisticStacking withMaxRuns(int maxRuns) {
        this.maxRuns = maxRuns;
        return this;
    }

    @Override
    public CBinaryLogisticStacking newInstance() {
        return new CBinaryLogisticStacking().copyParameterValues(this);
    }

    @Override
    public String name() {
        return "CBinaryLogisticStacking";
    }

    @Override
    public String fullName() {
        return null;
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities(
                1, 100_000,
                Arrays.asList(VarType.BINARY, VarType.INT, VarType.DOUBLE), false,
                1, 1, List.of(VarType.NOMINAL), false);
    }

    @Override
    protected FitSetup prepareFit(Frame df, Var weights, String... targetVars) {
        logger.config("predict method called.");
        List<Var> vars = new ArrayList<>();
        int pos = 0;
        logger.config("check learners for learning.... ");
        weaks.parallelStream().map(weak -> {
            if (!weak.hasLearned()) {
                logger.config("started learning for weak learner ...");
                weak.fit(df, weights, targetVars);
            }
            logger.config("started fitting weak learner...");
            return weak.predict(df).firstDensity().rvar(1);
        }).collect(toList()).forEach(var -> vars.add(var.copy().name("V" + vars.size())));

        List<Var> quadratic = vars.stream()
                .map(v -> v.copy()
                        .fapply(VApply.onDouble(x -> x * x))
                        .name(v.name() + "^2"))
                .collect(toList());
        vars.addAll(quadratic);

        List<String> targets = VarRange.of(targetVars).parseVarNames(df);
        vars.add(df.rvar(targets.get(0)).copy());

        return FitSetup.valueOf(SolidFrame.byVars(vars), weights, targetVars);
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        logger.config("started learning for binary logistic...");
        log.eps.set(tol);
        log.runs.set(maxRuns);
        log.fit(df, weights, targetNames());

        logger.config("end predict method call");
        return true;
    }

    @Override
    protected PredSetup preparePredict(Frame df, boolean withClasses, boolean withDistributions) {
        logger.config("predict method called.");
        List<Var> vars = new ArrayList<>();

        weaks.parallelStream().map(weak -> {
            logger.config("started fitting weak learner ...");
            return weak.predict(df).firstDensity().rvar(1);
        }).collect(toList()).forEach(var -> vars.add(var.copy().name("V" + vars.size())));

        List<Var> quadratic = vars.stream()
                .map(v -> v.copy().fapply(VApply.onDouble(x -> x * x)).name(v.name() + "^2"))
                .collect(toList());
        vars.addAll(quadratic);
        return PredSetup.valueOf(SolidFrame.byVars(vars), withClasses, withDistributions);
    }

    @Override
    protected ClassifierResult corePredict(Frame df, boolean withClasses, boolean withDistributions) {
        return ClassifierResult.copy(this, df, withClasses, withDistributions, log.predict(df));
    }
}
