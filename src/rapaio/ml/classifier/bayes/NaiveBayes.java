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

package rapaio.ml.classifier.bayes;

import rapaio.core.tools.DensityVector;
import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.ml.classifier.AbstractClassifierModel;
import rapaio.ml.classifier.ClassifierResult;
import rapaio.ml.classifier.bayes.estimator.GaussianPdf;
import rapaio.ml.classifier.bayes.estimator.MultinomialPmf;
import rapaio.ml.classifier.bayes.estimator.NomEstimator;
import rapaio.ml.classifier.bayes.estimator.NumEstimator;
import rapaio.ml.common.Capabilities;
import rapaio.printer.Printable;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static rapaio.printer.format.Format.floatFlex;

/**
 * Naive Bayes Classifier.
 * <p>
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class NaiveBayes
        extends AbstractClassifierModel<NaiveBayes, ClassifierResult<NaiveBayes>>
        implements Printable {

    private static final long serialVersionUID = -7602854063045679683L;
    private static final Logger logger = Logger.getLogger(NaiveBayes.class.getName());

    // algorithm parameters

    private double laplaceSmoother = 1;
    private PriorSupplier priorSupplier = PriorSupplier.PRIOR_MLE;
    private Map<String, Double> priors;

    private NumEstimator numEstimator = new GaussianPdf();
    private NomEstimator nomEstimator = new MultinomialPmf();

    private Map<String, NomEstimator> nomData = new ConcurrentHashMap<>();
    private Map<String, NumEstimator> numData = new ConcurrentHashMap<>();

    @Override
    public NaiveBayes newInstance() {
        return newInstanceDecoration(new NaiveBayes())
                .withNumEstimator(numEstimator)
                .withNomEstimator(nomEstimator)
                .withLaplaceSmoother(laplaceSmoother)
                .withPriorSupplier(priorSupplier);
    }

    @Override
    public String name() {
        return "NaiveBayes";
    }

    @Override
    public String fullName() {
        return name() + "(" +
                "numEstimator=" + numEstimator.name() + ", " +
                "nomEstimator=" + nomEstimator.name() + ")";
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

    public NumEstimator getNumEstimator() {
        return numEstimator;
    }

    public NaiveBayes withNumEstimator(NumEstimator numEstimator) {
        this.numEstimator = numEstimator;
        return this;
    }

    public NomEstimator getNomEstimator() {
        return nomEstimator;
    }

    public NaiveBayes withNomEstimator(NomEstimator nomEstimator) {
        this.nomEstimator = nomEstimator;
        return this;
    }

    public NaiveBayes withLaplaceSmoother(double laplaceSmoother) {
        this.laplaceSmoother = laplaceSmoother;
        return this;
    }

    public double getLaplaceSmoother() {
        return laplaceSmoother;
    }

    public PriorSupplier getPriorSupplier() {
        return priorSupplier;
    }

    public NaiveBayes withPriorSupplier(PriorSupplier priorSupplier) {
        this.priorSupplier = priorSupplier;
        return this;
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {

        // build priors

        priors = priorSupplier.learnPriors(df, weights, firstTargetName());

        // build conditional probabilities

        nomData = new ConcurrentHashMap<>();
        numData = new ConcurrentHashMap<>();

        logger.fine("start learning...");
        Arrays.stream(df.varNames()).parallel().forEach(
                testCol -> {
                    if (firstTargetName().equals(testCol)) {
                        return;
                    }
                    if (df.rvar(testCol).type().isNumeric()) {
                        NumEstimator estimator = numEstimator.newInstance();
                        estimator.learn(df, firstTargetName(), testCol);
                        numData.put(testCol, estimator);
                        return;
                    }
                    if (df.rvar(testCol).type().isNominal() || df.rvar(testCol).type().isBinary()) {
                        NomEstimator estimator = nomEstimator.newInstance();
                        estimator.learn(this, df, weights, firstTargetName(), testCol);
                        nomData.put(testCol, estimator);
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
                    DensityVector dv = DensityVector.empty(false, firstTargetLevels());
                    for (int j = 1; j < firstTargetLevels().size(); j++) {
                        double sumLog = Math.log(priors.get(firstTargetLevel(j)));

                        for (Map.Entry<String, NumEstimator> e : numData.entrySet()) {
                            if (df.isMissing(i, e.getKey())) {
                                continue;
                            }
                            sumLog += Math.log(e.getValue().computeProbability(df.getDouble(i, e.getKey()), firstTargetLevel(j)));
                        }
                        for (Map.Entry<String, NomEstimator> e : nomData.entrySet()) {
                            if (df.isMissing(i, e.getKey())) {
                                continue;
                            }
                            sumLog += Math.log(e.getValue().computeProbability(df.getLabel(i, e.getKey()), firstTargetLevel(j)));
                        }
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

    @Override
    public String toSummary() {
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

        if (!numData.isEmpty()) {
            sb.append("numerical estimators:\n");
            numData.forEach((key, value) -> sb.append("> ").append(key).append(" : ").append(value.learningInfo()).append("\n"));
        }
        if (!nomData.isEmpty()) {
            sb.append("nominal estimators:\n");
            nomData.forEach((key, value) -> sb.append("> ").append(key).append(" : ").append(value.learningInfo()).append("\n"));
        }
        return sb.toString();
    }

    @Override
    public String toContent() {
        return toSummary();
    }

    @Override
    public String toFullContent() {
        return toSummary();
    }

    @Override
    public String toString() {
        return fullName();
    }
}
