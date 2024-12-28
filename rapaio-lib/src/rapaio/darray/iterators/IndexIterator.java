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

package rapaio.darray.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;

import rapaio.darray.Order;
import rapaio.darray.Shape;

public class IndexIterator implements Iterator<int[]> {

    private final Shape shape;
    private final boolean cOrder;
    private final int[] index;
    private int pos = -1;

    public IndexIterator(Shape shape, Order askOrder) {
        if (askOrder != Order.C && askOrder != Order.F) {
            throw new IllegalArgumentException("Order must be either Order.C or Order.F");
        }
        this.shape = shape;
        this.cOrder = askOrder == Order.C;
        this.index = new int[shape.rank()];
        if (cOrder) {
            this.index[this.index.length - 1] = -1;
        } else {
            this.index[0] = -1;
        }
    }

    @Override
    public boolean hasNext() {
        return pos < shape.size() - 1;
    }

    @Override
    public int[] next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        advanceIndex();
        return index;
    }

    public int pos() {
        return pos;
    }

    private void advanceIndex() {
        if (cOrder) {
            advanceIndexCOrder();
        } else {
            advanceIndexFOrder();
        }
    }

    private void advanceIndexCOrder() {
        int last = shape.rank() - 1;
        index[last]++;
        while (last > 0) {
            if (index[last] == shape.dim(last)) {
                index[last] = 0;
                index[last - 1]++;
                last--;
                continue;
            }
            break;
        }
        pos++;
    }

    private void advanceIndexFOrder() {
        int last = 0;
        index[last]++;
        while (last < shape.rank() - 1) {
            if (index[last] == shape.dim(last)) {
                index[last] = 0;
                index[last + 1]++;
                last++;
                continue;
            }
            break;
        }
        pos++;
    }
}
