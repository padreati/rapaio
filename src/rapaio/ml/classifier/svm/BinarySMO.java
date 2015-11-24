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

package rapaio.ml.classifier.svm;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/16/15.
 */

import rapaio.core.MathTools;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.data.sample.FrameSample;
import rapaio.data.sample.FrameSampler;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.svm.kernel.Kernel;
import rapaio.ml.classifier.svm.kernel.PolyKernel;
import rapaio.ml.common.Capabilities;

import java.io.Serializable;
import java.util.BitSet;

import static rapaio.sys.WS.formatFlex;

/**
 * Class for building a binary support vector machine.
 */
public class BinarySMO extends AbstractClassifier implements Serializable {

    private static final long serialVersionUID = 1208515184777030598L;

    protected double[] alpha; // Lagrange multipliers from dual
    protected double b, bLow, bUp; // thresholds
    protected int iLow, iUp; // indices for bLow and bUp
    /**
     * Variables to hold weight vector in sparse form.
     * (To reduce storage requirements.)
     */
    protected double[] sparseWeights;
    protected int[] sparseIndices;
    protected Kernel kernel = new PolyKernel(1);
    protected double[] target;
    protected double[] fCache; // The current set of errors for all non-bound examples.
    /* The five different sets used by the algorithm. */
    protected BitSet I0; // i: 0 < alpha[i] < C
    protected BitSet I1; // i: classes[i] = 1, alpha[i] = 0
    protected BitSet I2; // i: classes[i] = -1, alpha[i] =C
    protected BitSet I3; // i: classes[i] = 1, alpha[i] = C
    protected BitSet I4; // i: classes[i] = -1, alpha[i] = 0
    /**
     * The set of support vectors
     */
    protected BitSet supportVectors; // {i: 0 < alpha[i]}
    protected double sumOfWeights = 0;
    // class indices
    private int classIndex1 = 1;
    private int classIndex2 = 2;
    private boolean oneVsAll = false;
    private int maxRuns = Integer.MAX_VALUE;
    private double C = 1.0; // complexity parameter
    private double tol = 1e-3; // tolerance of accuracy
    private Frame train;
    private Var weights;
    private int targetIndex;
    /**
     * Weight vector for linear machine.
     */
    private double[] linear_weights;

    @Override
    public String name() {
        return "BinarySMO";
    }

    @Override
    public String fullName() {
        return name() +
                "{" +
                "sampler=" + sampler().name() + ";" +
                "kernel=" + kernel.name() + ";" +
                "C=" + formatFlex(C) + ";" +
                "tol=" + formatFlex(tol) + ";" +
                "classIndex1=" + classIndex1 + ";" +
                "classIndex2=" + classIndex2 + ";" +
                "oneVsAll=" + oneVsAll + ";" +
                "maxRuns=" + maxRuns +
                "}";
    }

    @Override
    public Classifier newInstance() {
        return new BinarySMO()
                .withSampler(sampler())
                .withKernel(kernel.newInstance())
                .withC(C)
                .withTol(tol)
                .withFirstClassIndex(classIndex1)
                .withSecondClassIndex(classIndex2)
                .withOneVsAll(oneVsAll)
                .withMaxRuns(maxRuns);
    }

    public BinarySMO withKernel(Kernel value) {
        kernel = value;
        return this;
    }

    public BinarySMO withC(double c) {
        this.C = c;
        return this;
    }

    public BinarySMO withTol(double tol) {
        this.tol = tol;
        return this;
    }

    /**
     * Set target index of the first class.
     */
    public BinarySMO withFirstClassIndex(int classIndex1) {
        this.classIndex1 = classIndex1;
        return this;
    }

    /**
     * Sets target index of the second class, used only when
     * oneVsAll if false
     */
    public BinarySMO withSecondClassIndex(int classIndex2) {
        this.classIndex2 = classIndex2;
        return this;
    }

    /**
     * If true then first class index is classified against
     * all other target classes, if false than first class index
     * is classified against second class index.
     *
     * @param oneVsAll true for one vs all classification
     */
    public BinarySMO withOneVsAll(boolean oneVsAll) {
        this.oneVsAll = oneVsAll;
        return this;
    }

    public BinarySMO withMaxRuns(int maxRuns) {
        this.maxRuns = maxRuns;
        return this;
    }

    @Override
    public BinarySMO withSampler(FrameSampler sampler) {
        return (BinarySMO) super.withSampler(sampler);
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .withLearnType(Capabilities.LearnType.BINARY_CLASSIFIER)
                .withInputTypes(VarType.BINARY, VarType.INDEX, VarType.NOMINAL, VarType.NUMERIC)
                .withInputCount(1, 10000)
                .withAllowMissingInputValues(false)
                .withTargetTypes(VarType.NOMINAL)
                .withTargetCount(1, 1)
                .withAllowMissingTargetValues(false);
    }

    @Override
    protected boolean coreTrain(Frame df, Var weights) {

        // process classes

        if (classIndex1 == classIndex2) {
            throw new IllegalArgumentException("Indexes for first and second target " +
                    "class labels are equal, which is not allowed.");
        }

        if (!oneVsAll) {
            Mapping map = df
                    .stream()
                    .filter(s -> s.index(firstTargetName()) == classIndex1 || s.index(firstTargetName()) == classIndex2)
                    .collectMapping();
            df = df.mapRows(map);
            weights = weights.mapRows(map);
        }

        // perform sampling

        FrameSample sample = sampler().newSample(df, weights);
        df = sample.df;
        weights = sample.weights;

        if (df.rowCount() == 0) {
            throw new IllegalArgumentException("After filtering other classes, there " +
                    "were no other rows remained");
        }

        // Initialize some variables

        // Set the reference to the data
        this.train = df;
        this.weights = weights;

        bUp = -1;
        bLow = 1;
        b = 0;
        alpha = null;
        linear_weights = null;
        fCache = null;
        I0 = null;
        I1 = null;
        I2 = null;
        I3 = null;
        I4 = null;
        sparseWeights = null;
        sparseIndices = null;

        // Store the sum of weights
        sumOfWeights = weights.stream().mapToDouble().sum();

        // Set class values
        target = new double[train.rowCount()];
        iUp = -1;
        iLow = -1;
        for (int i = 0; i < train.rowCount(); i++) {
            if (df.var(firstTargetName()).index(i) == classIndex1) {
                target[i] = -1;
                iLow = i;
            } else {
                target[i] = 1;
                iUp = i;
            }
        }

        // Check whether one or both classes are missing

        if ((iUp == -1) || (iLow == -1)) {
            if (iUp != -1) {
                b = -1;
            } else if (iLow != -1) {
                b = 1;
            } else {
                target = null;
                return false;
            }
            if (kernel.isLinear()) {
                sparseWeights = new double[0];
                sparseIndices = new int[0];
                target = null;
            } else {
                supportVectors = new BitSet(0);
                alpha = new double[0];
                target = new double[0];
            }
            return false;
        }


        targetIndex = df.varIndex(firstTargetName());

        // If machine is linear, reserve space for weights

        if (kernel.isLinear()) {
            linear_weights = new double[inputNames().length];
        } else {
            linear_weights = null;
        }

        // Initialize alpha array to zero
        alpha = new double[df.rowCount()];

        // Initialize sets
        supportVectors = new BitSet(df.rowCount());
        I0 = new BitSet(df.rowCount());
        I1 = new BitSet(df.rowCount());
        I2 = new BitSet(df.rowCount());
        I3 = new BitSet(df.rowCount());
        I4 = new BitSet(df.rowCount());

        // Clean out some instance variables
        sparseWeights = null;
        sparseIndices = null;

        // init kernel
        kernel.buildKernel(inputNames(), df);

        // Initialize error cache
        fCache = new double[df.rowCount()];
        fCache[iLow] = 1;
        fCache[iUp] = -1;

        // Build up I1 and I4
        for (int i = 0; i < train.rowCount(); i++) {
            if (target[i] == 1) {
                I1.set(i, true);
            } else {
                I4.set(i, true);
            }
        }

        // Loop to find all the support vectors
        int numChanged = 0;
        boolean examineAll = true;

        int runs = maxRuns;

        while (numChanged > 0 || examineAll) {
            numChanged = 0;

            if (examineAll) {

                // add random as an additional step
                int offset = RandomSource.nextInt(train.rowCount());
                for (int i = offset; i < train.rowCount() + offset; i++) {
                    int pos = i;
                    if (pos >= train.rowCount())
                        pos -= train.rowCount();
                    if (examineExample(pos)) {
                        numChanged++;
                    }
                }

            } else {

                // This code implements Modification 1 from Keerthi et al.'s paper
//                int offset = RandomSource.nextInt(train.rowCount());
//                for (int i = offset; i < train.rowCount() + offset; i++) {
//
//                    int pos = i;
//                    if (pos >= train.rowCount())
//                        pos -= train.rowCount();
//
//                    if (alpha[pos] > 0 && alpha[pos] < C * weights.value(pos)) {
//                        if (examineExample(pos)) {
//                            numChanged++;
//                        }
////                        Is optimality on unbound vectors obtained?
//                        if (bUp > bLow - 2 * tol) {
//                            numChanged = 0;
//                            break;
//                        }
//                    }
//                }

                //This is the code for Modification 2 from Keerthi et al.'s paper
                boolean innerLoopSuccess = true;
                numChanged = 0;
                while ((bUp < bLow - 2 * tol) && innerLoopSuccess) {
                    innerLoopSuccess = takeStep(iUp, iLow);
                    if (innerLoopSuccess) {
                        numChanged++;
                    }
                }
            }

            if (examineAll) {
                examineAll = false;
            } else if (numChanged == 0) {
                examineAll = true;
            }

            if (runs == 0) {
                break;
            }
            runs--;
        }

        // Set threshold
        b = (bLow + bUp) / 2.0;

        // Save memory
        kernel.clean();

        fCache = null;
        I0 = I1 = I2 = I3 = I4 = null;

        // If machine is linear, delete training data
        // and store weight vector in sparse format
        if (kernel.isLinear()) {

            // We don't need to store the set of support vectors
            supportVectors = null;

            // We don't need to store the class values either
            target = null;

            // Convert weight vector
            double[] sparseWeights = new double[linear_weights.length];
            int[] sparseIndices = new int[linear_weights.length];
            int counter = 0;
            for (int i = 0; i < linear_weights.length; i++) {
                if (linear_weights[i] != 0.0) {
                    sparseWeights[counter] = linear_weights[i];
                    sparseIndices[counter] = i;
                    counter++;
                }
            }
            this.sparseWeights = new double[counter];
            this.sparseIndices = new int[counter];
            System.arraycopy(sparseWeights, 0, this.sparseWeights, 0, counter);
            System.arraycopy(sparseIndices, 0, this.sparseIndices, 0, counter);

            // Clean out weight vector
            linear_weights = null;

            // We don't need the alphas in the linear case
            alpha = null;
        }
        return true;
    }


    @Override
    protected CFit coreFit(Frame df, boolean withClasses, boolean withDistributions) {
        CFit cr = CFit.build(this, df, withClasses, withDistributions);
        for (int i = 0; i < df.rowCount(); i++) {
            double pred = predict(df, i);

            // TODO generalize
            pred = 1.0 / (1.0 + Math.exp(-pred));

            cr.firstClasses().setIndex(i, (pred < 0.5) ? classIndex1 : classIndex2);
            cr.firstDensity().setValue(i, firstTargetLevel(classIndex1), 1 - pred);
            cr.firstDensity().setValue(i, firstTargetLevel(classIndex2), pred);

            // this is the old distance variant

//            if (pred < 0) {
//                cr.firstClasses().setIndex(i, classIndex1);
//                cr.firstDensity().setValue(i, firstTargetLevel(classIndex1), -pred);
//                cr.firstDensity().setValue(i, firstTargetLevel(classIndex2), pred);
//            } else {
//                cr.firstClasses().setIndex(i, classIndex2);
//                cr.firstDensity().setValue(i, firstTargetLevel(classIndex1), -pred);
//                cr.firstDensity().setValue(i, firstTargetLevel(classIndex2), pred);
//            }
        }
        return cr;
    }

    /**
     * Computes SVM output for given instance.
     */
    protected double predict(Frame df, int row) {

        double result = 0;

        // Is the machine linear?
        if (kernel.isLinear()) {

            // Is weight vector stored in sparse format?
            if (sparseWeights == null) {
                int n1 = inputNames().length;
                for (int p = 0; p < n1; p++) {
                    if (p != targetIndex) {
                        result += linear_weights[p] * df.value(row, p);
                    }
                }
            } else {
                int n1 = inputNames().length;
                int n2 = sparseWeights.length;
                for (int p1 = 0, p2 = 0; p1 < n1 && p2 < n2; ) {
                    int ind1 = p1;
                    int ind2 = sparseIndices[p2];
                    if (ind1 == ind2) {
                        if (ind1 != targetIndex) {
                            result += df.value(row, p1) * sparseWeights[p2];
                        }
                        p1++;
                        p2++;
                    } else if (ind1 > ind2) {
                        p2++;
                    } else {
                        p1++;
                    }
                }
            }
        } else {
            for (int i = supportVectors.nextSetBit(0); i != -1; i = supportVectors.nextSetBit(i + 1)) {
                result += target[i] * alpha[i] * kernel.compute(train, i, df, row);
            }
        }
        result -= b;
        return result;
    }


    /**
     * Examines instance.
     *
     * @param i2 index of instance to examine
     * @return true if examination was successful
     */
    protected boolean examineExample(int i2) {

        double y2 = target[i2];
        double F2;

        if (I0.get(i2)) {
            F2 = fCache[i2];
        } else {
            F2 = predict(train, i2) + b - y2;
            fCache[i2] = F2;

            // Update thresholds
            if ((I1.get(i2) || I2.get(i2)) && (F2 < bUp)) {
                bUp = F2;
                iUp = i2;
            } else if ((I3.get(i2) || I4.get(i2)) && (F2 > bLow)) {
                bLow = F2;
                iLow = i2;
            }
        }

        int i1 = -1;
        // Check optimality using current bLow and bUp and, if
        // violated, find an index i1 to do joint optimization
        // with i2...
        boolean optimal = true;
        if (I0.get(i2) || I1.get(i2) || I2.get(i2)) {
            if (bLow - F2 > 2 * tol) {
                optimal = false;
                i1 = iLow;
            }
        }
        if (I0.get(i2) || I3.get(i2) || I4.get(i2)) {
            if (F2 - bUp > 2 * tol) {
                optimal = false;
                i1 = iUp;
            }
        }
        if (optimal) {
            return false;
        }

        // For i2 unbound choose the better i1...
        if (I0.get(i2)) {
            if (bLow - F2 > F2 - bUp) {
                i1 = iLow;
            } else {
                i1 = iUp;
            }
        }
        if (i1 == -1) {
            throw new RuntimeException("This should never happen!");
        }
        return takeStep(i1, i2);
    }

    /**
     * Method solving for the Lagrange multipliers for
     * two instances.
     *
     * @param i1 index of the first instance
     * @param i2 index of the second instance
     * @return true if multipliers could be found
     */
    protected boolean takeStep(int i1, int i2) {

        // Don't do anything if the two instances are the same
        if (i1 == i2) {
            return false;
        }

        // Initialize variables
        double alph1 = alpha[i1];
        double alph2 = alpha[i2];
        double y1 = target[i1];
        double y2 = target[i2];
        double F1 = fCache[i1];
        double F2 = fCache[i2];
        double s = y1 * y2;

        double C1 = C * weights.value(i1);
        double C2 = C * weights.value(i2);

        double L, H;

        // Find the constraints on a2
        if (y1 != y2) {
            L = Math.max(0, alph2 - alph1);
            H = Math.min(C2, C1 + alph2 - alph1);
        } else {
            L = Math.max(0, alph1 + alph2 - C1);
            H = Math.min(C2, alph1 + alph2);
        }

        if (MathTools.eq(L, H, 1e-10)) { // old condition was >=
            return false;
        }

        // Compute second derivative of objective function
        double k11 = kernel.compute(train, i1, train, i1);
        double k12 = kernel.compute(train, i1, train, i2);
        double k22 = kernel.compute(train, i2, train, i2);
        double eta = 2 * k12 - k11 - k22;

        double a1, a2;

        // Check if second derivative is negative
        double eps = 1e-12;
        if (eta < 0) {

            // Compute unconstrained maximum
            a2 = alph2 - y2 * (F1 - F2) / eta;

            // Compute constrained maximum
            if (a2 < L) {
                a2 = L;
            } else if (a2 > H) {
                a2 = H;
            }

        } else {

            // Look at endpoints of diagonal
            double f1 = predict(train, i1);
            double f2 = predict(train, i2);
            double v1 = f1 + b - y1 * alph1 * k11 - y2 * alph2 * k12;
            double v2 = f2 + b - y1 * alph1 * k12 - y2 * alph2 * k22;
            double gamma = alph1 + s * alph2;
            double Lobj = (gamma - s * L) + L - 0.5 * k11 * (gamma - s * L) * (gamma - s * L) -
                    0.5 * k22 * L * L - s * k12 * (gamma - s * L) * L -
                    y1 * (gamma - s * L) * v1 - y2 * L * v2;
            double Hobj = (gamma - s * H) + H - 0.5 * k11 * (gamma - s * H) * (gamma - s * H) -
                    0.5 * k22 * H * H - s * k12 * (gamma - s * H) * H -
                    y1 * (gamma - s * H) * v1 - y2 * H * v2;
            if (Lobj > Hobj + eps) {
                a2 = L;
            } else if (Lobj < Hobj - eps) {
                a2 = H;
            } else {
                a2 = alph2;
            }
        }
        if (Math.abs(a2 - alph2) < eps * (a2 + alph2 + eps)) {
            return false;
        }

        // To prevent precision problems
        double m_Del = 1000 * Double.MIN_VALUE;
        if (a2 > C2 - m_Del * C2) {
            a2 = C2;
        } else if (a2 <= m_Del * C2) {
            a2 = 0;
        }

        // Recompute a1
        a1 = alph1 + s * (alph2 - a2);

        // To prevent precision problems
        if (a1 > C1 - m_Del * C1) {
            a1 = C1;
        } else if (a1 <= m_Del * C1) {
            a1 = 0;
        }

        // Update sets
        if (a1 > 0) {
            supportVectors.set(i1, true);
        } else {
            supportVectors.set(i1, false);
        }
        if ((a1 > 0) && (a1 < C1)) {
            I0.set(i1, true);
        } else {
            I0.set(i1, false);
        }
        if ((y1 == 1) && (a1 == 0)) {
            I1.set(i1, true);
        } else {
            I1.set(i1, false);
        }
        if ((y1 == -1) && (a1 == C1)) {
            I2.set(i1, true);
        } else {
            I2.set(i1, false);
        }
        if ((y1 == 1) && (a1 == C1)) {
            I3.set(i1, true);
        } else {
            I3.set(i1, false);
        }
        if ((y1 == -1) && (a1 == 0)) {
            I4.set(i1, true);
        } else {
            I4.set(i1, false);
        }
        if (a2 > 0) {
            supportVectors.set(i2, true);
        } else {
            supportVectors.set(i2, false);
        }
        if ((a2 > 0) && (a2 < C2)) {
            I0.set(i2, true);
        } else {
            I0.set(i2, false);
        }
        if ((y2 == 1) && (a2 == 0)) {
            I1.set(i2, true);
        } else {
            I1.set(i2, false);
        }
        if ((y2 == -1) && (a2 == C2)) {
            I2.set(i2, true);
        } else {
            I2.set(i2, false);
        }
        if ((y2 == 1) && (a2 == C2)) {
            I3.set(i2, true);
        } else {
            I3.set(i2, false);
        }
        if ((y2 == -1) && (a2 == 0)) {
            I4.set(i2, true);
        } else {
            I4.set(i2, false);
        }

        // Update weight vector to reflect change a1 and a2, if linear SVM
        if (kernel.isLinear()) {
            for (int p1 = 0; p1 < inputNames().length; p1++) {
                if (p1 != targetIndex) {
                    linear_weights[p1] += y1 * (a1 - alph1) * train.value(i1, p1);
                }
            }
            for (int p2 = 0; p2 < inputNames().length; p2++) {
                if (p2 != targetIndex) {
                    linear_weights[p2] += y2 * (a2 - alph2) * train.value(i2, p2);
                }
            }
        }

        // Update error cache using new Lagrange multipliers
        for (int j = I0.nextSetBit(0); j != -1; j = I0.nextSetBit(j + 1)) {
            if ((j != i1) && (j != i2)) {
                fCache[j] +=
                        y1 * (a1 - alph1) * kernel.compute(train, i1, train, j) +
                                y2 * (a2 - alph2) * kernel.compute(train, i2, train, j);
            }
        }

        // Update error cache for i1 and i2
        fCache[i1] += y1 * (a1 - alph1) * k11 + y2 * (a2 - alph2) * k12;
        fCache[i2] += y1 * (a1 - alph1) * k12 + y2 * (a2 - alph2) * k22;

        // Update array with Lagrange multipliers
        alpha[i1] = a1;
        alpha[i2] = a2;

        // Update thresholds
        bLow = -Double.MAX_VALUE;
        bUp = Double.MAX_VALUE;
        iLow = -1;
        iUp = -1;
        for (int j = I0.nextSetBit(0); j != -1; j = I0.nextSetBit(j + 1)) {
            if (fCache[j] < bUp) {
                bUp = fCache[j];
                iUp = j;
            }
            if (fCache[j] > bLow) {
                bLow = fCache[j];
                iLow = j;
            }
        }
        if (!I0.get(i1)) {
            if (I3.get(i1) || I4.get(i1)) {
                if (fCache[i1] > bLow) {
                    bLow = fCache[i1];
                    iLow = i1;
                }
            } else {
                if (fCache[i1] < bUp) {
                    bUp = fCache[i1];
                    iUp = i1;
                }
            }
        }
        if (!I0.get(i2)) {
            if (I3.get(i2) || I4.get(i2)) {
                if (fCache[i2] > bLow) {
                    bLow = fCache[i2];
                    iLow = i2;
                }
            } else {
                if (fCache[i2] < bUp) {
                    bUp = fCache[i2];
                    iUp = i2;
                }
            }
        }
        if (iLow == -1 || iUp == -1) {
            throw new RuntimeException("This should never happen!");
        }

        // Made some progress.
        return true;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        int printed = 0;

        if ((alpha == null) && (sparseWeights == null)) {
            sb.append("BinarySMO: No model built yet.\n");
            return sb.toString();
        }
        try {
            sb.append("BinarySMO model\n");
            sb.append("===============\n");
            sb.append("**Parameters**\n");
            sb.append(fullName()).append("\n\n");

            sb.append("**Decision function**\n");
            // If machine linear, print weight vector
            if (kernel.isLinear()) {
                sb.append("Linear support vector: use attribute weights folding instead of kernel dot products.\n");

                // We can assume that the weight vector is stored in sparse
                // format because the classifier has been built
                for (int i = 0; i < sparseWeights.length; i++) {
                    if (sparseIndices[i] != targetIndex) {
                        if (printed > 0) {
                            if (sparseWeights[i] >= 0)
                                sb.append(" + ");
                            else sb.append(" - ");
                        } else {
                            sb.append("   ");
                        }
                        sb.append(formatFlex(Math.abs(sparseWeights[i]))).append(" * ");
                        sb.append("[").append(inputName(sparseIndices[i])).append("]\n");
                        printed++;
                    }
                }
            } else {
                for (int i = 0; i < alpha.length; i++) {
                    if (supportVectors.get(i)) {
                        double val = alpha[i];
                        if (target[i] == 1) {
                            if (printed > 0) {
                                sb.append(" + ");
                            } else {
                                sb.append("   ");
                            }
                        } else {
                            sb.append(" - ");
                        }
                        sb.append(formatFlex(val)).append(" * <[");
                        for (int j = 0; j < inputNames().length; j++) {
                            sb.append(formatFlex(train.value(i, inputNames()[j])));
                            if (j != inputNames().length - 1) {
                                sb.append(",");
                            }
                        }
                        sb.append("], X>\n");
                        printed++;
                    }
                }
            }
            if (b > 0) {
                sb.append(" - ").append(formatFlex(b));
            } else {
                sb.append(" + ").append(formatFlex(-b));
            }

            if (!kernel.isLinear()) {
                sb.append("\n\nNumber of support vectors: ").append(supportVectors.cardinality());
            }
//            int numEval = 0;
//            int numCacheHits = -1;
//            if (kernel != null) {
//                numEval = kernel.numEvals();
//                numCacheHits = kernel.numCacheHits();
//            }
//            text.append("\n\nNumber of kernel evaluations: " + numEval);
//            if (numCacheHits >= 0 && numEval > 0) {
//                double hitRatio = 1 - numEval * 1.0 / (numCacheHits + numEval);
//                text.append(" (" + Utils.doubleToString(hitRatio * 100, 7, 3).trim() + "% cached)");
//            }

        } catch (Exception e) {
            e.printStackTrace();
            sb.append("Can't print BinarySMO classifier.");
        }

        return sb.toString();
    }
}
