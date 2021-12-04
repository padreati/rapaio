/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.model.svm;

import static rapaio.printer.Format.floatFlex;

import java.io.Serial;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Set;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.math.MathTools;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ValueParam;
import rapaio.ml.common.kernel.Kernel;
import rapaio.ml.common.kernel.PolyKernel;
import rapaio.ml.model.ClassifierModel;
import rapaio.ml.model.ClassifierResult;
import rapaio.ml.model.RunInfo;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;


/**
 * Class for building a binary support vector machine.
 * <p>
 * todo:
 * <p>
 * - load state with all useless values after fit
 * - when fit is over optimize space for support vectors
 * - refactor kernels to use stripe matrices for speed
 * - write informative printing information
 * - as a side note: multi class svm can be deffered to a generic multiclass classifier which
 * should be able to transform any binary classifier into a multi class one.
 */
public class BinarySMO extends ClassifierModel<BinarySMO, ClassifierResult, RunInfo<BinarySMO>> {

    public static BinarySMO newModel() {
        return new BinarySMO();
    }

    @Serial
    private static final long serialVersionUID = 1208515184777030598L;

    /**
     * Kernel which maps inputs to feature space.
     */
    public final ValueParam<Kernel, BinarySMO> kernel = new ValueParam<>(this, new PolyKernel(1), "kernel");

    /**
     * Maximum number of iterations.
     */
    public final ValueParam<Integer, BinarySMO> maxRuns = new ValueParam<>(this, 1_000,
            "maxRuns", x -> x != null && x > 0);

    /**
     * Complexity parameter.
     */
    public final ValueParam<Double, BinarySMO> c = new ValueParam<>(this, 1.0, "c", Double::isFinite);

    /**
     * Tolerance threshold used to measure progress and considering numerical convergence.
     */
    public final ValueParam<Double, BinarySMO> eps = new ValueParam<>(this, 1e-12, "eps", Double::isFinite);

    /**
     * First target label.
     */
    public final ValueParam<String, BinarySMO> firstLabel = new ValueParam<>(this, "?", "firstLabel");

    /**
     * Second target label, used when oneVsAll is false
     */
    public final ValueParam<String, BinarySMO> secondLabel = new ValueParam<>(this, "?", "secondLabel");

    /**
     * Solver for problem with valid values: Keerthi1 and Keerthi2
     */
    public final ValueParam<String, BinarySMO> solver = new ValueParam<>(this, "Keerthi2",
            "solver", x -> Set.of("Keerthi1", "Keerthi2").contains(x));

    private static final double eps_delta = 1e-200;

    private double[] alpha; // Lagrange multipliers from dual
    private double b;

    /**
     * Variables to hold weight vector in sparse form.
     * (To reduce storage requirements.)
     */
    private double[] sparseWeights;
    private int[] sparseIndices;
    /**
     * The set of support vectors
     */
    private BitSet supportVectors; // {i: 0 < alpha[i]}

    private Frame train;
    private Var weights;

    private double[] y;
    private String label1;
    private String label2;
    private boolean oneVsAll;

    /**
     * Weight vector for linear machine.
     */
    private double[] linear_weights;

    private static final class State {
        double bLow;
        double bUp; // thresholds
        int iLow;
        int iUp; // indices for bLow and bUp

        double[] fCache; // The current set of errors for all non-bound examples.

        /* The five different sets used by the algorithm. */
        BitSet I0; // i: 0 < alpha[i] < c
        BitSet I1; // i: classes[i] = 1, alpha[i] = 0
        BitSet I2; // i: classes[i] = -1, alpha[i] =c
        BitSet I3; // i: classes[i] = 1, alpha[i] = c
        BitSet I4; // i: classes[i] = -1, alpha[i] = 0
    }

    private BinarySMO() {
    }

    @Override
    public String name() {
        return "BinarySMO";
    }

    @Override
    public BinarySMO newInstance() {
        return new BinarySMO().copyParameterValues(this);
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities(
                1, 100_000,
                Arrays.asList(VarType.BINARY, VarType.INT, VarType.NOMINAL, VarType.DOUBLE), false,
                1, 1, List.of(VarType.NOMINAL), false);
    }

    private void convertWeightVector() {
        double[] sparseWeights = new double[linear_weights.length];
        int[] sparseIndices = new int[linear_weights.length];
        int counter = 0;
        for (int i = 0; i < linear_weights.length; i++) {
            if (Math.abs(linear_weights[i]) >= eps_delta) {
                sparseWeights[counter] = linear_weights[i];
                sparseIndices[counter] = i;
                counter++;
            }
        }
        this.sparseWeights = new double[counter];
        this.sparseIndices = new int[counter];
        System.arraycopy(sparseWeights, 0, this.sparseWeights, 0, counter);
        System.arraycopy(sparseIndices, 0, this.sparseIndices, 0, counter);
    }

    @Override
    protected boolean coreFit(Frame initDf, Var initWeights) {
        State s = new State();
        prepareDataset(s, initDf, initWeights);
        initialize(s);

        // Loop to find all the support vectors
        int numChanged = 0;
        boolean examineAll = true;

        int runs = maxRuns.get();

        while (runs-- >= 0 && (numChanged > 0 || examineAll)) {
            numChanged = 0;

            if (examineAll) {

                // add random as an additional step
                int offset = RandomSource.nextInt(train.rowCount());
                for (int i = offset; i < train.rowCount() + offset; i++) {
                    int pos = i;
                    if (pos >= train.rowCount()) {
                        pos -= train.rowCount();
                    }
                    if (examineExample(s, pos)) {
                        numChanged++;
                    }
                }

            } else {

                if ("Keerthi1".equals(solver.get())) {
                    // This code implements Modification 1 from Keerthi et al.'s paper
                    int offset = RandomSource.nextInt(train.rowCount());
                    for (int i = offset; i < train.rowCount() + offset; i++) {

                        int pos = i;
                        if (pos >= train.rowCount()) {
                            pos -= train.rowCount();
                        }

                        if (alpha[pos] > 0 && alpha[pos] < c.get() * weights.getDouble(pos)) {
                            if (examineExample(s, pos)) {
                                numChanged++;
                            }
                            // Is optimality on unbound vectors obtained?
                            if (s.bUp > s.bLow - 2 * eps.get()) {
                                numChanged = 0;
                                break;
                            }
                        }
                    }
                } else {
                    //This is the code for Modification 2 from Keerthi et al.'s paper
                    boolean innerLoopSuccess = true;
                    numChanged = 0;
                    while ((s.bUp < s.bLow - 2 * eps.get()) && innerLoopSuccess) {
                        innerLoopSuccess = takeStep(s, s.iUp, s.iLow);
                        if (innerLoopSuccess) {
                            numChanged++;
                        }
                    }
                }
            }

            if (examineAll) {
                examineAll = false;
            } else if (numChanged == 0) {
                examineAll = true;
            }

            if (runningHook.get() != null) {
                runningHook.get().accept(RunInfo.forClassifier(this, runs));
            }
        }

        // Set threshold
        b = (s.bLow + s.bUp) / 2.0;

        // Save memory
        kernel.get().clean();

        // If machine is linear, delete training data
        // and store weight vector in sparse format
        if (kernel.get().isLinear()) {

            // We don't need to store the set of support vectors
            supportVectors = null;

            convertWeightVector();

            // Clean out weight vector
            linear_weights = null;

            // We don't need the alphas in the linear case
            alpha = null;
        }

        return true;
    }

    private void prepareDataset(State s, Frame df, Var w) {

        List<String> targetLevels = firstTargetLevels().subList(1, firstTargetLevels().size());
        boolean valid = false;
        if (!("?".equals(firstLabel.get()) || "?".equals(secondLabel.get()))) {
            // pre specified one vs one
            label1 = firstLabel.get();
            label2 = secondLabel.get();
            oneVsAll = false;

            Mapping map = df.stream()
                    .filter(sp -> sp.getLabel(firstTargetName()).equals(label1)
                            || sp.getLabel(firstTargetName()).equals(label2))
                    .collectMapping();
            if (map.isEmpty()) {
                throw new IllegalArgumentException("After filtering other classes, there were no other rows remained.");
            }
            this.train = df.mapRows(map).copy();
            this.weights = w.mapRows(map).copy();
            valid = true;
        } else if (!"?".equals(firstLabel.get())) {
            // one vs all type of classification
            label1 = firstLabel.get();
            label2 = "~" + firstLabel.get();
            oneVsAll = true;
            this.train = df;
            this.weights = w;
            valid = true;
        } else if (targetLevels.size() == 2) {
            label1 = targetLevels.get(0);
            label2 = targetLevels.get(1);
            oneVsAll = false;
            this.train = df;
            this.weights = w;
            valid = true;
        }

        if (valid) {
            y = new double[train.rowCount()];
            for (int i = 0; i < train.rowCount(); i++) {
                y[i] = label1.equals(train.getLabel(i, firstTargetName())) ? -1 : 1;
            }
        } else {
            throw new IllegalArgumentException("Invalid target labels specification.");
        }
    }

    private boolean initialize(State s) {

        final int n = train.rowCount();

        s.bUp = -1;
        s.bLow = 1;
        b = 0;
        alpha = null;
        linear_weights = null;
        sparseWeights = null;
        sparseIndices = null;

        // Set class values
        s.iUp = -1;
        s.iLow = -1;
        for (int i = 0; i < n; i++) {
            if (y[i] == -1) {
                s.iLow = i;
            } else {
                s.iUp = i;
            }
        }

        // Check whether one or both classes are missing

        if ((s.iUp == -1) || (s.iLow == -1)) {
            if (s.iUp != -1) {
                b = -1;
            } else if (s.iLow != -1) {
                b = 1;
            } else {
                y = null;
                return false;
            }
            if (kernel.get().isLinear()) {
                sparseWeights = new double[0];
                sparseIndices = new int[0];
                y = null;
            } else {
                supportVectors = new BitSet(0);
                alpha = new double[0];
                y = new double[0];
            }
            return false;
        }

        // If machine is linear, reserve space for weights

        if (kernel.get().isLinear()) {
            linear_weights = new double[inputNames().length];
        } else {
            linear_weights = null;
        }

        // Initialize alpha array to zero
        alpha = new double[n];

        // Initialize sets
        supportVectors = new BitSet(n);
        s.I0 = new BitSet(n);
        s.I1 = new BitSet(n);
        s.I2 = new BitSet(n);
        s.I3 = new BitSet(n);
        s.I4 = new BitSet(n);

        // Clean out some instance variables
        sparseWeights = null;
        sparseIndices = null;

        // init kernel
        kernel.get().buildKernelCache(inputNames(), train);

        // Initialize error cache
        s.fCache = new double[n];
        s.fCache[s.iLow] = 1;
        s.fCache[s.iUp] = -1;

        // Build up I1 and I4
        for (int i = 0; i < n; i++) {
            if (y[i] == 1) {
                s.I1.set(i, true);
            } else {
                s.I4.set(i, true);
            }
        }
        return true;
    }

    @Override
    protected ClassifierResult corePredict(Frame df, boolean withClasses, boolean withDistributions) {
        ClassifierResult cr = ClassifierResult.build(this, df, withClasses, withDistributions);
        for (int i = 0; i < df.rowCount(); i++) {
            double pred = predict(df, i);

            cr.firstClasses().setLabel(i, pred < 0 ? label1 : label2);
            cr.firstDensity().setDouble(i, label1, -pred);
            cr.firstDensity().setDouble(i, label2, pred);
        }
        return cr;
    }

    /**
     * Computes SVM output for given instance.
     */
    protected double predict(Frame df, int row) {

        double result = -b;

        if (kernel.get().isLinear()) {
            // Is weight vector stored in sparse format?
            if (sparseWeights == null) {
                for (int i = 0; i < linear_weights.length; i++) {
                    result += linear_weights[i] * df.getDouble(row, i);
                }
            } else {
                for (int i = 0; i < sparseIndices.length; i++) {
                    result += df.getDouble(row, sparseIndices[i]) * sparseWeights[i];
                }
            }
        } else {
            for (int i = supportVectors.nextSetBit(0); i != -1; i = supportVectors.nextSetBit(i + 1)) {
                result += y[i] * alpha[i] * kernel.get().compute(train, i, df, row);
            }
        }
        return result;
    }

    /**
     * Examines instance.
     *
     * @param i2 index of instance to examine
     * @return true if examination was successful
     */
    protected boolean examineExample(State s, int i2) {

        double y2 = y[i2];
        double F2;

        if (s.I0.get(i2)) {
            F2 = s.fCache[i2];
        } else {
            F2 = predict(train, i2) + b - y2;
            s.fCache[i2] = F2;

            // Update thresholds
            if ((s.I1.get(i2) || s.I2.get(i2)) && (F2 < s.bUp)) {
                s.bUp = F2;
                s.iUp = i2;
            } else if ((s.I3.get(i2) || s.I4.get(i2)) && (F2 > s.bLow)) {
                s.bLow = F2;
                s.iLow = i2;
            }
        }

        int i1 = -1;
        // Check optimality using current bLow and bUp and, if
        // violated, find an index i1 to do joint optimization
        // with i2...
        boolean optimal = true;
        if (s.I0.get(i2) || s.I1.get(i2) || s.I2.get(i2)) {
            if (s.bLow - F2 > 2 * eps.get()) {
                optimal = false;
                i1 = s.iLow;
            }
        }
        if (s.I0.get(i2) || s.I3.get(i2) || s.I4.get(i2)) {
            if (F2 - s.bUp > 2 * eps.get()) {
                optimal = false;
                i1 = s.iUp;
            }
        }
        if (optimal) {
            return false;
        }

        // For i2 unbound choose the better i1...
        if (s.I0.get(i2)) {
            i1 = (s.bLow - F2 > F2 - s.bUp) ? s.iLow : s.iUp;
        }
        if (i1 == -1) {
            throw new RuntimeException("This should never happen!");
        }
        return takeStep(s, i1, i2);
    }

    /**
     * Method solving for the Lagrange multipliers on two instances.
     *
     * @param i1 index of the first instance
     * @param i2 index of the second instance
     * @return true if multipliers could be found
     */
    protected boolean takeStep(State s, int i1, int i2) {

        // Don't do anything if the two instances are the same
        if (i1 == i2) {
            return false;
        }

        // Initialize variables
        double alpha1 = alpha[i1];
        double alpha2 = alpha[i2];
        double y1 = y[i1];
        double y2 = y[i2];
        double F1 = s.fCache[i1];
        double F2 = s.fCache[i2];
        double ss = y1 * y2;

        double C1 = c.get() * weights.getDouble(i1);
        double C2 = c.get() * weights.getDouble(i2);

        double L = (y1 != y2) ? Math.max(0, alpha2 - alpha1) : Math.max(0, alpha1 + alpha2 - C1);
        double H = (y1 != y2) ? Math.min(C2, C1 + alpha2 - alpha1) : Math.min(C2, alpha1 + alpha2);

        // TODO: parameter or add tolerance
        if (Math.abs(L - H) <= eps_delta) {
            return false;
        }

        // Compute second derivative of objective function
        double k11 = kernel.get().compute(train, i1, train, i1);
        double k12 = kernel.get().compute(train, i1, train, i2);
        double k22 = kernel.get().compute(train, i2, train, i2);
        double eta = 2 * k12 - k11 - k22;

        double a1, a2;

        // Check if second derivative is negative
        if (eta < 0) {

            // Compute unconstrained maximum
            a2 = alpha2 - y2 * (F1 - F2) / eta;

            // Compute constrained maximum
            a2 = MathTools.cut(a2, L, H);

        } else {

            // Look at endpoints of diagonal
            double f1 = predict(train, i1);
            double f2 = predict(train, i2);
            double v1 = f1 + b - y1 * alpha1 * k11 - y2 * alpha2 * k12;
            double v2 = f2 + b - y1 * alpha1 * k12 - y2 * alpha2 * k22;
            double gamma = alpha1 + ss * alpha2;
            double Lobj = (gamma - ss * L) + L - 0.5 * k11 * (gamma - ss * L) * (gamma - ss * L) -
                    0.5 * k22 * L * L - ss * k12 * (gamma - ss * L) * L -
                    y1 * (gamma - ss * L) * v1 - y2 * L * v2;
            double Hobj = (gamma - ss * H) + H - 0.5 * k11 * (gamma - ss * H) * (gamma - ss * H) -
                    0.5 * k22 * H * H - ss * k12 * (gamma - ss * H) * H -
                    y1 * (gamma - ss * H) * v1 - y2 * H * v2;
            if (Lobj > Hobj + eps_delta) {
                a2 = L;
            } else if (Lobj < Hobj - eps_delta) {
                a2 = H;
            } else {
                a2 = alpha2;
            }
        }
        if (Math.abs(a2 - alpha2) < eps_delta * (a2 + alpha2 + eps_delta)) {
            return false;
        }

        // To prevent precision problems
        if (a2 > C2 - eps_delta * C2) {
            a2 = C2;
        } else if (a2 <= eps_delta * C2) {
            a2 = 0;
        }

        // Recompute a1
        a1 = alpha1 + ss * (alpha2 - a2);

        // To prevent precision problems
        if (a1 > C1 - eps_delta * C1) {
            a1 = C1;
        } else if (a1 <= eps_delta * C1) {
            a1 = 0;
        }

        // Update sets
        supportVectors.set(i1, a1 > 0);
        s.I0.set(i1, (a1 > 0) && (a1 < C1));
        s.I1.set(i1, (y1 == 1) && (a1 == 0));
        s.I2.set(i1, (y1 == -1) && (a1 == C1));
        s.I3.set(i1, (y1 == 1) && (a1 == C1));
        s.I4.set(i1, (y1 == -1) && (a1 == 0));

        supportVectors.set(i2, a2 > 0);
        s.I0.set(i2, (a2 > 0) && (a2 < C2));
        s.I1.set(i2, (y2 == 1) && (a2 == 0));
        s.I2.set(i2, (y2 == -1) && (a2 == C2));
        s.I3.set(i2, (y2 == 1) && (a2 == C2));
        s.I4.set(i2, (y2 == -1) && (a2 == 0));

        // Update weight vector to reflect change a1 and a2, if linear SVM
        if (kernel.get().isLinear()) {
            for (int p1 = 0; p1 < inputNames().length; p1++) {
                linear_weights[p1] += y1 * (a1 - alpha1) * train.getDouble(i1, p1);
            }
            for (int p2 = 0; p2 < inputNames().length; p2++) {
                linear_weights[p2] += y2 * (a2 - alpha2) * train.getDouble(i2, p2);
            }
        }

        // Update error cache using new Lagrange multipliers
        for (int j = s.I0.nextSetBit(0); j != -1; j = s.I0.nextSetBit(j + 1)) {
            if ((j != i1) && (j != i2)) {
                s.fCache[j] +=
                        y1 * (a1 - alpha1) * kernel.get().compute(train, i1, train, j) +
                                y2 * (a2 - alpha2) * kernel.get().compute(train, i2, train, j);
            }
        }

        // Update error cache for i1 and i2
        s.fCache[i1] += y1 * (a1 - alpha1) * k11 + y2 * (a2 - alpha2) * k12;
        s.fCache[i2] += y1 * (a1 - alpha1) * k12 + y2 * (a2 - alpha2) * k22;

        // Update array with Lagrange multipliers
        alpha[i1] = a1;
        alpha[i2] = a2;

        // Update thresholds
        s.bLow = -Double.MAX_VALUE;
        s.bUp = Double.MAX_VALUE;
        s.iLow = -1;
        s.iUp = -1;
        for (int j = s.I0.nextSetBit(0); j != -1; j = s.I0.nextSetBit(j + 1)) {
            if (s.fCache[j] < s.bUp) {
                s.bUp = s.fCache[j];
                s.iUp = j;
            }
            if (s.fCache[j] > s.bLow) {
                s.bLow = s.fCache[j];
                s.iLow = j;
            }
        }
        if (!s.I0.get(i1)) {
            if (s.I3.get(i1) || s.I4.get(i1)) {
                if (s.fCache[i1] > s.bLow) {
                    s.bLow = s.fCache[i1];
                    s.iLow = i1;
                }
            } else {
                if (s.fCache[i1] < s.bUp) {
                    s.bUp = s.fCache[i1];
                    s.iUp = i1;
                }
            }
        }
        if (!s.I0.get(i2)) {
            if (s.I3.get(i2) || s.I4.get(i2)) {
                if (s.fCache[i2] > s.bLow) {
                    s.bLow = s.fCache[i2];
                    s.iLow = i2;
                }
            } else {
                if (s.fCache[i2] < s.bUp) {
                    s.bUp = s.fCache[i2];
                    s.iUp = i2;
                }
            }
        }
        if (s.iLow == -1 || s.iUp == -1) {
            throw new RuntimeException("This should never happen!");
        }

        // Made some progress.
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(fullName()).append(", fitted=").append(hasLearned());
        if (hasLearned()) {
            if (kernel.get().isLinear()) {
                sb.append(", fitted weights=").append(sparseWeights.length);
            } else {
                sb.append(", support vectors=").append(supportVectors.cardinality());
            }
        }
        return sb.toString();
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        int printed = 0;

        sb.append("BinarySMO model\n");
        sb.append("===============\n");
        sb.append(fullName()).append("\n");
        sb.append("fitted: ").append(hasLearned());

        if (!hasLearned()) {
            sb.append(".\n");
            return sb.toString();
        }
        if (hasLearned()) {
            if (kernel.get().isLinear()) {
                sb.append(", fitted weights=").append(sparseWeights.length).append("\n");
            } else {
                sb.append(", support vectors=").append(supportVectors.cardinality()).append("\n");
            }
        }
        sb.append("Decision function:\n");

        // If machine linear, print weight vector
        if (kernel.get().isLinear()) {
            sb.append("Linear support vector: use attribute weights folding.\n");

            // We can assume that the weight vector is stored in sparse
            // format because the classifier has been built
            for (int i = 0; i < sparseWeights.length; i++) {
                if (printed > 0) {
                    if (sparseWeights[i] >= 0) {
                        sb.append(" + ");
                    } else {
                        sb.append(" - ");
                    }
                } else {
                    sb.append("   ");
                }
                sb.append(floatFlex(Math.abs(sparseWeights[i]))).append(" * ");
                sb.append("[").append(inputName(sparseIndices[i])).append("]\n");
                printed++;
            }
        } else {
            for (int i = 0; i < alpha.length; i++) {
                if (supportVectors.get(i)) {
                    double val = alpha[i];
                    if (y[i] == 1) {
                        if (printed > 0) {
                            sb.append(" + ");
                        } else {
                            sb.append("   ");
                        }
                    } else {
                        sb.append(" - ");
                    }
                    sb.append(floatFlex(val)).append(" * <[");
                    for (int j = 0; j < inputNames().length; j++) {
                        sb.append(floatFlex(train.getDouble(i, inputNames()[j])));
                        if (j != inputNames().length - 1) {
                            sb.append(",");
                        }
                    }
                    sb.append("], x>\n");
                    printed++;
                }
            }
        }
        sb.append(b > 0 ? " - " : " + ").append(floatFlex(Math.abs(b)));

        return sb.toString();
    }

    @Override
    public String toContent(POption<?>... options) {
        return toSummary(options);
    }

    @Override
    public String toFullContent(POption<?>... options) {
        return toSummary(options);
    }
}
