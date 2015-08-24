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

package rapaio.ml.classifier.bayes;

import rapaio.data.VarType;
import rapaio.sys.WS;
import rapaio.core.tools.DVector;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.bayes.estimator.GaussianPdf;
import rapaio.ml.classifier.bayes.estimator.MultinomialPmf;
import rapaio.ml.classifier.bayes.estimator.NominalEstimator;
import rapaio.ml.classifier.bayes.estimator.NumericEstimator;
import rapaio.ml.common.Capabilities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

/**
 * Naive Bayes Classifier.
 * <p>
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class NaiveBayes extends AbstractClassifier {

    private static final long serialVersionUID = -7602854063045679683L;

    // algorithm parameters

    private boolean useLaplaceSmoother = true;
    private NumericEstimator numEstimator = new GaussianPdf();
    private NominalEstimator nomEstimator = new MultinomialPmf();

    // prediction artifacts

    private Map<String, Double> priors;
    private Map<String, NumericEstimator> numMap;
    private Map<String, NominalEstimator> nomMap;

    @Override
    public NaiveBayes newInstance() {
        return new NaiveBayes()
                .withNumEstimator(numEstimator)
                .withNomEstimator(nomEstimator)
                .withLaplaceSmoother(useLaplaceSmoother)
                .withDebug(debug);
    }

    @Override
    public String name() {
        return "NaiveBayes";
    }

    @Override
    public String fullName() {
        return name() + "(numEstimator=" + numEstimator.name() + ", nomEstimator=" + nomEstimator.name() + ")";
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .withLearnType(Capabilities.LearnType.MULTICLASS_CLASSIFIER)
                .withInputCount(0, Integer.MAX_VALUE)
                .withInputTypes(VarType.NOMINAL, VarType.NUMERIC)
                .withTargetCount(1, 1)
                .withTargetTypes(VarType.NOMINAL);
    }

    @Override
    public NaiveBayes withDebug(boolean debug) {
        return (NaiveBayes) super.withDebug(debug);
    }

    public NaiveBayes withNumEstimator(NumericEstimator numEstimator) {
        this.numEstimator = numEstimator;
        return this;
    }

    public NaiveBayes withNomEstimator(NominalEstimator nomEstimator) {
        this.nomEstimator = nomEstimator;
        return this;
    }

    public NaiveBayes withLaplaceSmoother(boolean useLaplaceSmoother) {
        this.useLaplaceSmoother = useLaplaceSmoother;
        return this;
    }

    @Override
    public NaiveBayes learn(Frame df, Var weights, String... targetVarNames) {

        prepareLearning(df, weights, targetVarNames);

        // build priors

        priors = new HashMap<>();
        DVector dv = DVector.newFromWeights(df.var(firstTargetName()), weights, firstDict());

        if (useLaplaceSmoother) {
            // laplace add-one smoothing
            IntStream.range(0, firstDict().length).forEach(i -> dv.increment(i, 1.0));
        }
        dv.normalize(false);
        for (int i = 1; i < firstDict().length; i++) {
            priors.put(firstDict()[i], dv.get(i));
        }

        // build conditional probabilities

        nomMap = new ConcurrentHashMap<>();
        numMap = new ConcurrentHashMap<>();

        if (debug) {
            WS.println("start learning...");
        }
        Arrays.stream(df.varNames()).parallel().forEach(
                testCol -> {
                    if (firstTargetName().equals(testCol)) {
                        return;
                    }
                    if (df.var(testCol).type().isNumeric()) {
                        NumericEstimator estimator = numEstimator.newInstance();
                        estimator.learn(df, firstTargetName(), testCol);
                        numMap.put(testCol, estimator);
                        if (debug)
                            WS.print(".");
                        return;
                    }
                    if (df.var(testCol).type().isNominal()) {
                        NominalEstimator estimator = nomEstimator.newInstance();
                        estimator.learn(df, weights, firstTargetName(), testCol);
                        nomMap.put(testCol, estimator);
                        if (debug)
                            WS.print(".");
                    }
                });
        if (debug)
            WS.println();
        return this;
    }

    @Override
    public CFit fit(Frame df, final boolean withClasses, final boolean withDensities) {

        if (debug)
            WS.println("start fitting values...");

        CFit pred = CFit.newEmpty(this, df, withClasses, withDensities);
        pred.addTarget(firstTargetName(), firstDict());

        IntStream.range(0, df.rowCount()).parallel().forEach(
                i -> {
                    DVector dv = DVector.newEmpty(firstDict());
                    for (int j = 1; j < firstDict().length; j++) {
                        double sumLog = Math.log(priors.get(firstDictTerm(j)));
                        for (String testCol : numMap.keySet()) {
                            if (df.missing(i, testCol))
                                continue;
                            sumLog += Math.log(numMap.get(testCol).cpValue(df.value(i, testCol), firstDictTerm(j)));
                        }
                        for (String testCol : nomMap.keySet()) {
                            if (df.missing(i, testCol))
                                continue;
                            sumLog += Math.log(nomMap.get(testCol).cpValue(df.label(i, testCol), firstDictTerm(j)));
                        }
                        dv.increment(j, Math.exp(sumLog));
                    }
                    dv.normalize(false);

                    if (withClasses) {
                        pred.firstClasses().setIndex(i, dv.findBestIndex(false));
                    }
                    if (withDensities) {
                        for (int j = 0; j < firstDict().length; j++) {
                            pred.firstDensity().setValue(i, j, dv.get(j));
                        }
                    }
                });
        return pred;
    }
}
