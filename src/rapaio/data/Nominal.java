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
 * Nominal var contains values for nominal or categorical observations.
 * <p>
 * The domain of the definition is called dictionary and is
 * given at construction time.
 * <p>
 * This var accepts two value representation: as labels and as indexes.
 * <p>
 * Label representation is the natural representation since in experiments
 * the nominal vectors are given as string values.
 * <p>
 * The index representation is learn based on the canonical form of the
 * term dictionary and is used often for performance reasons instead of
 * label representation, where the actual label value does not matter.
 *
 * @author Aurelian Tutuianu
 */
public class Nominal extends AbstractVar {

    private static final String missingValue = "?";
    private static final int missingIndex = 0;

    int rows = 0;
    List<String> dict;
    short[] data;
    Map<String, Short> reverse;

    public static Nominal newEmpty() {
        return new Nominal();
    }

    public static Nominal newEmpty(int rows, String... dict) {
        return Nominal.newEmpty(rows, Arrays.asList(dict));
    }

    public static Nominal newEmpty(int rows, Collection<String> dict) {
        Nominal nominal = new Nominal();
        for (String next : dict) {
            if (nominal.dict.contains(next)) continue;
            nominal.dict.add(next);
            nominal.reverse.put(next, (short) nominal.reverse.size());
        }
        nominal.data = new short[rows];
        nominal.rows = rows;
        return nominal;
    }

    protected Nominal() {
        // set the missing value
        this.reverse = new HashMap<>();
        this.reverse.put("?", (short) 0);
        this.dict = new ArrayList<>();
        this.dict.add("?");
        data = new short[0];
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
    public boolean isMappedVector() {
        return false;
    }

    @Override
    public Var source() {
        return this;
    }

    @Override
    public Mapping mapping() {
        return null;
    }

    @Override
    public int rowCount() {
        return rows;
    }

    @Override
    public int rowId(int row) {
        return row;
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
        Short idx = reverse.get(value);
        if (idx == null) {
            dict.add(value);
            reverse.put(value, (short) reverse.size());
            idx = (short) (reverse.size() - 1);
        }
        data[row] = idx;
    }

    @Override
    public void addLabel(String label) {
        grow(rows + 1);
        if (!reverse.containsKey(label)) {
            dict.add(label);
            reverse.put(label, (short) (reverse.size()));
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
        Map<String, Short> oldReverse = this.reverse;

        this.dict = new ArrayList<>();
        this.reverse = new HashMap<>();
        this.dict.add("?");
        this.reverse.put("?", (short) 0);

        for (String term : dict) {
            if (!reverse.containsKey(term)) {
                this.dict.add(term);
                this.reverse.put(term, (short) this.reverse.size());
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

    @Override
    public Nominal solidCopy() {
        Nominal copy = Nominal.newEmpty(rowCount(), dictionary());
        for (int i = 0; i < rowCount(); i++) {
            copy.setLabel(i, label(i));
        }
        return copy;
    }

    @Override
    public String toString() {
        return "Nominal[" + rowCount() + "]";
    }
}
