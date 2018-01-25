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

import rapaio.data.Frame;

import java.io.Serializable;

/**
 * Frame spot is a reference to an observation from a frame and usually is used in context of streams
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public final class FSpot implements Serializable {

    private static final long serialVersionUID = 5414699690274410204L;
    private final Frame df;
    private final int row;

    /**
     * Builds a frame spot for a given frame and a given row of the observation
     *
     * @param df  given data frame
     * @param row given row
     */
    public FSpot(final Frame df, final int row) {
        this.df = df;
        this.row = row;
    }

    /**
     * Returns the underlying frame of the spot
     *
     * @return the underlying frame
     */
    public Frame frame() {
        return df;
    }

    /**
     * Returns the row of the observation referenced by spot
     *
     * @return row number
     */
    public int row() {
        return row;
    }

    /**
     * @return true if missing value on any variable, false otherwise
     */
    public boolean isMissing() {
        return df.isMissing(row);
    }

    /**
     * Returns true if the spot has a missing value on the variable specified
     *
     * @param varIndex index of the variable
     * @return true if is missing, false otherwise
     */
    public boolean isMissing(int varIndex) {
        return df.isMissing(row, varIndex);
    }

    /**
     * Returns true if the spot has a missing value on the variable specified
     *
     * @param varName variable name
     * @return true is missing value, false otherwise
     */
    public boolean isMissing(String varName) {
        return df.isMissing(row, varName);
    }

    /**
     * Sets missing value on given variable
     *
     * @param varIndex index of the variable
     */
    public void setMissing(int varIndex) {
        df.setMissing(row, varIndex);
    }

    /**
     * Sets missing value for given variable
     *
     * @param varName variable name
     */
    public void setMissing(String varName) {
        df.setMissing(row, varName);
    }

    /**
     * Returns numeric value of the given variable
     *
     * @param varIndex index of the variable
     * @return numerical value of the given variable on the current spot
     */
    public double value(int varIndex) {
        return df.value(row, varIndex);
    }

    /**
     * Returns numeric value of the given variable on the current spot
     *
     * @param varName variable name
     * @return numeric value of the given variable on the current spot
     */
    public double value(String varName) {
        return df.value(row, varName);
    }

    /**
     * Sets numeric value of the given variable on the current spot
     *
     * @param varIndex index of the variable
     * @param value    given numeric value
     */
    public void setValue(int varIndex, double value) {
        df.setValue(row, varIndex, value);
    }

    /**
     * Sets numeric value of the given variable on the current spot
     *
     * @param varName variable name
     * @param value   given numeric value
     */
    public void setValue(String varName, double value) {
        df.setValue(row, varName, value);
    }

    /**
     * Returns index value of the given variable
     *
     * @param varIndex index of the variable
     * @return index value of the given variable on the current spot
     */
    public int index(int varIndex) {
        return df.index(row, varIndex);
    }

    /**
     * Returns index value of the given variable
     *
     * @param varName variable name
     * @return index value of the given variable on the current spot
     */
    public int index(String varName) {
        return df.index(row, varName);
    }

    /**
     * Sets the index value of the given variable on the current spot
     *
     * @param varIndex index of the variable
     * @param value    given index value
     */
    public void setIndex(int varIndex, int value) {
        df.setIndex(row, varIndex, value);
    }

    /**
     * Sets the index value of the given variable on the current spot
     *
     * @param varName variable name
     * @param value   given index value
     */
    public void setIndex(String varName, int value) {
        df.setIndex(row, varName, value);
    }

    /**
     * Returns label of the given variable on the current spot
     *
     * @param varIndex index of the variable
     * @return label value
     */
    public String label(int varIndex) {
        return df.label(row, varIndex);
    }

    /**
     * Returns label of the given variable on the current spot
     *
     * @param varName variable name
     * @return label value
     */
    public String label(String varName) {
        return df.label(row, varName);
    }

    /**
     * Sets label value of the given variable on the current spot
     *
     * @param varIndex index of the variable
     * @param value    given label value
     */
    public void setLabel(int varIndex, String value) {
        df.setLabel(row, varIndex, value);
    }

    /**
     * Sets label value of the given variable on the current spot
     *
     * @param varName variable name
     * @param value   given label value
     */
    public void setLabel(String varName, String value) {
        df.setLabel(row, varName, value);
    }

    /**
     * Returns label levels of the given variable
     *
     * @param varIndex index of the variable
     * @return label levels
     */
    public String[] levels(int varIndex) {
        return df.rvar(varIndex).levels();
    }

    /**
     * Returns label levels of the given variable
     *
     * @param varName variable name
     * @return label levels
     */
    public String[] levels(String varName) {
        return df.levels(varName);
    }

    public boolean binary(String name) {
        return df.binary(row, name);
    }
}
