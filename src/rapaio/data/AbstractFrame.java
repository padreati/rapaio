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

import rapaio.data.collect.FIterator;
import rapaio.data.mapping.MappedFrame;
import rapaio.data.mapping.Mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Base class for a frame, which provides behavior for the utility
 * access methods based on row and column indexes.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public abstract class AbstractFrame implements Frame {

    @Override
    public double value(int row, int col) {
        return col(col).value(row);
    }

    @Override
    public double value(int row, String colName) {
        return col(colName).value(row);
    }

    @Override
    public void setValue(int row, int col, double value) {
        col(col).setValue(row, value);
    }

    @Override
    public void setValue(int row, String colName, double value) {
        col(colName).setValue(row, value);
    }

    @Override
    public int index(int row, int col) {
        return col(col).index(row);
    }

    @Override
    public int index(int row, String colName) {
        return col(colName).index(row);
    }

    @Override
    public void setIndex(int row, int col, int value) {
        col(col).setIndex(row, value);
    }

    @Override
    public void setIndex(int row, String colName, int value) {
        col(colName).setIndex(row, value);
    }

    @Override
    public String label(int row, int col) {
        return col(col).label(row);
    }

    @Override
    public String label(int row, String colName) {
        return col(colName).label(row);
    }

    @Override
    public void setLabel(int row, int col, String value) {
        col(col).setLabel(row, value);
    }

    @Override
    public void setLabel(int row, String colName, String value) {
        col(colName).setLabel(row, value);
    }

    @Override
    public boolean missing(int row, int col) {
        return col(col).missing(row);
    }

    @Override
    public boolean missing(int row, String colName) {
        return col(colName).missing(row);
    }

    @Override
    public boolean missing(int row) {
        for (String colName : colNames()) {
            if (col(colName).missing(row)) return true;
        }
        return false;
    }

    @Override
    public void setMissing(int row, int col) {
        col(col).setMissing(row);
    }

    @Override
    public void setMissing(int row, String colName) {
        col(colName).setMissing(row);
    }

    @Override
    public FIterator iterator(boolean complete) {
        return new FrameIterator(complete, rowCount(), this);
    }

    @Override
    public FIterator iterator() {
        return iterator(false);
    }
}

class FrameIterator implements FIterator {

    private static final String DEFAULT_MAPPING_KEY = "$$DEFAULT$$";
    final boolean complete;
    final int size;
    final Frame frame;
    private final HashMap<String, Mapping> mappings = new HashMap<>();

    int pos = -1;
    int cyclePos = -1;

    FrameIterator(boolean complete, int size, Frame frame) {
        this.complete = complete;
        this.size = size;
        this.frame = frame;
    }

    @Override
    public boolean next() {
        while (pos < size - 1) {
            pos++;
            cyclePos++;
            if (cyclePos >= frame.rowCount()) {
                cyclePos = 0;
            }
            if (complete && frame.missing(cyclePos)) continue;
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        pos = -1;
        cyclePos = -1;
    }

    @Override
    public int getRowId() {
        return frame.rowId(cyclePos);
    }

    @Override
    public int row() {
        return cyclePos;
    }

    @Override
    public double getValue(int col) {
        return frame.value(cyclePos, col);
    }

    @Override
    public double value(String colName) {
        return frame.value(cyclePos, colName);
    }

    @Override
    public void setValue(int col, double value) {
        frame.setValue(cyclePos, col, value);
    }

    @Override
    public void setValue(String colName, double value) {
        frame.setValue(cyclePos, colName, value);
    }

    @Override
    public int getIndex(int col) {
        return frame.index(cyclePos, col);
    }

    @Override
    public int getIndex(String colName) {
        return frame.index(cyclePos, colName);
    }

    @Override
    public void setIndex(int col, int value) {
        frame.setIndex(cyclePos, col, value);
    }

    @Override
    public void setIndex(String colName, int value) {
        frame.setIndex(cyclePos, colName, value);
    }

    @Override
    public String getLabel(int col) {
        return frame.label(cyclePos, col);
    }

    @Override
    public String label(String colName) {
        return frame.label(cyclePos, colName);
    }

    @Override
    public void setLabel(int col, String value) {
        frame.setLabel(cyclePos, col, value);
    }

    @Override
    public void setLabel(String colName, String value) {
        frame.setLabel(cyclePos, colName, value);
    }

    @Override
    public boolean isMissing(int col) {
        return frame.missing(cyclePos, col);
    }

    @Override
    public boolean isMissing(String colName) {
        return frame.missing(cyclePos, colName);
    }

    @Override
    public boolean isMissing() {
        return frame.missing(cyclePos);
    }

    @Override
    public void setMissing(int col) {
        frame.setMissing(cyclePos, col);
    }

    @Override
    public void setMissing(String colName) {
        frame.setMissing(cyclePos, colName);
    }

    @Override
    public void appendToMapping() {
        if (!mappings.containsKey(DEFAULT_MAPPING_KEY)) {
            mappings.put(DEFAULT_MAPPING_KEY, new Mapping());
        }
        mappings.get(DEFAULT_MAPPING_KEY).add(getRowId());
    }

    @Override
    public void appendToMapping(String key) {
        if (!mappings.containsKey(key)) {
            mappings.put(key, new Mapping());
        }
        mappings.get(key).add(getRowId());
    }

    @Override
    public void appendToMapping(int key) {
        appendToMapping(String.valueOf(key));
    }

    @Override
    public int getMappingsCount() {
        return mappings.size();
    }

    @Override
    public Set<String> getMappingsKeys() {
        return mappings.keySet();
    }

    @Override
    public Mapping getMapping() {
        return mappings.get(DEFAULT_MAPPING_KEY);
    }

    @Override
    public Mapping getMapping(String key) {
        return mappings.get(key);
    }

    @Override
    public Mapping getMapping(int key) {
        return mappings.get(String.valueOf(key));
    }

    @Override
    public Frame getMappedFrame() {
        return new MappedFrame(frame.sourceFrame(), getMapping());
    }

    @Override
    public Frame getMappedFrame(String key) {
        return new MappedFrame(frame.sourceFrame(), getMapping(key));
    }

    @Override
    public Frame getMappedFrame(int key) {
        return getMappedFrame(String.valueOf(key));
    }

    @Override
    public Map<String, Frame> getMappedFrames() {
        Map<String, Frame> map = new HashMap<>();
        for (String key : getMappingsKeys()) {
            map.put(key, getMappedFrame(key));
        }
        return map;
    }
}