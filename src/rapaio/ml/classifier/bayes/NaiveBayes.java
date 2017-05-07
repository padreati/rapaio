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

package rapaio.ml.classifier.bayes;

import rapaio.core.tools.DVector;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.data.filter.FFilter;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.bayes.estimator.*;
import rapaio.ml.common.Capabilities;
import rapaio.sys.WS;
import rapaio.util.Tag;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * Naive Bayes Classifier.
 * <p>
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class NaiveBayes extends AbstractClassifier {

    private static final long serialVersionUID = -7602854063045679683L;
    private static final Logger logger = Logger.getLogger(NaiveBayes.class.getName());

    // algorithm parameters
    public static Tag<PriorSupplier> PRIORS_MLE = Tag.valueOf("PRIORS_MLE", (df, weights, nb) -> {
        Map<String, Double> priors = new HashMap<>();
        DVector dv = DVector.fromWeights(false, df.var(nb.firstTargetName()), weights, nb.firstTargetLevels());
        dv.normalize();
        for (int i = 1; i < nb.firstTargetLevels().length; i++) {
            priors.put(nb.firstTargetLevels()[i], dv.get(i));
        }
        return priors;
    });
    public static Tag<PriorSupplier> PRIORS_UNIFORM = Tag.valueOf("PRIORS_UNIFORM", (df, weights, nb) -> {
        Map<String, Double> priors = new HashMap<>();
        double p = 1.0 / nb.firstTargetLevels().length;
        for (int i = 1; i < nb.firstTargetLevels().length; i++) {
            priors.put(nb.firstTargetLevels()[i], p);
        }
        return priors;
    });
    private double laplaceSmoother = 1;
    private Tag<PriorSupplier> priorSupplier = PRIORS_MLE;
    private Map<String, Double> priors;
    //private NaiveBayesData data = new NaiveBayesData(new MultinomialPmf(), new GaussianPdf(), new MultinomialPmf());
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
                .withInputTypes(VarType.NOMINAL, VarType.NUMERIC, VarType.INDEX, VarType.BINARY)
                .withTargetCount(1, 1)
                .withTargetTypes(VarType.NOMINAL)
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

    public NaiveBayes withPriorSupplier(Tag<PriorSupplier> priorSupplier) {
        this.priorSupplier = priorSupplier;
        return this;
    }

    @Override
    protected boolean coreTrain(Frame df, Var weights) {

        // build priors

        priors = PRIORS_MLE.get().learnPriors(df, weights, this);

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
                    if (df.var(testCol).type().isBinary()) {
                        BinaryEstimator estimator = binData.binEstimator.newInstance();
                        estimator.learn(this, df, weights, firstTargetName(), testCol);
                        binData.binMap.put(testCol, estimator);
                        return;
                    }
                    if (df.var(testCol).type().isNumeric()) {
                        NumericEstimator estimator = numData.numEstimator.newInstance();
                        estimator.learn(df, firstTargetName(), testCol);
                        numData.numMap.put(testCol, estimator);
                        return;
                    }
                    if (df.var(testCol).type().isNominal()) {
                        NominalEstimator estimator = nomData.nomEstimator.newInstance();
                        estimator.learn(this, df, weights, firstTargetName(), testCol);
                        nomData.nomMap.put(testCol, estimator);
                    }
                });
        logger.fine("learning phase finished");
        return true;
    }

    @Override
    protected CFit coreFit(Frame df, final boolean withClasses, final boolean withDensities) {

        logger.fine("start fitting values...");

        CFit pred = CFit.build(this, df, withClasses, withDensities);
        IntStream.range(0, df.rowCount()).parallel().forEach(
                i -> {
                    DVector dv = DVector.empty(false, firstTargetLevels());
                    for (int j = 1; j < firstTargetLevels().length; j++) {
                        double sumLog = Math.log(priors.get(firstTargetLevel(j)));
                        sumLog += buildSumLog(df, i, j, numData);
                        sumLog += buildSumLog(df, i, j, nomData);
                        sumLog += buildSumLog(df, i, j, binData);
                        dv.increment(j, Math.exp(sumLog));
                    }
                    dv.normalize();

                    if (withClasses) {
                        pred.firstClasses().setIndex(i, dv.findBestIndex());
                    }
                    if (withDensities) {
                        for (int j = 1; j < firstTargetLevels().length; j++) {
                            pred.firstDensity().setValue(i, j, dv.get(j));
                        }
                    }
                });
        logger.fine("fitting phase finished.");
        return pred;
    }

	private double buildSumLog(Frame df, int i, int j, NaiveBayesData data) {
		double sumLog = 0.0;
		for (String testCol : data.keySet()) {
		    if (df.missing(i, testCol))
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
        sb.append(capabilities().summary()).append("\n");

        sb.append("Learned model:\n");

        if (!hasLearned()) {
            sb.append("Learning phase not called\n\n");
            return sb.toString();
        }

        sb.append(baseSummary());

        sb.append("prior probabilities:\n");
        String targetName = firstTargetName();
        Arrays.stream(firstTargetLevels()).skip(1).forEach(label -> sb.append("> P(").append(targetName).append("='").append(label).append("')=").append(WS.formatFlex(priors.get(label))).append("\n"));

        if (!numData.numMap.isEmpty()) {
            sb.append("numerical estimators:\n");
            numData.numMap.entrySet().forEach(e -> sb.append("> ").append(e.getKey()).append(" : ").append(e.getValue().learningInfo()).append("\n"));
        }
        if (!nomData.nomMap.isEmpty()) {
            sb.append("nominal estimators:\n");
            nomData.nomMap.entrySet().forEach(e -> sb.append("> ").append(e.getKey()).append(" : ").append(e.getValue().learningInfo()).append("\n"));
        }
        return sb.toString();
    }

    @Override
    public NaiveBayes withInputFilters(List<FFilter> filters) {
        return (NaiveBayes) super.withInputFilters(filters);
    }

    @Override
    public NaiveBayes withInputFilters(FFilter... filters) {
        return (NaiveBayes) super.withInputFilters(filters);
    }

    interface PriorSupplier extends Serializable {
        Map<String, Double> learnPriors(Frame df, Var weights, NaiveBayes nb);
    }
}
