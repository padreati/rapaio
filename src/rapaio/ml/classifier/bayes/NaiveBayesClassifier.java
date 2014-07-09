/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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

import rapaio.core.distributions.cu.Norm;
import rapaio.core.distributions.empirical.*;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.Frames;
import rapaio.data.Nominal;
import rapaio.data.Var;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.colselect.ColSelector;
import rapaio.ml.classifier.tools.DensityVector;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
@Deprecated
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
                .withColSelector(colSelector)
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
        sb.append("NaiveBayes");
        return sb.toString();
    }

    @Override
    public NaiveBayesClassifier withColSelector(ColSelector colSelector) {
        return (NaiveBayesClassifier) super.withColSelector(colSelector);
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
    public void learn(Frame df, String targetCol) {
        this.targetCol = targetCol;
        this.dict = df.col(targetCol).dictionary();

        // build priors

        priors = new HashMap<>();
        DensityVector dv = new DensityVector(dict);
        df.stream().forEach(s -> dv.update(s.index(targetCol), s.weight()));
        // laplace add-one smoothing
        for (int i = 0; i < dict.length; i++) {
            dv.update(i, 1.0);
        }
        dv.normalize(false);
        for (int i = 1; i < dict.length; i++) {
            priors.put(dict[i], dv.get(i));
        }

        // build conditional probabilities

        dvpEstimatorMap = new HashMap<>();
        cvpEstimatorMap = new HashMap<>();

        for (String testCol : df.colNames()) {
            if (targetCol.equals(testCol)) continue;
            if (df.col(testCol).type().isNumeric()) {

                CvpEstimator estimator = cvpEstimator.newInstance();
                estimator.learn(df, targetCol, testCol);
                cvpEstimatorMap.put(testCol, estimator);
                continue;
            }

            if (df.col(testCol).type().isNominal()) {

                DvpEstimator estimator = dvpEstimator.newInstance();
                estimator.learn(df, targetCol, testCol);
                dvpEstimatorMap.put(testCol, estimator);
            }
        }
    }

    @Override
    public void predict(Frame df) {

        pred = Nominal.newEmpty(df.rowCount(), dict);
        dist = Frames.newMatrix(df.rowCount(), dict);

        for (int i = 0; i < df.rowCount(); i++) {
            DensityVector dv = new DensityVector(dict);
            for (int j = 1; j < dict.length; j++) {
                double sumLog = Math.log(priors.get(dict[j]));
                for (String testCol : cvpEstimatorMap.keySet()) {
                    if (df.missing(i, testCol)) continue;
                    sumLog += cvpEstimatorMap.get(testCol).cpValue(df.value(i, testCol), dict[j]);
                }
                for (String testCol : dvpEstimatorMap.keySet()) {
                    if (df.missing(i, testCol)) continue;
                    sumLog += dvpEstimatorMap.get(testCol).cpValue(df.label(i, testCol), dict[j]);
                }
                dv.update(j, sumLog);
            }

            pred.setIndex(i, dv.findBestIndex());
            for (int j = 0; j < dict.length; j++) {
                dist.setValue(i, j, dv.get(j));
            }
        }
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

        private final Map<String, Norm> normals = new HashMap<>();

        @Override
        public String name() {
            return "GaussianEmpiric";
        }

        @Override
        public void learn(Frame df, String targetCol, String testCol) {
            String[] dict = df.col(targetCol).dictionary();
            normals.clear();

            for (String classLabel : dict) {
                if ("?".equals(classLabel)) continue;
                Frame cond = df.stream().filter(s -> classLabel.equals(s.label(targetCol))).toMappedFrame();
                Var v = cond.col(testCol);
                double mu = new Mean(v).value();
                double sd = Math.sqrt(new Variance(v).getValue());
                normals.put(classLabel, new Norm(mu, sd));
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

            for (String classLabel : df.col(targetCol).dictionary()) {
                if ("?".equals(classLabel)) continue;
                Frame cond = df.stream().filter(s -> classLabel.equals(s.label(targetCol))).toMappedFrame();
                Var v = cond.col(testCol);
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

            String[] targetDict = df.col(targetCol).dictionary();
            String[] testDict = df.col(testCol).dictionary();

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
