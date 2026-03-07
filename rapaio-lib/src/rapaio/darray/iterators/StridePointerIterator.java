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

package rapaio.darray.iterators;

import java.util.NoSuchElementException;

import rapaio.darray.Order;
import rapaio.darray.layout.StrideLayout;

public final class StridePointerIterator implements PointerIterator {

    private final int size;
    private final int[] dims;
    private final int[] strides;
    private final int[] index;
    private int position = 0;
    private int ptr;

    public StridePointerIterator(StrideLayout layout, Order askOrder) {
        this(layout, askOrder, true);
    }

    public StridePointerIterator(StrideLayout layout, Order askOrder, boolean compact) {
        var c = layout.computeFortranLayout(askOrder, compact);

        this.size = c.size();
        this.index = new int[c.rank()];
        this.dims = c.dims();
        this.strides = c.strides();
        this.ptr = c.offset();
    }

    @Override
    public boolean hasNext() {
        return position < size;
    }

    @Override
    public int nextInt() {
        if (position >= size) {
            throw new NoSuchElementException();
        }
        int currentPtr = ptr;
        position++;
        int i = 0;
        if (dims.length > 0) {
            index[i]++;
            ptr += strides[i];
        }
        while (i < dims.length) {
            if (index[i] == dims[i]) {
                index[i] = 0;
                ptr -= dims[i] * strides[i];
                if (i < dims.length - 1) {
                    index[i + 1]++;
                    ptr += strides[i + 1];
                }
                i++;
                continue;
            }
            break;
        }
        return currentPtr;
    }

    @Override
    public int position() {
        return position - 1;
    }

    @Override
    public int size() {
        return size;
    }
}
