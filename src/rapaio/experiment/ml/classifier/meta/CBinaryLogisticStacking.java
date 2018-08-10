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

package rapaio.experiment.ml.classifier.meta;

import rapaio.data.*;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CPrediction;
import rapaio.ml.classifier.Classifier;
import rapaio.experiment.ml.classifier.linear.BinaryLogistic;
import rapaio.ml.common.Capabilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toList;

/**
 * Stacking with Binary Logistic as stacking classifier
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/30/15.
 */
public class CBinaryLogisticStacking extends AbstractClassifier {

    private static final long serialVersionUID = -9087871586729573030L;

    private static final Logger logger = Logger.getLogger(CBinaryLogisticStacking.class.getName());

    private List<Classifier> weaks = new ArrayList<>();
    private BinaryLogistic log = new BinaryLogistic();
    private double tol = 1e-5;
    private int maxRuns = 1_000_000;

    public CBinaryLogisticStacking withLearners(Classifier... learners) {
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
    public Classifier newInstance() {
        return new CBinaryLogisticStacking();
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
        return new Capabilities()
                .withAllowMissingTargetValues(false)
                .withAllowMissingInputValues(false)
                .withInputTypes(VarType.BOOLEAN, VarType.INT, VarType.DOUBLE)
                .withTargetTypes(VarType.NOMINAL)
                .withInputCount(1, 100_000)
                .withTargetCount(1, 1);
    }

    @Override
    protected BaseTrainSetup baseFit(Frame df, Var weights, String... targetVars) {
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
        }).collect(toList()).forEach(var -> vars.add(var.solidCopy().withName("V" + vars.size())));

        List<Var> quadratic = vars.stream()
                .map(v -> v.solidCopy().stream().transValue(x -> x * x).toMappedVar().withName(v.name() + "^2").solidCopy())
                .collect(toList());
        vars.addAll(quadratic);

        List<String> targets = VRange.of(targetVars).parseVarNames(df);
        vars.add(df.rvar(targets.get(0)).solidCopy());

        return BaseTrainSetup.valueOf(SolidFrame.byVars(vars), weights, targetVars);
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        logger.config("started learning for binary logistic...");
        log.withTol(tol);
        log.withMaxRuns(maxRuns);
        log.fit(df, weights, targetNames());

        logger.config("end predict method call");
        return true;
    }

    @Override
    protected BaseFitSetup basePredict(Frame df, boolean withClasses, boolean withDistributions) {
        logger.config("predict method called.");
        List<Var> vars = new ArrayList<>();

        weaks.parallelStream().map(weak -> {
            logger.config("started fitting weak learner ...");
            return weak.predict(df).firstDensity().rvar(1);
        }).collect(toList()).forEach(var -> vars.add(var.solidCopy().withName("V" + vars.size())));

        List<Var> quadratic = vars.stream()
                .map(v -> v.solidCopy().stream().transValue(x -> x * x).toMappedVar().withName(v.name() + "^2").solidCopy())
                .collect(toList());
        vars.addAll(quadratic);
        return BaseFitSetup.valueOf(SolidFrame.byVars(vars), withClasses, withDistributions);
    }

    @Override
    protected CPrediction corePredict(Frame df, boolean withClasses, boolean withDistributions) {
        logger.config("started fitting binary logistic regression.. ");
        CPrediction fit = log.predict(df);

        logger.config("end predict method call");
        return fit;
    }
}
