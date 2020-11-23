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

import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POption;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Categorical variable type. The nominal variable type is represented as a string label and/or as an short
 * index value, assigned to each string label. Nominal variable contains values for categorical observations
 * where order of labels is not important.
 * <p>
 * The domain of the definition is called levels and is given at construction time or can be changed latter.
 * This type of variable accepts two value representation: as labels and as indexes.
 * <p>
 * Label representation is the natural representation since in experiments the nominal vectors are given as
 * string values.
 * <p>
 * The index representation is based on the term levels and is used often for performance reasons instead of label
 * representation, where the actual label value does not matter. Even if index values is a short number the
 * order of the indexes for nominal variables is irrelevant.
 * <p>
 * Additionally the nominal variable is limited to 32767 levels, including missing label index.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
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
            nominal.reverse.put(next, (short) nominal.reverse.size());
        }
        nominal.data = new short[rows];
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
    public static final String MISSING_VALUE = "?";
    private static final int missingIndex = 0;

    private int rows;
    private ArrayList<String> dict;
    private short[] data;
    private HashMap<String, Short> reverse;

    private VarNominal() {
        this.reverse = new HashMap<>();
        this.reverse.put("?", (short) 0);
        this.dict = new ArrayList<>();
        this.dict.add("?");
        data = new short[0];
        rows = 0;
    }

    public static Collector<String, VarNominal, VarNominal> collector() {

        return new Collector<>() {
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
                return (left, right) -> (VarNominal) left.bindRows(right).copy();
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
    public VarNominal name(String name) {
        return (VarNominal) super.name(name);
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
        if (value > 128 || value < 0) {
            throw new IllegalArgumentException("Invalid value for nominal index.");
        }
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
        if (value.equals(MISSING_VALUE)) {
            data[row] = missingIndex;
            return;
        }
        if (!reverse.containsKey(value)) {
            if (dict.size() == Short.MAX_VALUE - 1) {
                throw new IllegalStateException("Cannot add new label since dictionary achieved it's maximum size.");
            }
            dict.add(value);
            reverse.put(value, (short) reverse.size());
        }
        data[row] = reverse.get(value);
    }

    @Override
    public void addLabel(String label) {
        grow(rows + 1);
        if (!reverse.containsKey(label)) {
            if (dict.size() == Short.MAX_VALUE - 1) {
                throw new IllegalStateException("Cannot add new label since dictionary achieved it's maximum size.");
            }
            dict.add(label);
            reverse.put(label, (short) reverse.size());
        }
        data[rows++] = reverse.get(label);
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
        this.reverse = new HashMap<>(dict.length);
        this.dict.add("?");
        this.reverse.put("?", (short) 0);

        short[] pos = new short[oldDict.size()];
        for (int i = 0; i < dict.length; i++) {
            String term = dict[i];
            if (!reverse.containsKey(term)) {
                this.dict.add(term);
                this.reverse.put(term, (short) this.reverse.size());
            }
            if (i < oldDict.size())
                pos[i] = this.reverse.get(term);
        }

        for (int i = 0; i < rows; i++) {
            data[i] = pos[data[i]];
        }
    }

    @Override
    public long getLong(int row) {
        throw new OperationNotAvailableException();
    }

    @Override
    public void setLong(int row, long value) {
        throw new OperationNotAvailableException();
    }

    @Override
    public void addLong(long value) {
        throw new OperationNotAvailableException();
    }

    @Override
    public void addInstant(Instant value) {
        throw new OperationNotAvailableException();
    }

    @Override
    public void setInstant(int row, Instant value) {
        throw new OperationNotAvailableException();
    }

    @Override
    public Instant getInstant(int row) {
        throw new OperationNotAvailableException();
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
    public VarNominal copy() {
        return (VarNominal) super.copy();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(rowCount());
        out.writeInt(dict.size());
        for (String factor : dict) {
            out.writeUTF(factor);
        }
        for (int i = 0; i < rowCount(); i++) {
            out.writeShort(data[i]);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException {
        rows = in.readInt();
        dict = new ArrayList<>();
        reverse = new HashMap<>();
        int len = in.readInt();
        for (int i = 0; i < len; i++) {
            dict.add(in.readUTF());
            reverse.put(dict.get(i), (short) i);
        }
        data = new short[rows];
        for (int i = 0; i < rows; i++) {
            data[i] = in.readShort();
        }
    }

    @Override
    protected void textTablePutValue(TextTable tt, int i, int j, int row, Printer printer, POption<?>[] options) {
        tt.textCenter(i, j, getLabel(row));
    }

    @Override
    protected String toStringClassName() {
        return "VarNominal";
    }

    @Override
    protected int toStringDisplayValueCount() {
        return 12;
    }
}
