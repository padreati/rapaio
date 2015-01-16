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

package rapaio.ml.classifier.svm;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/16/15.
 */

import rapaio.core.MathBase;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CResult;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.svm.kernel.Kernel;
import rapaio.ml.classifier.svm.kernel.PolyKernel;
import rapaio.ml.common.VarSelector;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * Class for building a binary support vector machine.
 */
public class BinarySMO extends AbstractClassifier implements Serializable {

    protected double[] m_alpha; // Lagrange multipliers from dual
    protected double m_b, m_bLow, m_bUp; // thresholds
    protected int m_iLow, m_iUp; // indices for m_bLow and m_bUp

    // class indices
    protected int cl1 = 1;
    protected int cl2 = 2;
    protected int maxRuns = 1_000_000;

    protected double m_C = 1.0; // complexity parameter
    protected double m_eps = 1e-12; // epsilon for rounding
    protected double m_tol = 1e-3; // tolerance of accuracy
    protected static double m_Del = 1000 * Double.MIN_VALUE; // precision constant for updating sets

    protected Frame train;
    protected Var weights;


    protected int m_classIndex;
    /**
     * Weight vector for linear machine.
     */
    protected double[] m_weights;

    /**
     * Variables to hold weight vector in sparse form.
     * (To reduce storage requirements.)
     */
    protected double[] m_sparseWeights;
    protected int[] m_sparseIndices;
    protected Kernel m_kernel = new PolyKernel(1, false);
    protected double[] m_class;
    protected double[] m_errors; // The current set of errors for all non-bound examples.

    /* The five different sets used by the algorithm. */
    protected SMOSet m_I0; // i: 0 < m_alpha[i] < C
    protected SMOSet m_I1; // i: m_class[i] = 1, m_alpha[i] = 0
    protected SMOSet m_I2; // i: m_class[i] = -1, m_alpha[i] =C
    protected SMOSet m_I3; // i: m_class[i] = 1, m_alpha[i] = C
    protected SMOSet m_I4; // i: m_class[i] = -1, m_alpha[i] = 0

    /**
     * The set of support vectors
     */
    protected SMOSet m_supportVectors; // {i: 0 < m_alpha[i]}

    /**
     * Stores logistic regression model for probability estimate
     */
//    protected Logistic m_logistic = null;

    /**
     * Stores the weight of the training instances
     */
    protected double m_sumOfWeights = 0;


    @Override
    public String name() {
        return "SMO";
    }

    @Override
    public String fullName() {
        return "not implemented";
    }

    @Override
    public Classifier newInstance() {
        return null;
    }

    public BinarySMO withKernel(Kernel value) {
        m_kernel = value;
        return this;
    }

    public BinarySMO withC(double c) {
        this.m_C = c;
        return this;
    }

    public BinarySMO withTol(double tol) {
        this.m_tol = tol;
        return this;
    }

    public BinarySMO withMaxRuns(int maxRuns) {
        this.maxRuns = maxRuns;
        return this;
    }

    @Override
    public void learn(Frame df, Var weights, String... targetVarNames) {

        List<String> targetVarsList = new VarRange(targetVarNames).parseVarNames(df);
        if (targetVarsList.size() != 1) {
            throw new IllegalArgumentException("Binary classifiers can learn only one target variable");
        }
        this.targetNames = targetVarsList.toArray(new String[targetVarsList.size()]);
        this.dict = new HashMap<>();
        this.dict.put(firstTargetName(), df.var(firstTargetName()).dictionary());

        this.varSelector = new VarSelector.Standard();
        this.varSelector.initialize(df, new VarRange(targetVarNames));

        // filter out other classes

        Mapping map = df.stream().filter(s -> s.index(firstTargetName()) == cl1 || s.index(firstTargetName()) == cl2).collectMapping();
        df = df.mapRows(map);
        weights = weights.mapRows(map);

        if (df.rowCount() == 0) {
            throw new IllegalArgumentException("After filtering other classes, there " +
                    "were no other rows remained");
        }

        // Initialize some variables

        m_bUp = -1;
        m_bLow = 1;
        m_b = 0;
        m_alpha = null;
        m_weights = null;
        m_errors = null;
//        m_logistic = null;
        m_I0 = null;
        m_I1 = null;
        m_I2 = null;
        m_I3 = null;
        m_I4 = null;
        m_sparseWeights = null;
        m_sparseIndices = null;

        // Store the sum of weights
        m_sumOfWeights = weights.stream().mapToDouble().sum();

        // Set class values
        m_class = new double[df.rowCount()];
        m_iUp = -1;
        m_iLow = -1;
        for (int i = 0; i < m_class.length; i++) {
            if (df.var(firstTargetName()).index(i) == cl1) {
                m_class[i] = -1;
                m_iLow = i;
            } else if (df.var(firstTargetName()).index(i) == cl2) {
                m_class[i] = 1;
                m_iUp = i;
            } else {
                throw new RuntimeException("This should never happen! Solve only binary case!");
            }
        }

        // Check whether one or both classes are missing

        if ((m_iUp == -1) || (m_iLow == -1)) {
            if (m_iUp != -1) {
                m_b = -1;
            } else if (m_iLow != -1) {
                m_b = 1;
            } else {
                m_class = null;
                return;
            }
            if (m_kernel.isLinear()) {
                m_sparseWeights = new double[0];
                m_sparseIndices = new int[0];
                m_class = null;
            } else {
                m_supportVectors = new SMOSet(0);
                m_alpha = new double[0];
                m_class = new double[0];
            }

//            Fit sigmoid if requested
//            if (fitLogistic) {
//                fitLogistic(insts, cl1, cl2, numFolds, new Random(randomSeed));
//            }
            return;
        }

        // Set the reference to the data
        this.train = df;
        this.weights = weights;

        m_classIndex = df.varIndex(firstTargetName());

        // If machine is linear, reserve space for weights

        if (m_kernel.isLinear()) {
            m_weights = new double[varSelector.nextVarNames().length];
        } else {
            m_weights = null;
        }

        // Initialize alpha array to zero
        m_alpha = new double[df.rowCount()];

        // Initialize sets
        m_supportVectors = new SMOSet(df.rowCount());
        m_I0 = new SMOSet(df.rowCount());
        m_I1 = new SMOSet(df.rowCount());
        m_I2 = new SMOSet(df.rowCount());
        m_I3 = new SMOSet(df.rowCount());
        m_I4 = new SMOSet(df.rowCount());

        // Clean out some instance variables
        m_sparseWeights = null;
        m_sparseIndices = null;

        // init kernel
        m_kernel.buildKernel(varSelector.nextVarNames());

        // Initialize error cache
        m_errors = new double[df.rowCount()];
        m_errors[m_iLow] = 1;
        m_errors[m_iUp] = -1;

        // Build up I1 and I4
        for (int i = 0; i < m_class.length; i++) {
            if (m_class[i] == 1) {
                m_I1.insert(i);
            } else {
                m_I4.insert(i);
            }
        }

        // Loop to find all the support vectors
        int numChanged = 0;
        boolean examineAll = true;

        int runs = maxRuns;
        while ((numChanged > 0) || examineAll) {
            numChanged = 0;

            if (examineAll) {
                for (int i = 0; i < m_alpha.length; i++) {
                    if (examineExample(i)) {
                        numChanged++;
                    }
                }
            } else {

                // This code implements Modification 1 from Keerthi et al.'s paper
                for (int i = 0; i < m_alpha.length; i++) {
                    if ((m_alpha[i] > 0) &&
                            (m_alpha[i] < m_C * weights.value(i))) {
                        if (examineExample(i)) {
                            numChanged++;
                        }

                        // Is optimality on unbound vectors obtained?
                        if (m_bUp > m_bLow - 2 * m_tol) {
                            numChanged = 0;
                            break;
                        }
                    }
                }

//                System.out.println(m_bUp-m_bLow);

                //This is the code for Modification 2 from Keerthi et al.'s paper
//                boolean innerLoopSuccess = true;
//                numChanged = 0;
//                while ((m_bUp < m_bLow - 2 * m_tol) && innerLoopSuccess) {
//                    innerLoopSuccess = takeStep(m_iUp, m_iLow, m_errors[m_iLow]);
//                }
            }

            if (examineAll) {
                examineAll = false;
            } else if (numChanged == 0) {
                examineAll = true;
            }

//            checkClassifier();
            if (runs == 0) {
                break;
            }
            runs--;
        }
//        System.out.println("last: " + (m_bUp-m_bLow));

        // Set threshold
        m_b = (m_bLow + m_bUp) / 2.0;

        // Save memory
        m_kernel.clean();

        m_errors = null;
        m_I0 = m_I1 = m_I2 = m_I3 = m_I4 = null;

        // If machine is linear, delete training data
        // and store weight vector in sparse format
        if (m_kernel.isLinear()) {

            // We don't need to store the set of support vectors
            m_supportVectors = null;

            // We don't need to store the class values either
            m_class = null;

            // Convert weight vector
            double[] sparseWeights = new double[m_weights.length];
            int[] sparseIndices = new int[m_weights.length];
            int counter = 0;
            for (int i = 0; i < m_weights.length; i++) {
                if (m_weights[i] != 0.0) {
                    sparseWeights[counter] = m_weights[i];
                    sparseIndices[counter] = i;
                    counter++;
                }
            }
            m_sparseWeights = new double[counter];
            m_sparseIndices = new int[counter];
            System.arraycopy(sparseWeights, 0, m_sparseWeights, 0, counter);
            System.arraycopy(sparseIndices, 0, m_sparseIndices, 0, counter);

            // Clean out weight vector
            m_weights = null;

            // We don't need the alphas in the linear case
            m_alpha = null;
        }

        // Fit sigmoid if requested
//        if (fitLogistic) {
//            fitLogistic(insts, cl1, cl2, numFolds, new Random(randomSeed));
//        }

    }


    @Override
    public CResult predict(Frame df, boolean withClasses, boolean withDistributions) {
        CResult cr = CResult.newEmpty(this, df, withClasses, withDistributions);
        cr.addTarget(firstTargetName(), firstDictionary());

        for (int i = 0; i < df.rowCount(); i++) {
            try {
                double pred = predict(df, i);
                if (MathBase.gr(pred, 0)) {
                    cr.firstClasses().setIndex(i, cl2);
                    cr.firstDensity().setValue(i, firstDictionary()[cl2], 1);
                } else {
                    cr.firstClasses().setIndex(i, cl1);
                    cr.firstDensity().setValue(i, firstDictionary()[cl1], 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return cr;
    }

    /**
     * Computes SVM output for given instance.
     */
    public double predict(Frame df, int row) {

        double result = 0;
        String[] varNames = varSelector.nextVarNames();

        // Is the machine linear?
        if (m_kernel.isLinear()) {

            // Is weight vector stored in sparse format?
            if (m_sparseWeights == null) {
                int n1 = varNames.length;
                for (int p = 0; p < n1; p++) {
                    if (p != m_classIndex) {
                        result += m_weights[p] * df.value(row, p);
                    }
                }
            } else {
                int n1 = varSelector.nextVarNames().length;
                int n2 = m_sparseWeights.length;
                for (int p1 = 0, p2 = 0; p1 < n1 && p2 < n2; ) {
                    int ind1 = p1;
                    int ind2 = m_sparseIndices[p2];
                    if (ind1 == ind2) {
                        if (ind1 != m_classIndex) {
                            result += df.value(row, p1) * m_sparseWeights[p2];
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
            for (int i = m_supportVectors.getNext(-1); i != -1; i = m_supportVectors.getNext(i)) {
                result += m_class[i] * m_alpha[i] * m_kernel.eval(train, i, df, row);
            }
        }
        result -= m_b;

        return result;
    }


    /**
     * Examines instance.
     *
     * @param i2 index of instance to examine
     * @return true if examination was successfull
     * @throws Exception if something goes wrong
     */
    protected boolean examineExample(int i2) {

        double y2, F2;
        int i1 = -1;

        y2 = m_class[i2];
        if (m_I0.contains(i2)) {
            F2 = m_errors[i2];
        } else {
            F2 = predict(train, i2) + m_b - y2;
            m_errors[i2] = F2;

            // Update thresholds
            if ((m_I1.contains(i2) || m_I2.contains(i2)) && (F2 < m_bUp)) {
                m_bUp = F2;
                m_iUp = i2;
            } else if ((m_I3.contains(i2) || m_I4.contains(i2)) && (F2 > m_bLow)) {
                m_bLow = F2;
                m_iLow = i2;
            }
        }

        // Check optimality using current bLow and bUp and, if
        // violated, find an index i1 to do joint optimization
        // with i2...
        boolean optimal = true;
        if (m_I0.contains(i2) || m_I1.contains(i2) || m_I2.contains(i2)) {
            if (m_bLow - F2 > 2 * m_tol) {
                optimal = false;
                i1 = m_iLow;
            }
        }
        if (m_I0.contains(i2) || m_I3.contains(i2) || m_I4.contains(i2)) {
            if (F2 - m_bUp > 2 * m_tol) {
                optimal = false;
                i1 = m_iUp;
            }
        }
        if (optimal) {
            return false;
        }

        // For i2 unbound choose the better i1...
        if (m_I0.contains(i2)) {
            if (m_bLow - F2 > F2 - m_bUp) {
                i1 = m_iLow;
            } else {
                i1 = m_iUp;
            }
        }
        if (i1 == -1) {
            throw new RuntimeException("This should never happen!");
        }
        return takeStep(i1, i2, F2);
    }

    /**
     * Method solving for the Lagrange multipliers for
     * two instances.
     *
     * @param i1 index of the first instance
     * @param i2 index of the second instance
     * @param F2
     * @return true if multipliers could be found
     * @throws Exception if something goes wrong
     */
    protected boolean takeStep(int i1, int i2, double F2) {

        double alph1, alph2, y1, y2, F1, s, L, H, k11, k12, k22, eta,
                a1, a2, f1, f2, v1, v2, Lobj, Hobj;
        double C1 = m_C * weights.value(i1);
        double C2 = m_C * weights.value(i2);

        // Don't do anything if the two instances are the same
        if (i1 == i2) {
            return false;
        }

        // Initialize variables
        alph1 = m_alpha[i1];
        alph2 = m_alpha[i2];
        y1 = m_class[i1];
        y2 = m_class[i2];
        F1 = m_errors[i1];
        s = y1 * y2;

        // Find the constraints on a2
        if (y1 != y2) {
            L = Math.max(0, alph2 - alph1);
            H = Math.min(C2, C1 + alph2 - alph1);
        } else {
            L = Math.max(0, alph1 + alph2 - C1);
            H = Math.min(C2, alph1 + alph2);
        }
        if (L >= H) {
            return false;
        }

        // Compute second derivative of objective function
        k11 = m_kernel.eval(train, i1, train, i1);
        k12 = m_kernel.eval(train, i1, train, i2);
        k22 = m_kernel.eval(train, i2, train, i2);
        eta = 2 * k12 - k11 - k22;

        // Check if second derivative is negative
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
            f1 = predict(train, i1);
            f2 = predict(train, i2);
            v1 = f1 + m_b - y1 * alph1 * k11 - y2 * alph2 * k12;
            v2 = f2 + m_b - y1 * alph1 * k12 - y2 * alph2 * k22;
            double gamma = alph1 + s * alph2;
            Lobj = (gamma - s * L) + L - 0.5 * k11 * (gamma - s * L) * (gamma - s * L) -
                    0.5 * k22 * L * L - s * k12 * (gamma - s * L) * L -
                    y1 * (gamma - s * L) * v1 - y2 * L * v2;
            Hobj = (gamma - s * H) + H - 0.5 * k11 * (gamma - s * H) * (gamma - s * H) -
                    0.5 * k22 * H * H - s * k12 * (gamma - s * H) * H -
                    y1 * (gamma - s * H) * v1 - y2 * H * v2;
            if (Lobj > Hobj + m_eps) {
                a2 = L;
            } else if (Lobj < Hobj - m_eps) {
                a2 = H;
            } else {
                a2 = alph2;
            }
        }
        if (Math.abs(a2 - alph2) < m_eps * (a2 + alph2 + m_eps)) {
            return false;
        }

        // To prevent precision problems
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
            m_supportVectors.insert(i1);
        } else {
            m_supportVectors.delete(i1);
        }
        if ((a1 > 0) && (a1 < C1)) {
            m_I0.insert(i1);
        } else {
            m_I0.delete(i1);
        }
        if ((y1 == 1) && (a1 == 0)) {
            m_I1.insert(i1);
        } else {
            m_I1.delete(i1);
        }
        if ((y1 == -1) && (a1 == C1)) {
            m_I2.insert(i1);
        } else {
            m_I2.delete(i1);
        }
        if ((y1 == 1) && (a1 == C1)) {
            m_I3.insert(i1);
        } else {
            m_I3.delete(i1);
        }
        if ((y1 == -1) && (a1 == 0)) {
            m_I4.insert(i1);
        } else {
            m_I4.delete(i1);
        }
        if (a2 > 0) {
            m_supportVectors.insert(i2);
        } else {
            m_supportVectors.delete(i2);
        }
        if ((a2 > 0) && (a2 < C2)) {
            m_I0.insert(i2);
        } else {
            m_I0.delete(i2);
        }
        if ((y2 == 1) && (a2 == 0)) {
            m_I1.insert(i2);
        } else {
            m_I1.delete(i2);
        }
        if ((y2 == -1) && (a2 == C2)) {
            m_I2.insert(i2);
        } else {
            m_I2.delete(i2);
        }
        if ((y2 == 1) && (a2 == C2)) {
            m_I3.insert(i2);
        } else {
            m_I3.delete(i2);
        }
        if ((y2 == -1) && (a2 == 0)) {
            m_I4.insert(i2);
        } else {
            m_I4.delete(i2);
        }

        // Update weight vector to reflect change a1 and a2, if linear SVM
        if (m_kernel.isLinear()) {
            for (int p1 = 0; p1 < varSelector.nextVarNames().length; p1++) {
                if (p1 != m_classIndex) {
                    m_weights[p1] += y1 * (a1 - alph1) * train.value(i1, p1);
                }
            }
            for (int p2 = 0; p2 < varSelector.nextVarNames().length; p2++) {
                if (p2 != m_classIndex) {
                    m_weights[p2] += y2 * (a2 - alph2) * train.value(i2, p2);
                }
            }
        }

        // Update error cache using new Lagrange multipliers
        for (int j = m_I0.getNext(-1); j != -1; j = m_I0.getNext(j)) {
            if ((j != i1) && (j != i2)) {
                m_errors[j] +=
                        y1 * (a1 - alph1) * m_kernel.eval(train, i1, train, j) +
                                y2 * (a2 - alph2) * m_kernel.eval(train, i2, train, j);
            }
        }

        // Update error cache for i1 and i2
        m_errors[i1] += y1 * (a1 - alph1) * k11 + y2 * (a2 - alph2) * k12;
        m_errors[i2] += y1 * (a1 - alph1) * k12 + y2 * (a2 - alph2) * k22;

        // Update array with Lagrange multipliers
        m_alpha[i1] = a1;
        m_alpha[i2] = a2;

        // Update thresholds
        m_bLow = -Double.MAX_VALUE;
        m_bUp = Double.MAX_VALUE;
        m_iLow = -1;
        m_iUp = -1;
        for (int j = m_I0.getNext(-1); j != -1; j = m_I0.getNext(j)) {
            if (m_errors[j] < m_bUp) {
                m_bUp = m_errors[j];
                m_iUp = j;
            }
            if (m_errors[j] > m_bLow) {
                m_bLow = m_errors[j];
                m_iLow = j;
            }
        }
        if (!m_I0.contains(i1)) {
            if (m_I3.contains(i1) || m_I4.contains(i1)) {
                if (m_errors[i1] > m_bLow) {
                    m_bLow = m_errors[i1];
                    m_iLow = i1;
                }
            } else {
                if (m_errors[i1] < m_bUp) {
                    m_bUp = m_errors[i1];
                    m_iUp = i1;
                }
            }
        }
        if (!m_I0.contains(i2)) {
            if (m_I3.contains(i2) || m_I4.contains(i2)) {
                if (m_errors[i2] > m_bLow) {
                    m_bLow = m_errors[i2];
                    m_iLow = i2;
                }
            } else {
                if (m_errors[i2] < m_bUp) {
                    m_bUp = m_errors[i2];
                    m_iUp = i2;
                }
            }
        }
        if ((m_iLow == -1) || (m_iUp == -1)) {
            throw new RuntimeException("This should never happen!");
        }

        // Made some progress.
        return true;
    }

    /**
     * Quick and dirty check whether the quadratic programming problem is solved.
     *
     * @throws Exception if checking fails
     */
    protected void checkClassifier() {

        double sum = 0;
        for (int i = 0; i < m_alpha.length; i++) {
            if (m_alpha[i] > 0) {
                sum += m_class[i] * m_alpha[i];
            }
        }
        System.out.println("Sum of y(i) * alpha(i): " + sum);

//        for (int i = 0; i < m_alpha.length; i++) {
//            double output = predict(train, i);
//            if (MathBase.eq(m_alpha[i], 0)) {
//                if (MathBase.sm(m_class[i] * output, 1)) {
//                    System.err.println("KKT condition 1 violated: " + m_class[i] * output);
//                }
//            }
//            if (MathBase.gr(m_alpha[i], 0) &&
//                    MathBase.sm(m_alpha[i], m_C * weights.value(i))) {
//                if (!MathBase.eq(m_class[i] * output, 1)) {
//                    System.err.println("KKT condition 2 violated: " + m_class[i] * output);
//                }
//            }
//            if (MathBase.eq(m_alpha[i], m_C * weights.value(i))) {
//                if (MathBase.gr(m_class[i] * output, 1)) {
//                    System.err.println("KKT condition 3 violated: " + m_class[i] * output);
//                }
//            }
//        }
    }

    @Override
    public void buildSummary(StringBuilder sb) {

    }

    /**
     * Prints out the classifier.
     *
     * @return a description of the classifier as a string
     */
    /*
    public String toString() {

        StringBuffer text = new StringBuffer();
        int printed = 0;

        if ((m_alpha == null) && (m_sparseWeights == null)) {
            return "BinarySMO: No model built yet.\n";
        }
        try {
            text.append("BinarySMO\n\n");

            // If machine linear, print weight vector
            if (m_KernelIsLinear) {
                text.append("Machine linear: showing attribute weights, ");
                text.append("not support vectors.\n\n");

                // We can assume that the weight vector is stored in sparse
                // format because the classifier has been built
                for (int i = 0; i < m_sparseWeights.length; i++) {
                    if (m_sparseIndices[i] != (int) m_classIndex) {
                        if (printed > 0) {
                            text.append(" + ");
                        } else {
                            text.append("   ");
                        }
                        text.append(Utils.doubleToString(m_sparseWeights[i], 12, 4) +
                                " * ");
                        if (m_filterType == FILTER_STANDARDIZE) {
                            text.append("(standardized) ");
                        } else if (m_filterType == FILTER_NORMALIZE) {
                            text.append("(normalized) ");
                        }
                        if (!m_checksTurnedOff) {
                            text.append(m_data.attribute(m_sparseIndices[i]).name() + "\n");
                        } else {
                            text.append("attribute with index " +
                                    m_sparseIndices[i] + "\n");
                        }
                        printed++;
                    }
                }
            } else {
                for (int i = 0; i < m_alpha.length; i++) {
                    if (m_supportVectors.contains(i)) {
                        double val = m_alpha[i];
                        if (m_class[i] == 1) {
                            if (printed > 0) {
                                text.append(" + ");
                            }
                        } else {
                            text.append(" - ");
                        }
                        text.append(Utils.doubleToString(val, 12, 4)
                                + " * <");
                        for (int j = 0; j < m_data.numAttributes(); j++) {
                            if (j != m_data.classIndex()) {
                                text.append(m_data.instance(i).toString(j));
                            }
                            if (j != m_data.numAttributes() - 1) {
                                text.append(" ");
                            }
                        }
                        text.append("> * X]\n");
                        printed++;
                    }
                }
            }
            if (m_b > 0) {
                text.append(" - " + Utils.doubleToString(m_b, 12, 4));
            } else {
                text.append(" + " + Utils.doubleToString(-m_b, 12, 4));
            }

            if (!m_kernel.isLinear()) {
                text.append("\n\nNumber of support vectors: " +
                        m_supportVectors.numElements());
            }
            int numEval = 0;
            int numCacheHits = -1;
            if (m_kernel != null) {
                numEval = m_kernel.numEvals();
                numCacheHits = m_kernel.numCacheHits();
            }
            text.append("\n\nNumber of kernel evaluations: " + numEval);
            if (numCacheHits >= 0 && numEval > 0) {
                double hitRatio = 1 - numEval * 1.0 / (numCacheHits + numEval);
                text.append(" (" + Utils.doubleToString(hitRatio * 100, 7, 3).trim() + "% cached)");
            }

        } catch (Exception e) {
            e.printStackTrace();

            return "Can't print BinarySMO classifier.";
        }

        return text.toString();
    }
    */
/**
 * Fits logistic regression model to SVM outputs analogue
 * to John Platt's method.
 * <p>
 * //     * @param insts    the set of training instances
 * //     * @param cl1      the first class' index
 * //     * @param cl2      the second class' index
 * //     * @param numFolds the number of folds for cross-validation
 * //     * @param random   for randomizing the data
 *
 * @throws Exception if the sigmoid can't be fit successfully
 */
//    protected void fitLogistic(Instances insts, int cl1, int cl2,
//                               int numFolds, Random random)
//            throws Exception {
//
//        Create header of instances object
//        FastVector atts = new FastVector(2);
//        atts.addElement(new Attribute("pred"));
//        FastVector attVals = new FastVector(2);
//        attVals.addElement(insts.classAttribute().value(cl1));
//        attVals.addElement(insts.classAttribute().value(cl2));
//        atts.addElement(new Attribute("class", attVals));
//        Instances data = new Instances("data", atts, insts.numInstances());
//        data.setClassIndex(1);
//
//        Collect data for fitting the logistic model
//        if (numFolds <= 0) {
//
//            Use training data
//            for (int j = 0; j < insts.numInstances(); j++) {
//                Instance inst = insts.instance(j);
//                double[] vals = new double[2];
//                vals[0] = SVMOutput(-1, inst);
//                if (inst.classValue() == cl2) {
//                    vals[1] = 1;
//                }
//                data.add(new DenseInstance(inst.weight(), vals));
//            }
//        } else {
//
//            Check whether number of folds too large
//            if (numFolds > insts.numInstances()) {
//                numFolds = insts.numInstances();
//            }
//
//            Make copy of instances because we will shuffle them around
//            insts = new Instances(insts);
//
//            Perform three-fold cross-validation to collect
//            unbiased predictions
//            insts.randomize(random);
//            insts.stratify(numFolds);
//            for (int i = 0; i < numFolds; i++) {
//                Instances train = insts.trainCV(numFolds, i, random);
//          /*	  SerializedObject so = new SerializedObject(this);
//                  BinarySMO smo = (BinarySMO)so.getObject(); */
//                BinarySMO smo = new BinarySMO();
//                smo.setKernel(Kernel.makeCopy(SMO.this.m_kernel));
//                smo.buildClassifier(train, cl1, cl2, false, -1, -1);
//                Instances test = insts.testCV(numFolds, i);
//                for (int j = 0; j < test.numInstances(); j++) {
//                    double[] vals = new double[2];
//                    vals[0] = smo.SVMOutput(-1, test.instance(j));
//                    if (test.instance(j).classValue() == cl2) {
//                        vals[1] = 1;
//                    }
//                    data.add(new DenseInstance(test.instance(j).weight(), vals));
//                }
//            }
//        }
//
//        Build logistic regression model
//        m_logistic = new Logistic();
//        m_logistic.buildClassifier(data);
//    }
}