/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
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

package rapaio.data.stream;

import rapaio.data.Var;

import java.io.Serializable;

/**
 * A variable spot is a reference to an observation from a variable and is used in the context of streams.
 *
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class VSpot implements Serializable {

    private static final long serialVersionUID = -6730609711071770571L;
    private final int row;
    private final Var var;

    /**
     * Builds a spot for a given variable at a given row
     * @param row row of the spotted observation
     * @param var given variable
     */
    public VSpot(int row, Var var) {
        this.row = row;
        this.var = var;
    }

    /**
     * @return row of the spotted variable
     */
    public int getRow() {
        return row;
    }

    /**
     * @return underlying support variable
     */
    public Var getVar() {
        return var;
    }

    /**
     * @return true if the observation is missing, false otherwise
     */
    public boolean isMissing() {
        return var.isMissing(row);
    }

    /**
     * Sets the spotted observation value to missing value
     */
    public void setMissing() {
        var.setMissing(row);
    }

    /**
     * @return numeric double value of the observation
     */
    public double getValue() {
        return var.value(row);
    }

    /**
     * Assigns a numeric value to the observation
     * @param value given numeric value
     */
    public void setValue(final double value) {
        var.setValue(row, value);
    }

    /**
     * @return index value of the observation
     */
    public int getIndex() {
        return var.index(row);
    }

    /**
     * Assigns index value to the observation value
     * @param index given index value
     */
    public void setIndex(final int index) {
        var.setIndex(row, index);
    }

    /**
     * @return label value of the observation
     */
    public String getLabel() {
        return var.label(row);
    }

    /**
     * Assigns label value to the current observation value
     * @param label given label value
     */
    public void setLabel(String label) {
        var.setLabel(row, label);
    }

    /**
     * @return binary value of the observation
     */
    public boolean getBinary() {
        return var.binary(row);
    }

    /**
     * Assigns binary value to the current observation value
     * @param value binary value
     */
    public void setBinary(boolean value) {
        var.setBinary(row, value);
    }

    /**
     * @return stamp value of the observation
     */
    public long getStamp() {
        return var.stamp(row);
    }

    /**
     * Assigns the given stamp value to the current observation
     * @param value stamp value
     */
    public void setStamp(long value) {
        var.setStamp(row, value);
    }
}
