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

import rapaio.sys.WS;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * Base class used to implement categorical variable types: nominal and ordinal.
 * From the implementation point of view the only difference between nominal and ordinal is
 * the fact that ordinal variables assigns a meaning to the order of the labels.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public abstract class FactorBase extends AbstractVar {

    private static final long serialVersionUID = -7541719735879481349L;

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

    protected void grow(int minCapacity) {
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
    public String[] levels() {
        return dict.toArray(new String[dict.size()]);
    }

    @Override
    public void setLevels(String... dict) {
        List<String> oldDict = this.dict;
        if (dict.length > 0 && !dict[0].equals("?")) {
            String[] newDict = new String[dict.length + 1];
            newDict[0] = "?";
            System.arraycopy(dict, 0, newDict, 1, dict.length);
            dict = newDict;
        }

        if (this.dict.size() > dict.length) {
            throw new IllegalArgumentException("new levels does not contains all old labels");
        }

        this.dict = new ArrayList<>();
        this.reverse = new HashMap<>();
        this.dict.add("?");
        this.reverse.put("?", 0);

        int[] pos = new int[oldDict.size()];
        for (int i = 0; i < dict.length; i++) {
            String term = dict[i];
            if (!reverse.containsKey(term)) {
                this.dict.add(term);
                this.reverse.put(term, this.reverse.size());
            }
            if (i < oldDict.size())
                pos[i] = this.reverse.get(term);
        }

        for (int i = 0; i < rows; i++) {
            data[i] = pos[data[i]];
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
        if (numMoved > 0) {
            System.arraycopy(data, index + 1, data, index, numMoved);
            rows--;
        }
    }

    public void clear() {
        rows = 0;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(rowCount());
        out.writeInt(dict.size());
        for (String factor : dict) {
            out.writeUTF(factor);
        }
        for (int i = 0; i < rowCount(); i++) {
            out.writeInt(data[i]);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        rows = in.readInt();
        dict = new ArrayList<>();
        reverse = new HashMap<>();
        int len = in.readInt();
        for (int i = 0; i < len; i++) {
            dict.add(in.readUTF());
            reverse.put(dict.get(i), i);
        }
        data = new int[rows];
        for (int i = 0; i < rows; i++) {
            data[i] = in.readInt();
        }
    }
}
