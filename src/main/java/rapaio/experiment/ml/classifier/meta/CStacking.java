/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.ensemble.CForest;
import rapaio.ml.common.Capabilities;
import rapaio.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toList;

/**
 * Stacking with a stacking classifier
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/30/15.
 */
public class CStacking extends AbstractClassifier {

    private static final long serialVersionUID = -9087871586729573030L;

    private static final Logger logger = Logger.getLogger(CStacking.class.getName());

    private List<Classifier> weaks = new ArrayList<>();
    private Classifier stacker = CForest.newRF();

    public CStacking withLearners(Classifier... learners) {
        weaks.clear();
        Collections.addAll(weaks, learners);
        return this;
    }

    public CStacking withStacker(Classifier stacker) {
        this.stacker = stacker;
        return this;
    }

    @Override
    public Classifier newInstance() {
        return new CStacking()
                .withLearners(weaks.stream().map(Classifier::newInstance).toArray(Classifier[]::new))
                .withStacker(stacker.newInstance())
                .withRunPoolSize(runPoolSize())
                .withRunningHook(runningHook())
                .withRuns(runs())
                .withInputFilters(inputFilters());
    }

    @Override
    public String name() {
        return "CStacking";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append("CStacking{stacker=").append(stacker.fullName()).append(";");
        return sb.toString();
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .withAllowMissingTargetValues(false)
                .withAllowMissingInputValues(false)
                .withInputTypes(VarType.BINARY, VarType.INDEX, VarType.NUMERIC)
                .withTargetTypes(VarType.NOMINAL)
                .withInputCount(1, 100_000)
                .withTargetCount(1, 1);
    }

    protected BaseTrainSetup baseTrain(Frame df, Var w, String... targetVars) {
        logger.fine("train method called.");
        int pos = 0;
        logger.fine("check learners for learning.... ");
        List<Var> vars =
                Util.rangeStream(weaks.size(), true)
                        .boxed()
                        .map(i -> {
                            if (!weaks.get(i).hasLearned()) {
                                logger.fine("started learning for weak learner ...");
                                weaks.get(i).train(df, w, targetVars);
                            }
                            logger.fine("started fitting weak learner...");
                            return weaks.get(i).fit(df).firstDensity().var(1).solidCopy()
                                    .withName("V" + i);
                        })
                        .collect(toList());

        List<String> targets = VRange.of(targetVars).parseVarNames(df);
        vars.add(df.var(targets.get(0)).solidCopy());

        return BaseTrainSetup.valueOf(SolidFrame.newByVars(vars), w, targetVars);
    }

    @Override
    protected boolean coreTrain(Frame df, Var weights) {

        logger.fine("started learning for stacker classifier...");
        stacker.train(df, weights, targetNames());

        logger.fine("end train method call");
        return true;
    }

    protected BaseFitSetup baseFit(Frame df, boolean withClasses, boolean withDistributions) {
        logger.fine("fit method called.");
        List<Var> vars = Util.rangeStream(weaks.size(), true)
                .boxed()
                .map(i -> {
                    logger.fine("started fitting weak learner ...");
                    return weaks.get(i)
                            .fit(df)
                            .firstDensity()
                            .var(1)
                            .solidCopy()
                            .withName("V" + i);
                }).collect(toList());
        return BaseFitSetup.valueOf(SolidFrame.newByVars(vars), withClasses, withDistributions);
    }

    @Override
    protected CFit coreFit(Frame df, boolean withClasses, boolean withDistributions) {
        logger.fine("started fitting stacker classifier .. ");
        CFit fit = stacker.fit(df);

        logger.fine("end fit method call");
        return fit;
    }
}
