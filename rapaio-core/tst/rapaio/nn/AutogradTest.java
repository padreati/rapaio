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

package rapaio.nn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.narray.NArrays;

public class AutogradTest {

    private TensorManager tm;

    @BeforeEach
    void beforeEach() {
        tm = TensorManager.ofFloat();
    }


    @Test
    void testOrder() {
        Tensor a = tm.scalarTensor(-1.).name("a");
        Tensor f = tm.scalarTensor(1.).name("f");
        Tensor c = a.log().name("c");
        Tensor b = a.add(c).name("b");
        Tensor d = b.log().name("d");
        Tensor e1 = c.add(d).name("e1");
        Tensor e2 = e1.add(f).name("e2");
        Tensor g = e2.add(d).name("g");
        Tensor h = f.add(g).name("h");
        h.setGrad(NArrays.scalar(1.));

        Autograd.ComputeGraph graph = new Autograd.ComputeGraph(h, true);
        graph.run();
        assertEquals(h, graph.root);
        assertTrue(graph.retainGrad);

        assertEquals(9, graph.reverse.size());
        testBefore(graph.reverse, "a", "c");
        testBefore(graph.reverse, "a", "b");
        testBefore(graph.reverse, "c", "b");
        testBefore(graph.reverse, "b", "d");
        testBefore(graph.reverse, "d", "e1");
        testBefore(graph.reverse, "c", "e1");
        testBefore(graph.reverse, "e1", "e2");
        testBefore(graph.reverse, "f", "e2");
        testBefore(graph.reverse, "d", "g");
        testBefore(graph.reverse, "e2", "g");
        testBefore(graph.reverse, "g", "h");
        testBefore(graph.reverse, "f", "h");
    }

    @Test
    void testRequiresGradDefault() {

        Tensor a = tm.scalarTensor(-1.).name("a");
        Tensor f = tm.scalarTensor(1.).name("f");
        Tensor c = a.log().name("c");
        Tensor b = a.add(c).name("b");
        Tensor d = b.log().name("d");
        Tensor e1 = c.add(d).name("e1");
        Tensor e2 = e1.add(f).name("e2");
        Tensor g = e2.add(d).name("g");
        Tensor h = f.add(g).name("h");
        h.setGrad(NArrays.scalar(1.));

        Autograd.ComputeGraph graph = new Autograd.ComputeGraph(h, true);
        graph.run();

        // by default no variable required gradients
        assertNull(a.grad());
        assertNull(b.grad());
        assertNull(c.grad());
        assertNull(d.grad());
        assertNull(e1.grad());
        assertNull(e2.grad());
        assertNull(f.grad());
        assertNull(g.grad());
        assertNotNull(h.grad());
    }

    @Test
    void testRequiresGradVariant1() {

        Tensor a = tm.scalarTensor(-1.).name("a");
        Tensor f = tm.scalarTensor(1.).name("f");
        Tensor c = a.log().name("c");
        Tensor b = a.add(c).name("b");
        Tensor d = b.log().name("d");
        Tensor e1 = c.add(d).name("e1");
        Tensor e2 = e1.add(f).name("e2");
        Tensor g = e2.add(d).name("g");
        Tensor h = f.add(g).name("h");
        h.setGrad(NArrays.scalar(1.));

        a.requiresGrad(true);

        Autograd.ComputeGraph graph = new Autograd.ComputeGraph(h, true);
        graph.run();

        assertNotNull(a.grad());
        assertNotNull(b.grad());
        assertNotNull(c.grad());
        assertNotNull(d.grad());
        assertNotNull(e1.grad());
        assertNotNull(e2.grad());
        assertNull(f.grad());
        assertNotNull(g.grad());
        assertNotNull(h.grad());
    }

    @Test
    void testRequiredGradVariant2() {

        Tensor a = tm.scalarTensor(-1.).name("a");
        Tensor f = tm.scalarTensor(1.).name("f");
        Tensor c = a.log().name("c");
        Tensor b = a.add(c).name("b");
        Tensor d = b.log().name("d");
        Tensor e1 = c.add(d).name("e1");
        Tensor e2 = e1.add(f).name("e2");
        Tensor g = e2.add(d).name("g");
        Tensor h = f.add(g).name("h");
        h.setGrad(NArrays.scalar(1.));

        b.requiresGrad(true);

        Autograd.ComputeGraph graph = new Autograd.ComputeGraph(h, true);
        graph.run();

        graph.run();
        assertNull(a.grad());
        assertNotNull(b.grad());
        assertNull(c.grad());
        assertNotNull(d.grad());
        assertNotNull(e1.grad());
        assertNotNull(e2.grad());
        assertNull(f.grad());
        assertNotNull(g.grad());
        assertNotNull(h.grad());
    }

    @Test
    void testRetainGradTrue() {

        Tensor a = tm.scalarTensor(-1.).name("a");
        Tensor f = tm.scalarTensor(1.).name("f");
        Tensor c = a.log().name("c");
        Tensor b = a.add(c).name("b");
        Tensor d = b.log().name("d");
        Tensor e1 = c.add(d).name("e1");
        Tensor e2 = e1.add(f).name("e2");
        Tensor g = e2.add(d).name("g");
        Tensor h = f.add(g).name("h");
        h.setGrad(NArrays.scalar(1.));

        a.requiresGrad(true);
        f.requiresGrad(true);

        Autograd.ComputeGraph graph = new Autograd.ComputeGraph(h, true);
        graph.run();

        assertNotNull(a.grad());
        assertNotNull(b.grad());
        assertNotNull(c.grad());
        assertNotNull(d.grad());
        assertNotNull(e1.grad());
        assertNotNull(e2.grad());
        assertNotNull(f.grad());
        assertNotNull(g.grad());
        assertNotNull(h.grad());

        assertEquals(0, a.backfuns().size());
        assertEquals(2, b.backfuns().size());
        assertEquals(1, c.backfuns().size());
        assertEquals(1, d.backfuns().size());
        assertEquals(2, e1.backfuns().size());
        assertEquals(2, e2.backfuns().size());
        assertEquals(0, f.backfuns().size());
        assertEquals(2, g.backfuns().size());
        assertEquals(2, h.backfuns().size());
    }

    @Test
    void testRetainGradFalse() {

        Tensor a = tm.scalarTensor(-1.).name("a");
        Tensor f = tm.scalarTensor(1.).name("f");
        Tensor c = a.log().name("c");
        Tensor b = a.add(c).name("b");
        Tensor d = b.log().name("d");
        Tensor e1 = c.add(d).name("e1");
        Tensor e2 = e1.add(f).name("e2");
        Tensor g = e2.add(d).name("g");
        Tensor h = f.add(g).name("h");
        h.setGrad(NArrays.scalar(1.));

        a.requiresGrad(true);
        f.requiresGrad(true);

        Autograd.ComputeGraph graph = new Autograd.ComputeGraph(h, false);
        graph.run();

        assertNotNull(a.grad());
        assertNotNull(b.grad());
        assertNotNull(c.grad());
        assertNotNull(d.grad());
        assertNotNull(e1.grad());
        assertNotNull(e2.grad());
        assertNotNull(f.grad());
        assertNotNull(g.grad());
        assertNotNull(h.grad());

        assertEquals(0, a.backfuns().size());
        assertEquals(0, b.backfuns().size());
        assertEquals(0, c.backfuns().size());
        assertEquals(0, d.backfuns().size());
        assertEquals(0, e1.backfuns().size());
        assertEquals(0, e2.backfuns().size());
        assertEquals(0, f.backfuns().size());
        assertEquals(0, g.backfuns().size());
        assertEquals(0, h.backfuns().size());
    }

    private void testBefore(List<Tensor> sorted, String after, String before) {
        int beforeIndex = -1;
        int afterIndex = -1;
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).name().equals(before)) {
                beforeIndex = i;
            }
            if (sorted.get(i).name().equals(after)) {
                afterIndex = i;
            }
        }
        assertTrue(beforeIndex < afterIndex);
    }
}
