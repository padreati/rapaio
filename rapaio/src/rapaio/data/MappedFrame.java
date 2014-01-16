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

import java.util.HashMap;

/**
 * A frame which is learn on the base of another frame with
 * the row order and row selection specified by a
 * mapping give at construction time.
 * <p/>
 * This frame does not hold actual values, it delegate the behavior
 * to the wrapped frame, thus the wrapping affects only the rowCount
 * selected anf the order of these rowCount.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class MappedFrame extends AbstractFrame {

    private final Mapping mapping;
    private final Frame source;
    private final HashMap<Integer, Vector> vectors = new HashMap<>();

    public MappedFrame(Frame df, Mapping mapping) {
        if (df.isMappedFrame()) {
            throw new IllegalArgumentException("Not allowed mapped frames as source");
        }
        this.mapping = mapping;
        this.source = df;
    }

    @Override
    public int rowCount() {
        return mapping.size();
    }

    @Override
    public int colCount() {
        return source.colCount();
    }

    @Override
    public int rowId(int row) {
        return mapping.get(row);
    }

    @Override
    public boolean isMappedFrame() {
        return true;
    }

    @Override
    public Frame sourceFrame() {
        return source;
    }

    @Override
    public String[] colNames() {
        return source.colNames();
    }

    @Override
    public int colIndex(String name) {
        return source.colIndex(name);
    }

    @Override
    public Vector col(int col) {
        if (!vectors.containsKey(col)) {
            vectors.put(col, new MappedVector(source.col(col), mapping));
        }
        return vectors.get(col);
    }

    @Override
    public Vector col(String name) {
        return col(colIndex(name));
    }
}
