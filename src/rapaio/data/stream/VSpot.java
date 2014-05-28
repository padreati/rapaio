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

package rapaio.data.stream;

import rapaio.data.Vector;

import java.io.Serializable;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class VSpot implements Serializable {

    private final int row;
    private final Vector vector;

    public VSpot(int row, Vector vector) {
        this.row = row;
        this.vector = vector;
    }

    public int row() {
        return row;
    }

    public int rowId() {
        return vector.rowId(row);
    }

    public Vector vector() {
        return vector;
    }

    public boolean missing() {
        return vector.missing(row);
    }

    public void setMissing() {
        vector.setMissing(row);
    }

    public double value() {
        return vector.value(row);
    }

    public void setValue(final double value) {
        vector.setValue(row, value);
    }

    public int index() {
        return vector.index(row);
    }

    public void setIndex(final int index) {
        vector.setIndex(row, index);
    }

    public String label() {
        return vector.label(row);
    }

    public void setLabel(String label) {
        vector.setLabel(row, label);
    }

    public String[] dictionary() {
        return vector.dictionary();
    }
}
