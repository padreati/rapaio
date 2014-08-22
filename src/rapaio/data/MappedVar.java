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

package rapaio.data;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * A variable which wraps another variable and the row selection
 * and order is specified by a mapping given at construction time.
 * <p>
 * This variable does not hold actual values, it delegates the behavior to the
 * wrapped variable, thus the wrapping affects only the rows selected and the
 * order of these rows.
 * <p>
 * Mapped variables does not allows adding new values
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class MappedVar extends AbstractVar {

    private final Var source;
    private final Mapping mapping;

    /**
     * Builds a mapped variable specifying selected positions through a mapping
     *
     * @param source  wrapped variable
     * @param mapping mapping of indexed values
     * @return mapped variable
     */
    public static MappedVar newByRows(Var source, Mapping mapping) {
        return new MappedVar(source, mapping);
    }

    /**
     * Build a mapped variable specifying the selected positions through a variable array
     *
     * @param source wrapped variable
     * @param rows   variable array of indexed values
     * @return mapped variable
     */
    public static MappedVar newByRows(Var source, int... rows) {
        return new MappedVar(source, Mapping.newCopyOf(rows));
    }

    private MappedVar(Var var, Mapping mapping) {
        withName(var.name());
        if (var instanceof MappedVar) {
            this.mapping = Mapping.newWrapOf(mapping.rowStream().map(row -> ((MappedVar)var).mapping().get(row)).mapToObj(row -> row).collect(Collectors.toList()));
            this.source = ((MappedVar)var).source();
        } else {
            this.mapping = mapping;
            this.source = var;
        }
    }

    @Override
    public VarType type() {
        return source.type();
    }

    @Override
    public int rowCount() {
        return mapping.size();
    }

    public Var source() {
        return source;
    }

    public Mapping mapping() {
        return mapping;
    }

    @Override
    public Var bindRows(Var var) {
        return BoundVar.newFrom(this, var);
    }

    @Override
    public Var mapRows(Mapping mapping) {
        return MappedVar.newByRows(this, mapping);
    }

    @Override
    public double value(int row) {
        return source.value(mapping.get(row));
    }

    @Override
    public void setValue(int row, double value) {
        source.setValue(mapping.get(row), value);
    }

    @Override
    public void addValue(double value) {
        throw new IllegalArgumentException("operation not available on mapped vectors");
    }

    @Override
    public int index(int row) {
        return source.index(mapping.get(row));
    }

    @Override
    public void setIndex(int row, int value) {
        source.setIndex(mapping.get(row), value);
    }

    @Override
    public void addIndex(int value) {
        throw new IllegalArgumentException("operation not available on mapped vectors");
    }

    @Override
    public String label(int row) {
        return source.label(mapping.get(row));
    }

    @Override
    public void setLabel(int row, String value) {
        source.setLabel(mapping.get(row), value);
    }

    @Override
    public void addLabel(String value) {
        throw new IllegalArgumentException("operation not available on mapped vectors");
    }

    @Override
    public String[] dictionary() {
        return source.dictionary();
    }

    @Override
    public void setDictionary(String[] dict) {
        source.setDictionary(dict);
    }

    @Override
    public boolean binary(int row) {
        return source.binary(mapping.get(row));
    }

    @Override
    public void setBinary(int row, boolean value) {
        source.setBinary(mapping.get(row), value);
    }

    @Override
    public void addBinary(boolean value) {
        throw new IllegalArgumentException("operation not available on mapped vectors");
    }

    @Override
    public long stamp(int row) {
        return source.stamp(mapping.get(row));
    }

    @Override
    public void setStamp(int row, long value) {
        source.setStamp(mapping.get(row), value);
    }

    @Override
    public void addStamp(long value) {
        throw new IllegalArgumentException("operation not available on mapped vectors");
    }

    @Override
    public boolean missing(int row) {
        return source.missing(mapping.get(row));
    }

    @Override
    public void setMissing(int row) {
        source.setMissing(mapping.get(row));
    }

    @Override
    public void addMissing() {
        throw new IllegalArgumentException("operation not available on mapped vectors");
    }

    @Override
    public void remove(int row) {
        throw new IllegalArgumentException("operation not available on mapped vectors");
    }

    @Override
    public void clear() {
        throw new IllegalArgumentException("operation not available on mapped vectors");
    }
}
