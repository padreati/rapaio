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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * A frame which is build on the base of another frame with
 * the row order and row selection specified by a mapping given at construction time.
 * <p>
 * This frame does not hold actual values, it delegates the behavior
 * to the wrapped frame, thus the wrapping affects only the rows
 * selected and the order of these rows.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class MappedFrame extends AbstractFrame {

    public static MappedFrame byRow(Frame df, int... mapping) {
        return new MappedFrame(df, Mapping.copy(mapping));
    }

    public static MappedFrame byRow(Frame df, Mapping mapping) {
        return new MappedFrame(df, mapping);
    }

    public static MappedFrame byRow(Frame df, Mapping mapping, String varRange) {
        return MappedFrame.byRow(df, mapping, VRange.of(varRange));
    }

    public static MappedFrame byRow(Frame df, Mapping mapping, VRange vRange) {
        return new MappedFrame(df, mapping, vRange.parseVarNames(df));
    }

    private static final long serialVersionUID = 1368765233851124235L;
    private final Frame source;
    private final Mapping mapping;
    private final String[] names;
    private final HashMap<String, Integer> colReverse;
    private final int[] colIndexes;

    private MappedFrame(Frame df, Mapping mapping) {
        this(df, mapping, Arrays.asList(df.getVarNames()));
    }

    private MappedFrame(Frame df, Mapping mapping, List<String> columns) {
        if (mapping == null)
            mapping = Mapping.empty();
        if (df instanceof MappedFrame) {
            MappedFrame mappedFrame = (MappedFrame) df;
            this.source = mappedFrame.sourceFrame();
            List<Integer> rows = new ArrayList<>();
            for (int i = 0; i < mapping.size(); i++) {
                rows.add(mappedFrame.mapping().get(mapping.get(i)));
            }
            this.mapping = Mapping.wrap(rows);
        } else {
            this.source = df;
            this.mapping = mapping;
        }
        this.names = new String[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            names[i] = columns.get(i);
        }
        this.colReverse = new HashMap<>();
        this.colIndexes = new int[columns.size()];
        for(int i=0; i< names.length; i++) {
            colIndexes[i] = source.getVarIndex(names[i]);
            colReverse.put(names[i], i);
        }
    }

    @Override
    public int getRowCount() {
        return mapping.size();
    }

    @Override
    public int getVarCount() {
        return names.length;
    }

    protected Frame sourceFrame() {
        return source;
    }

    protected Mapping mapping() {
        return mapping;
    }

    @Override
    public String[] getVarNames() {
        return names;
    }

    @Override
    public int getVarIndex(String name) {
        if (!colReverse.containsKey(name)) {
            throw new IllegalArgumentException(String.format("var name: %s does not exist", name));
        }
        return colReverse.get(name);
    }

    @Override
    public Var getVar(int varIndex) {
        return MappedVar.byRows(this.source.getVar(names[varIndex]), this.mapping).withName(names[varIndex]);
    }

    @Override
    public Var getVar(String varName) {
        return getVar(getVarIndex(varName));
    }

    @Override
    public Frame bindVars(Var... vars) {
        return BoundFrame.byVars(this, BoundFrame.byVars(vars));
    }

    @Override
    public Frame bindVars(Frame df) {
        return BoundFrame.byVars(this, df);
    }

    @Override
    public Frame mapVars(VRange range) {
        return MappedFrame.byRow(this, Mapping.range(0, this.getRowCount()), range);
    }

    @Override
    public Frame addRows(int rowCount) {
        return BoundFrame.byRows(this, SolidFrame.emptyFrom(this, rowCount));
    }

    @Override
    public Frame bindRows(Frame df) {
        return BoundFrame.byRows(this, df);
    }

    @Override
    public Frame mapRows(Mapping mapping) {
        return MappedFrame.byRow(this, mapping);
    }

    @Override
    public double getValue(int row, int col) {
        return source.getValue(mapping.get(row), colIndexes[col]);
    }

    @Override
    public double getValue(int row, String varName) {
        return source.getValue(mapping.get(row), varName);
    }

    @Override
    public void setValue(int row, int col, double value) {
        source.setValue(mapping.get(row), colIndexes[col], value);
    }

    @Override
    public void setValue(int row, String varName, double value) {
        source.setValue(mapping.get(row), varName, value);
    }

    @Override
    public int getIndex(int row, int col) {
        return source.getIndex(mapping.get(row), colIndexes[col]);
    }

    @Override
    public int getIndex(int row, String varName) {
        return source.getIndex(mapping.get(row), varName);
    }

    @Override
    public void setIndex(int row, int col, int value) {
        source.setIndex(mapping.get(row), colIndexes[col], value);
    }

    @Override
    public void setIndex(int row, String varName, int value) {
        source.setIndex(mapping.get(row), varName, value);
    }

    @Override
    public String getLabel(int row, int col) {
        return source.getLabel(mapping.get(row), colIndexes[col]);
    }

    @Override
    public String getLabel(int row, String varName) {
        return source.getLabel(mapping.get(row), varName);
    }

    @Override
    public void setLabel(int row, int col, String value) {
        source.setLabel(mapping.get(row), colIndexes[col], value);
    }

    @Override
    public void setLabel(int row, String varName, String value) {
        source.setLabel(mapping.get(row), varName, value);
    }

    @Override
    public boolean getBinary(int row, int col) {
        return source.getBinary(mapping.get(row), colIndexes[col]);
    }

    @Override
    public boolean getBinary(int row, String varName) {
        return source.getBinary(mapping.get(row), varName);
    }

    @Override
    public void setBinary(int row, int col, boolean value) {
        source.setBinary(mapping.get(row), colIndexes[col], value);
    }

    @Override
    public void setBinary(int row, String varName, boolean value) {
        source.setBinary(mapping.get(row), varName, value);
    }

    @Override
    public boolean isMissing(int row, int col) {
        return source.isMissing(mapping.get(row), colIndexes[col]);
    }

    @Override
    public boolean isMissing(int row, String varName) {
        return source.isMissing(mapping.get(row), varName);
    }

    @Override
    public boolean isMissing(int row) {
        return source.isMissing(mapping.get(row));
    }

    @Override
    public void setMissing(int row, int col) {
        source.setMissing(mapping.get(row), colIndexes[col]);
    }

    @Override
    public void setMissing(int row, String varName) {
        source.setMissing(mapping.get(row), varName);
    }


}
