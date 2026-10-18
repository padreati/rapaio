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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.darray.DArray;
import rapaio.darray.Shape;
import rapaio.datasets.Batch;
import rapaio.nn.Loss.Reduce;
import rapaio.nn.layer.AbstractNetwork;
import rapaio.nn.layer.BatchNorm1D;
import rapaio.nn.layer.Dropout;
import rapaio.nn.layer.Linear;
import rapaio.nn.layer.Sequential;
import rapaio.nn.loss.MSELoss;
import rapaio.nn.loss.NegativeLikelihoodLoss;

/**
 * Regression tests for the network plumbing: losses, batched losses, reflection based layer discovery, state
 * loading, batch norm statistics under parallel forward and back propagation over deep graphs.
 */
public class NetworkPlumbingTest {

    private TensorManager tm;

    @BeforeEach
    void beforeEach() {
        tm = TensorManager.ofDouble().seed(42);
    }

    @AfterEach
    void afterEach() {
        tm.close();
    }

    // losses

    @Test
    void mseLossPairsPredictionsWithTargets() {
        Tensor pred = tm.strideTensor(Shape.of(4, 1), 1., 2., 3., 4.);

        // a column target, the same shape as the prediction
        Tensor yColumn = tm.strideTensor(Shape.of(4, 1), 1., 2., 3., 5.);
        assertEquals(0.25, new MSELoss(tm).forward(pred, yColumn).lossValue(), 1e-12);
        assertEquals(Shape.of(4, 1), yColumn.shape());

        // a vector target is aligned to the column, without changing the caller's tensor
        Tensor yVector = tm.strideTensor(Shape.of(4), 1., 2., 3., 5.);
        MSELoss loss = new MSELoss(tm);
        assertEquals(0.25, loss.forward(pred, yVector).lossValue(), 1e-12);
        assertEquals(0.25, loss.forward(pred, yVector).lossValue(), 1e-12);
        assertEquals(Shape.of(4), yVector.shape());

        // several outputs
        Tensor pred2 = tm.strideTensor(Shape.of(3, 2), 1., 2., 3., 4., 5., 6.);
        Tensor y2 = tm.strideTensor(Shape.of(3, 2), 1., 2., 3., 4., 5., 7.);
        assertEquals(1.0 / 6, new MSELoss(tm).forward(pred2, y2).lossValue(), 1e-12);

        // shapes which would only match by broadcasting are rejected
        assertThrows(IllegalArgumentException.class,
                () -> new MSELoss(tm).forward(pred2, tm.strideTensor(Shape.of(3), 1., 2., 3.)));
    }

    @Test
    void mseLossGradientFlowsIntoVectorTarget() {
        Tensor pred = tm.strideTensor(Shape.of(2, 1), 1., 2.);
        Tensor y = tm.strideTensor(Shape.of(2), 0., 0.).requiresGrad(true);
        new MSELoss(tm).forward(pred, y).backward();
        // d/dy mean((p - y)^2) = -2 (p - y) / n
        assertEquals(Shape.of(2), y.grad().shape());
        assertEquals(-1.0, y.grad().getDouble(0), 1e-12);
        assertEquals(-2.0, y.grad().getDouble(1), 1e-12);
    }

    @Test
    void negativeLikelihoodLossKeepsCallerTarget() {
        Tensor pred = tm.strideTensor(Shape.of(2, 3), -1., -2., -3., -4., -5., -6.);
        Tensor y = tm.strideTensor(Shape.of(2), 0., 2.);
        NegativeLikelihoodLoss loss = new NegativeLikelihoodLoss(tm);
        assertEquals(3.5, loss.forward(pred, y).lossValue(), 1e-12);
        assertEquals(Shape.of(2), y.shape());
        assertEquals(3.5, loss.forward(pred, y).lossValue(), 1e-12);
    }

    @Test
    void batchedLossGradientMatchesReportedLoss() {
        tm.seed(1);
        DArray<?> x = tm.randomArray(Shape.of(30, 3));
        DArray<?> y = tm.randomArray(Shape.of(30));

        Linear linear = new Linear(tm, 3, 1, true);
        var full = new MSELoss(tm).forward(linear.forward11(tm.var(x)), tm.var(y));
        full.backward();
        List<DArray<?>> fullGrads = linear.parameters().stream().<DArray<?>>map(p -> p.grad().copy()).toList();
        linear.parameters().forEach(Tensor::zeroGrad);

        // three equal batches: the average of the batch means is the full mean
        List<Batch> batches = linear.batchForward(10, false, false, tm.var(x));
        var batched = new MSELoss(tm).batchForward(batches, tm.var(y));
        assertEquals(full.lossValue(), batched.lossValue(), 1e-12);
        batched.backward();
        for (int i = 0; i < fullGrads.size(); i++) {
            assertTrue(fullGrads.get(i).deepEquals(linear.parameters().get(i).grad(), 1e-12),
                    "gradient of parameter " + i + " differs from the full batch gradient");
        }
    }

    @Test
    void batchedLossKeepsConfiguredReduce() {
        DArray<?> x = tm.randomArray(Shape.of(20, 2));
        DArray<?> y = tm.randomArray(Shape.of(20));
        Linear linear = new Linear(tm, 2, 1, false);

        MSELoss sum = new MSELoss(tm).reduce.set(Reduce.SUM);
        double fullSum = sum.forward(linear.forward11(tm.var(x)), tm.var(y)).lossValue();
        List<Batch> batches = linear.batchForward(10, false, false, tm.var(x));
        // two batches, each reduced with SUM, then averaged
        assertEquals(fullSum / 2, sum.batchForward(batches, tm.var(y)).lossValue(), 1e-9);
    }

    // reflection based discovery of layers

    static class Base extends AbstractNetwork {
        final Linear first;
        final Dropout dropout;

        Base(TensorManager tm) {
            super(tm);
            first = new Linear(tm, 2, 2, true);
            dropout = new Dropout(tm, 0.5);
        }

        @Override
        public Tensor forward11(Tensor x) {
            return dropout.forward11(first.forward11(x));
        }
    }

    static class Derived extends Base {
        final Linear second;
        final List<Network> extra;
        final BatchNorm1D[] norms;
        final Linear alias;

        Derived(TensorManager tm) {
            super(tm);
            second = new Linear(tm, 2, 2, false);
            extra = List.of(new Linear(tm, 2, 2, false));
            norms = new BatchNorm1D[] {new BatchNorm1D(tm, 2)};
            alias = second;
        }

        @Override
        public Tensor forward11(Tensor x) {
            return second.forward11(super.forward11(x));
        }
    }

    @Test
    void reflectionFindsLayersOfTheWholeHierarchyOnce() {
        Base base = new Base(tm);
        assertEquals(2, base.parameters().size());
        assertEquals(2, base.state().tensors().size());

        Derived derived = new Derived(tm);
        // first: w, b; second: w (also reachable through alias); extra linear: w; batch norm: gamma, beta
        assertEquals(6, derived.parameters().size());
        // the same plus the two running statistics of the batch norm
        assertEquals(8, derived.state().tensors().size());
    }

    @Test
    void trainAndEvalReachChildLayers() {
        Base base = new Base(tm);
        Tensor ones = tm.fullTensor(Shape.of(1000), 1);

        base.train();
        double trained = base.dropout.forward11(ones).value().sum().doubleValue();
        assertNotEquals(1000.0, trained, "dropout of a child layer must be active after train()");

        base.eval();
        assertEquals(1000.0, base.dropout.forward11(ones).value().sum().doubleValue(), 1e-12);

        // a Sequential nested in a custom network
        Wrapper wrapper = new Wrapper(tm);
        wrapper.train();
        assertNotEquals(1000.0, wrapper.forward11(ones).value().sum().doubleValue());
    }

    static class Wrapper extends AbstractNetwork {
        final Sequential seq;

        Wrapper(TensorManager tm) {
            super(tm);
            seq = new Sequential(tm, new Dropout(tm, 0.5));
        }

        @Override
        public Tensor forward11(Tensor x) {
            return seq.forward11(x);
        }
    }

    // state

    @Test
    void stateRoundTripAndShapeMismatch() throws IOException {
        Derived source = new Derived(tm);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        source.saveState(bos);

        Derived target = new Derived(tm);
        target.loadState(new ByteArrayInputStream(bos.toByteArray()));
        List<Tensor> expected = source.state().tensors();
        List<Tensor> actual = target.state().tensors();
        for (int i = 0; i < expected.size(); i++) {
            assertTrue(expected.get(i).value().deepEquals(actual.get(i).value()));
        }

        ByteArrayOutputStream small = new ByteArrayOutputStream();
        new Linear(tm, 3, 2, true).saveState(small);
        Linear other = new Linear(tm, 2, 2, true);
        assertThrows(IOException.class, () -> other.loadState(new ByteArrayInputStream(small.toByteArray())));
    }

    // batch norm under parallel forward

    @Test
    void batchNormRunningStatisticsCountEveryParallelBatch() {
        // every batch of 10 rows has the same values, so the running statistics do not depend on the order in which
        // the batches are processed, only on how many updates were applied. A small momentum keeps the running
        // average far from convergence, so each lost update leaves a visible difference
        int batches = 400;
        double momentum = 1e-3;
        DArray<?> block = tm.randomArray(Shape.of(10, 2));
        DArray<?> x = tm.zerosArray(Shape.of(10 * batches, 2));
        for (int i = 0; i < 10 * batches; i++) {
            for (int j = 0; j < 2; j++) {
                x.setDouble(block.getDouble(i % 10, j), i, j);
            }
        }

        BatchNorm1D sequential = new BatchNorm1D(tm, 2, momentum, BatchNorm1D.DEFAULT_EPSILON);
        sequential.train();
        for (int i = 0; i < batches; i++) {
            sequential.forward11(tm.var(block.copy()));
        }

        BatchNorm1D parallel = new BatchNorm1D(tm, 2, momentum, BatchNorm1D.DEFAULT_EPSILON);
        parallel.train();
        parallel.batchForward(10, false, false, tm.var(x));

        List<Tensor> expected = sequential.state().tensors();
        List<Tensor> actual = parallel.state().tensors();
        assertTrue(expected.get(0).value().deepEquals(actual.get(0).value(), 1e-9), "running mean lost updates");
        assertTrue(expected.get(1).value().deepEquals(actual.get(1).value(), 1e-9), "running std lost updates");
    }

    // autograd

    @Test
    void parallelBatchForwardIsRepeatableWithDropout() {
        DArray<?> x = tm.fullArray(Shape.of(400, 8), 1);
        Sequential nn = new Sequential(tm, new Dropout(tm, 0.5));
        nn.train();

        List<List<DArray<?>>> runs = new java.util.ArrayList<>();
        for (int run = 0; run < 3; run++) {
            tm.seed(123);
            List<Batch> batches = nn.batchForward(10, true, false, tm.var(x));
            runs.add(batches.stream().<DArray<?>>map(b -> b.outputs()[0].value()).toList());
        }
        for (int run = 1; run < runs.size(); run++) {
            for (int i = 0; i < runs.getFirst().size(); i++) {
                assertTrue(runs.getFirst().get(i).deepEquals(runs.get(run).get(i)),
                        "dropout mask of batch " + i + " differs between runs with the same seed");
            }
        }

        // the masks are the same as a single threaded replay which seeds each batch the same way
        tm.seed(123);
        var it = new rapaio.datasets.TabularDataset(tm, x).batchIterator(10, true, false);
        int i = 0;
        while (it.hasNext()) {
            Batch batch = it.next();
            java.util.Random batchRandom = new java.util.Random(tm.random().nextLong());
            DArray<?> expected = tm.withRandom(batchRandom, () -> nn.forward11(batch.tensors()[0]).value());
            assertTrue(expected.deepEquals(runs.getFirst().get(i)), "batch " + i + " differs from the sequential replay");
            i++;
        }
        assertEquals(runs.getFirst().size(), i);
    }

    @Test
    void withRandomRestoresTheSharedSource() {
        java.util.Random scoped = new java.util.Random(1);
        assertTrue(tm.random() != scoped);
        tm.withRandom(scoped, () -> {
            assertTrue(tm.random() == scoped);
            java.util.Random inner = new java.util.Random(2);
            tm.withRandom(inner, () -> {
                assertTrue(tm.random() == inner);
                return null;
            });
            assertTrue(tm.random() == scoped);
            return null;
        });
        assertTrue(tm.random() != scoped);
    }

    // autograd

    @Test
    void backwardOverDeepChain() {
        Tensor x = tm.scalarTensor(1).requiresGrad(true);
        Tensor t = x;
        for (int i = 0; i < 50_000; i++) {
            t = t.add(1.0);
        }
        t.setGrad(tm.scalarArray(1));
        Autograd.backward(t);
        assertEquals(1.0, x.grad().getDouble(), 1e-12);
    }
}
