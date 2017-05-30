/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.data;

import java.util.stream.Collectors;

/**
 * A variable which wraps rows from another variable. The row selection
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

    /**
     * Builds a mapped variable specifying selected positions through a mapping
     *
     * @param source  wrapped variable
     * @param mapping mapping of indexed values
     * @return mapped variable
     */
    public static MappedVar byRows(Var source, Mapping mapping) {
        return new MappedVar(source, mapping);
    }

    /**
     * Build a mapped variable specifying the selected positions through a variable array
     *
     * @param source wrapped variable
     * @param rows   variable array of indexed values
     * @return mapped variable
     */
    public static MappedVar byRows(Var source, int... rows) {
        return new MappedVar(source, Mapping.copy(rows));
    }

    private static final long serialVersionUID = -2293127457462742840L;
    private final Var source;
    private final Mapping mapping;

    private MappedVar(Var var, Mapping mapping) {
        withName(var.getName());
        if (var instanceof MappedVar) {

            this.mapping = Mapping.wrap(mapping.rowStream().map(row -> ((MappedVar) var).getMapping().get(row)).boxed().collect(Collectors.toList()));
            this.source = ((MappedVar) var).source();
        } else {
            this.mapping = mapping;
            this.source = var;
        }
    }

    @Override
    public VarType getType() {
        return source.getType();
    }

    @Override
    public int getRowCount() {
        return mapping.size();
    }

    @Override
    public void addRows(int rowCount) {
        throw new IllegalArgumentException("operation not available on mapped vectors");
    }

    public Var source() {
        return source;
    }

    public Mapping getMapping() {
        return mapping;
    }

    @Override
    public double getValue(int row) {
        return source.getValue(mapping.get(row));
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
    public int getIndex(int row) {
        return source.getIndex(mapping.get(row));
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
    public String getLabel(int row) {
        return source.getLabel(mapping.get(row));
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
    public String[] getLevels() {
        return source.getLevels();
    }

    @Override
    public void setLevels(String[] dict) {
        source.setLevels(dict);
    }

    @Override
    public boolean getBinary(int row) {
        return source.getBinary(mapping.get(row));
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
    public long getStamp(int row) {
        return source.getStamp(mapping.get(row));
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
    public boolean isMissing(int row) {
        return source.isMissing(mapping.get(row));
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
        mapping.remove(row);
    }

    @Override
    public void clear() {
        mapping.clear();
    }

    @Override
    public Var newInstance(int rows) {
        return source.newInstance(rows);
    }

    @Override
    public String toString() {
        return "MappedVar:" + source.getType() + "[name:" + getName() + ", rowCount:" + mapping.size() + ']';
    }
}
