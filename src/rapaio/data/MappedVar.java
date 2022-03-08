/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data;

import java.io.Serial;
import java.time.Instant;
import java.util.List;

import rapaio.math.linear.DVector;
import rapaio.math.linear.dense.DVectorDense;
import rapaio.math.linear.dense.DVectorMap;
import rapaio.math.linear.dense.DVectorVar;
import rapaio.math.linear.option.AlgebraOption;
import rapaio.math.linear.option.AlgebraOptions;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POption;

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
        return new MappedVar(source, Mapping.wrap(rows));
    }

    @Serial
    private static final long serialVersionUID = -2293127457462742840L;
    private final Var source;
    private final Mapping mapping;

    private MappedVar(Var var, Mapping mapping) {
        name(var.name());
        if (var instanceof MappedVar src) {
            Mapping srcMap = src.getMapping();
            this.mapping = Mapping.from(mapping, srcMap::get);
            this.source = src.source;
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
    public int size() {
        return mapping.size();
    }

    @Override
    public void addRows(int rowCount) {
        throw new OperationNotAvailableException();
    }

    public Var getSource() {
        return source;
    }

    public Mapping getMapping() {
        return mapping;
    }

    @Override
    public double getDouble(int row) {
        return source.getDouble(mapping.get(row));
    }

    @Override
    public void setDouble(int row, double value) {
        source.setDouble(mapping.get(row), value);
    }

    @Override
    public void addDouble(double value) {
        throw new OperationNotAvailableException();
    }

    @Override
    public int getInt(int row) {
        return source.getInt(mapping.get(row));
    }

    @Override
    public void setInt(int row, int value) {
        source.setInt(mapping.get(row), value);
    }

    @Override
    public void addInt(int value) {
        throw new OperationNotAvailableException();
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
        throw new OperationNotAvailableException();
    }

    @Override
    public List<String> levels() {
        return source.levels();
    }

    @Override
    public void setLevels(String[] dict) {
        source.setLevels(dict);
    }

    @Override
    public long getLong(int row) {
        return source.getLong(mapping.get(row));
    }

    @Override
    public void setLong(int row, long value) {
        source.setLong(mapping.get(row), value);
    }

    @Override
    public void addLong(long value) {
        throw new OperationNotAvailableException();
    }

    @Override
    public void addInstant(Instant value) {
        throw new OperationNotAvailableException();
    }

    @Override
    public void setInstant(int row, Instant value) {
        source.setInstant(mapping.get(row), value);
    }

    @Override
    public Instant getInstant(int row) {
        return source.getInstant(mapping.get(row));
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
        throw new OperationNotAvailableException();
    }

    @Override
    public void removeRow(int row) {
        mapping.remove(row);
    }

    @Override
    public void clearRows() {
        mapping.clear();
    }

    @Override
    public DVector dv(AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] values = new double[mapping.size()];
            for (int i = 0; i < mapping.size(); i++) {
                values[i] = source.getDouble(mapping.get(i));
            }
            return new DVectorDense(0, mapping.size(), values);
        }
        if (source instanceof VarDouble vd) {
            new DVectorDense(0, vd.size(), vd.elements()).map(mapping.elements());
        }
        return new DVectorVar<>(this);
    }

    @Override
    public Var newInstance(int rows) {
        return source.newInstance(rows);
    }

    @Override
    protected String toStringClassName() {
        return "MappedVar(type=" + source.type().code() + ")";
    }

    @Override
    protected int toStringDisplayValueCount() {
        if (source instanceof AbstractVar) {
            return ((AbstractVar) source).toStringDisplayValueCount();
        }
        return 12;
    }

    @Override
    protected void textTablePutValue(TextTable tt, int i, int j, int row, Printer printer, POption<?>[] options) {
        if (source instanceof AbstractVar) {
            ((AbstractVar) source).textTablePutValue(tt, i, j, mapping.get(row), printer, options);
        } else {
            tt.textCenter(i, j, getLabel(row));
        }
    }

    @Override
    public String toString() {
        return "MappedVar[type=" + source.type().code() + ", name:" + name() + ", rowCount:" + mapping.size() + ']';
    }
}
