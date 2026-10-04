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

package rapaio.darray.layout;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.DArrays;
import rapaio.darray.DType;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.darray.iterators.IndexIterator;
import rapaio.util.collection.Ints;

/**
 * Layout and view behaviour which depends on how a layout was built: {@link StrideLayout#ofDense} always produces
 * an {@link ArrayStrideLayout}, while {@link StrideLayout#of} produces the rank-specialised implementations, and
 * the two used to disagree on narrow, permute, reshape, squeeze and equality.
 */
public class StrideLayoutContractTest {

    private static final DArrayManager dm = DArrayManager.base();

    /** The same dense layout, once through each factory. */
    private static List<StrideLayout> bothFactories(Shape shape, Order order) {
        StrideLayout dense = StrideLayout.ofDense(shape, 0, order);
        return List.of(dense, StrideLayout.of(shape, 0, dense.strides()));
    }

    @Test
    void moveAxisWorksInBothDirections() {
        for (StrideLayout layout : bothFactories(Shape.of(2, 3, 4), Order.C)) {
            StrideLayout moved = layout.moveAxis(2, 0);
            assertArrayEquals(new int[] {4, 2, 3}, moved.dims(), layout.getClass().getSimpleName());
            assertArrayEquals(new int[] {1, 12, 4}, moved.strides(), layout.getClass().getSimpleName());
            // moving forward and back is the identity
            assertEquals(layout, moved.moveAxis(0, 2));
        }
        // the matrix implementation: [4,3] strides [1,4] is the transpose of a dense 3x4
        StrideLayout t = StrideLayout.of(Shape.of(4, 3), 0, new int[] {1, 4});
        StrideLayout moved = t.moveAxis(1, 0);
        assertArrayEquals(new int[] {3, 4}, moved.dims());
        assertArrayEquals(new int[] {4, 1}, moved.strides());

        // and the data follows the layout
        DArray<Double> m = DArrays.seq(Shape.of(2, 3, 4));
        DArray<Double> mm = m.moveAxis(2, 0);
        assertArrayEquals(new int[] {4, 2, 3}, mm.shape().dims());
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 4; k++) {
                    assertEquals(m.getDouble(i, j, k), mm.getDouble(k, i, j));
                }
            }
        }
    }

    @Test
    void permuteIsAcceptedForMatrixLayouts() {
        StrideLayout matrix = StrideLayout.of(Shape.of(3, 4), 0, new int[] {4, 1});
        assertEquals(matrix, matrix.permute(new int[] {0, 1}));
        StrideLayout swapped = matrix.permute(new int[] {1, 0});
        assertArrayEquals(new int[] {4, 3}, swapped.dims());
        assertArrayEquals(new int[] {1, 4}, swapped.strides());

        // reachable from a darray whose layout went through StrideLayout.of
        DArray<Double> view = DArrays.seq(Shape.of(3, 4)).t_();
        DArray<Double> permuted = view.permute(1, 0);
        assertArrayEquals(new int[] {3, 4}, permuted.shape().dims());
        assertEquals(view.getDouble(2, 1), permuted.getDouble(1, 2));

        assertThrows(IllegalArgumentException.class, () -> matrix.permute(new int[] {0, 2}));
        assertThrows(IllegalArgumentException.class, () -> matrix.permute(new int[] {1, 1}));
    }

    @Test
    void indexFromPointerAccountsForTheOffset() {
        // a matrix layout, both factories
        StrideLayout dense = StrideLayout.ofDense(Shape.of(3, 4), 0, Order.C);
        assertArrayEquals(new int[] {1, 1}, dense.index(5));
        StrideLayout matrix = StrideLayout.of(Shape.of(3, 4), 0, dense.strides());
        assertArrayEquals(new int[] {1, 1}, matrix.index(5));
        StrideLayout fdense = StrideLayout.ofDense(Shape.of(3, 4), 0, Order.F);
        assertArrayEquals(new int[] {2, 1}, fdense.index(5));

        // with an offset, the pointer is absolute and the index relative
        StrideLayout shifted = StrideLayout.of(Shape.of(2, 3), 5, new int[] {4, 1});
        assertArrayEquals(new int[] {0, 1}, shifted.index(6));
        assertArrayEquals(new int[] {1, 2}, shifted.index(11));

        // every pointer of a rank-3 layout maps back to its own index
        StrideLayout cube = StrideLayout.ofDense(Shape.of(2, 3, 4), 7, Order.C);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 4; k++) {
                    assertArrayEquals(new int[] {i, j, k}, cube.index(cube.pointer(i, j, k)));
                }
            }
        }
        // an expanded axis has stride 0 and does not divide
        StrideLayout expanded = StrideLayout.of(Shape.of(3, 2), 0, new int[] {0, 1});
        assertArrayEquals(new int[] {0, 1}, expanded.index(1));
    }

    @Test
    void narrowDropsOnlyAUnitDimension() {
        // rank-1, both factories: keepDim=false with a non-unit result keeps the dimension
        for (StrideLayout layout : bothFactories(Shape.of(10), Order.C)) {
            String name = layout.getClass().getSimpleName();
            assertArrayEquals(new int[] {5}, layout.narrow(0, false, 0, 5).dims(), name);
            assertEquals(0, layout.narrow(0, false, 3, 4).rank(), name);
            assertArrayEquals(new int[] {1}, layout.narrow(0, true, 3, 4).dims(), name);
        }
        // chunk on a vector view whose layout is a VectorStrideLayout
        DArray<Double> view = DArrays.seq(Shape.of(10)).narrow(0, 0, 10);
        List<DArray<Double>> chunks = view.chunk(0, false, 2);
        assertEquals(5, chunks.size());
        for (DArray<Double> chunk : chunks) {
            assertArrayEquals(new int[] {2}, chunk.shape().dims());
        }
    }

    @Test
    void narrowAllHonoursKeepDim() {
        for (StrideLayout layout : bothFactories(Shape.of(2, 3), Order.C)) {
            String name = layout.getClass().getSimpleName();
            assertArrayEquals(new int[] {1, 3}, layout.narrowAll(true, new int[] {0, 0}, new int[] {1, 3}).dims(), name);
            assertArrayEquals(new int[] {3}, layout.narrowAll(false, new int[] {0, 0}, new int[] {1, 3}).dims(), name);
            assertArrayEquals(new int[] {2, 3}, layout.narrowAll(false, new int[] {0, 0}, new int[] {2, 3}).dims(), name);
        }
        for (StrideLayout layout : bothFactories(Shape.of(2, 3, 4), Order.C)) {
            assertArrayEquals(new int[] {3, 4},
                    layout.narrowAll(false, new int[] {0, 0, 0}, new int[] {1, 3, 4}).dims(),
                    layout.getClass().getSimpleName());
        }
    }

    @Test
    void narrowAllKeepsUnitAxesItDidNotNarrow() {
        for (StrideLayout layout : bothFactories(Shape.of(1, 3), Order.C)) {
            String name = layout.getClass().getSimpleName();
            // axis 0 is already unitary, so this request narrows nothing and every axis survives
            assertArrayEquals(new int[] {1, 3}, layout.narrowAll(false, new int[] {0, 0}, new int[] {1, 3}).dims(), name);
            // axis 1 is narrowed down to a single element and is dropped, axis 0 is still left alone
            assertArrayEquals(new int[] {1}, layout.narrowAll(false, new int[] {0, 1}, new int[] {1, 2}).dims(), name);
        }
        for (StrideLayout layout : bothFactories(Shape.of(1, 1, 4), Order.C)) {
            String name = layout.getClass().getSimpleName();
            assertArrayEquals(new int[] {1, 1, 4}, layout.narrowAll(false, new int[] {0, 0, 0}, new int[] {1, 1, 4}).dims(),
                    name);
            assertArrayEquals(new int[] {1, 1}, layout.narrowAll(false, new int[] {0, 0, 2}, new int[] {1, 1, 3}).dims(),
                    name);
        }
        // the vector implementation delegates to narrow, which drops a named axis more eagerly
        for (StrideLayout layout : bothFactories(Shape.of(1), Order.C)) {
            String name = layout.getClass().getSimpleName();
            assertArrayEquals(new int[] {1}, layout.narrowAll(false, new int[] {0}, new int[] {1}).dims(), name);
            assertEquals(0, layout.narrow(0, false, 0, 1).rank(), name);
        }
        for (StrideLayout layout : bothFactories(Shape.of(5), Order.C)) {
            assertEquals(0, layout.narrowAll(false, new int[] {2}, new int[] {3}).rank(), layout.getClass().getSimpleName());
        }
    }

    @Test
    void reshapeBetweenRankZeroAndRankOneWorksForEveryFactory() {
        // a narrowed vector keeps a VectorStrideLayout
        DArray<Double> unit = DArrays.seq(Shape.of(5)).narrow(0, true, 2, 3);
        assertEquals(0, unit.reshape(Shape.of()).rank());
        assertEquals(2.0, unit.reshape(Shape.of()).getDouble());

        // a scalar built through StrideLayout.of has a ScalarStrideLayout
        DArray<Double> scalar = DArrays.scalar(3.0);
        assertArrayEquals(new int[] {1}, scalar.reshape(Shape.of(1)).shape().dims());
        assertArrayEquals(new int[] {1, 1}, scalar.reshape(Shape.of(1, 1)).shape().dims());
        assertEquals(3.0, scalar.reshape(Shape.of(1, 1)).getDouble(0, 0));
        // squeeze with explicit axes is a no-op rather than an error
        assertEquals(scalar.layout(), scalar.squeeze().layout());
    }

    @Test
    void stretchAcceptsEveryAxisOfTheResult() {
        DArray<Double> v = DArrays.seq(Shape.of(5));
        assertArrayEquals(new int[] {1, 1, 5, 1}, v.stretch(0, 1, 3).shape().dims());
        assertArrayEquals(new int[] {5, 1}, v.stretch(1).shape().dims());
        assertThrows(IndexOutOfBoundsException.class, () -> v.stretch(2));

        DArray<Double> m = DArrays.seq(Shape.of(2, 3)).t_();
        assertArrayEquals(new int[] {1, 3, 2}, m.stretch(0).shape().dims());
        assertArrayEquals(new int[] {3, 2, 1}, m.stretch(2).shape().dims());
        assertThrows(IndexOutOfBoundsException.class, () -> m.stretch(3));
    }

    @Test
    void layoutsDescribingTheSameViewAreEqual() {
        StrideLayout dense = StrideLayout.ofDense(Shape.of(2, 3), 0, Order.C);
        StrideLayout specialised = StrideLayout.of(Shape.of(2, 3), 0, new int[] {3, 1});
        assertEquals(dense, specialised);
        assertEquals(specialised, dense);
        assertEquals(dense.hashCode(), specialised.hashCode());

        assertEquals(StrideLayout.ofDense(Shape.of(4), 0, Order.C), StrideLayout.of(Shape.of(4), 0, new int[] {1}));
        assertEquals(StrideLayout.ofDense(Shape.of(), 0, Order.C), StrideLayout.of(Shape.of(), 0, new int[0]));
        // a squeeze which removes nothing keeps the layout equal to itself
        assertEquals(specialised, specialised.squeeze(0));
        assertFalse(dense.equals(StrideLayout.of(Shape.of(2, 3), 1, new int[] {3, 1})));
        assertFalse(dense.equals(StrideLayout.of(Shape.of(3, 2), 0, new int[] {3, 1})));
    }

    @Test
    void stridesAreLentNotCopied() {
        for (StrideLayout layout : bothFactories(Shape.of(2, 3), Order.C)) {
            String name = layout.getClass().getSimpleName();
            // the getter lends the layout's own array instead of copying it, so reading a layout allocates nothing
            // and two calls answer with the same instance. The caller must not modify it, as strides() documents
            assertSame(layout.strides(), layout.strides(), name);
            assertArrayEquals(new int[] {3, 1}, layout.strides(), name);
        }
        // rank 0 and rank 1 keep no stride array to lend, so they are free to build one: an empty array has nothing
        // a caller could modify, and a vector layout holds its single stride as a field
        assertArrayEquals(new int[0], StrideLayout.of(Shape.of(), 0, new int[0]).strides());
        assertArrayEquals(new int[] {2}, StrideLayout.of(Shape.of(5), 0, new int[] {2}).strides());
    }

    @Test
    void derivingALayoutLeavesItsSourceUnchanged() {
        // Every operation below needs a working buffer of dims or strides, and under the lending contract that
        // buffer has to be a copy: taking the lent array directly would redimension the very layout being derived
        // from, silently, since nothing validates a layout after construction. This is the regression test for
        // dims() and strides() no longer copying, and it is why each such site copies explicitly.
        for (int factory = 0; factory < 2; factory++) {
            // rebuilt per iteration: the two factories are handed the same stride array, so a corruption in one
            // round would otherwise become the next round's baseline and go unnoticed
            StrideLayout source = bothFactories(Shape.of(2, 3), Order.C).get(factory);
            String name = source.getClass().getSimpleName();
            int[] dims = Ints.copy(source.dims());
            int[] strides = Ints.copy(source.strides());

            for (Order order : new Order[] {Order.C, Order.F, Order.S}) {
                source.computeFortranLayout(order, true);
                source.computeFortranLayout(order, false);
            }
            source.revert();
            source.moveAxis(0, 1);
            source.swapAxis(0, 1);
            source.narrow(0, true, 0, 1);
            source.narrow(1, false, 1, 2);
            source.narrowAll(true, new int[] {0, 0}, new int[] {1, 2});
            source.permute(1, 0);
            source.squeeze();
            source.squeeze(0);
            source.stretch(0);
            source.attemptReshape(Shape.of(6), Order.C);
            source.attemptReshape(Shape.of(3, 2), Order.F);
            source.ptrIterator(Order.C);
            source.ptrIterator(Order.F);

            assertArrayEquals(dims, source.dims(), name);
            assertArrayEquals(strides, source.strides(), name);
        }

        // expand needs a unit axis, so it gets its own round, at every rank which has a specialised layout
        for (Shape shape : new Shape[] {Shape.of(1), Shape.of(1, 3), Shape.of(1, 3, 4)}) {
            for (StrideLayout source : bothFactories(shape, Order.C)) {
                String name = source.getClass().getSimpleName() + " " + shape;
                int[] dims = Ints.copy(source.dims());
                int[] strides = Ints.copy(source.strides());

                source.expand(0, 4);

                assertArrayEquals(dims, source.dims(), name);
                assertArrayEquals(strides, source.strides(), name);
            }
        }
    }

    @Test
    void vectorLayoutReportsAUnitStrideAsDense() {
        assertTrue(StrideLayout.of(Shape.of(5), 0, new int[] {1}).isDense());
        assertFalse(StrideLayout.of(Shape.of(5), 0, new int[] {2}).isDense());
        // both factories agree
        assertEquals(StrideLayout.ofDense(Shape.of(5), 0, Order.C).isDense(),
                StrideLayout.of(Shape.of(5), 0, new int[] {1}).isDense());
    }

    @Test
    void indexIteratorHandlesARankZeroShape() {
        for (Order order : new Order[] {Order.C, Order.F}) {
            IndexIterator it = new IndexIterator(Shape.of(), order);
            assertTrue(it.hasNext());
            assertArrayEquals(new int[0], it.next());
            assertFalse(it.hasNext());
        }
    }

    @Test
    void sortingAnEmptyIndexSetIsANoOp() {
        DArray<Double> x = DArrays.stride(3, 1, 2);
        x.externalSort(new int[0], true);
        assertArrayEquals(new double[] {3, 1, 2}, x.toDoubleArray());
        int[] one = new int[] {1};
        x.externalSort(one, true);
        assertArrayEquals(new int[] {1}, one);
    }

    @Test
    void concatenationRejectsArraysOfDifferentRanks() {
        DArray<Double> a = DArrays.seq(Shape.of(2, 3));
        DArray<Double> b = DArrays.seq(Shape.of(2, 3, 4));
        var ex = assertThrows(IllegalArgumentException.class, () -> dm.cat(DType.DOUBLE, 0, List.of(a, b)));
        assertEquals("DArrays are not valid for concatenation", ex.getMessage());
        assertThrows(IllegalArgumentException.class, () -> dm.cat(DType.DOUBLE, 0, List.of(b, a)));
        assertThrows(IllegalArgumentException.class, () -> dm.cat(DType.DOUBLE, 2, List.of(a, a)));

        // the valid case still concatenates every element
        DArray<Double> cat = dm.cat(DType.DOUBLE, 0, List.of(a, a));
        assertArrayEquals(new int[] {4, 3}, cat.shape().dims());
        assertEquals(2 * a.sum(), cat.sum());
    }

    @Test
    void asArrayViewsDoNotLeakTheWholeStorage() {
        DArray<Double> row = DArrays.seq(Shape.of(2, 5)).selsq(0, 0);
        assertEquals(5, row.asDoubleArray().length);
        assertArrayEquals(new double[] {0, 1, 2, 3, 4}, row.asDoubleArray());

        DArray<Double> head = DArrays.seq(Shape.of(10)).narrow(0, 0, 3);
        assertEquals(3, head.asDoubleArray().length);
        assertEquals(3, head.dv().size());
        assertArrayEquals(new double[] {0, 1, 2}, head.dv().darray_().toDoubleArray());

        // a darray covering its storage exactly still shares the array
        double[] backing = new double[] {1, 2, 3};
        DArray<Double> whole = dm.stride(DType.DOUBLE, Shape.of(3), Order.C, backing);
        assertEquals(3, whole.asDoubleArray().length);
        assertEquals(3, whole.dv().size());
    }

    @Test
    void gatherComparesShapesByValue() {
        DArray<Double> src = DArrays.seq(Shape.of(2, 3));
        DArray<Integer> index = dm.stride(DType.INTEGER, Shape.of(2, 2), Order.C, 0, 2, 1, 0);
        DArray<Double> dst = DArrays.zeros(Shape.of(2, 2));
        assertNotNull(dst.gather_(1, index, src));
        assertEquals(src.getDouble(0, 0), dst.getDouble(0, 0));
        assertEquals(src.getDouble(0, 2), dst.getDouble(0, 1));
        assertEquals(src.getDouble(1, 1), dst.getDouble(1, 0));
        assertEquals(src.getDouble(1, 0), dst.getDouble(1, 1));

        // gather() itself is unchanged
        assertTrue(dst.deepEquals(src.gather(1, index)));

        assertThrows(IllegalArgumentException.class, () -> DArrays.zeros(Shape.of(2, 3)).gather_(1, index, src));
        assertThrows(IllegalArgumentException.class, () -> dst.gather_(5, index, src));
    }

    @Test
    void scatterRequiresMatchingIndexAndInputShapes() {
        DArray<Double> dst = DArrays.zeros(Shape.of(2, 3));
        DArray<Integer> index = dm.stride(DType.INTEGER, Shape.of(2, 2), Order.C, 0, 2, 1, 0);
        DArray<Double> input = DArrays.seq(Shape.of(2, 3));
        assertThrows(IllegalArgumentException.class, () -> dst.scatter_(1, index, input));

        DArray<Double> matching = DArrays.seq(Shape.of(2, 2)).add(1.0);
        assertNotNull(dst.scatter_(1, index, matching));
        assertEquals(1.0, dst.getDouble(0, 0));
        assertEquals(2.0, dst.getDouble(0, 2));
        assertEquals(4.0, dst.getDouble(1, 0));
        assertEquals(3.0, dst.getDouble(1, 1));
    }

    @Test
    void ravelAndToArrayAcceptTheAutomaticOrder() {
        DArray<Double> m = DArrays.seq(Shape.of(2, 3));
        assertArrayEquals(new double[] {0, 1, 2, 3, 4, 5}, m.ravel(Order.A).toDoubleArray());
        assertArrayEquals(new double[] {0, 1, 2, 3, 4, 5}, m.toDoubleArray(Order.A));
        DArray<Double> t = m.t();
        assertEquals(6, t.ravel(Order.A).size());
        assertEquals(6, t.toDoubleArray(Order.A).length);
        DArray<Double> view = DArrays.seq(Shape.of(4, 4)).narrow(1, true, 0, 2);
        assertEquals(8, view.ravel(Order.A).size());
    }

    /**
     * An aliased layout is one which maps two distinct index tuples onto the same storage position, which happens
     * exactly when an axis longer than one element has a zero stride. The distinction from an axis of a single element
     * with a zero stride is load bearing: {@code stretch} produces those, and in-place operations must keep accepting
     * them while rejecting genuinely expanded layouts.
     */
    @Test
    void aliasedElementsAreDetectedOnlyForRepeatedAxes() {
        for (StrideLayout layout : bothFactories(Shape.of(2, 3, 4), Order.C)) {
            String name = layout.getClass().getSimpleName();
            assertFalse(layout.hasAliasedElements(), name);
            // a dense layout stays unaliased under every view which only selects elements
            assertFalse(layout.revert().hasAliasedElements(), name);
            assertFalse(layout.narrow(1, true, 0, 2).hasAliasedElements(), name);
            assertFalse(layout.squeeze().hasAliasedElements(), name);
        }

        for (StrideLayout layout : bothFactories(Shape.of(2, 1, 4), Order.C)) {
            String name = layout.getClass().getSimpleName();
            // the unit axis of a dense layout carries a real stride, so it is not aliased either way
            assertFalse(layout.hasAliasedElements(), name);
            assertTrue(layout.expand(1, 5).hasAliasedElements(), name);
            // expanding to a single element repeats nothing
            assertFalse(layout.expand(1, 1).hasAliasedElements(), name);
        }

        // stretch inserts axes of a single element with a zero stride: addressed once, repeating nothing
        StrideLayout stretched = StrideLayout.ofDense(Shape.of(4), 0, Order.C).stretch(0, 2);
        assertArrayEquals(new int[] {1, 4, 1}, stretched.dims());
        assertArrayEquals(new int[] {0, 1, 0}, stretched.strides());
        assertFalse(stretched.hasAliasedElements());
        // until one of them is expanded
        assertTrue(stretched.expand(0, 3).hasAliasedElements());

        // a rank 0 layout has no axis at all
        assertFalse(StrideLayout.ofDense(Shape.of(), 0, Order.C).hasAliasedElements());
    }

    @Test
    void narrowRejectsBoundsOutsideTheAxis() {
        for (StrideLayout layout : bothFactories(Shape.of(3, 4), Order.C)) {
            String name = layout.getClass().getSimpleName();

            // past the end of the axis
            assertTrue(assertThrows(IllegalArgumentException.class, () -> layout.narrow(0, true, 0, 4), name)
                    .getMessage().contains("out of range for axis 0 of dimension 3"), name);
            assertThrows(IllegalArgumentException.class, () -> layout.narrow(1, true, 2, 5), name);
            // a negative start
            assertThrows(IllegalArgumentException.class, () -> layout.narrow(0, true, -1, 2), name);
            // an empty or inverted range
            assertThrows(IllegalArgumentException.class, () -> layout.narrow(0, true, 1, 1), name);
            assertThrows(IllegalArgumentException.class, () -> layout.narrow(0, true, 2, 1), name);

            // the whole axis and a proper sub-range are accepted
            assertArrayEquals(new int[] {3, 4}, layout.narrow(0, true, 0, 3).dims(), name);
            assertArrayEquals(new int[] {2, 4}, layout.narrow(0, true, 1, 3).dims(), name);
        }

        // rank 1 goes through the vector implementation, which has its own narrow
        for (StrideLayout layout : bothFactories(Shape.of(5), Order.C)) {
            String name = layout.getClass().getSimpleName();
            assertThrows(IllegalArgumentException.class, () -> layout.narrow(0, true, 0, 6), name);
            assertThrows(IllegalArgumentException.class, () -> layout.narrow(0, true, -2, 3), name);
            assertThrows(IllegalArgumentException.class, () -> layout.narrow(0, true, 3, 3), name);
            assertArrayEquals(new int[] {5}, layout.narrow(0, true, 0, 5).dims(), name);
        }

        // rank 3 goes through ArrayStrideLayout under both factories
        for (StrideLayout layout : bothFactories(Shape.of(2, 3, 4), Order.C)) {
            String name = layout.getClass().getSimpleName();
            assertThrows(IllegalArgumentException.class, () -> layout.narrow(2, true, 0, 5), name);
            assertArrayEquals(new int[] {2, 3, 2}, layout.narrow(2, true, 1, 3).dims(), name);
        }
    }

    @Test
    void narrowAllRejectsBoundsOutsideAnyAxis() {
        for (StrideLayout layout : bothFactories(Shape.of(3, 4), Order.C)) {
            String name = layout.getClass().getSimpleName();

            // the second axis is the one out of range
            assertTrue(assertThrows(IllegalArgumentException.class,
                    () -> layout.narrowAll(true, new int[] {0, 0}, new int[] {3, 5}), name)
                    .getMessage().contains("out of range for axis 1 of dimension 4"), name);
            assertThrows(IllegalArgumentException.class,
                    () -> layout.narrowAll(true, new int[] {-1, 0}, new int[] {2, 2}), name);
            assertThrows(IllegalArgumentException.class,
                    () -> layout.narrowAll(true, new int[] {1, 2}, new int[] {1, 3}), name);

            assertArrayEquals(new int[] {2, 2},
                    layout.narrowAll(true, new int[] {1, 2}, new int[] {3, 4}).dims(), name);
        }
    }

    @Test
    void narrowOfANarrowCannotEscapeIntoTheParent() {
        // the bound a view must respect is its own dimension, not the storage of the array it was cut from, which is
        // what makes this silent rather than an out of bounds failure when it is not validated
        DArray<Double> parent = DArrays.seq(Shape.of(6));
        DArray<Double> view = parent.narrow(0, true, 0, 2);
        assertEquals(2, view.dim(0));

        assertThrows(IllegalArgumentException.class, () -> view.narrow(0, true, 0, 5));
        assertThrows(IllegalArgumentException.class, () -> view.narrow(0, true, 2, 4));
        assertThrows(IllegalArgumentException.class, () -> view.narrowAll(true, new int[] {2}, new int[] {4}));

        // the parent is untouched, since nothing was ever written through an escaped view
        for (int i = 0; i < 6; i++) {
            assertEquals(i, parent.getDouble(i), 0.0);
        }
        // and a legitimate narrow of the view still works
        assertEquals(1.0, view.narrow(0, true, 1, 2).getDouble(0), 0.0);
    }
}
