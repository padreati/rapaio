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

    public Mapping(Mapping mapping) {
        this.size = mapping.indexes.length;
        this.indexes = Arrays.copyOf(mapping.indexes, mapping.indexes.length);
    }

    public Mapping(List<Integer> indexes) {
        this.size = indexes.size();
        this.indexes = new int[size];
        for (int i = 0; i < indexes.size(); i++) {
            this.indexes[i] = indexes.get(i);
        }
    }

    public int size() {
        return size;
    }

    public int get(int pos) {
        return indexes[pos];
    }
}
