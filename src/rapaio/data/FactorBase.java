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

import rapaio.data.mapping.Mapping;

import java.util.*;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
@Deprecated
public abstract class FactorBase extends AbstractVar {

    protected static final String missingValue = "?";
    protected static final int missingIndex = 0;

    int rows = 0;
    List<String> dict;
    int[] data;
    Map<String, Integer> reverse;

    protected FactorBase() {
        // set the missing value
        this.reverse = new HashMap<>();
        this.reverse.put("?", 0);
        this.dict = new ArrayList<>();
        this.dict.add("?");
        data = new int[0];
        rows = 0;
    }

    @Override
    public VarType type() {
        return VarType.NOMINAL;
    }

    private void grow(int minCapacity) {
        if (minCapacity - data.length <= 0) return;

        // overflow-conscious code
        int oldCapacity = data.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        // minCapacity is usually close to size, so this is a win:
        data = Arrays.copyOf(data, newCapacity);
    }

    @Override
    public boolean isMapped() {
        return false;
    }

    @Override
    public Var source() {
        return this;
    }

    @Override
    public Mapping mapping() {
        return Mapping.newSolidMap(rowCount());
    }

    @Override
    public int rowCount() {
        return rows;
    }

    @Override
    public int index(int row) {
        return data[row];
    }

    @Override
    public void setIndex(int row, int value) {
        data[row] = (short) value;
    }

    @Override
    public void addIndex(int value) {
        addLabel(dict.get(value));
    }

    @Override
    public double value(int row) {
        return data[row];
    }

    @Override
    public void setValue(int row, double value) {
        setIndex(row, (int) Math.rint(value));
    }

    @Override
    public void addValue(double value) {
        addIndex((int) Math.rint(value));
    }

    @Override
    public String label(int row) {
        return dict.get(data[row]);
    }

    @Override
    public void setLabel(int row, String value) {
        if (value.equals(missingValue)) {
            data[row] = missingIndex;
            return;
        }
        Integer idx = reverse.get(value);
        if (idx == null) {
            dict.add(value);
            reverse.put(value, reverse.size());
            idx = reverse.size() - 1;
        }
        data[row] = idx;
    }

    @Override
    public void addLabel(String label) {
        grow(rows + 1);
        if (!reverse.containsKey(label)) {
            dict.add(label);
            reverse.put(label, reverse.size());
        }
        data[rows++] = reverse.get(label);
    }

    @Override
    public String[] dictionary() {
        return dict.toArray(new String[dict.size()]);
    }

    @Override
    public void setDictionary(String[] dict) {
        List<String> oldDict = this.dict;
        Map<String, Integer> oldReverse = this.reverse;

        this.dict = new ArrayList<>();
        this.reverse = new HashMap<>();
        this.dict.add("?");
        this.reverse.put("?", 0);

        for (String term : dict) {
            if (!reverse.containsKey(term)) {
                this.dict.add(term);
                this.reverse.put(term, this.reverse.size());
            }
        }

        for (int i = 0; i < rows; i++) {
            if (!this.reverse.containsKey(oldDict.get(data[i]))) {
                this.dict = oldDict;
                this.reverse = oldReverse;
                throw new IllegalArgumentException("new dictionary does not contains all old labels");
            }
        }

        for (int i = 0; i < rows; i++) {
            data[i] = this.reverse.get(oldDict.get(data[i]));
        }
    }

    @Override
    public boolean binary(int row) {
        throw new IllegalArgumentException("This call is not allowed");
    }

    @Override
    public void setBinary(int row, boolean value) {
        throw new IllegalArgumentException("This call is not allowed");
    }

    @Override
    public void addBinary(boolean value) {
        throw new IllegalArgumentException("This call is not allowed");
    }

    @Override
    public long stamp(int row) {
        throw new IllegalArgumentException("This call is not allowed");
    }

    @Override
    public void setStamp(int row, long value) {
        throw new IllegalArgumentException("This call is not allowed");
    }

    @Override
    public void addStamp(long value) {
        throw new IllegalArgumentException("This call is not allowed");
    }

    @Override
    public boolean missing(int row) {
        return missingIndex == index(row);
    }

    @Override
    public void setMissing(int row) {
        setIndex(row, missingIndex);
    }

    @Override
    public void addMissing() {
        addIndex(missingIndex);
    }

    @Override
    public void remove(int index) {
        int numMoved = rows - index - 1;
        if (numMoved > 0)
            System.arraycopy(data, index + 1, data, index, numMoved);
    }

    public void clear() {
        rows = 0;
    }
}
