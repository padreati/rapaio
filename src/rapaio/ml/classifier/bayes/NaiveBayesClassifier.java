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
 */

package rapaio.ml.classifier.bayes;

import rapaio.core.distributions.Normal;
import rapaio.core.distributions.empirical.KDE;
import rapaio.core.distributions.empirical.KFunc;
import rapaio.core.distributions.empirical.KFuncGaussian;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.ClassifierFit;
import rapaio.ml.classifier.tools.DensityVector;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class NaiveBayesClassifier extends AbstractClassifier {

    // algorithm parameters

    private CvpEstimator cvpEstimator = new CvpEstimatorGaussianEmpiric();
    private DvpEstimator dvpEstimator = new DvpEstimatorMultinomial();

    // prediction artifacts

    private Map<String, Double> priors;
    private Map<String, CvpEstimator> cvpEstimatorMap;
    private Map<String, DvpEstimator> dvpEstimatorMap;

    @Override
    public NaiveBayesClassifier newInstance() {
        return new NaiveBayesClassifier()
                .withCvpEstimator(cvpEstimator)
                .withDvpEstimator(dvpEstimator);
    }

    @Override
    public String name() {
        return "NaiveBayes";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name());
        sb.append("(");
        sb.append("cvpEstimator=").append(cvpEstimator.name());
        sb.append(",");
        sb.append("dvpEstimator=").append(dvpEstimator.name());
        sb.append(")");
        return sb.toString();
    }

    public NaiveBayesClassifier withCvpEstimator(CvpEstimator cvpEstimator) {
        this.cvpEstimator = cvpEstimator;
        return this;
    }

    public CvpEstimator getCvpEstimator() {
        return cvpEstimator;
    }

    public NaiveBayesClassifier withDvpEstimator(DvpEstimator dvpEstimator) {
        this.dvpEstimator = dvpEstimator;
        return this;
    }

    public DvpEstimator getDvpEstimator() {
        return dvpEstimator;
    }

    @Override
    public void learn(Frame df, Var weights, String... targetVarNames) {

        prepareLearning(df, weights, targetVarNames);

        if (targetNames().length != 1) {
            throw new IllegalArgumentException("NaiveBayes is able to predict only one target variable");
        }

        // build priors

        priors = new HashMap<>();
        DensityVector dv = new DensityVector(firstDict());
        df.stream().forEach(s -> dv.update(s.index(firstTargetName()), weights.value(s.row())));

        // laplace add-one smoothing
        for (int i = 0; i < firstDict().length; i++) {
            dv.update(i, 1.0);
        }
        dv.normalize(false);
        for (int i = 1; i < firstDict().length; i++) {
            priors.put(firstDict()[i], dv.get(i));
        }

        // build conditional probabilities

        dvpEstimatorMap = new HashMap<>();
        cvpEstimatorMap = new HashMap<>();

        for (String testCol : df.varNames()) {
            if (firstTargetName().equals(testCol)) continue;
            if (df.getVar(testCol).getType().isNumeric()) {

                CvpEstimator estimator = cvpEstimator.newInstance();
                estimator.learn(df, firstTargetName(), testCol);
                cvpEstimatorMap.put(testCol, estimator);
                continue;
            }

            if (df.getVar(testCol).getType().isNominal()) {

                DvpEstimator estimator = dvpEstimator.newInstance();
                estimator.learn(df, firstTargetName(), testCol);
                dvpEstimatorMap.put(testCol, estimator);
            }
        }
    }

    @Override
    public ClassifierFit predict(Frame df, final boolean withClasses, final boolean withDensities) {

        ClassifierFit pred = ClassifierFit.newEmpty(this, df, withClasses, withDensities);
        pred.addTarget(firstTargetName(), firstDict());

        for (int i = 0; i < df.rowCount(); i++) {
            DensityVector dv = new DensityVector(firstDict());
            for (int j = 1; j < firstDict().length; j++) {
                double sumLog = Math.log(priors.get(firstDict(j)));
                for (String testCol : cvpEstimatorMap.keySet()) {
                    if (df.missing(i, testCol)) continue;
                    sumLog += cvpEstimatorMap.get(testCol).cpValue(df.value(i, testCol), firstDict(j));
                }
                for (String testCol : dvpEstimatorMap.keySet()) {
                    if (df.missing(i, testCol)) continue;
                    sumLog += dvpEstimatorMap.get(testCol).cpValue(df.label(i, testCol), firstDict(j));
                }
                dv.update(j, sumLog);
            }

            if (withClasses) {
                pred.firstClasses().setIndex(i, dv.findBestIndex());
            }
            if (withDensities) {
                for (int j = 0; j < firstDict().length; j++) {
                    pred.firstDensity().setValue(i, j, dv.get(j));
                }
            }
        }
        return pred;
    }

    @Override
    public void buildSummary(StringBuilder sb) {

    }

    public static interface DvpEstimator extends Serializable {

        String name();

        default void learn(Frame df, String targetCol, String testCol) {
        }

        double cpValue(String testLabel, String classLabel);

        DvpEstimator newInstance();
    }

    public static interface CvpEstimator extends Serializable {

        String name();

        void learn(Frame df, String targetCol, String testCol);

        double cpValue(double testValue, String classLabel);

        CvpEstimator newInstance();
    }

    public static class CvpEstimatorGaussianEmpiric implements CvpEstimator {

        private final Map<String, Normal> normals = new HashMap<>();

        @Override
        public String name() {
            return "GaussianEmpiric";
        }

        @Override
        public void learn(Frame df, String targetCol, String testCol) {
            String[] dict = df.getVar(targetCol).dictionary();
            normals.clear();

            for (String classLabel : dict) {
                if ("?".equals(classLabel)) continue;
                Frame cond = df.stream().filter(s -> classLabel.equals(s.label(targetCol))).toMappedFrame();
                Var v = cond.getVar(testCol);
                double mu = new Mean(v).value();
                double sd = Math.sqrt(new Variance(v).value());
                normals.put(classLabel, new Normal(mu, sd));
            }
        }

        @Override
        public double cpValue(double testValue, String classLabel) {
            return normals.get(classLabel).pdf(testValue);
        }

        @Override
        public CvpEstimator newInstance() {
            return new CvpEstimatorGaussianEmpiric();
        }
    }

    public static class CvpEstimatorKDE implements CvpEstimator {

        private Map<String, KDE> kde = new HashMap<>();
        private KFunc kfunc = new KFuncGaussian();
        private double bandwidth = 0;

        public CvpEstimatorKDE() {
        }

        public CvpEstimatorKDE(KFunc kfunc, double bandwidth) {
            this.kfunc = kfunc;
            this.bandwidth = bandwidth;
        }

        @Override
        public String name() {
            return "KDE";
        }

        @Override
        public void learn(Frame df, String targetCol, String testCol) {
            kde.clear();

            for (String classLabel : df.getVar(targetCol).dictionary()) {
                if ("?".equals(classLabel)) continue;
                Frame cond = df.stream().filter(s -> classLabel.equals(s.label(targetCol))).toMappedFrame();
                Var v = cond.getVar(testCol);
                KDE k = new KDE(v, kfunc, (bandwidth == 0) ? KDE.getSilvermanBandwidth(v) : bandwidth);

                kde.put(classLabel, k);
            }
        }

        @Override
        public double cpValue(double testValue, String classLabel) {
            return kde.get(classLabel).pdf(testValue);
        }

        @Override
        public CvpEstimator newInstance() {
            return new CvpEstimatorKDE();
        }
    }

    public static class DvpEstimatorMultinomial implements DvpEstimator {

        private double[][] density;
        private Map<String, Integer> invTreeTarget;
        private Map<String, Integer> invTreeTest;

        @Override
        public String name() {
            return "Multinomial";
        }

        @Override
        public void learn(Frame df, String targetCol, String testCol) {

            String[] targetDict = df.getVar(targetCol).dictionary();
            String[] testDict = df.getVar(testCol).dictionary();

            invTreeTarget = new HashMap<>();
            invTreeTest = new HashMap<>();

            for (int i = 0; i < targetDict.length; i++) {
                invTreeTarget.put(targetDict[i], i);
            }
            for (int i = 0; i < testDict.length; i++) {
                invTreeTest.put(testDict[i], i);
            }

            density = new double[targetDict.length][testDict.length];
            for (int i = 0; i < targetDict.length; i++) {
                for (int j = 0; j < testDict.length; j++) {
                    density[i][j] = 1.0;
                }
            }
            df.stream().forEach(s -> density[s.index(targetCol)][s.index(testCol)]++);
            for (int i = 0; i < targetDict.length; i++) {
                double t = 0;
                for (int j = 0; j < testDict.length; j++) {
                    t += density[i][j];
                }
                for (int j = 0; j < testDict.length; j++) {
                    density[i][j] /= t;
                }
            }
        }

        @Override
        public double cpValue(String testLabel, String classLabel) {
            if (!invTreeTarget.containsKey(classLabel)) {
                return 1e-10;
            }
            if (!invTreeTest.containsKey(testLabel)) {
                return 1e-10;
            }
            return density[invTreeTarget.get(classLabel)][invTreeTest.get(testLabel)];
        }

        @Override
        public DvpEstimator newInstance() {
            return new DvpEstimatorMultinomial();
        }
    }
}
