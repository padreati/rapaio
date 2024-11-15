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

package rapaio.math.narrays.layout;

import java.util.Arrays;

import rapaio.math.narrays.Order;
import rapaio.math.narrays.Shape;

public abstract class AbstractStrideLayout implements StrideLayout {

    @Override
    public StrideLayout attemptReshape(Shape shape, Order askOrder) {
        if (askOrder == Order.S) {
            throw new IllegalArgumentException("Requested order must be Order.C or Order.F.");
        }

        int newRank = shape.rank();
        int[] newstrides = new int[newRank];

        int[] olddims = Arrays.copyOf(dims(), dims().length);
        int[] oldstrides = Arrays.copyOf(strides(), strides().length);
        int last_stride;

        int oldRank = 0;
        /*
         * Remove axes with dimension 1 from the old array. They have no effect
         * but would need special cases since their strides do not matter.
         */
        for (int oi = 0; oi < dims().length; oi++) {
            if (dim(oi) != 1) {
                olddims[oldRank] = dim(oi);
                oldstrides[oldRank] = stride(oi);
                oldRank++;
            }
        }

        /* oi to oj and ni to nj give the axis ranges currently worked with */
        int oi = 0, oj = 1, ni = 0, nj = 1;
        while (ni < newRank && oi < oldRank) {
            int np = shape.dim(ni);
            int op = olddims[oi];

            while (np != op) {
                if (np < op) {
                    /* Misses trailing 1s, these are handled later */
                    np *= shape.dim(nj++);
                } else {
                    op *= olddims[oj++];
                }
            }

            /* Check whether the original axes can be combined */
            for (int ok = oi; ok < oj - 1; ok++) {
                if (askOrder == Order.F) {
                    if (oldstrides[ok + 1] != olddims[ok] * oldstrides[ok]) {
                        /* not contiguous enough */
                        return null;
                    }
                } else {
                    /* C order */
                    if (oldstrides[ok] != olddims[ok + 1] * oldstrides[ok + 1]) {
                        /* not contiguous enough */
                        return null;
                    }
                }
            }

            /* Calculate new strides for all axes currently worked with */
            if (askOrder == Order.F) {
                newstrides[ni] = oldstrides[oi];
                for (int nk = ni + 1; nk < nj; nk++) {
                    newstrides[nk] = newstrides[nk - 1] * shape.dim(nk - 1);
                }
            } else {
                /* C order */
                newstrides[nj - 1] = oldstrides[oj - 1];
                for (int nk = nj - 1; nk > ni; nk--) {
                    newstrides[nk - 1] = newstrides[nk] * shape.dim(nk);
                }
            }
            ni = nj++;
            oi = oj++;
        }

        /*
         * Set strides corresponding to trailing 1s of the new shape.
         */
        if (ni >= 1) {
            last_stride = newstrides[ni - 1];
        } else {
            last_stride = 1; //PyArray_ITEMSIZE(self);
        }
        if (Order.F == askOrder) {
            last_stride *= shape.dim(ni - 1);
        }
        for (int nk = ni; nk < newRank; nk++) {
            newstrides[nk] = last_stride;
        }

        return StrideLayout.of(shape, offset(), newstrides);
    }
}
