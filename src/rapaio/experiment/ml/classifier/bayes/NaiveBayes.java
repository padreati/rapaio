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

package rapaio.experiment.ml.classifier.bayes;

import rapaio.core.tools.*;
import rapaio.data.*;
import rapaio.experiment.ml.classifier.bayes.data.*;
import rapaio.experiment.ml.classifier.bayes.estimator.*;
import rapaio.ml.classifier.*;
import rapaio.ml.common.*;
import rapaio.printer.*;
import rapaio.util.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static rapaio.printer.format.Format.*;

/**
 * Naive Bayes Classifier.
 * <p>
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class NaiveBayes
        extends AbstractClassifierModel<NaiveBayes, ClassifierResult<NaiveBayes>>
        implements DefaultPrintable {

    private static final long serialVersionUID = -7602854063045679683L;
    private static final Logger logger = Logger.getLogger(NaiveBayes.class.getName());

    // algorithm parameters

    private double laplaceSmoother = 1;
    private PriorSupplier priorSupplier = PriorSupplier.PRIOR_MLE;
    private Map<String, Double> priors;

    private NumericData numData = new NumericData(new GaussianPdf());
    private NominalData nomData = new NominalData(new MultinomialPmf());
    private BinaryData binData = new BinaryData(new MultinomialPmf());

    @Override
    public NaiveBayes newInstance() {
        return new NaiveBayes()
                .withBinEstimator(binData.binEstimator)
                .withNumEstimator(numData.numEstimator)
                .withNomEstimator(nomData.nomEstimator)
                .withLaplaceSmoother(laplaceSmoother)
                .withPriorSupplier(priorSupplier);
    }

    @Override
    public String name() {
        return "NaiveBayes";
    }

    @Override
    public String fullName() {
        return name() + "(numEstimator=" + numData.numEstimator.name() + ", nomEstimator=" + nomData.nomEstimator.name() + ")";
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .withInputCount(0, 1_000_000)
                .withInputTypes(VType.NOMINAL, VType.DOUBLE, VType.INT, VType.BINARY)
                .withTargetCount(1, 1)
                .withTargetTypes(VType.NOMINAL)
                .withAllowMissingTargetValues(false)
                .withAllowMissingInputValues(true);
    }

    public NaiveBayes withBinEstimator(BinaryEstimator binEstimator) {
        this.binData.binEstimator = binEstimator;
        return this;
    }

    public NaiveBayes withNumEstimator(NumericEstimator numEstimator) {
        this.numData.numEstimator = numEstimator;
        return this;
    }

    public NaiveBayes withNomEstimator(NominalEstimator nomEstimator) {
        this.nomData.nomEstimator = nomEstimator;
        return this;
    }

    public NaiveBayes withLaplaceSmoother(double laplaceSmoother) {
        this.laplaceSmoother = laplaceSmoother;
        return this;
    }

    public double laplaceSmoother() {
        return laplaceSmoother;
    }

    public NaiveBayes withPriorSupplier(PriorSupplier priorSupplier) {
        this.priorSupplier = priorSupplier;
        return this;
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {

        // build priors

        priors = priorSupplier.learnPriors(df, weights, this);

        // build conditional probabilities

        nomData.nomMap = new ConcurrentHashMap<>();
        numData.numMap = new ConcurrentHashMap<>();
        binData.binMap = new ConcurrentHashMap<>();

        logger.fine("start learning...");
        Arrays.stream(df.varNames()).parallel().forEach(
                testCol -> {
                    if (firstTargetName().equals(testCol)) {
                        return;
                    }
                    if (df.rvar(testCol).type().isBinary()) {
                        BinaryEstimator estimator = binData.binEstimator.newInstance();
                        estimator.learn(this, df, weights, firstTargetName(), testCol);
                        binData.binMap.put(testCol, estimator);
                        return;
                    }
                    if (df.rvar(testCol).type().isNumeric()) {
                        NumericEstimator estimator = numData.numEstimator.newInstance();
                        estimator.learn(df, firstTargetName(), testCol);
                        numData.numMap.put(testCol, estimator);
                        return;
                    }
                    if (df.rvar(testCol).type().isNominal()) {
                        NominalEstimator estimator = nomData.nomEstimator.newInstance();
                        estimator.learn(this, df, weights, firstTargetName(), testCol);
                        nomData.nomMap.put(testCol, estimator);
                    }
                });
        logger.fine("learning phase finished");
        return true;
    }

    @Override
    protected ClassifierResult<NaiveBayes> corePredict(Frame df, final boolean withClasses, final boolean withDensities) {

        logger.fine("start fitting values...");

        ClassifierResult<NaiveBayes> pred = ClassifierResult.build(this, df, withClasses, withDensities);
        IntStream.range(0, df.rowCount()).parallel().forEach(
                i -> {
                    DVector dv = DVector.empty(false, firstTargetLevels());
                    for (int j = 1; j < firstTargetLevels().size(); j++) {
                        double sumLog = Math.log(priors.get(firstTargetLevel(j)));
                        sumLog += buildSumLog(df, i, j, numData);
                        sumLog += buildSumLog(df, i, j, nomData);
                        sumLog += buildSumLog(df, i, j, binData);
                        dv.increment(j, Math.exp(sumLog));
                    }
                    dv.normalize();

                    if (withClasses) {
                        pred.firstClasses().setInt(i, dv.findBestIndex());
                    }
                    if (withDensities) {
                        for (int j = 1; j < firstTargetLevels().size(); j++) {
                            pred.firstDensity().setDouble(i, j, dv.get(j));
                        }
                    }
                });
        logger.fine("fitting phase finished.");
        return pred;
    }

    private double buildSumLog(Frame df, int i, int j, NaiveBayesData data) {
        double sumLog = 0.0;
        for (String testCol : data.keySet()) {
            if (df.isMissing(i, testCol))
                continue;
            sumLog += Math.log(data.calcSumLog(testCol, df, i, firstTargetLevel(j)));
        }
        return sumLog;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("NaiveBayes model\n");
        sb.append("================\n\n");

        sb.append("Description:\n");
        sb.append(fullName()).append("\n\n");

        sb.append("Capabilities:\n");
        sb.append(capabilities().toString()).append("\n");

        sb.append("Learned model:\n");

        if (!hasLearned()) {
            sb.append("Learning phase not called\n\n");
            return sb.toString();
        }

        sb.append(baseSummary());

        sb.append("prior probabilities:\n");
        String targetName = firstTargetName();
        firstTargetLevels().stream().skip(1).forEach(label -> sb.append("> P(").append(targetName).append("='").append(label).append("')=").append(floatFlex(priors.get(label))).append("\n"));

        if (!numData.numMap.isEmpty()) {
            sb.append("numerical estimators:\n");
            numData.numMap.forEach((key, value) -> sb.append("> ").append(key).append(" : ").append(value.learningInfo()).append("\n"));
        }
        if (!nomData.nomMap.isEmpty()) {
            sb.append("nominal estimators:\n");
            nomData.nomMap.forEach((key, value) -> sb.append("> ").append(key).append(" : ").append(value.learningInfo()).append("\n"));
        }
        return sb.toString();
    }

}
