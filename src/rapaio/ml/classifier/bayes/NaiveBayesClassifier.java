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

import rapaio.core.distributions.Normal;
import rapaio.core.distributions.empirical.KernelDensityEstimator;
import rapaio.core.distributions.empirical.KernelFunctionEpanechnikov;
import rapaio.core.distributions.empirical.KernelFunctionUniform;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.Frames;
import rapaio.data.Nominal;
import rapaio.data.Vector;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.colselect.ColSelector;
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
        this.dict = df.col(targetCol).getDictionary();

        // build priors

        priors = new HashMap<>();
        DensityVector dv = new DensityVector(dict);
        df.stream().forEach(s -> dv.update(s.getIndex(targetCol), 1.0));
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

        pred = new Nominal(df.rowCount(), dict);
        dist = Frames.newMatrix(df.rowCount(), dict);

        for (int i = 0; i < df.rowCount(); i++) {
            DensityVector dv = new DensityVector(dict);
            for (int j = 1; j < dict.length; j++) {
                double sumLog = Math.log(priors.get(dict[j]));
                for (String testCol : cvpEstimatorMap.keySet()) {
                    sumLog += cvpEstimatorMap.get(testCol).cpValue(df.getValue(i, testCol), dict[j]);
                }
                for (String testCol : dvpEstimatorMap.keySet()) {
                    sumLog += dvpEstimatorMap.get(testCol).cpValue(df.getLabel(i, testCol), dict[j]);
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

        private final Map<String, Normal> normals = new HashMap<>();

        @Override
        public String name() {
            return "GaussianEmpiric";
        }

        @Override
        public void learn(Frame df, String targetCol, String testCol) {
            String[] dict = df.col(targetCol).getDictionary();
            normals.clear();

            for (String classLabel : dict) {
                if ("?".equals(classLabel)) continue;
                Frame cond = df.stream().filter(s -> classLabel.equals(s.getLabel(targetCol))).toMappedFrame();
                Vector v = cond.col(testCol);
                double mu = new Mean(v).getValue();
                double sd = Math.sqrt(new Variance(v).getValue());
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

        private Map<String, KernelDensityEstimator> kde = new HashMap<>();

        @Override
        public String name() {
            return "KDE";
        }

        @Override
        public void learn(Frame df, String targetCol, String testCol) {
            kde.clear();

            for (String classLabel : df.col(targetCol).getDictionary()) {
                if ("?".equals(classLabel)) continue;
                Frame cond = df.stream().filter(s -> classLabel.equals(s.getLabel(targetCol))).toMappedFrame();
                Vector v = cond.col(testCol);
                KernelDensityEstimator k = new KernelDensityEstimator(v);

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

            String[] targetDict = df.col(targetCol).getDictionary();
            String[] testDict = df.col(testCol).getDictionary();

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
            df.stream().forEach(s -> density[s.getIndex(targetCol)][s.getIndex(testCol)]++);
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
            return density[invTreeTarget.get(classLabel)][invTreeTest.get(testLabel)];
        }

        @Override
        public DvpEstimator newInstance() {
            return new DvpEstimatorMultinomial();
        }
    }
}
