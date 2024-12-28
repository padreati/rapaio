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

package rapaio.data.stream;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import rapaio.data.Frame;

/**
 * Frame spot is a reference to an observation from a frame and usually is used in context of streams
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public record FSpot(Frame df, int row) implements Serializable {

    @Serial
    private static final long serialVersionUID = 5414699690274410204L;

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
    public double getDouble(int varIndex) {
        return df.getDouble(row, varIndex);
    }

    /**
     * Returns numeric value of the given variable on the current spot
     *
     * @param varName variable name
     * @return numeric value of the given variable on the current spot
     */
    public double getDouble(String varName) {
        return df.getDouble(row, varName);
    }

    /**
     * Sets numeric value of the given variable on the current spot
     *
     * @param varIndex index of the variable
     * @param value    given numeric value
     */
    public void setDouble(int varIndex, double value) {
        df.setDouble(row, varIndex, value);
    }

    /**
     * Sets numeric value of the given variable on the current spot
     *
     * @param varName variable name
     * @param value   given numeric value
     */
    public void setDouble(String varName, double value) {
        df.setDouble(row, varName, value);
    }

    /**
     * Returns index value of the given variable
     *
     * @param varIndex index of the variable
     * @return index value of the given variable on the current spot
     */
    public int getInt(int varIndex) {
        return df.getInt(row, varIndex);
    }

    /**
     * Returns index value of the given variable
     *
     * @param varName variable name
     * @return index value of the given variable on the current spot
     */
    public int getInt(String varName) {
        return df.getInt(row, varName);
    }

    /**
     * Sets the index value of the given variable on the current spot
     *
     * @param varIndex index of the variable
     * @param value    given index value
     */
    public void setInt(int varIndex, int value) {
        df.setInt(row, varIndex, value);
    }

    /**
     * Sets the index value of the given variable on the current spot
     *
     * @param varName variable name
     * @param value   given index value
     */
    public void setInt(String varName, int value) {
        df.setInt(row, varName, value);
    }

    /**
     * Returns label of the given variable on the current spot
     *
     * @param varIndex index of the variable
     * @return label value
     */
    public String getLabel(int varIndex) {
        return df.getLabel(row, varIndex);
    }

    /**
     * Returns label of the given variable on the current spot
     *
     * @param varName variable name
     * @return label value
     */
    public String getLabel(String varName) {
        return df.getLabel(row, varName);
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
    public List<String> levels(int varIndex) {
        return df.rvar(varIndex).levels();
    }

    /**
     * Returns label levels of the given variable
     *
     * @param varName variable name
     * @return label levels
     */
    public List<String> levels(String varName) {
        return df.levels(varName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FSpot fSpot = (FSpot) o;
        return row == fSpot.row && Objects.equals(df, fSpot.df);
    }

    @Override
    public int hashCode() {
        return Objects.hash(df, row);
    }
}
