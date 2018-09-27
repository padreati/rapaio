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
import java.util.Objects;

/**
 * A variable spot is a reference to an observation from a variable and is used in the context of streams.
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class VSpot implements Comparable<VSpot>, Serializable {

    private static final long serialVersionUID = -6730609711071770571L;
    private final int row;
    private final Var var;

    /**
     * Builds a spot for a given variable at a given row
     *
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
    public int row() {
        return row;
    }

    /**
     * @return underlying support variable
     */
    public Var rvar() {
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
    public double getDouble() {
        return var.getDouble(row);
    }

    /**
     * Assigns a numeric value to the observation
     *
     * @param value given numeric value
     */
    public void setDouble(final double value) {
        var.setDouble(row, value);
    }

    /**
     * @return index value of the observation
     */
    public int getInt() {
        return var.getInt(row);
    }

    /**
     * Assigns index value to the observation value
     *
     * @param index given index value
     */
    public void setInt(final int index) {
        var.setInt(row, index);
    }

    /**
     * @return label value of the observation
     */
    public String getLabel() {
        return var.getLabel(row);
    }

    /**
     * Assigns label value to the current observation value
     *
     * @param label given label value
     */
    public void setLabel(String label) {
        var.setLabel(row, label);
    }

    /**
     * @return binary value of the observation
     */
    public boolean getBoolean() {
        return var.getBoolean(row);
    }

    /**
     * Assigns binary value to the current observation value
     *
     * @param value binary value
     */
    public void setBoolean(boolean value) {
        var.setBoolean(row, value);
    }

    /**
     * @return stamp value of the observation
     */
    public long getLong() {
        return var.getLong(row);
    }

    /**
     * Assigns the given stamp value to the current observation
     *
     * @param value stamp value
     */
    public void getLong(long value) {
        var.setLong(row, value);
    }

    @Override
    public int compareTo(VSpot o) {
        switch (var.type()) {
            case DOUBLE:
                return Double.compare(getDouble(), o.getDouble());
            case INT:
                return Integer.compare(getInt(), o.getInt());
            case LONG:
                return Long.compare(getLong(), o.getLong());
            case BOOLEAN:
                return Boolean.compare(getBoolean(), o.getBoolean());
            default:
                return getLabel().compareTo(o.getLabel());
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VSpot vSpot = (VSpot) o;
        return row == vSpot.row && Objects.equals(var, vSpot.var);
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, var);
    }
}
