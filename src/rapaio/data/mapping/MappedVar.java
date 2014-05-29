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

package rapaio.data.mapping;

import rapaio.data.*;

/**
 * A var which is learn on the base of another var and the row selection
 * and order is specified by a getMapping give at construction time.
 * <p>
 * This var does not hold actual values, it delegate the behavior to the
 * wrapped var, thus the wrapping affects only the getRowCount selected anf the
 * order of these getRowCount.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class MappedVar extends AbstractVar {

    private final Var source;
    private final Mapping mapping;

    public MappedVar(Var source, Mapping mapping) {
        if (source.isMappedVector()) {
            throw new IllegalArgumentException("Now allowed mapped var as source");
        }
        this.source = source;
        this.mapping = mapping;
    }

    @Override
    public VarType type() {
        return source.type();
    }

    @Override
    public int rowCount() {
        return mapping.size();
    }

    @Override
    public boolean isMappedVector() {
        return true;
    }

    @Override
    public Var source() {
        return source;
    }

    @Override
    public Mapping mapping() {
        return mapping;
    }

    @Override
    public int rowId(int row) {
        return source.rowId(mapping.get(row));
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

    @Override
    public void ensureCapacity(int minCapacity) {
        throw new IllegalArgumentException("operation not available on mapped vectors");
    }

    @Override
    public Var solidCopy() {
        switch (source.type()) {
            case NOMINAL:
                Nominal nom = new Nominal(mapping.size(), source.dictionary());
                for (int i = 0; i < mapping.size(); i++) {
                    nom.setLabel(i, label(i));
                }
                return nom;
            case INDEX:
                Index idx = new Index(rowCount(), rowCount(), 0);
                for (int i = 0; i < rowCount(); i++) {
                    idx.setIndex(i, index(i));
                }
                return idx;
            default:
                Numeric num = new Numeric(rowCount());
                for (int i = 0; i < rowCount(); i++) {
                    num.setValue(i, value(i));
                }
                return num;
        }
    }
}
