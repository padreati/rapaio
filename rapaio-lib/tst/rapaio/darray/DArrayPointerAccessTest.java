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

package rapaio.darray;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import rapaio.darray.iterators.PointerIterator;

/**
 * Pins the documented contract of pointer access (the {@code ptr*} family): pointers are offsets into the storage of
 * one specific array and depend on its layout.
 */
public class DArrayPointerAccessTest {

    private final DArrayManager dm = DArrayManager.base();

    private static List<Integer> pointers(DArray<?> x, Order order) {
        List<Integer> list = new ArrayList<>();
        PointerIterator it = x.ptrIterator(order);
        while (it.hasNext()) {
            list.add(it.nextInt());
        }
        return list;
    }

    @Test
    void sameLogicalElementHasDifferentPointersInDifferentLayouts() {
        DArray<Double> c = dm.seq(DType.DOUBLE, Shape.of(3, 4), Order.C);
        DArray<Double> f = c.copy(Order.F);
        DArray<Double> t = c.t_();
        assertTrue(c.deepEquals(f));

        // element (1, 2) has value 6 in both, but sits at different storage offsets
        List<Integer> pc = pointers(c, Order.C);
        List<Integer> pf = pointers(f, Order.C);
        int logical = 1 * 4 + 2;
        assertEquals(6.0, c.ptrGetDouble(pc.get(logical)));
        assertEquals(6.0, f.ptrGetDouble(pf.get(logical)));
        assertNotEquals(pc.get(logical), pf.get(logical));
        // using a pointer of one array on the other addresses a different element
        assertNotEquals(c.ptrGetDouble(pc.get(logical)), c.ptrGetDouble(pf.get(logical)));

        // the transposed view shares the storage, so pointers of the same logical element agree
        List<Integer> pt = pointers(t, Order.F);
        assertEquals(pc, pt);
    }

    @Test
    void storageOrderIsNotAlignedAcrossLayouts() {
        DArray<Double> c = dm.seq(DType.DOUBLE, Shape.of(3, 4), Order.C);
        DArray<Double> f = c.copy(Order.F);
        // explicit orders pair logical elements, storage order does not
        List<Integer> pc = pointers(c, Order.C);
        List<Integer> pf = pointers(f, Order.C);
        for (int i = 0; i < pc.size(); i++) {
            assertEquals(c.ptrGetDouble(pc.get(i)), f.ptrGetDouble(pf.get(i)));
        }
        List<Integer> sc = pointers(c, Order.S);
        List<Integer> sf = pointers(f, Order.S);
        boolean allEqual = true;
        for (int i = 0; i < sc.size(); i++) {
            allEqual &= c.ptrGetDouble(sc.get(i)) == f.ptrGetDouble(sf.get(i));
        }
        assertTrue(!allEqual);
    }

    @Test
    void writesThroughViewPointersAreVisibleInTheBaseArray() {
        DArray<Double> base = dm.zeros(DType.DOUBLE, Shape.of(4, 5));
        DArray<Double> view = base.narrow(0, true, 1, 3).narrow(1, true, 2, 4);
        PointerIterator it = view.ptrIterator(Order.C);
        double v = 1;
        while (it.hasNext()) {
            view.ptrSetDouble(it.nextInt(), v++);
        }
        assertEquals(1.0, base.getDouble(1, 2));
        assertEquals(2.0, base.getDouble(1, 3));
        assertEquals(3.0, base.getDouble(2, 2));
        assertEquals(4.0, base.getDouble(2, 3));
        assertEquals(10.0, base.sum());
    }

    @Test
    void typedPointerAccessConvertsLikeTypedGetSet() {
        DArray<Integer> x = dm.seq(DType.INTEGER, Shape.of(5));
        int ptr = pointers(x, Order.C).get(3);
        assertEquals(3.0, x.ptrGetDouble(ptr));
        x.ptrSetDouble(ptr, 7.9);
        assertEquals(7, x.ptrGetInt(ptr));
        assertEquals(7, x.getInt(3));
        x.ptrIncDouble(ptr, 1.5);
        assertEquals(8, x.getInt(3));
    }
}
