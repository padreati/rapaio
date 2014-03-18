/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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

package rapaio.data.collect;

import rapaio.data.Vector;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class VInstance {

    final int row;
    final Vector vector;

    public VInstance(int row, Vector vector) {
        this.row = vector.rowId(row);
        this.vector = vector.source();
    }

    public boolean isMissing() {
        return vector.isMissing(row);
    }

    public void setMissing() {
        vector.setMissing(row);
    }

    public double getValue() {
        return vector.getValue(row);
    }

    public void setValue(final double value) {
        vector.setValue(row, value);
    }
}
