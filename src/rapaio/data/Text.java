/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Text extends AbstractVar {

    private static final long serialVersionUID = -7130782019269889796L;
    private List<String> values = new ArrayList<>();

    public static Text empty() {
        return new Text(0);
    }

    public static Text empty(int rows) {
        return new Text(rows);
    }

    public static Text copy(String... values) {
        Text text = new Text(0);
        text.values = Arrays.stream(values).collect(Collectors.toList());
        return text;
    }

    public static Text copy(List<String> values) {
        Text text = new Text(0);
        Collections.copy(text.values, values);
        return text;
    }

    public static Text wrap(List<String> values) {
        Text text = new Text(0);
        text.values = values;
        return text;
    }

    //
    // Public static builders
    //

    private Text(int rows) {
        values = new ArrayList<>(rows);
        IntStream.range(0, rows).forEach(i -> values.add(null));
    }

    //
    // private constructor
    //

    public static Text from(int rows, Supplier<String> supplier) {
        Text text = new Text(rows);
        for (int i = 0; i < rows; i++) {
            text.values.set(i, supplier.get());
        }
        return text;
    }

    @Override
    public Text withName(String name) {
        return (Text) super.withName(name);
    }

    @Override
    public VarType type() {
        return VarType.TEXT;
    }

    @Override
    public int rowCount() {
        return values.size();
    }

    @Override
    public void addRows(int rowCount) {
        for (int i = 0; i < rowCount(); i++) {
            addLabel("");
        }
    }

    @Override
    public double value(int row) {
        throw new RuntimeException("This operation is not available for text variables");
    }

    @Override
    public void setValue(int row, double value) {
        throw new RuntimeException("This operation is not available for text variables");
    }

    @Override
    public void addValue(double value) {
        throw new RuntimeException("This operation is not available for text variables");
    }

    @Override
    public int index(int row) {
        throw new RuntimeException("This operation is not available for text variables");
    }

    @Override
    public void setIndex(int row, int value) {
        throw new RuntimeException("This operation is not available for text variables");
    }

    @Override
    public void addIndex(int value) {
        throw new RuntimeException("This operation is not available for text variables");
    }

    @Override
    public String label(int row) {
        return values.get(row);
    }

    @Override
    public void setLabel(int row, String value) {
        values.set(row, value);
    }

    @Override
    public void addLabel(String value) {
        values.add(value);
    }

    @Override
    public String[] levels() {
        return new String[0];
    }

    @Override
    public void setLevels(String[] dict) {
        throw new RuntimeException("This operation is not available for text variables");
    }

    @Override
    public boolean binary(int row) {
        throw new RuntimeException("This operation is not available for text variables");
    }

    @Override
    public void setBinary(int row, boolean value) {
        throw new RuntimeException("This operation is not available for text variables");
    }

    @Override
    public void addBinary(boolean value) {
        throw new RuntimeException("This operation is not available for text variables");
    }

    @Override
    public long stamp(int row) {
        throw new RuntimeException("This operation is not available for text variables");
    }

    @Override
    public void setStamp(int row, long value) {
        throw new RuntimeException("This operation is not available for text variables");
    }

    @Override
    public void addStamp(long value) {
        throw new RuntimeException("This operation is not available for text variables");
    }

    @Override
    public boolean missing(int row) {
        return values.get(row) == null;
    }

    @Override
    public void setMissing(int row) {
        values.set(row, null);
    }

    @Override
    public void addMissing() {
        values.add(null);
    }

    @Override
    public void remove(int row) {
        values.remove(row);
    }

    @Override
    public void clear() {
        values.clear();
    }

    @Override
    public Var newInstance(int rows) {
        return Text.empty(rows);
    }

    @Override
    public Text solidCopy() {
        return (Text) super.solidCopy();
    }
}
