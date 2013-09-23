/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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
 */

package rapaio.data;

import java.util.Arrays;
import java.util.List;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Mapping {

    private final int size;
    private final int[] indexes;
    private final int hash;

    public Mapping(Mapping mapping) {
        this.size = mapping.indexes.length;
        this.indexes = Arrays.copyOf(mapping.indexes, mapping.indexes.length);
        this.hash = buildHash();
    }

    public Mapping(List<Integer> indexes) {
        this.size = indexes.size();
        this.indexes = new int[size];
        for (int i = 0; i < indexes.size(); i++) {
            this.indexes[i] = indexes.get(i);
        }
        this.hash = buildHash();
    }

    public int size() {
        return size;
    }

    public int get(int pos) {
        return indexes[pos];
    }

    private int buildHash() {
        byte[] data = new byte[4 * indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            data[i * 4 + 0] = (byte) ((indexes[i] >> 24) & 0xF);
            data[i * 4 + 1] = (byte) ((indexes[i] >> 16) & 0xF);
            data[i * 4 + 2] = (byte) ((indexes[i] >> 8) & 0xF);
            data[i * 4 + 3] = (byte) ((indexes[i] >> 0) & 0xF);
        }
        return hash32(data);
    }

    /**
     * Generates 32 bit hash from byte array of the given length and
     * seed.
     *
     * @param data byte array to hash
     * @return 32 bit hash of the given array
     */
    private int hash32(final byte[] data) {
        // 'm' and 'r' are mixing constants generated offline.
        // They're not really 'magic', they just happen to work well.
        final int m = 0x5bd1e995;
        final int r = 24;

        // Initialize the hash to a random value
        int h = 0x9747b28c ^ data.length;
        int length4 = data.length / 4;

        for (int i = 0; i < length4; i++) {
            final int i4 = i * 4;
            int k = (data[i4 + 0] & 0xff) + ((data[i4 + 1] & 0xff) << 8)
                    + ((data[i4 + 2] & 0xff) << 16) + ((data[i4 + 3] & 0xff) << 24);
            k *= m;
            k ^= k >>> r;
            k *= m;
            h *= m;
            h ^= k;
        }

        // Handle the last few bytes of the input array
        switch (data.length % 4) {
            case 3:
                h ^= (data[(data.length & ~3) + 2] & 0xff) << 16;
            case 2:
                h ^= (data[(data.length & ~3) + 1] & 0xff) << 8;
            case 1:
                h ^= (data[data.length & ~3] & 0xff);
                h *= m;
        }
        h ^= h >>> 13;
        h *= m;
        h ^= h >>> 15;
        return h;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Mapping mapping = (Mapping) o;

        if (hash != mapping.hash) return false;
        if (size != mapping.size) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
