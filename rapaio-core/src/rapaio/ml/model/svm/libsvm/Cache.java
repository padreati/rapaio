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

package rapaio.ml.model.svm.libsvm;

import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.Tensors;
import rapaio.util.Reference;

/**
 * Kernel cache.
 */
public class Cache {

    /**
     * Number of entries available in cache. An entry is a value slot in any position.
     * When we add entries to cache we should remove others which are already there.
     */
    private long size;

    private static final class Entry {
        private Entry prev;
        private Entry next;
        private Tensor<Double> data;

        public int len() {
            return data == null ? 0 : data.size();
        }
    }

    private final Entry[] entries;
    private final Entry lruEntry;

    Cache(int len, long size) {
        entries = new Entry[len];
        for (int i = 0; i < len; i++) {
            entries[i] = new Entry();
        }
        this.size = Math.max(size, 4L * len);
        lruEntry = new Entry();
        lruEntry.next = lruEntry.prev = lruEntry;
    }

    private void lruUnlink(Entry h) {
        if (h.len() == 0) {
            return;
        }
        // delete from current location
        h.prev.next = h.next;
        h.next.prev = h.prev;
    }

    private void lruLink(Entry h) {
        if (h.len() == 0) {
            return;
        }
        // insert to last position
        h.next = lruEntry;
        h.prev = lruEntry.prev;
        h.prev.next = h;
        h.next.prev = h;
    }

    /**
     * Request data as a vector of length len. If the cached vector is not completely cached it returns
     * the position until it is computed, starting from 0. The other positions will be filled by
     * the caller and the values will remain in cache since data is passed as reference.
     */
    public int getData(int index, Reference<Tensor<Double>> data, int len) {
        Entry h = entries[index];

        lruUnlink(h);
        int oldLen = h.len();
        int more = len - h.len();

        if (more > 0) {
            // since we increase the current entry data, we have to remove some
            // lru entries to keep the constraint that the total size to be less than size
            while (size < more) {
                Entry old = lruEntry.next;
                lruUnlink(old);
                size += old.len();
                old.data = null;
            }
            // allocate new space
            h.data = (h.data == null) ? Tensors.zeros(Shape.of(len)) : h.data.pad(0, more);
            size -= more;
        }

        lruLink(h);
        data.set(h.data);
        return oldLen;
    }

    void swapIndex(int i, int j) {
        if (i == j) {
            return;
        }
        if (i > j) {
            swapIndex(j, i);
            return;
        }

        lruUnlink(entries[i]);
        lruUnlink(entries[j]);

        var buf = entries[i].data;
        entries[i].data = entries[j].data;
        entries[j].data = buf;

        lruLink(entries[i]);
        lruLink(entries[j]);

        for (Entry h = lruEntry.next; h != lruEntry; h = h.next) {
            if (h.len() > i) {
                if (h.len() > j) {
                    var tmp = h.data.get(i);
                    h.data.set(h.data.get(j), i);
                    h.data.set(tmp, j);
                } else {
                    // give up
                    lruUnlink(h);
                    size += h.len();
                    h.data = null;
                }
            }
        }
    }
}
