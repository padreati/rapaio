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

package rapaio.darray.storage.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.DArrays;
import rapaio.darray.DType;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.darray.Storage;
import rapaio.data.VarDouble;
import rapaio.data.VarFloat;
import rapaio.data.VarInt;

/**
 * The storages which wrap a variable rather than owning an array have to honour the same contract as the array backed
 * ones. They used to delegate straight to the variable, which indexes its own array without checking the row against
 * its size; since that array is sized by the variable's growth capacity, a position between the size and the capacity
 * read stale slack, or wrote into slack which the next value added to the variable overwrote.
 */
public class VarStorageBoundsTest {

    private static final DArrayManager dm = DArrayManager.base();

    @Test
    void aPositionPastTheSizeIsRejectedJustAsAnArrayStorageRejectsIt() {
        // the variable holds four values in an array grown beyond them, so positions 4.. are inside the capacity
        VarDouble vd = VarDouble.wrap(1, 2, 3);
        vd.addDouble(4);
        Storage wrapper = new VarDoubleStorage(vd);
        assertEquals(4, wrapper.size());

        assertEquals(4.0, wrapper.getDouble(3));
        assertThrows(IndexOutOfBoundsException.class, () -> wrapper.getDouble(4));
        assertThrows(IndexOutOfBoundsException.class, () -> wrapper.getDouble(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> wrapper.setDouble(4, 9));
        assertThrows(IndexOutOfBoundsException.class, () -> wrapper.incDouble(4, 9));

        // an array backed storage of the same size rejects the same position, which is the behaviour being matched
        Storage array = DArrays.seq(Shape.of(4)).storage();
        assertEquals(4, array.size());
        assertThrows(RuntimeException.class, () -> array.getDouble(4));

        VarFloat vf = VarFloat.wrap(1f, 2f, 3f);
        vf.addFloat(4f);
        Storage floatWrapper = new VarFloatStorage(vf);
        assertEquals(4, floatWrapper.size());
        assertEquals(4f, floatWrapper.getFloat(3));
        assertThrows(IndexOutOfBoundsException.class, () -> floatWrapper.getFloat(4));
        assertThrows(IndexOutOfBoundsException.class, () -> floatWrapper.setFloat(4, 9f));
        assertThrows(IndexOutOfBoundsException.class, () -> floatWrapper.incFloat(4, 9f));
        // the cross-type accessors route through the same check
        assertThrows(IndexOutOfBoundsException.class, () -> floatWrapper.getDouble(4));
        assertThrows(IndexOutOfBoundsException.class, () -> floatWrapper.setDouble(4, 9));
    }

    /**
     * A darray built over a variable snapshots the variable's size as its shape. If the variable later shrinks, the
     * darray keeps the old shape, and reading it used to hand back the values left in the slack rather than failing.
     */
    @Test
    void aViewOverAVariableWhichShrankFailsRatherThanReadingSlack() {
        VarFloat vf = VarFloat.wrap(1f, 2f, 3f);
        vf.addFloat(4f);
        DArray<?> view = vf.darray_(DType.FLOAT);
        assertEquals(Shape.of(4), view.shape());
        assertEquals(4.0, view.getDouble(3));

        vf.removeRow(0);
        assertEquals(3, vf.size());
        assertEquals(Shape.of(4), view.shape());
        // position 3 is now outside the variable: it used to return the 4 left in the slack
        assertThrows(IndexOutOfBoundsException.class, () -> view.getDouble(3));
        assertThrows(IndexOutOfBoundsException.class, view::sum);
        // and the part still inside the variable reads the shifted values
        assertEquals(2.0, view.getDouble(0));
    }

    @Test
    void fillValidatesItsRangeTheSameWayTheArrayStoragesDo() {
        Storage wrapper = new VarDoubleStorage(VarDouble.wrap(1, 2, 3, 4));
        Storage array = DArrays.seq(Shape.of(4)).storage();

        // a negative length is rejected by Arrays.fill, so the wrapper rejects it too instead of doing nothing
        assertThrows(IllegalArgumentException.class, () -> wrapper.fill(9.0, 2, -1));
        assertThrows(IllegalArgumentException.class, () -> array.fill(9.0, 2, -1));

        assertThrows(IndexOutOfBoundsException.class, () -> wrapper.fill(9.0, 2, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> wrapper.fill(9.0, -1, 2));

        // the valid range still fills
        wrapper.fill(9.0, 1, 2);
        assertEquals(1.0, wrapper.getDouble(0));
        assertEquals(9.0, wrapper.getDouble(1));
        assertEquals(9.0, wrapper.getDouble(2));
        assertEquals(4.0, wrapper.getDouble(3));
        // an empty range is a no-op rather than an error, as Arrays.fill has it
        wrapper.fill(0.0, 4, 0);
    }

    /**
     * A variable of another type viewed as a floating point darray writes through the variable's own conversion, which
     * rounds half to even rather than truncating. This is the documented behaviour, pinned here because it is the one
     * narrowing in darray which is not a plain cast.
     */
    @Test
    void aVariableOfAnotherTypeRoundsOnWriteAsDocumented() {
        VarInt vi = VarInt.wrap(0, 0, 0, 0);
        DArray<?> view = vi.darray_(DType.DOUBLE);
        assertTrue(view.dt() == DType.DOUBLE);

        view.ptrSetDouble(0, 2.5);
        view.ptrSetDouble(1, 3.5);
        view.ptrSetDouble(2, -0.5);
        view.ptrSetDouble(3, 2.4);

        // half to even, where a truncating cast would give 2, 3, 0, 2
        assertEquals(2.0, view.getDouble(0));
        assertEquals(4.0, view.getDouble(1));
        assertEquals(0.0, view.getDouble(2));
        assertEquals(2.0, view.getDouble(3));
    }

    /**
     * An increment on an integral array narrows the increment rather than the sum, which is the documented conversion
     * order: the fractional part is lost before the addition, so repeated small increments never accumulate.
     */
    @Test
    void anIncrementOnAnIntegralArrayNarrowsTheIncrementAsDocumented() {
        DArray<Integer> i = dm.full(DType.INTEGER, Shape.of(1), 5);
        for (int k = 0; k < 10; k++) {
            i.ptrIncDouble(0, 0.9);
        }
        assertEquals(5.0, i.ptrGetDouble(0));

        DArray<Double> d = dm.full(DType.DOUBLE, Shape.of(1), 5.0);
        d.ptrIncDouble(0, 0.9);
        assertEquals(5.9, d.ptrGetDouble(0), 1e-12);

        // a whole increment does apply on the integral array
        i.ptrIncDouble(0, 2.0);
        assertEquals(7.0, i.ptrGetDouble(0));
    }

    /**
     * A scalar operand is converted to the array's data type before the operation, which is the documented conversion
     * order and is why a fractional scalar on an integral array can yield zeros.
     */
    @Test
    void aScalarOperandIsNarrowedBeforeTheOperationAsDocumented() {
        DArray<Integer> x = dm.seq(DType.INTEGER, Shape.of(5));
        assertEquals(0, x.copy().mul(0.5).sum().intValue());
        // 2.5 becomes 2, so this is a division by two
        assertEquals(x.copy().div(2).sum().intValue(), x.copy().div(2.5).sum().intValue());

        DArray<Byte> b = dm.stride(DType.BYTE, Shape.of(3), Order.C, new byte[] {1, 2, 3});
        // 300 wraps to 44 in a byte
        assertEquals(45, b.copy().add(300).getDouble(0));
    }
}
