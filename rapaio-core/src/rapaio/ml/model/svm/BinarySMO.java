/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.ml.model.svm;

import static rapaio.printer.Format.floatFlex;

import java.io.Serial;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import rapaio.core.param.ValueParam;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.math.MathTools;
import rapaio.math.tensor.Tensor;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.kernel.Kernel;
import rapaio.ml.common.kernel.PolyKernel;
import rapaio.ml.common.kernel.cache.KernelCache;
import rapaio.ml.model.ClassifierModel;
import rapaio.ml.model.ClassifierResult;
import rapaio.ml.model.RunInfo;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;


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

    private String label1;
    private String label2;
    private boolean oneVsAll;

    private int _vectorsCount;
    private Tensor<Double> _vectors;
    private double[] _sparseWeights;
    private int[] _sparseIndices;
    private double _b;
    private double[] _alpha;
    private double[] _y;


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
        return new Capabilities()
                .inputs(1, 100_000, false, VarType.BINARY, VarType.INT, VarType.NOMINAL, VarType.FLOAT, VarType.DOUBLE)
                .targets(1, 1, false, VarType.NOMINAL);
    }

    @Override
    protected boolean coreFit(Frame initDf, Var initWeights) {
        Random random = getRandom();
        State state = new State();
        prepareDataset(initDf, initWeights, state);
        state.initialize(this);

        // Loop to find all the support vectors
        int numChanged = 0;
        boolean examineAll = true;

        int runs = maxRuns.get();

        while (runs-- >= 0 && (numChanged > 0 || examineAll)) {
            numChanged = 0;

            if (examineAll) {

                // add random as an additional step
                int offset = random.nextInt(state.train.dim(0));
                for (int i = offset; i < state.train.dim(0) + offset; i++) {
                    int pos = i;
                    if (pos >= state.train.dim(0)) {
                        pos -= state.train.dim(0);
                    }
                    if (examineExample(state, pos)) {
                        numChanged++;
                    }
                }

            } else {

                if ("Keerthi1".equals(solver.get())) {
                    // This code implements Modification 1 from Keerthi et al.'s paper
                    int offset = random.nextInt(state.train.dim(0));
                    for (int i = offset; i < state.train.dim(0) + offset; i++) {

                        int pos = i;
                        if (pos >= state.train.dim(0)) {
                            pos -= state.train.dim(0);
                        }

                        if (state.alpha[pos] > 0 && state.alpha[pos] < c.get() * state.weights.getDouble(pos)) {
                            if (examineExample(state, pos)) {
                                numChanged++;
                            }
                            // Is optimality on unbound vectors obtained?
                            if (state.bUp > state.bLow - 2 * eps.get()) {
                                numChanged = 0;
                                break;
                            }
                        }
                    }
                } else {
                    //This is the code for Modification 2 from Keerthi et al.'s paper
                    boolean innerLoopSuccess = true;
                    while ((state.bUp < state.bLow - 2 * eps.get()) && innerLoopSuccess) {
                        innerLoopSuccess = takeStep(state, state.iUp, state.iLow);
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

        compactVectors(state);
        state.close();
        return true;
    }

    private void prepareDataset(Frame df, Var w, State state) {

        List<String> targetLevels = firstTargetLevels().subList(1, firstTargetLevels().size());
        boolean valid = false;

        Frame dfTrain = null;
        if (!("?".equals(firstLabel.get()) || "?".equals(secondLabel.get()))) {
            // if both labels were specified, we select data only for those labels
            label1 = firstLabel.get();
            label2 = secondLabel.get();
            oneVsAll = false;
            Mapping mapping = df.stream()
                    .filter(spot -> {
                        String label = spot.getLabel(firstTargetName());
                        return label.equals(label1) || label.equals(label2);
                    })
                    .collectMapping();
            dfTrain = df.mapRows(mapping);
            state.weights = w.mapRows(mapping).tensor();
            valid = true;
        } else if (!"?".equals(firstLabel.get())) {
            // one vs all type of classification
            label1 = firstLabel.get();
            label2 = "~" + firstLabel.get();
            oneVsAll = true;
            dfTrain = df;
            state.weights = w.tensor();
            valid = true;
        } else if (targetLevels.size() == 2) {
            label1 = targetLevels.get(0);
            label2 = targetLevels.get(1);
            oneVsAll = false;
            dfTrain = df;
            state.weights = w.tensor();
            valid = true;
        }

        if (!valid) {
            throw new IllegalArgumentException("Invalid target labels specification.");
        }
        if (dfTrain.rowCount() == 0) {
            throw new IllegalArgumentException("No instances in the training data.");
        }
        state.train = dfTrain.mapVars(inputNames).tensor();

        state.y = new double[state.train.dim(0)];
        for (int i = 0; i < state.train.dim(0); i++) {
            state.y[i] = label1.equals(dfTrain.getLabel(i, firstTargetName())) ? -1 : 1;
        }
    }

    /**
     * Examines instance.
     *
     * @param i2 index of instance to examine
     * @return true if examination was successful
     */
    private boolean examineExample(State state, int i2) {

        double y2 = state.y[i2];
        double F2;

        if (state.I0.get(i2)) {
            F2 = state.fCache[i2];
        } else {
            F2 = state.predict(kernel.get(), state.train, i2, state.train, state.y) + state.b - y2;
            state.fCache[i2] = F2;

            // Update thresholds
            if ((state.I1.get(i2) || state.I2.get(i2)) && (F2 < state.bUp)) {
                state.bUp = F2;
                state.iUp = i2;
            } else if ((state.I3.get(i2) || state.I4.get(i2)) && (F2 > state.bLow)) {
                state.bLow = F2;
                state.iLow = i2;
            }
        }

        int i1 = -1;
        // Check optimality using current bLow and bUp and, if
        // violated, find an index i1 to do joint optimization
        // with i2...
        boolean optimal = true;
        if (state.I0.get(i2) || state.I1.get(i2) || state.I2.get(i2)) {
            if (state.bLow - F2 > 2 * eps.get()) {
                optimal = false;
                i1 = state.iLow;
            }
        }
        if (state.I0.get(i2) || state.I3.get(i2) || state.I4.get(i2)) {
            if (F2 - state.bUp > 2 * eps.get()) {
                optimal = false;
                i1 = state.iUp;
            }
        }
        if (optimal) {
            return false;
        }

        // For i2 unbound choose the better i1...
        if (state.I0.get(i2)) {
            i1 = (state.bLow - F2 > F2 - state.bUp) ? state.iLow : state.iUp;
        }
        if (i1 == -1) {
            throw new RuntimeException("This should never happen!");
        }
        return takeStep(state, i1, i2);
    }

    /**
     * Method solving for the Lagrange multipliers on two instances.
     *
     * @param i1 index of the first instance
     * @param i2 index of the second instance
     * @return true if multipliers could be found
     */
    private boolean takeStep(State state, final int i1, final int i2) {

        // Don't do anything if the two instances are the same
        if (i1 == i2) {
            return false;
        }

        // Initialize variables
        double alpha1 = state.alpha[i1];
        double alpha2 = state.alpha[i2];
        double y1 = state.y[i1];
        double y2 = state.y[i2];
        double F1 = state.fCache[i1];
        double F2 = state.fCache[i2];
        double ss = y1 * y2;

        double C1 = c.get() * state.weights.getDouble(i1);
        double C2 = c.get() * state.weights.getDouble(i2);

        double L = (y1 != y2) ? Math.max(0, alpha2 - alpha1) : Math.max(0, alpha1 + alpha2 - C1);
        double H = (y1 != y2) ? Math.min(C2, C1 + alpha2 - alpha1) : Math.min(C2, alpha1 + alpha2);

        // TODO: parameter or add tolerance
        if (Math.abs(L - H) <= eps_delta) {
            return false;
        }

        // Compute second derivative of objective function
        Tensor<Double> row1 = state.train.takesq(0, i1);
        Tensor<Double> row2 = state.train.takesq(0, i2);
        double k11 = state.kernelCache.cachedCompute(i1, i1, row1, row1);
        double k12 = state.kernelCache.cachedCompute(i1, i2, row1, row2);
        double k22 = state.kernelCache.cachedCompute(i2, i2, row2, row2);
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
            double f1 = state.predict(kernel.get(), state.train, i1, state.train, state.y);
            double f2 = state.predict(kernel.get(), state.train, i2, state.train, state.y);
            double v1 = f1 + state.b - y1 * alpha1 * k11 - y2 * alpha2 * k12;
            double v2 = f2 + state.b - y1 * alpha1 * k12 - y2 * alpha2 * k22;
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
        state.supportVectors.set(i1, a1 > 0);
        state.I0.set(i1, (a1 > 0) && (a1 < C1));
        state.I1.set(i1, (y1 == 1) && (a1 == 0));
        state.I2.set(i1, (y1 == -1) && (a1 == C1));
        state.I3.set(i1, (y1 == 1) && (a1 == C1));
        state.I4.set(i1, (y1 == -1) && (a1 == 0));

        state.supportVectors.set(i2, a2 > 0);
        state.I0.set(i2, (a2 > 0) && (a2 < C2));
        state.I1.set(i2, (y2 == 1) && (a2 == 0));
        state.I2.set(i2, (y2 == -1) && (a2 == C2));
        state.I3.set(i2, (y2 == 1) && (a2 == C2));
        state.I4.set(i2, (y2 == -1) && (a2 == 0));

        // Update weight vector to reflect change a1 and a2, if linear SVM
        if (kernel.get().isLinear()) {
            for (int p1 = 0; p1 < inputNames().length; p1++) {
                state.linear_weights[p1] += y1 * (a1 - alpha1) * state.train.getDouble(i1, p1);
            }
            for (int p2 = 0; p2 < inputNames().length; p2++) {
                state.linear_weights[p2] += y2 * (a2 - alpha2) * state.train.getDouble(i2, p2);
            }
        }

        // Update error cache using new Lagrange multipliers
        for (int j = state.I0.nextSetBit(0); j != -1; j = state.I0.nextSetBit(j + 1)) {
            if ((j != i1) && (j != i2)) {
                Tensor<Double> rowj = state.train.takesq(0, j);
                state.fCache[j] +=
                        y1 * (a1 - alpha1) * state.kernelCache.cachedCompute(i1, j, row1, rowj) +
                                y2 * (a2 - alpha2) * state.kernelCache.cachedCompute(i2, j, row2, rowj);
            }
        }

        // Update error cache for i1 and i2
        state.fCache[i1] += y1 * (a1 - alpha1) * k11 + y2 * (a2 - alpha2) * k12;
        state.fCache[i2] += y1 * (a1 - alpha1) * k12 + y2 * (a2 - alpha2) * k22;

        // Update array with Lagrange multipliers
        state.alpha[i1] = a1;
        state.alpha[i2] = a2;

        // Update thresholds
        state.bLow = -Double.MAX_VALUE;
        state.bUp = Double.MAX_VALUE;
        state.iLow = -1;
        state.iUp = -1;
        for (int j = state.I0.nextSetBit(0); j != -1; j = state.I0.nextSetBit(j + 1)) {
            if (state.fCache[j] < state.bUp) {
                state.bUp = state.fCache[j];
                state.iUp = j;
            }
            if (state.fCache[j] > state.bLow) {
                state.bLow = state.fCache[j];
                state.iLow = j;
            }
        }
        if (!state.I0.get(i1)) {
            if (state.I3.get(i1) || state.I4.get(i1)) {
                if (state.fCache[i1] > state.bLow) {
                    state.bLow = state.fCache[i1];
                    state.iLow = i1;
                }
            } else {
                if (state.fCache[i1] < state.bUp) {
                    state.bUp = state.fCache[i1];
                    state.iUp = i1;
                }
            }
        }
        if (!state.I0.get(i2)) {
            if (state.I3.get(i2) || state.I4.get(i2)) {
                if (state.fCache[i2] > state.bLow) {
                    state.bLow = state.fCache[i2];
                    state.iLow = i2;
                }
            } else {
                if (state.fCache[i2] < state.bUp) {
                    state.bUp = state.fCache[i2];
                    state.iUp = i2;
                }
            }
        }
        if (state.iLow == -1 || state.iUp == -1) {
            throw new RuntimeException("This should never happen!");
        }

        // Made some progress.
        return true;
    }

    private void compactVectors(State state) {
        _b = (state.bLow + state.bUp) / 2.0;

        if (kernel.get().isLinear()) {
            double[] sparseWeights = new double[state.linear_weights.length];
            int[] sparseIndices = new int[state.linear_weights.length];
            int counter = 0;
            for (int i = 0; i < state.linear_weights.length; i++) {
                if (Math.abs(state.linear_weights[i]) >= eps_delta) {
                    sparseWeights[counter] = state.linear_weights[i];
                    sparseIndices[counter] = i;
                    counter++;
                }
            }
            _vectorsCount = counter;
            _sparseWeights = new double[counter];
            System.arraycopy(sparseWeights, 0, _sparseWeights, 0, counter);
            _sparseIndices = new int[counter];
            System.arraycopy(sparseIndices, 0, _sparseIndices, 0, counter);
        } else {
            _vectorsCount = state.supportVectors.cardinality();
            _alpha = new double[_vectorsCount];
            _y = new double[_vectorsCount];

            int[] sparse = new int[_vectorsCount];
            int pos = 0;
            for (int i = state.supportVectors.nextSetBit(0); i != -1; i = state.supportVectors.nextSetBit(i + 1)) {
                sparse[pos++] = i;
            }
            for (int i = 0; i < sparse.length; i++) {
                _alpha[i] = state.alpha[sparse[i]];
                _y[i] = state.y[sparse[i]];
            }
            _vectors = state.train.take(0, sparse);
        }
    }

    private double predict(Kernel kernel, Tensor<Double> df, int row) {
        double result = -_b;
        if (kernel.isLinear()) {
            for (int i = 0; i < _vectorsCount; i++) {
                result += df.getDouble(row, i) * _sparseWeights[i];
            }
        } else {
            for (int i = 0; i < _vectorsCount; i++) {
                result += _y[i] * _alpha[i] * kernel.compute(_vectors.takesq(0, i), df.takesq(0, row));
            }
        }
        return result;
    }


    @Override
    protected ClassifierResult corePredict(Frame df, boolean withClasses, boolean withDistributions) {
        ClassifierResult cr = ClassifierResult.build(this, df, withClasses, withDistributions);
        for (int i = 0; i < df.rowCount(); i++) {
            double pred = predict(kernel.get(), df.mapVars(inputNames).tensor(), i);

            cr.firstClasses().setLabel(i, pred <= 0 ? label1 : label2);
            cr.firstDensity().setDouble(i, label1, -pred);
            cr.firstDensity().setDouble(i, label2, pred);
        }
        return cr;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(fullName()).append(", fitted=").append(hasLearned());
        if (hasLearned()) {
            sb.append(", support vectors=").append(_vectorsCount);
        }
        return sb.toString();
    }

    @Override
    public String toSummary(Printer printer, POpt<?>... options) {
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
            sb.append(", support vectors=").append(_vectorsCount).append("\n");
        }
        sb.append("Decision function:\n");

        // If machine linear, print weight vector
        if (kernel.get().isLinear()) {
            sb.append("Linear support vector: use attribute weights folding.\n");

            // We can assume that the weight vector is stored in sparse
            // format because the classifier has been built
            for (int i = 0; i < _vectorsCount; i++) {
                if (printed > 0) {
                    if (_sparseWeights[i] >= 0) {
                        sb.append(" + ");
                    } else {
                        sb.append(" - ");
                    }
                } else {
                    sb.append("   ");
                }
                sb.append(floatFlex(Math.abs(_sparseWeights[i]))).append(" * ");
                sb.append("[").append(inputName(_sparseIndices[i])).append("]\n");
                printed++;
            }
        } else {
            for (int i = 0; i < _vectorsCount; i++) {
                double val = _alpha[i];
                if (_y[i] == 1) {
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
                    sb.append(floatFlex(_vectors.getDouble(i, j)));
                    if (j != inputNames().length - 1) {
                        sb.append(",");
                    }
                }
                sb.append("], x>\n");
                printed++;
            }
        }
        sb.append(_b > 0 ? " - " : " + ").append(floatFlex(Math.abs(_b)));

        return sb.toString();
    }

    @Override
    public String toContent(POpt<?>... options) {
        return toSummary(options);
    }

    @Override
    public String toFullContent(POpt<?>... options) {
        return toSummary(options);
    }
}

final class State {
    double bLow, bUp; // thresholds
    int iLow, iUp; // indices for bLow and bUp

    double[] fCache; // The current set of errors for all non-bound examples.

    /* The five different sets used by the algorithm. */
    BitSet I0; // i: 0 < alpha[i] < c
    BitSet I1; // i: classes[i] = 1, alpha[i] = 0
    BitSet I2; // i: classes[i] = -1, alpha[i] =c
    BitSet I3; // i: classes[i] = 1, alpha[i] = c
    BitSet I4; // i: classes[i] = -1, alpha[i] = 0

    BitSet supportVectors; // {i: 0 < alpha[i]}
    double[] alpha;
    double b;
    // linear machine
    double[] linear_weights;

    Tensor<Double> train;
    Tensor<Double> weights;
    double[] y;

    KernelCache kernelCache;

    public void initialize(BinarySMO parent) throws IllegalArgumentException {

        final int n = train.dim(0);

        bUp = -1;
        bLow = 1;

        // Set class values
        iUp = -1;
        iLow = -1;
        for (int i = 0; i < n; i++) {
            if (y[i] == -1) {
                iLow = i;
            } else {
                iUp = i;
            }
        }

        // Check whether one or both classes are missing
        if ((iUp == -1) || (iLow == -1)) {
            if (iUp != -1) {
                throw new IllegalArgumentException("There are no training positive examples.");
            } else if (iLow != -1) {
                throw new IllegalArgumentException("There are no training negative examples.");
            } else {
                throw new IllegalArgumentException("There are no training examples.");
            }
        }

        // If machine is linear, reserve space for weights
        if (parent.kernel.get().isLinear()) {
            linear_weights = new double[parent.inputNames().length];
        }

        b = 0;
        alpha = new double[n];

        // Initialize sets
        supportVectors = new BitSet(n);
        I0 = new BitSet(n);
        I1 = new BitSet(n);
        I2 = new BitSet(n);
        I3 = new BitSet(n);
        I4 = new BitSet(n);

        // init kernel
        kernelCache = new KernelCache(train, parent.kernel.get());

        // Initialize error cache
        fCache = new double[n];
        fCache[iLow] = 1;
        fCache[iUp] = -1;

        // Build up I1 and I4
        for (int i = 0; i < n; i++) {
            if (y[i] == 1) {
                I1.set(i, true);
            } else {
                I4.set(i, true);
            }
        }
    }

    public double predict(Kernel kernel, Tensor<Double> df, int row, Tensor<Double> train, double[] y) {
        double result = -b;
        if (kernel.isLinear()) {
            // Is weight vector stored in sparse format?
            for (int i = 0; i < linear_weights.length; i++) {
                result += linear_weights[i] * df.getDouble(row, i);
            }
        } else {
            for (int i = supportVectors.nextSetBit(0); i != -1; i = supportVectors.nextSetBit(i + 1)) {
                result += y[i] * alpha[i] * kernel.compute(train.takesq(0, i), df.takesq(0, row));
            }
        }
        return result;
    }

    public void close() {
        kernelCache.clean();
        kernelCache = null;
    }

}
