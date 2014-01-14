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
    Map<String, Integer> index;

    public NomVector(int size, String[] dict) {
        this(size, Arrays.asList(dict));
    }

    public NomVector(int size, Collection<String> dict) {

        // set the missing value
        this.index = new HashMap<>();
        this.index.put("?", 0);
        this.dict = new ArrayList<>();
        this.dict.add("?");

        Iterator<String> it = dict.iterator();
        while (it.hasNext()) {
            String next = it.next();
            if (this.dict.contains(next)) continue;
            this.dict.add(next);
            this.index.put(next, index.size());
        }
        data = new int[size];
        rows = size;
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

    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = data.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        // minCapacity is usually close to size, so this is a win:
        data = Arrays.copyOf(data, newCapacity);
    }

    // Positional Access Operations

    public void add(int x) {
        ensureCapacity(rows + 1);
        data[rows++] = x;
    }

    public void add(int index, int element) {
        rangeCheck(index);

        ensureCapacity(rows + 1);
        System.arraycopy(data, index, data, index + 1, rows - index);
        data[index] = element;
        rows++;
    }

    public double remove(int index) {
        rangeCheck(index);
        double oldValue = data[index];
        int numMoved = rows - index - 1;
        if (numMoved > 0)
            System.arraycopy(data, index + 1, data, index, numMoved);
        return oldValue;
    }

    public void clear() {
        rows = 0;
    }

    public boolean addAll(Collection<? extends String> c) {
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacity(rows + numNew);
        System.arraycopy(a, 0, data, rows, numNew);
        rows += numNew;
        return numNew != 0;
    }

    public boolean addAll(int index, Collection<? extends String> c) {
        rangeCheck(index);

        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacity(rows + numNew);

        int numMoved = rows - index;
        if (numMoved > 0)
            System.arraycopy(data, index, data, index + numNew, numMoved);

        System.arraycopy(a, 0, data, index, numNew);
        rows += numNew;
        return numNew != 0;
    }

    protected void removeRange(int fromIndex, int toIndex) {
        int numMoved = rows - toIndex;
        System.arraycopy(data, toIndex, data, fromIndex,
                numMoved);

        // clear to let GC do its work
        int newSize = rows - (toIndex - fromIndex);
        rows = newSize;
    }

    private void rangeCheck(int index) {
        if (index > rows || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + rows;
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
    public double getValue(int row) {
        return data[row];
    }

    @Override
    public void setValue(int row, double value) {
        setIndex(row, (int) Math.rint(value));
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
        Integer idx = index.get(value);
        if (idx == null) {
            dict.add(value);
            index.put(value, index.size());
        }
        data[row] = idx;
    }

    @Override
    public String[] getDictionary() {
        return dict.toArray(new String[]{});
    }

    @Override
    public boolean isMissing(int row) {
        return missingIndex == getIndex(row);
    }

    @Override
    public void setMissing(int row) {
        setIndex(row, missingIndex);
    }
}
