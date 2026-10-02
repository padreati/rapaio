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

import java.util.Arrays;

import rapaio.darray.Layout;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.darray.iterators.DensePointerIterator;
import rapaio.darray.iterators.PointerIterator;
import rapaio.darray.iterators.StridePointerIterator;
import rapaio.io.atom.AtomSerialization;
import rapaio.io.atom.LoadAtomHandler;
import rapaio.io.atom.SaveAtomHandler;
import rapaio.util.collection.Ints;

public interface StrideLayout extends Layout {

    static StrideLayout of(int[] dims, int offset, int[] strides) {
        return of(Shape.of(dims), offset, strides);
    }

    static StrideLayout of(Shape shape, int offset, int[] strides) {
        if (shape.rank() != strides.length) {
            throw new IllegalArgumentException(
                    "Dimensions and strides must have same length (dim size: " + shape.rank() + ", stride size: " + strides.length + ".");
        }
        return switch (shape.rank()) {
            case 0 -> new ScalarStrideLayout(offset);
            case 1 -> new VectorStrideLayout(shape, offset, strides);
            case 2 -> new MatrixStrideLayout(shape, offset, strides);
            default -> new ArrayStrideLayout(shape, offset, strides);
        };
    }

    static StrideLayout ofDense(Shape shape, int offset, Order order) {
        // a rank 0 shape holds a single element and has no stride, so it always gets the canonical scalar layout;
        // otherwise a degenerate ArrayStrideLayout of rank 0 would compete with it as a second representation
        if (shape.rank() == 0) {
            return new ScalarStrideLayout(offset);
        }
        order = Order.autoFC(order);
        int[] strides = switch (order) {
            case C -> {
                int[] rowStrides = Ints.fill(shape.rank(), 1);
                for (int i = rowStrides.length - 2; i >= 0; i--) {
                    rowStrides[i] = shape.dim(i + 1) * rowStrides[i + 1];
                }
                yield rowStrides;
            }
            case F -> {
                int[] colStrides = Ints.fill(shape.rank(), 1);
                for (int i = 1; i < colStrides.length; i++) {
                    colStrides[i] = shape.dim(i - 1) * colStrides[i - 1];
                }
                yield colStrides;
            }
            default -> throw new IllegalArgumentException("Order type is invalid.");
        };
        return new ArrayStrideLayout(shape, offset, strides);
    }

    int offset();

    /**
     * @return a copy of the stride array, owned by the caller; mutating it does not affect the layout
     */
    int[] strides();

    /**
     * Implements {@link rapaio.darray.Layout#hasAliasedElements()} for every stride layout: an index is repeated
     * exactly when some axis is longer than one element and has a stride of zero, so that advancing along it does not
     * move the storage position.
     * <p>
     * An axis of a single element with a zero stride is not aliasing: it addresses one position and repeats nothing.
     * {@link #stretch(int...)} creates exactly such axes, and the batched matrix products and the neural network
     * gradients rely on being able to write through a stretched layout.
     *
     * @return true if two distinct index tuples of this layout share a storage position
     */
    @Override
    default boolean hasAliasedElements() {
        for (int i = 0; i < rank(); i++) {
            if (stride(i) == 0 && dim(i) > 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Structural equality for stride layouts: two layouts are equal when they describe the same view, which means the
     * same shape, the same offset and the same strides. Runtime classes are deliberately not compared, since the same
     * view is represented by a rank-specialised implementation when it is built by {@link #of} and by
     * {@link ArrayStrideLayout} when it is built by {@link #ofDense}.
     * <p>
     * Every implementation must route {@code equals} here, so that the relation stays symmetric across them.
     *
     * @param layout layout on whose behalf the comparison is made
     * @param o      object to compare with
     * @return true if the other object is a stride layout describing the same view
     */
    static boolean structuralEquals(StrideLayout layout, Object o) {
        if (layout == o) {
            return true;
        }
        if (!(o instanceof StrideLayout other)) {
            return false;
        }
        if (layout.offset() != other.offset() || !layout.shape().equals(other.shape())) {
            return false;
        }
        // equal shapes imply equal ranks, so the strides can be compared position by position without allocating
        for (int i = 0; i < layout.rank(); i++) {
            if (layout.stride(i) != other.stride(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Hash code consistent with {@link #structuralEquals(StrideLayout, Object)}: it is derived from the shape, the
     * offset and the strides, so two implementations describing the same view hash alike.
     *
     * @param layout layout to hash
     * @return hash code of the described view
     */
    static int structuralHashCode(StrideLayout layout) {
        int result = 31 * layout.shape().hashCode() + layout.offset();
        for (int i = 0; i < layout.rank(); i++) {
            result = 31 * result + layout.stride(i);
        }
        return result;
    }

    int stride(int i);

    StrideLayout squeeze();

    StrideLayout squeeze(int... axes);

    StrideLayout stretch(int... axes);

    StrideLayout expand(int axis, int size);

    StrideLayout revert();

    StrideLayout moveAxis(int src, int dst);

    StrideLayout swapAxis(int src, int dst);

    default StrideLayout narrow(int axis, int start, int end) {
        return narrow(axis, true, start, end);
    }

    StrideLayout narrow(int axis, boolean keepDim, int start, int end);

    default StrideLayout narrowAll(int[] starts, int[] ends) {
        return narrowAll(true, starts, ends);
    }

    /**
     * Creates a view with every axis truncated to {@code [starts[i], ends[i])}. When {@code keepDim} is false, the
     * axes which this request reduced to a single element are dropped, as {@link #narrowedUnitAxes} defines; an axis
     * which was already unitary is left alone, since the request did not narrow it.
     *
     * @param keepDim keep every axis, even the ones truncated to a single element
     * @param starts  start index per axis, inclusive
     * @param ends    end index per axis, exclusive
     * @return a view with truncated axes
     */
    StrideLayout narrowAll(boolean keepDim, int[] starts, int[] ends);

    /**
     * The axes a {@code narrowAll} request reduces to a single element, which are the ones dropped when it is asked
     * not to keep dimensions. An axis whose dimension is already 1 is not reported: the request did not narrow it, so
     * it survives, unlike in {@link #narrow(int, boolean, int, int)} where the caller names the axis explicitly.
     * <p>
     * Callers must validate that {@code starts} and {@code ends} have one entry per axis before calling this.
     *
     * @param layout layout the request is applied to, before it is narrowed
     * @param starts start index per axis, inclusive
     * @param ends   end index per axis, exclusive
     * @return the axes to squeeze, in increasing order, empty when the request narrows nothing down to a single element
     */
    /**
     * Normalizes an axis for the operations which act along an axis rather than creating a view of it, which are the
     * {@code *1d} reduction family and {@code sort_}. Those accept a negative axis counted back from the last one,
     * and this resolves it and rejects an axis which is out of range either way.
     * <p>
     * The operations which create a view of a named axis do not share this convention: {@code narrow}, {@code sel},
     * {@code rem}, {@code squeeze} and the splitting operations built on them require a non-negative axis. See the
     * axis conventions in {@code rapaio.darray} package documentation.
     *
     * @param layout    layout the operation is applied to
     * @param axis      axis as the caller gave it, possibly negative
     * @param operation name of the operation, used in the message
     * @return the axis counted from the first one
     */
    static int normalizeAxis(StrideLayout layout, int axis, String operation) {
        int resolved = axis < 0 ? axis + layout.rank() : axis;
        if (resolved < 0 || resolved >= layout.rank()) {
            throw new IllegalArgumentException(String.format(
                    "Axis %d is out of bounds for operation %s on a darray of rank %d.", axis, operation, layout.rank()));
        }
        return resolved;
    }

    /**
     * Rejects a narrow request whose bounds do not describe a non-empty range inside the axis, which means
     * {@code 0 <= start < end <= dim(axis)}.
     * <p>
     * Validating here is what keeps a narrow of a narrow honest. Without it the resulting layout simply records
     * {@code end - start} as the new dimension, and when the layout is a view the out-of-range positions still fall
     * inside the storage of the parent, so both reading and writing silently reach elements the view does not own.
     * Every path which truncates an axis goes through {@code narrow} or {@code narrowAll}, including {@code split},
     * {@code splitAll}, {@code chunk}, {@code chunkAll}, {@code unbind} and the autograd narrow node.
     *
     * @param layout layout the request is applied to, before it is narrowed
     * @param axis   axis being truncated, already validated to be in bounds
     * @param start  start index, inclusive
     * @param end    end index, exclusive
     */
    static void validateNarrowBounds(StrideLayout layout, int axis, int start, int end) {
        if (start < 0 || end > layout.dim(axis) || start >= end) {
            throw new IllegalArgumentException(String.format(
                    "Narrow bounds [%d,%d) are out of range for axis %d of dimension %d.",
                    start, end, axis, layout.dim(axis)));
        }
    }

    /**
     * Rejects a {@code narrowAll} request whose bounds do not describe a non-empty range inside each axis, as
     * {@link #validateNarrowBounds} defines for a single axis.
     *
     * @param layout layout the request is applied to, before it is narrowed
     * @param starts start index per axis, inclusive
     * @param ends   end index per axis, exclusive
     */
    static void validateNarrowAllBounds(StrideLayout layout, int[] starts, int[] ends) {
        for (int axis = 0; axis < layout.rank(); axis++) {
            validateNarrowBounds(layout, axis, starts[axis], ends[axis]);
        }
    }

    static int[] narrowedUnitAxes(StrideLayout layout, int[] starts, int[] ends) {
        int[] axes = new int[layout.rank()];
        int len = 0;
        for (int i = 0; i < layout.rank(); i++) {
            if (ends[i] - starts[i] == 1 && layout.dim(i) > 1) {
                axes[len++] = i;
            }
        }
        return Arrays.copyOf(axes, len);
    }

    StrideLayout permute(int... dims);

    StrideLayout computeFortranLayout(Order askOrder, boolean compact);

    int[] narrowStrides(int axis);

    /**
     * Attempts to compute a stride layout for reshape, if possible.
     * <p>
     * The shape should be compatible (have the same size as original), but this validation is not checked, it is left in the scope
     * of caller. This is to distinguish between cases when a reshape is invalid or requires copy.
     * <p>
     * If the attempt fails, which means a new copy of the data is required, the returned stride array is null.
     * If no copy is needed, returns a new stride array prepared for use on the new tensor.
     * <p>
     * The "askOrder" argument describes how the array should be viewed during the reshape, not how it is stored in memory.
     * If a copy is needed, this will be also the order of storage for the new copy.
     * <p>
     * If some output dimensions have length 1, the strides assigned to them are arbitrary. In the current implementation, they are the
     * stride of the next-fastest index.
     */
    StrideLayout attemptReshape(Shape shape, Order askOrder);

    class Serialization extends AtomSerialization<StrideLayout> {

        @Override
        public LoadAtomHandler<StrideLayout> loadAtomHandler() {
            return (in, _) -> {
                Shape shape = in.loadAtom(Shape.class);
                int offset = in.readInt();
                int[] strides = in.readInts();
                return StrideLayout.of(shape, offset, strides);
            };
        }

        @Override
        public SaveAtomHandler<StrideLayout> saveAtomHandler() {
            return (atom, out) -> {
                if(!(atom instanceof StrideLayout layout)) {
                    throw new IllegalArgumentException("Can't serialize scalar stride layout from instance: " + atom);
                }
                out.saveAtom(layout.shape());
                out.saveInt(layout.offset());
                out.saveInts(layout.strides());
            };
        }
    }

    default PointerIterator ptrIterator(Order askOrder) {
        if (isCOrdered() && askOrder != Order.F) {
            return new DensePointerIterator(shape(), offset(), stride(-1));
        }
        if (isFOrdered() && askOrder != Order.C) {
            return new DensePointerIterator(shape(), offset(), stride(0));
        }
        return new StridePointerIterator(this, askOrder);
    }

}
