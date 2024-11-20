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

package rapaio.narray.iterators;

import java.util.NoSuchElementException;

public final class ScalarPointerIterator implements PointerIterator {

    private final int offset;
    private boolean consumed = false;

    public ScalarPointerIterator(int offset) {
        this.offset = offset;
    }

    @Override
    public int nextInt() {
        if (consumed) {
            throw new NoSuchElementException();
        }
        consumed = true;
        return offset;
    }

    @Override
    public int position() {
        return 0;
    }

    @Override
    public boolean hasNext() {
        return !consumed;
    }

    @Override
    public int size() {
        return 1;
    }
}
