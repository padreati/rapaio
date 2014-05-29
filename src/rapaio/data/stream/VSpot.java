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

import rapaio.data.Var;

import java.io.Serializable;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class VSpot implements Serializable {

    private final int row;
    private final Var var;

    public VSpot(int row, Var var) {
        this.row = row;
        this.var = var;
    }

    public int row() {
        return row;
    }

    public int rowId() {
        return var.rowId(row);
    }

    public Var vector() {
        return var;
    }

    public boolean missing() {
        return var.missing(row);
    }

    public void setMissing() {
        var.setMissing(row);
    }

    public double value() {
        return var.value(row);
    }

    public void setValue(final double value) {
        var.setValue(row, value);
    }

    public int index() {
        return var.index(row);
    }

    public void setIndex(final int index) {
        var.setIndex(row, index);
    }

    public String label() {
        return var.label(row);
    }

    public void setLabel(String label) {
        var.setLabel(row, label);
    }

    public String[] dictionary() {
        return var.dictionary();
    }
}
