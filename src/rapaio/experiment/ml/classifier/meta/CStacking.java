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

import static java.util.stream.Collectors.toList;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.ml.common.Capabilities;
import rapaio.ml.model.ClassifierModel;
import rapaio.ml.model.ClassifierResult;
import rapaio.ml.model.RunInfo;
import rapaio.ml.model.ensemble.CForest;
import rapaio.printer.Printable;

/**
 * Stacking with a stacking classifier
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/30/15.
 */
public class CStacking extends ClassifierModel<CStacking, ClassifierResult, RunInfo<CStacking>> implements Printable {

    @Serial
    private static final long serialVersionUID = -9087871586729573030L;

    private static final Logger logger = Logger.getLogger(CStacking.class.getName());

    private final List<ClassifierModel<?, ?, ?>> weaks = new ArrayList<>();
    private ClassifierModel<?, ?, ?> stacker = CForest.newModel();

    public CStacking withLearners(ClassifierModel<?, ?, ?>... learners) {
        weaks.clear();
        Collections.addAll(weaks, learners);
        return this;
    }

    public CStacking withStacker(ClassifierModel<?, ?, ?> stacker) {
        this.stacker = stacker;
        return this;
    }

    @Override
    public CStacking newInstance() {
        return new CStacking().copyParameterValues(this)
                .withLearners(weaks.stream().map(ClassifierModel::newInstance).toArray(ClassifierModel[]::new))
                .withStacker(stacker.newInstance());
    }

    @Override
    public String name() {
        return "CStacking";
    }

    @Override
    public String fullName() {
        return "CStacking{stacker=" + stacker.fullName() + ";";
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .inputs(1, 100_000, false, VarType.BINARY, VarType.INT, VarType.DOUBLE)
                .targets(1, 1, false, VarType.NOMINAL);
    }

    protected FitSetup baseFit(Frame df, Var w, String... targetVars) {
        logger.fine("predict method called.");
        int pos = 0;
        logger.fine("check learners for learning.... ");
        List<Var> vars =
                IntStream.range(0, weaks.size()).parallel()
                        .boxed()
                        .map(i -> {
                            if (!weaks.get(i).hasLearned()) {
                                logger.fine("started learning for weak learner ...");
                                weaks.get(i).fit(df, w, targetVars);
                            }
                            logger.fine("started fitting weak learner...");
                            return weaks.get(i).predict(df).firstDensity().rvar(1).copy()
                                    .name("V" + i);
                        })
                        .collect(toList());

        List<String> targets = VarRange.of(targetVars).parseVarNames(df);
        vars.add(df.rvar(targets.get(0)).copy());

        return FitSetup.valueOf(SolidFrame.byVars(vars), w, targetVars);
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {

        logger.fine("started learning for stacker classifier...");
        stacker.fit(df, weights, targetNames());

        logger.fine("end predict method call");
        return true;
    }

    protected PredSetup baseFit(Frame df, boolean withClasses, boolean withDistributions) {
        logger.fine("predict method called.");
        List<Var> vars = IntStream.range(0, weaks.size()).parallel()
                .boxed()
                .map(i -> {
                    logger.fine("started fitting weak learner ...");
                    return weaks.get(i)
                            .predict(df)
                            .firstDensity()
                            .rvar(1)
                            .copy()
                            .name("V" + i);
                }).collect(toList());
        return PredSetup.valueOf(SolidFrame.byVars(vars), withClasses, withDistributions);
    }

    @Override
    protected ClassifierResult corePredict(Frame df, boolean withClasses, boolean withDistributions) {
        return ClassifierResult.copy(this, df, withClasses, withDistributions, stacker.predict(df));
    }
}
