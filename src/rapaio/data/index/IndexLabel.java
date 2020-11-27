/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

package rapaio.data.index;

import rapaio.data.Frame;
import rapaio.data.Index;
import rapaio.data.Var;
import rapaio.data.VarNominal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/19/20.
 */
public class IndexLabel implements Index<String> {

    private static final long serialVersionUID = -1426561398594993638L;

    public static IndexLabel fromLabelValues(List<String> values) {
        return new IndexLabel(values);
    }

    public static IndexLabel fromLabelValues(String... values) {
        return new IndexLabel(Arrays.asList(values));
    }

    public static IndexLabel fromVarLevels(boolean withMissing, Frame df, String varName) {
        return fromVarLevels(withMissing, df.rvar(varName));
    }

    public static IndexLabel fromVarLevels(boolean withMissing, Var v) {
        List<String> levels;
        switch (v.type()) {
            case NOMINAL:
                levels = v.levels();
                if (!withMissing) {
                    levels = levels.subList(1, levels.size());
                }
                break;
            case BINARY:
                if (withMissing) {
                    levels = Arrays.asList("?", "0", "1");
                } else {
                    levels = Arrays.asList("0", "1");
                }
                break;
            default:
                throw new IllegalArgumentException("Builder from levels not available for this type of variable.");
        }
        return new IndexLabel(levels);
    }

    private final List<String> values;
    private final HashMap<String, Integer> reverse;

    private IndexLabel(List<String> values) {
        this.values = new ArrayList<>();
        this.reverse = new HashMap<>();
        for (String value : values) {
            if (reverse.containsKey(value)) {
                continue;
            }
            this.values.add(value);
            reverse.put(value, reverse.size());
        }
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean containsValue(String value) {
        return reverse.containsKey(value);
    }

    @Override
    public boolean containsValue(Var v, int row) {
        return reverse.containsKey(v.getLabel(row));
    }

    @Override
    public boolean containsValue(Frame df, String varName, int row) {
        return reverse.containsKey(df.getLabel(row, varName));
    }

    @Override
    public int getIndex(String value) {
        return reverse.getOrDefault(String.valueOf(value), -1);
    }

    @Override
    public int getIndex(Var v, int row) {
        return reverse.getOrDefault(v.getLabel(row), -1);
    }

    @Override
    public int getIndex(Frame df, String varName, int row) {
        return reverse.getOrDefault(df.getLabel(row, varName), -1);
    }

    @Override
    public List<Integer> getIndexList(Var v) {
        return v.stream()
                .filter(s -> reverse.containsKey(s.getLabel()))
                .map(s -> reverse.get(s.getLabel()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Integer> getIndexList(Frame df, String varName) {
        return df.rvar(varName).stream()
                .filter(s -> reverse.containsKey(s.getLabel()))
                .map(s -> reverse.get(s.getLabel()))
                .collect(Collectors.toList());
    }

    @Override
    public String getValue(int pos) {
        return values.get(pos);
    }

    @Override
    public String getValue(Var v, int row) {
        String value = v.getLabel(row);
        return reverse.containsKey(value) ? value : VarNominal.MISSING_VALUE;
    }

    @Override
    public String getValue(Frame df, String varName, int row) {
        String value = df.getLabel(row, varName);
        return reverse.containsKey(value) ? value : VarNominal.MISSING_VALUE;
    }

    @Override
    public List<String> getValues() {
        return values;
    }

    @Override
    public List<String> getValueList(Var v) {
        return v.stream().mapToString().filter(reverse::containsKey).collect(Collectors.toList());
    }

    @Override
    public List<String> getValueList(Frame df, String varName) {
        return df.rvar(varName).stream().mapToString().filter(reverse::containsKey).collect(Collectors.toList());
    }

    @Override
    public String getValueString(int pos) {
        return values.get(pos);
    }

    @Override
    public List<String> getValueStrings() {
        return values;
    }
}
