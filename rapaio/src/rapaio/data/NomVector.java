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

import java.util.*;

/**
 * Nominal vector contains values for nominal or categorical observations.
 * <p/>
 * The domain of the definition is called dictionary and is
 * given at construction time.
 * <p/>
 * This vector accepts two value representation: as labels and as indexes.
 * <p/>
 * Label representation is the natural representation since in experiments
 * the nominal vectors are given as string values.
 * <p/>
 * The index representation is learn based on the canonical form of the
 * term dictionary and is used often for performance reasons instead of
 * label representation, where the actual label value does not matter.
 *
 * @author Aurelian Tutuianu
 */
public class NomVector extends AbstractVector {

    private static final String missingValue = "?";
    private static final int missingIndex = 0;

    int rows = 0;
    List<String> dict;
    int[] data;
    Map<String, Integer> reverse;

    public NomVector() {
        // set the missing value
        this.reverse = new HashMap<>();
        this.reverse.put("?", 0);
        this.dict = new ArrayList<>();
        this.dict.add("?");
        data = new int[0];
        rows = 0;
    }

    public NomVector(int size, String[] dict) {
        this(size, Arrays.asList(dict));
    }

    public NomVector(int size, Collection<String> dict) {
        this();
        for (String next : dict) {
            if (this.dict.contains(next)) continue;
            this.dict.add(next);
            this.reverse.put(next, reverse.size());
        }
        data = new int[size];
        rows = size;
    }

    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = data.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        // minCapacity is usually close to size, so this is a win:
        data = Arrays.copyOf(data, newCapacity);
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    @Override
    public boolean isNominal() {
        return true;
    }

    @Override
    public boolean isMappedVector() {
        return false;
    }

    @Override
    public Vector getSourceVector() {
        return this;
    }

    @Override
    public Mapping getMapping() {
        return null;
    }

    @Override
    public int getRowCount() {
        return rows;
    }

    @Override
    public int getRowId(int row) {
        return row;
    }

    @Override
    public int getIndex(int row) {
        return data[row];
    }

    @Override
    public void setIndex(int row, int value) {
        data[row] = value;
    }

    @Override
    public void addIndex(int value) {
        addLabel(dict.get(value));
    }

    @Override
    public void addIndex(int row, int value) {
        addLabel(row, dict.get(value));
    }

    @Override
    public double getValue(int row) {
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
    public void addValue(int row, double value) {
        addIndex(row, (int) Math.rint(value));
    }

    @Override
    public String getLabel(int row) {
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
        }
        data[row] = idx;
    }

    @Override
    public void addLabel(String label) {
        ensureCapacity(rows + 1);
        if (!reverse.containsKey(label)) {
            dict.add(label);
            reverse.put(label, reverse.size());
        }
        data[rows++] = reverse.get(label);
    }

    @Override
    public void addLabel(int pos, String label) {
        ensureCapacity(rows + 1);
        System.arraycopy(data, pos, data, pos + 1, rows - pos);

        if (!reverse.containsKey(label)) {
            dict.add(label);
            reverse.put(label, reverse.size());
        }
        data[pos] = reverse.get(label);
        rows++;
    }

    @Override
    public String[] getDictionary() {
        return dict.toArray(new String[]{});
    }

    @Override
    public void setDictionary(String[] dict) {
        List<String> oldDict = this.dict;
        Map<String, Integer> oldReverse = this.reverse;

        this.dict = new ArrayList<>();
        this.reverse = new HashMap<>();
        this.dict.add("?");
        this.reverse.put("?", 0);

        for (int i = 0; i < dict.length; i++) {
            if (!reverse.containsKey(dict[i])) {
                this.dict.add(dict[i]);
                this.reverse.put(dict[i], this.reverse.size());
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
    public boolean isMissing(int row) {
        return missingIndex == getIndex(row);
    }

    @Override
    public void setMissing(int row) {
        setIndex(row, missingIndex);
    }

    @Override
    public void remove(int index) {
        int numMoved = rows - index - 1;
        if (numMoved > 0)
            System.arraycopy(data, index + 1, data, index, numMoved);
    }

    @Override
    public void removeRange(int fromIndex, int toIndex) {
        int numMoved = rows - toIndex;
        System.arraycopy(data, toIndex, data, fromIndex, numMoved);
        int newSize = rows - (toIndex - fromIndex);
        rows = newSize;
    }

    public void clear() {
        rows = 0;
    }

    public void trimToSize() {
        if (rows < data.length) {
            data = Arrays.copyOf(data, rows);
        }
    }

    public void ensureCapacity(int minCapacity) {
        if (minCapacity - data.length > 0)
            grow(minCapacity);
    }
}
