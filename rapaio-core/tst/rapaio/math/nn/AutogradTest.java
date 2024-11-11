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

package rapaio.math.nn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import rapaio.math.tensor.Tensors;

public class AutogradTest {

    @Test
    void testOrder() {

        Node a = Autograd.var(Tensors.scalar(-1.)).name("a");
        Node f = Autograd.var(Tensors.scalar(1.)).name("f");
        Node c = a.log().name("c");
        Node b = a.add(c).name("b");
        Node d = b.log().name("d");
        Node e1 = c.add(d).name("e1");
        Node e2 = e1.add(f).name("e2");
        Node g = e2.add(d).name("g");
        Node h = f.add(g).name("h");
        h.setGrad(Tensors.scalar(1.));

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

        Node a = Autograd.var(Tensors.scalar(-1.)).name("a");
        Node f = Autograd.var(Tensors.scalar(1.)).name("f");
        Node c = a.log().name("c");
        Node b = a.add(c).name("b");
        Node d = b.log().name("d");
        Node e1 = c.add(d).name("e1");
        Node e2 = e1.add(f).name("e2");
        Node g = e2.add(d).name("g");
        Node h = f.add(g).name("h");
        h.setGrad(Tensors.scalar(1.));

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

        Node a = Autograd.var(Tensors.scalar(-1.)).name("a");
        Node f = Autograd.var(Tensors.scalar(1.)).name("f");
        Node c = a.log().name("c");
        Node b = a.add(c).name("b");
        Node d = b.log().name("d");
        Node e1 = c.add(d).name("e1");
        Node e2 = e1.add(f).name("e2");
        Node g = e2.add(d).name("g");
        Node h = f.add(g).name("h");
        h.setGrad(Tensors.scalar(1.));

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

        Node a = Autograd.var(Tensors.scalar(-1.)).name("a");
        Node f = Autograd.var(Tensors.scalar(1.)).name("f");
        Node c = a.log().name("c");
        Node b = a.add(c).name("b");
        Node d = b.log().name("d");
        Node e1 = c.add(d).name("e1");
        Node e2 = e1.add(f).name("e2");
        Node g = e2.add(d).name("g");
        Node h = f.add(g).name("h");
        h.setGrad(Tensors.scalar(1.));

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

        Node a = Autograd.var(Tensors.scalar(-1.)).name("a");
        Node f = Autograd.var(Tensors.scalar(1.)).name("f");
        Node c = a.log().name("c");
        Node b = a.add(c).name("b");
        Node d = b.log().name("d");
        Node e1 = c.add(d).name("e1");
        Node e2 = e1.add(f).name("e2");
        Node g = e2.add(d).name("g");
        Node h = f.add(g).name("h");
        h.setGrad(Tensors.scalar(1.));

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

        Node a = Autograd.var(Tensors.scalar(-1.)).name("a");
        Node f = Autograd.var(Tensors.scalar(1.)).name("f");
        Node c = a.log().name("c");
        Node b = a.add(c).name("b");
        Node d = b.log().name("d");
        Node e1 = c.add(d).name("e1");
        Node e2 = e1.add(f).name("e2");
        Node g = e2.add(d).name("g");
        Node h = f.add(g).name("h");
        h.setGrad(Tensors.scalar(1.));

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

    private void testBefore(List<Node> sorted, String after, String before) {
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
