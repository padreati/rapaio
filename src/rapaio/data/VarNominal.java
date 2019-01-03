/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Categorical variable type. The nominal variable type is represented as a string label and/or as an integer 
 * index value, assigned to each string label.
 *
 * Nominal var contains values for categorical observations where order of labels is not important.
 * <p>
 * The domain of the definition is called levels and is given at construction time or can be changed latter.
 * <p>
 * This type of variable accepts two value representation: as labels and as indexes.
 * <p>
 * Label representation is the natural representation since in experiments
 * the nominal vectors are given as string values.
 * <p>
 * The index representation is learn based on the term levels and is used often for performance
 * reasons instead of label representation, where the actual label value does not matter.
 * <p>
 * Even if index values is an integer number the order of the indexes for
 * nominal variables is irrelevant.
 * 
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public final class VarNominal extends AbstractVar {

    /**
     * Builds a new empty nominal variable
     *
     * @return new variable instance of nominal type
     */
    public static VarNominal empty() {
        return new VarNominal();
    }

    /**
     * Builds a new nominal variable of given size, with given term levels, filled with missing values.
     *
     * @param rows variable size
     * @param dict term levels
     * @return new variable instance of nominal type
     */
    public static VarNominal empty(int rows, String... dict) {
        return VarNominal.empty(rows, Arrays.asList(dict));
    }

    /**
     * Builds a new nominal variable of given size, with given term levels, filled with missing values.
     *
     * @param rows variable size
     * @param dict term levels
     * @return new variable instance of nominal type
     */
    public static VarNominal empty(int rows, List<String> dict) {
        VarNominal nominal = new VarNominal();
        HashSet<String> used = new HashSet<>();
        used.add("?");
        for (String next : dict) {
            if (used.contains(next)) continue;
            used.add(next);
            nominal.dict.add(next);
            nominal.reverse.put(next, nominal.reverse.size());
        }
        nominal.data = new int[rows];
        nominal.rows = rows;
        return nominal;
    }

    public static VarNominal copy(String... values) {
        VarNominal nominal = VarNominal.empty();
        for (String value : values)
            nominal.addLabel(value);
        return nominal;
    }

    public static VarNominal copy(List<String> values) {
        VarNominal nominal = VarNominal.empty();
        for (String value : values)
            nominal.addLabel(value);
        return nominal;
    }

    public static VarNominal from(int rows, Function<Integer, String> func, String... dict) {
        VarNominal nominal = VarNominal.empty(rows, dict);
        for (int i = 0; i < rows; i++) {
            nominal.setLabel(i, func.apply(i));
        }
        return nominal;
    }

    private static final long serialVersionUID = -7541719735879481349L;
    private static final String missingValue = "?";
    private static final int missingIndex = 0;

    private int rows;
    private ArrayList<String> dict;
    private int[] data;
    private Object2IntMap<String> reverse;

    private VarNominal() {
        this.reverse = new Object2IntOpenHashMap<>();
        this.reverse.put("?", 0);
        this.dict = new ArrayList<>();
        this.dict.add("?");
        data = new int[0];
        rows = 0;
    }
    
    public static Collector<String, VarNominal, VarNominal> collector() {

        return new Collector<String, VarNominal, VarNominal>() {
            @Override
            public Supplier<VarNominal> supplier() {
                return VarNominal::empty;
            }

            @Override
            public BiConsumer<VarNominal, String> accumulator() {
                return VarNominal::addLabel;
            }

            @Override
            public BinaryOperator<VarNominal> combiner() {
                return (left, right) -> (VarNominal) left.bindRows(right).solidCopy();
            }

            @Override
            public Function<VarNominal, VarNominal> finisher() {
                return var -> var;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return new HashSet<>();
            }
        };
    }

    @Override
    public VarNominal withName(String name) {
        return (VarNominal) super.withName(name);
    }

    @Override
    public VType type() {
        return VType.NOMINAL;
    }

    @Override
    public void addRows(int rowCount) {
        grow(rows + rowCount);
        for (int i = 0; i < rowCount; i++) {
            data[rows + i] = 0;
        }
        rows += rowCount;
    }

    @Override
    public void removeRow(int index) {
        int numMoved = rows - index - 1;
        if (numMoved > 0) {
            System.arraycopy(data, index + 1, data, index, numMoved);
            rows--;
        }
    }

    public void clearRows() {
        rows = 0;
    }

    private void grow(int minCapacity) {
        if (minCapacity - data.length <= 0) return;

        int oldCapacity = data.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        data = Arrays.copyOf(data, newCapacity);
    }

    @Override
    public int rowCount() {
        return rows;
    }

    @Override
    public int getInt(int row) {
        return data[row];
    }

    @Override
    public void setInt(int row, int value) {
        data[row] = (short) value;
    }

    @Override
    public void addInt(int value) {
        addLabel(dict.get(value));
    }

    @Override
    public double getDouble(int row) {
        return data[row];
    }

    @Override
    public void setDouble(int row, double value) {
        setInt(row, (int) Math.rint(value));
    }

    @Override
    public void addDouble(double value) {
        addInt((int) Math.rint(value));
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
        if (!reverse.containsKey(value)) {
            dict.add(value);
            reverse.put(value, reverse.size());
            data[row] = reverse.size() - 1;
        } else {
            data[row] = reverse.getInt(value);
        }
    }

    @Override
    public void addLabel(String label) {
        grow(rows + 1);
        if (!reverse.containsKey(label)) {
            dict.add(label);
            reverse.put(label, reverse.size());
        }
        data[rows++] = reverse.getInt(label);
    }

    @Override
    public List<String> levels() {
        return dict;
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
        this.reverse = new Object2IntOpenHashMap<>(dict.length);
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
                pos[i] = this.reverse.getInt(term);
        }

        for (int i = 0; i < rows; i++) {
            data[i] = pos[data[i]];
        }
    }

    private IllegalStateException notImplementedException() {
        return new IllegalStateException("This operation is not available for nominal variables");
    }

    @Override
    public long getLong(int row) {
        throw notImplementedException();
    }

    @Override
    public void setLong(int row, long value) {
        throw notImplementedException();
    }

    @Override
    public void addLong(long value) {
        throw notImplementedException();
    }

    @Override
    public boolean isMissing(int row) {
        return missingIndex == getInt(row);
    }

    @Override
    public void setMissing(int row) {
        setInt(row, missingIndex);
    }

    @Override
    public void addMissing() {
        addInt(missingIndex);
    }

    @Override
    public Var newInstance(int rows) {
        return VarNominal.empty(rows, levels());
    }

    @Override
    public VarNominal solidCopy() {
        return (VarNominal) super.solidCopy();
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
        reverse = new Object2IntOpenHashMap<>();
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

    @Override
    protected String stringClassName() {
        return "VarNominal";
    }

    @Override
    protected int stringPrefix() {
        return 10;
    }
}
