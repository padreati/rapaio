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
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VRange;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.experiment.ml.classifier.ensemble.CForest;
import rapaio.ml.classifier.AbstractClassifierModel;
import rapaio.ml.classifier.ClassifierModel;
import rapaio.ml.classifier.ClassifierResult;
import rapaio.ml.common.Capabilities;
import rapaio.printer.Printable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * Stacking with a stacking classifier
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/30/15.
 */
public class CStacking extends AbstractClassifierModel<CStacking, ClassifierResult> implements Printable {

    private static final long serialVersionUID = -9087871586729573030L;

    private static final Logger logger = Logger.getLogger(CStacking.class.getName());

    private final List<ClassifierModel> weaks = new ArrayList<>();
    private ClassifierModel stacker = CForest.newRF();

    public CStacking withLearners(ClassifierModel... learners) {
        weaks.clear();
        Collections.addAll(weaks, learners);
        return this;
    }

    public CStacking withStacker(ClassifierModel stacker) {
        this.stacker = stacker;
        return this;
    }

    @Override
    public CStacking newInstance() {
        return newInstanceDecoration(new CStacking())
                .withLearners(weaks.stream().map(ClassifierModel::newInstance).toArray(ClassifierModel[]::new))
                .withStacker(stacker.newInstance());
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
        return Capabilities.builder()
                .allowMissingTargetValues(false)
                .allowMissingInputValues(false)
                .inputTypes(Arrays.asList(VType.BINARY, VType.INT, VType.DOUBLE))
                .targetType(VType.NOMINAL)
                .minInputCount(1).maxInputCount(100_000)
                .minTargetCount(1).maxTargetCount(1)
                .build();
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
                                    .withName("V" + i);
                        })
                        .collect(toList());

        List<String> targets = VRange.of(targetVars).parseVarNames(df);
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
                            .withName("V" + i);
                }).collect(toList());
        return PredSetup.valueOf(SolidFrame.byVars(vars), withClasses, withDistributions);
    }

    @Override
    protected ClassifierResult corePredict(Frame df, boolean withClasses, boolean withDistributions) {
        return ClassifierResult.copy(this, df, withClasses, withDistributions, stacker.predict(df));
    }
}
