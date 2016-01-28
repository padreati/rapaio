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
 *
 */

package rapaio.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    private static final long serialVersionUID = 1368765233851124235L;
    private final Mapping mapping;
    private final Frame source;
    private final String[] names;
    private final HashMap<String, Integer> colIndex;
    private final Var[] vars;

    private MappedFrame(Frame df, Mapping mapping) {
        this(df, mapping, Arrays.asList(df.varNames()));
    }

    private MappedFrame(Frame df, Mapping mapping, List<String> columns) {
        if (mapping == null) mapping = Mapping.copy();
        if (df instanceof MappedFrame) {
            MappedFrame mappedFrame = (MappedFrame) df;
            this.source = mappedFrame.sourceFrame();
            this.mapping = Mapping.wrap(mapping.rowStream()
                    .map(row -> mappedFrame.mapping().get(row))
                    .mapToObj(row -> row).collect(Collectors.toList()));
        } else {
            this.source = df;
            this.mapping = mapping;
        }
        this.names = new String[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            names[i] = columns.get(i);
        }
        this.colIndex = new HashMap<>();
        this.vars = new Var[names.length];
        IntStream.range(0, names.length).forEach(i -> {
            colIndex.put(names[i], i);
            vars[i] = MappedVar.byRows(this.source.var(names[i]), this.mapping).withName(names[i]);
        });
    }

    public static MappedFrame newByRow(Frame df, int... mapping) {
        return new MappedFrame(df, Mapping.copy(mapping));
    }

    public static MappedFrame newByRow(Frame df, Mapping mapping) {
        return new MappedFrame(df, mapping);
    }

    public static MappedFrame newByRow(Frame df, Mapping mapping, String varRange) {
        return MappedFrame.newByRow(df, mapping, VRange.of(varRange));
    }

    public static MappedFrame newByRow(Frame df, Mapping mapping, VRange vRange) {
        return new MappedFrame(df, mapping, vRange.parseVarNames(df));
    }

    @Override
    public int rowCount() {
        return mapping.size();
    }

    @Override
    public int varCount() {
        return names.length;
    }

    protected Frame sourceFrame() {
        return source;
    }

    protected Mapping mapping() {
        return mapping;
    }

    @Override
    public String[] varNames() {
        return names;
    }

    @Override
    public int varIndex(String name) {
        if (!colIndex.containsKey(name)) {
            throw new IllegalArgumentException(String.format("var name: %s does not exist", name));
        }
        return colIndex.get(name);
    }

    @Override
    public Var var(int varIndex) {
        return vars[varIndex];
    }

    @Override
    public Var var(String varName) {
        return var(varIndex(varName));
    }

    @Override
    public Frame bindVars(Var... vars) {
        return BoundFrame.newByVars(this, BoundFrame.newByVars(vars));
    }

    @Override
    public Frame bindVars(Frame df) {
        return BoundFrame.newByVars(this, df);
    }

    @Override
    public Frame mapVars(VRange range) {
        return MappedFrame.newByRow(this, Mapping.range(0, this.rowCount()), range);
    }

    @Override
    public Frame addRows(int rowCount) {
        return BoundFrame.newByRows(this, SolidFrame.emptyFrom(this, rowCount));
    }

    @Override
    public Frame bindRows(Frame df) {
        return BoundFrame.newByRows(this, df);
    }

    @Override
    public Frame mapRows(Mapping mapping) {
        return MappedFrame.newByRow(this, mapping);
    }
}
