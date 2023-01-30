/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.stream;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import rapaio.data.Var;

/**
 * A variable spot is a reference to an observation from a variable and is used in the context of streams.
 * <p>
 * User: <a href="padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public record VSpot(int row, Var rvar) implements Comparable<VSpot>, Serializable {

    @Serial
    private static final long serialVersionUID = -6730609711071770571L;


    /**
     * @return true if the observation is missing, false otherwise
     */
    public boolean isMissing() {
        return rvar.isMissing(row);
    }

    /**
     * Sets the spotted observation value to missing value
     */
    public void setMissing() {
        rvar.setMissing(row);
    }

    /**
     * @return numeric float value of the observation
     */
    public float getFloat() {
        return rvar.getFloat(row);
    }

    /**
     * Assigns a numeric value to the observation
     *
     * @param value given numeric value
     */
    public void setFloat(final float value) {
        rvar.setFloat(row, value);
    }

    /**
     * @return numeric double value of the observation
     */
    public double getDouble() {
        return rvar.getDouble(row);
    }

    /**
     * Assigns a numeric value to the observation
     *
     * @param value given numeric value
     */
    public void setDouble(final double value) {
        rvar.setDouble(row, value);
    }

    /**
     * @return index value of the observation
     */
    public int getInt() {
        return rvar.getInt(row);
    }

    /**
     * Assigns index value to the observation value
     *
     * @param index given index value
     */
    public void setInt(final int index) {
        rvar.setInt(row, index);
    }

    /**
     * @return label value of the observation
     */
    public String getLabel() {
        return rvar.getLabel(row);
    }

    /**
     * Assigns label value to the current observation value
     *
     * @param label given label value
     */
    public void setLabel(String label) {
        rvar.setLabel(row, label);
    }

    /**
     * @return stamp value of the observation
     */
    public long getLong() {
        return rvar.getLong(row);
    }

    /**
     * Assigns the given stamp value to the current observation
     *
     * @param value stamp value
     */
    public void setLong(long value) {
        rvar.setLong(row, value);
    }

    public Instant getInstant() {
        return rvar.getInstant(row);
    }

    public void setInstant(Instant value) {
        rvar.setInstant(row, value);
    }

    @Override
    public int compareTo(VSpot o) {
        return switch (rvar.type()) {
            case DOUBLE -> Double.compare(getDouble(), o.getDouble());
            case BINARY, INT -> Integer.compare(getInt(), o.getInt());
            case LONG -> Long.compare(getLong(), o.getLong());
            default -> getLabel().compareTo(o.getLabel());
        };
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VSpot vSpot = (VSpot) o;
        return row == vSpot.row && Objects.equals(rvar, vSpot.rvar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, rvar);
    }
}
