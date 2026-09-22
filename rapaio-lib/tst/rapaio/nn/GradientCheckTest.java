/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

package rapaio.nn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Uniform;
import rapaio.darray.Compare;
import rapaio.darray.DArray;
import rapaio.darray.Shape;
import rapaio.darray.iterators.PointerIterator;
import rapaio.nn.Loss.Reduce;
import rapaio.nn.loss.NegativeLogLikelihoodLoss;

/**
 * Finite difference gradient check for every differentiable tensor operation.
 * <p>
 * For an operation {@code f} with inputs {@code x_1..x_k} the harness builds the scalar objective
 * {@code L = sum(f(x) * w)} with a fixed random {@code w} of the output shape, computes the gradient of every input
 * by backpropagation and compares it, element by element, with the central difference
 * {@code (L(x + eps e_i) - L(x - eps e_i)) / (2 eps)} computed in double precision. The random {@code w} makes the
 * check sensitive to every output element, which a plain {@code sum()} objective would not be (it cannot see a
 * gradient that is permuted or scattered to the wrong output position).
 */
public class GradientCheckTest {

    private static final double EPS = 1e-6;
    private static final double TOL = 1e-6;

    private TensorManager tm;
    private Random random;

    @BeforeEach
    void beforeEach() {
        tm = TensorManager.ofDouble().seed(42);
        random = new Random(42);
    }

    /**
     * Random input away from zero, so ops like log, sqrt, div and sigmoid are checked on a smooth region.
     */
    private DArray<?> positiveInput(Shape shape) {
        return tm.randomArray(shape, Uniform.of(0.5, 2.0));
    }

    /**
     * Random input with both signs, for ops that are smooth everywhere.
     */
    private DArray<?> signedInput(Shape shape) {
        return tm.randomArray(shape, Uniform.of(-2.0, 2.0));
    }

    /**
     * Failures collected during a test method; one line per failing op, reported together at the end so a single
     * run lists every broken backward instead of stopping at the first one.
     */
    private final List<String> failures = new ArrayList<>();

    @AfterEach
    void afterEach() {
        if (!failures.isEmpty()) {
            fail(failures.size() + " gradient check(s) failed:\n  " + String.join("\n  ", failures));
        }
    }

    private void check(String op, List<DArray<?>> inputs, Function<List<Tensor>, Tensor> f) {
        try {
            checkOrThrow(op, inputs, f);
        } catch (AssertionError | RuntimeException e) {
            failures.add(e.getMessage() == null ? op + ": " + e : e.getMessage());
        }
    }

    private void checkOrThrow(String op, List<DArray<?>> inputs, Function<List<Tensor>, Tensor> f) {
        // fixed weights of the output shape; the objective is the weighted sum of the output
        DArray<?> probeOut;
        try {
            probeOut = f.apply(vars(inputs, -1)).value();
        } catch (RuntimeException e) {
            throw new AssertionError(op + ": forward failed: " + e.getMessage(), e);
        }
        DArray<?> w = tm.randomArray(probeOut.shape(), Uniform.of(-1, 1));

        for (int in = 0; in < inputs.size(); in++) {
            List<Tensor> vars = vars(inputs, in);
            Tensor out = f.apply(vars);
            Tensor loss = out.mul(tm.var(w)).sum();
            loss.setGrad(tm.scalarArray(1));
            try {
                Autograd.backward(loss);
            } catch (RuntimeException e) {
                throw new AssertionError(op + ": backward failed for input " + in + ": " + e.getMessage(), e);
            }
            DArray<?> analytic = vars.get(in).grad();
            assertTrue(analytic != null, op + ": no gradient for input " + in);
            assertEquals(inputs.get(in).shape(), analytic.shape(), op + ": gradient shape of input " + in);

            DArray<?> x = inputs.get(in);
            PointerIterator it = x.ptrIterator();
            int checked = 0;
            int worstIndex = -1;
            double worstA = 0, worstN = 0, worstRel = 0;
            while (it.hasNext()) {
                int ptr = it.nextInt();
                double x0 = x.ptrGetDouble(ptr);
                x.ptrSetDouble(ptr, x0 + EPS);
                double lp = objective(inputs, f, w);
                x.ptrSetDouble(ptr, x0 - EPS);
                double lm = objective(inputs, f, w);
                x.ptrSetDouble(ptr, x0);
                double numeric = (lp - lm) / (2 * EPS);
                double a = analytic.ptrGetDouble(ptr);
                double scale = Math.max(1.0, Math.max(Math.abs(a), Math.abs(numeric)));
                double rel = Math.abs(a - numeric) / scale;
                if (rel > worstRel) {
                    worstRel = rel;
                    worstIndex = checked;
                    worstA = a;
                    worstN = numeric;
                }
                checked++;
            }
            assertTrue(worstRel <= TOL, String.format("%s: input %d, worst element %d: analytic %.9f, numeric %.9f (rel %.2e)",
                    op, in, worstIndex, worstA, worstN, worstRel));
        }
    }

    private List<Tensor> vars(List<DArray<?>> inputs, int gradInput) {
        List<Tensor> vars = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
            vars.add(tm.var(inputs.get(i).copy()).requiresGrad(i == gradInput));
        }
        return vars;
    }

    private double objective(List<DArray<?>> inputs, Function<List<Tensor>, Tensor> f, DArray<?> w) {
        Tensor out = f.apply(vars(inputs, -1));
        return out.value().mul(w).sum().doubleValue();
    }

    // unary

    @Test
    void unaryOps() {
        Shape s = Shape.of(3, 4);
        check("identity", List.of(signedInput(s)), v -> v.getFirst().identity());
        check("neg", List.of(signedInput(s)), v -> v.getFirst().neg());
        check("exp", List.of(signedInput(s)), v -> v.getFirst().exp());
        check("log", List.of(positiveInput(s)), v -> v.getFirst().log());
        check("log(eps)", List.of(positiveInput(s)), v -> v.getFirst().log(1e-8));
        check("sqr", List.of(signedInput(s)), v -> v.getFirst().sqr());
        check("sqrt", List.of(positiveInput(s)), v -> v.getFirst().sqrt());
        check("sigmoid", List.of(signedInput(s)), v -> v.getFirst().sigmoid());
        check("tanh", List.of(signedInput(s)), v -> v.getFirst().tanh());
        check("max(0)", List.of(signedInput(s)), v -> v.getFirst().max(0));
        check("softmax(0)", List.of(signedInput(s)), v -> v.getFirst().softmax(0));
        check("softmax(1)", List.of(signedInput(s)), v -> v.getFirst().softmax(1));
        check("logsoftmax(0)", List.of(signedInput(s)), v -> v.getFirst().logsoftmax(0));
        check("logsoftmax(1)", List.of(signedInput(s)), v -> v.getFirst().logsoftmax(1));
    }

    @Test
    void scalarArithmetic() {
        Shape s = Shape.of(3, 4);
        check("add(c)", List.of(signedInput(s)), v -> v.getFirst().add(1.5));
        check("sub(c)", List.of(signedInput(s)), v -> v.getFirst().sub(1.5));
        check("mul(c)", List.of(signedInput(s)), v -> v.getFirst().mul(-2.5));
        check("div(c)", List.of(signedInput(s)), v -> v.getFirst().div(4));
    }

    // binary, same shape and broadcast

    @Test
    void binaryOpsSameShape() {
        Shape s = Shape.of(3, 4);
        check("add", List.of(signedInput(s), signedInput(s)), v -> v.get(0).add(v.get(1)));
        check("sub", List.of(signedInput(s), signedInput(s)), v -> v.get(0).sub(v.get(1)));
        check("mul", List.of(signedInput(s), signedInput(s)), v -> v.get(0).mul(v.get(1)));
        check("div", List.of(signedInput(s), positiveInput(s)), v -> v.get(0).div(v.get(1)));
    }

    @Test
    void binaryOpsBroadcast() {
        Shape big = Shape.of(3, 4);
        Shape row = Shape.of(1, 4);
        Shape col = Shape.of(3, 1);
        Shape vec = Shape.of(4);
        check("add row", List.of(signedInput(big), signedInput(row)), v -> v.get(0).add(v.get(1)));
        check("add col", List.of(signedInput(big), signedInput(col)), v -> v.get(0).add(v.get(1)));
        check("add vec", List.of(signedInput(big), signedInput(vec)), v -> v.get(0).add(v.get(1)));
        check("sub row", List.of(signedInput(row), signedInput(big)), v -> v.get(0).sub(v.get(1)));
        check("mul col", List.of(signedInput(big), signedInput(col)), v -> v.get(0).mul(v.get(1)));
        check("mul vec", List.of(signedInput(vec), signedInput(big)), v -> v.get(0).mul(v.get(1)));
        check("div row", List.of(signedInput(big), positiveInput(row)), v -> v.get(0).div(v.get(1)));
        check("div col", List.of(signedInput(col), positiveInput(big)), v -> v.get(0).div(v.get(1)));
    }

    @Test
    void sameInputUsedTwice() {
        Shape s = Shape.of(3, 4);
        // x appears on both sides of the operation, so its gradient is accumulated from two edges
        check("x*x", List.of(signedInput(s)), v -> v.getFirst().mul(v.getFirst()));
        check("x+x", List.of(signedInput(s)), v -> v.getFirst().add(v.getFirst()));
        check("x-x^2", List.of(signedInput(s)), v -> v.getFirst().sub(v.getFirst().sqr()));
        check("sum1d(x)+x", List.of(signedInput(s)), v -> v.getFirst().sum1d(0).add(v.getFirst()));
    }

    // reductions

    @Test
    void reductions() {
        Shape s = Shape.of(3, 4);
        Shape cube = Shape.of(2, 3, 4);
        check("sum", List.of(signedInput(s)), v -> v.getFirst().sum());
        check("sum1d(0)", List.of(signedInput(s)), v -> v.getFirst().sum1d(0));
        check("sum1d(1)", List.of(signedInput(s)), v -> v.getFirst().sum1d(1));
        check("sum1d(1) cube", List.of(signedInput(cube)), v -> v.getFirst().sum1d(1));
        check("mean1d(0)", List.of(signedInput(s)), v -> v.getFirst().mean1d(0));
        check("mean1d(1)", List.of(signedInput(s)), v -> v.getFirst().mean1d(1));
        check("meanOn(4)", List.of(signedInput(s)), v -> v.getFirst().meanOn(Shape.of(4)));
        check("meanOn(3,4) cube", List.of(signedInput(cube)), v -> v.getFirst().meanOn(Shape.of(3, 4)));
        check("std1d(0)", List.of(signedInput(s)), v -> v.getFirst().std1d(0));
        check("std1d(1,1)", List.of(signedInput(s)), v -> v.getFirst().std1d(1, 1));
        check("stdOn(4)", List.of(signedInput(s)), v -> v.getFirst().stdOn(Shape.of(4)));
        check("stdOn(3,4) cube", List.of(signedInput(cube)), v -> v.getFirst().stdOn(Shape.of(3, 4), 1));
        check("standardize1d(0)", List.of(signedInput(s)), v -> v.getFirst().standardize1d(0, 0, 1e-8));
        check("standardize1d(1)", List.of(signedInput(s)), v -> v.getFirst().standardize1d(1, 1, 1e-8));
        check("standardizeOn(4)", List.of(signedInput(s)), v -> v.getFirst().standardizeOn(Shape.of(4), 0, 1e-8));
    }

    // shape ops

    @Test
    void shapeOps() {
        Shape s = Shape.of(3, 4);
        check("reshape", List.of(signedInput(s)), v -> v.getFirst().reshape(Shape.of(4, 3)));
        check("reshape flat", List.of(signedInput(s)), v -> v.getFirst().reshape(Shape.of(12)));
        check("stretch(0)", List.of(signedInput(s)), v -> v.getFirst().stretch(0));
        check("stretch(2)", List.of(signedInput(s)), v -> v.getFirst().stretch(2));
        check("narrow(0)", List.of(signedInput(s)), v -> v.getFirst().narrow(0, 1, 3));
        check("narrow(1)", List.of(signedInput(s)), v -> v.getFirst().narrow(1, 0, 2));
        check("cat(0)", List.of(signedInput(Shape.of(2, 4)), signedInput(Shape.of(3, 4))),
                v -> tm.cat(0, v.get(0), v.get(1)));
        check("cat(1)", List.of(signedInput(Shape.of(3, 2)), signedInput(Shape.of(3, 1))),
                v -> tm.cat(1, v.get(0), v.get(1)));
        check("split then use", List.of(signedInput(s)), v -> {
            List<Tensor> parts = v.getFirst().split(1, 0, 2);
            return parts.get(0).mul(2).add(parts.get(1).sqr().sum1d(1).stretch(1));
        });
    }

    @Test
    void gatherAlongClassAxis() {
        Shape s = Shape.of(4, 3);
        DArray<?> index = tm.strideArray(Shape.of(4, 1), 0, 2, 1, 2);
        check("gather(1)", List.of(signedInput(s)), v -> v.getFirst().gather(1, tm.var(index)));
    }

    // matrix products and convolutions

    @Test
    void batchVectorTimesMatrix() {
        check("bvtm batch x matrix", List.of(signedInput(Shape.of(5, 3)), signedInput(Shape.of(3, 2))),
                v -> v.get(0).bvtm(v.get(1)));
        check("bvtm vector x matrix", List.of(signedInput(Shape.of(3)), signedInput(Shape.of(3, 2))),
                v -> v.get(0).bvtm(v.get(1)));
    }

    @Test
    void conv1d() {
        // x: (batch, in channels, length); w: (out channels, in channels, kernel); b: (out channels)
        check("conv1d", List.of(signedInput(Shape.of(2, 2, 6)), signedInput(Shape.of(3, 2, 3)), signedInput(Shape.of(3))),
                v -> v.get(0).conv1d(v.get(1), v.get(2), 1, 0, 1, 1));
        check("conv1d stride 2 pad 1", List.of(signedInput(Shape.of(2, 2, 6)), signedInput(Shape.of(3, 2, 3)), signedInput(Shape.of(3))),
                v -> v.get(0).conv1d(v.get(1), v.get(2), 2, 1, 1, 1));
        check("conv1d dilation 2", List.of(signedInput(Shape.of(1, 2, 7)), signedInput(Shape.of(2, 2, 2)), signedInput(Shape.of(2))),
                v -> v.get(0).conv1d(v.get(1), v.get(2), 1, 0, 2, 1));
        check("conv1d groups 2", List.of(signedInput(Shape.of(2, 4, 6)), signedInput(Shape.of(4, 2, 3)), signedInput(Shape.of(4))),
                v -> v.get(0).conv1d(v.get(1), v.get(2), 1, 0, 1, 2));
    }

    @Test
    void conv2d() {
        // x: (batch, in channels, h, w); w: (out channels, in channels, kh, kw); b: (out channels)
        check("conv2d", List.of(signedInput(Shape.of(2, 2, 4, 4)), signedInput(Shape.of(3, 2, 2, 2)), signedInput(Shape.of(3))),
                v -> v.get(0).conv2d(v.get(1), v.get(2), 1, 0, 1, 1));
        check("conv2d stride 2 pad 1", List.of(signedInput(Shape.of(2, 2, 4, 4)), signedInput(Shape.of(3, 2, 3, 3)), signedInput(Shape.of(3))),
                v -> v.get(0).conv2d(v.get(1), v.get(2), 2, 1, 1, 1));
        check("conv2d dilation 2", List.of(signedInput(Shape.of(1, 2, 5, 5)), signedInput(Shape.of(2, 2, 2, 2)), signedInput(Shape.of(2))),
                v -> v.get(0).conv2d(v.get(1), v.get(2), 1, 0, 2, 1));
        check("conv2d groups 2", List.of(signedInput(Shape.of(1, 4, 4, 4)), signedInput(Shape.of(4, 2, 2, 2)), signedInput(Shape.of(4))),
                v -> v.get(0).conv2d(v.get(1), v.get(2), 1, 0, 1, 2));
    }

    // masks and dropout

    @Test
    void compareMasks() {
        Shape s = Shape.of(3, 4);
        check("compareTrue(GT,0)", List.of(signedInput(s)), v -> v.getFirst().compareTrue(Compare.GT, 0));
        check("compareFalse(GT,0)", List.of(signedInput(s)), v -> v.getFirst().compareFalse(Compare.GT, 0));
    }

    @Test
    void dropoutBackwardMatchesForwardScale() {
        // dropout is random, so the forward is rebuilt from the same seed every time it is evaluated
        Shape s = Shape.of(4, 5);
        check("dropout(0.4)", List.of(signedInput(s)), v -> v.getFirst().dropout(0.4, new Random(7)));
    }

    // losses

    // gradient buffer ownership

    /**
     * A leaf feeding both a pass-through node (identity: its backward returns the parent's gradient array itself)
     * and a second path. Before addGrad copied its first contribution, the leaf stored the identity node's own
     * gradient array by reference and the second contribution was then added in place into that shared array,
     * corrupting the identity node's gradient (and, one level up, the root's).
     */
    @Test
    void gradientAccumulationDoesNotAliasUpstreamGradient() {
        Shape s = Shape.of(3, 4);
        check("identity + x", List.of(signedInput(s)), v -> v.getFirst().identity().add(v.getFirst()));
        check("reshape + x", List.of(signedInput(s)), v -> v.getFirst().reshape(Shape.of(4, 3)).reshape(s).add(v.getFirst()));
        check("stretch/sum1d + x", List.of(signedInput(s)),
                v -> v.getFirst().sum1d(0).stretch(0).add(v.getFirst()));

        // direct check of the invariant: the identity node's gradient must survive accumulation into the leaf
        Tensor x = tm.var(signedInput(s)).requiresGrad(true);
        Tensor id = x.identity();
        Tensor out = id.add(x.mul(3.0));
        out.setGrad(tm.fullArray(s, 1.0));
        Autograd.backward(out, true);
        DArray<?> ones = tm.fullArray(s, 1.0);
        assertTrue(id.grad().deepEquals(ones, 1e-12), "identity node gradient was modified by the leaf's accumulation");
        assertTrue(x.grad().deepEquals(tm.fullArray(s, 4.0), 1e-12), "leaf gradient must be 1 + 3");
    }

    @Test
    void negativeLogLikelihoodReduction() {
        // pred: probabilities (rows, classes); y: class index per row
        DArray<?> pred = tm.randomArray(Shape.of(4, 3), Uniform.of(0.2, 0.9));
        DArray<?> y = tm.strideArray(Shape.of(4), 0, 2, 1, 1);

        double sum = 0;
        for (int i = 0; i < 4; i++) {
            sum -= Math.log(pred.getDouble(i, (int) y.getDouble(i)));
        }

        var sumLoss = new NegativeLogLikelihoodLoss(tm);
        sumLoss.reduce.set(Reduce.SUM);
        assertEquals(sum, sumLoss.forward(tm.var(pred.copy()), tm.var(y.copy())).lossValue(), 1e-12);

        var meanLoss = new NegativeLogLikelihoodLoss(tm);
        assertEquals(sum / 4, meanLoss.forward(tm.var(pred.copy()), tm.var(y.copy())).lossValue(), 1e-12);
    }

    @Test
    void negativeLogLikelihoodGradient() {
        // Loss.forward seeds the gradient of its output tensor (it is meant to be the root of the graph),
        // so the loss is checked directly rather than through the weighted-sum harness
        DArray<?> y = tm.strideArray(Shape.of(4), 0, 2, 1, 1);
        for (Reduce reduce : Reduce.values()) {
            DArray<?> pred = tm.randomArray(Shape.of(4, 3), Uniform.of(0.2, 0.9));
            Tensor x = tm.var(pred.copy()).requiresGrad(true);
            var loss = new NegativeLogLikelihoodLoss(tm);
            loss.reduce.set(reduce);
            Autograd.backward(loss.forward(x, tm.var(y.copy())).tensor());
            DArray<?> analytic = x.grad();
            assertEquals(pred.shape(), analytic.shape());

            PointerIterator it = pred.ptrIterator();
            while (it.hasNext()) {
                int ptr = it.nextInt();
                double x0 = pred.ptrGetDouble(ptr);
                pred.ptrSetDouble(ptr, x0 + EPS);
                double lp = new NegativeLogLikelihoodLoss(tm).reduce.set(reduce).forward(tm.var(pred.copy()), tm.var(y.copy())).lossValue();
                pred.ptrSetDouble(ptr, x0 - EPS);
                double lm = new NegativeLogLikelihoodLoss(tm).reduce.set(reduce).forward(tm.var(pred.copy()), tm.var(y.copy())).lossValue();
                pred.ptrSetDouble(ptr, x0);
                double numeric = (lp - lm) / (2 * EPS);
                double a = analytic.ptrGetDouble(ptr);
                assertEquals(numeric, a, TOL * Math.max(1.0, Math.abs(numeric)), "nll " + reduce + " at ptr " + ptr);
            }
        }
    }
}
