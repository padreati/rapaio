/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.experiment.ml.svm.libsvm;

//
// Kernel Cache
//
// l is the number of total data items
// size is the cache size limit in bytes
//

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Kernel cache.
 */
public class Cache {
    /**
     * Number of training dataset instances.
     */
    private final int l;
    /**
     * Number of entries available in cache. An entry is a value slot in any position.
     * When we add entries to cache we should remove others which are already there.
     */
    private long size;

    private static final class Entry {
        Entry prev;
        Entry next;
        float[] data;

        public int len() {
            return data == null ? 0 : data.length;
        }

        public boolean nonEmpty() {
            return data != null;
        }

        public boolean isEmpty() {
            return data == null;
        }
    }

    private final Entry[] entries;
    private Entry lruEntry;

    Cache(int l, long size) {
        this.l = l;
        entries = new Entry[l];
        for (int i = 0; i < l; i++) {
            entries[i] = new Entry();
        }
        this.size = Math.max(size, 2L * l);
        lruEntry = new Entry();
        lruEntry.next = lruEntry.prev = lruEntry;
    }

    private void lruUnlink(Entry h) {
        if (h.isEmpty()) {
            return;
        }
        // delete from current location
        h.prev.next = h.next;
        h.next.prev = h.prev;
    }

    private void lruLink(Entry h) {
        if (h.isEmpty()) {
            return;
        }
        // insert to last position
        h.next = lruEntry;
        h.prev = lruEntry.prev;
        h.prev.next = h;
        h.next.prev = h;
    }

    // request data [0,len)
    // return some position p where [p,len) need to be filled
    // (p >= len if nothing needs to be filled)
    public int getData(int index, AtomicReference<float[]> data, int len) {
        Entry h = entries[index];

        lruUnlink(h);
        int oldLen = h.len();
        int more = len - h.len();

        if (len > h.len()) {
            // free old space
            while (size < more) {
                Entry old = lruEntry.next;
                lruUnlink(old);
                size += old.len();
                old.data = null;
            }

            // allocate new space
            h.data = (h.data == null) ? new float[len] : Arrays.copyOf(h.data, len);
            size -= more;
        }

        lruLink(h);
        data.set(h.data);
        return oldLen;
    }

    public void swap_index(int i, int j) {
        if (i == j) {
            return;
        }
        if (i > j) {
            swap_index(j, i);
            return;
        }

        lruUnlink(entries[i]);
        lruUnlink(entries[j]);

        float[] buf = entries[i].data;
        entries[i].data = entries[j].data;
        entries[j].data = buf;

        lruLink(entries[i]);
        lruLink(entries[j]);

        for (Entry h = lruEntry.next; h != lruEntry; h = h.next) {
            if (h.len() > i) {
                if (h.len() > j) {
                    float tmp = h.data[i];
                    h.data[i] = h.data[j];
                    h.data[j] = tmp;
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
