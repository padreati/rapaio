/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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
 * Variable spots are pointer to a single observation of a given variable and are used in the context of streams.
 *
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class VSpot implements Serializable {

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
     * Row of the spotted observation
     *
     * @return row of the spotted variable
     */
    public int row() {
        return row;
    }

    /**
     * Returns the underlying variable
     * @return support variable
     */
    public Var var() {
        return var;
    }

    /**
     * Returns true if the spotted observation value is missing / not specified
     * @return true if value is missing, false otherwise
     */
    public boolean missing() {
        return var.missing(row);
    }

    /**
     * Sets the spotted observation value to missing value
     */
    public void setMissing() {
        var.setMissing(row);
    }

    /**
     * Returns the observation value as a numerical double value
     * @return numeric value
     */
    public double value() {
        return var.value(row);
    }

    /**
     * Assigns a numeric value to the observation value
     * @param value given numeric value
     */
    public void setValue(final double value) {
        var.setValue(row, value);
    }

    /**
     * Returns the index value of the observation
     * @return index value
     */
    public int index() {
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
     * Returns the label value of the observation
     * @return label value
     */
    public String label() {
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
     * Returns the label dictionary used by given variable
     * @return dictionary of the variable
     */
    public String[] dictionary() {
        return var.dictionary();
    }

    /**
     * Returns binary value of the observation
     * @return binary value
     */
    public boolean binary() {
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
     * Returns stamp value of the current observation
     * @return stamp value
     */
    public long stamp() {
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
