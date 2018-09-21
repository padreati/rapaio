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
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class VarText extends AbstractVar {

    public static VarText empty() {
        return new VarText(0);
    }

    public static VarText empty(int rows) {
        return new VarText(rows);
    }

    public static VarText copy(String... values) {
        VarText text = new VarText(0);
        text.values = Arrays.stream(values).collect(Collectors.toList());
        return text;
    }

    public static VarText copy(List<String> values) {
        VarText text = new VarText(0);
        text.values = new ArrayList<>(values);
        return text;
    }

    public static VarText wrap(List<String> values) {
        VarText text = new VarText(0);
        text.values = values;
        return text;
    }

    public static VarText from(int rows, Supplier<String> supplier) {
        VarText text = new VarText(rows);
        for (int i = 0; i < rows; i++) {
            text.values.set(i, supplier.get());
        }
        return text;
    }

    private static final long serialVersionUID = -7130782019269889796L;
    private List<String> values;

    private VarText(int rows) {
        values = new ArrayList<>(rows);
        for (int i = 0; i < rows; i++) {
            values.add(null);
        }
    }

    @Override
    public VarText withName(String name) {
        return (VarText) super.withName(name);
    }

    @Override
    public VType type() {
        return VType.TEXT;
    }

    @Override
    public int rowCount() {
        return values.size();
    }

    @Override
    public void addRows(int rowCount) {
        for (int i = 0; i < rowCount; i++) {
            values.add(null);
        }
    }

    @Override
    public void removeRow(int row) {
        values.remove(row);
    }

    @Override
    public void clearRows() {
        values.clear();
    }

    private IllegalStateException notImplemented() {
        return new IllegalStateException("This operation is not available for text variables");
    }

    @Override
    public double getDouble(int row) {
        throw notImplemented();
    }

    @Override
    public void setDouble(int row, double value) {
        throw notImplemented();
    }

    @Override
    public void addDouble(double value) {
        throw notImplemented();
    }

    @Override
    public int getInt(int row) {
        throw notImplemented();
    }

    @Override
    public void setInt(int row, int value) {
        throw notImplemented();
    }

    @Override
    public void addInt(int value) {
        throw notImplemented();
    }

    @Override
    public String getLabel(int row) {
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
    public List<String> levels() {
        throw notImplemented();
    }

    @Override
    public void setLevels(String[] dict) {
        throw notImplemented();
    }

    @Override
    public boolean getBoolean(int row) {
        throw notImplemented();
    }

    @Override
    public void setBoolean(int row, boolean value) {
        throw notImplemented();
    }

    @Override
    public void addBoolean(boolean value) {
        throw notImplemented();
    }

    @Override
    public long getLong(int row) {
        throw notImplemented();
    }

    @Override
    public void setLong(int row, long value) {
        throw notImplemented();
    }

    @Override
    public void addLong(long value) {
        throw notImplemented();
    }

    @Override
    public boolean isMissing(int row) {
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
    public VarText newInstance(int rows) {
        return VarText.empty(rows);
    }

    @Override
    public VarText solidCopy() {
        VarText copy = new VarText(0).withName(name());
        copy.values = new ArrayList<>(values);
        return copy;
    }

    @Override
    public String toString() {
        return "VarText[rowCount:" + values.size() + "]";
    }
}
