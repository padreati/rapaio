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

package rapaio.experiment.data;

import rapaio.data.Frame;
import rapaio.data.MappedFrame;
import rapaio.data.Mapping;
import rapaio.data.Var;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public final class FrameJoin {

    public static Frame leftJoinComplete(Frame dst, Frame src, String... keyFields) {

        HashMap<KeyValue, Integer> map = new HashMap<>();
        KeyField srcKeyField = new KeyField(src, keyFields);
        for (int i = 0; i < src.getRowCount(); i++) {
            KeyValue key = new KeyValue(srcKeyField, i);
            if(map.containsKey(key)) {
                throw new IllegalArgumentException("We found duplicate keys in source frame.");
            }
            map.put(key, i);
        }

        List<Integer> dstRows = new ArrayList<>();
        List<Integer> srcRows = new ArrayList<>();

        KeyField dstKeyField = new KeyField(dst, keyFields);

        for (int i = 0; i < dst.getRowCount(); i++) {
            KeyValue key = new KeyValue(dstKeyField, i);
            if(map.containsKey(key)) {
                dstRows.add(i);
                srcRows.add(map.get(key));
            }
        }

        MappedFrame dstMap = MappedFrame.byRow(dst, Mapping.copy(dstRows));
        MappedFrame srcMap = MappedFrame.byRow(src, Mapping.copy(srcRows));
        return dstMap.bindVars(srcMap.removeVars(keyFields));
    }
}

class KeyField {

    final Frame df;
    final String[] keyFields;
    final Var[] keyVars;

    public KeyField(Frame df, String[] keyFields) {
        this.df = df;
        this.keyFields = keyFields;
        keyVars = new Var[keyFields.length];
        for (int i = 0; i < keyFields.length; i++) {
            keyVars[i] = df.getVar(keyFields[i]);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyField keyField = (KeyField) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(keyFields, keyField.keyFields);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(keyVars);
    }
}

class KeyValue implements Comparable<KeyValue> {

    final KeyField keyField;
    final int row;

    public KeyValue(KeyField keyField, int row) {
        this.keyField = keyField;
        this.row = row;
    }


    @Override
    public int compareTo(KeyValue o) {
        for (int i = 0; i < keyField.keyVars.length; i++) {

            if (!keyField.keyVars[i].getName().equals(o.keyField.keyVars[i].getName())) {
                throw new IllegalArgumentException("Key field names do not match.");
            }
            if (!keyField.keyVars[i].getType().equals(o.keyField.keyVars[i].getType())) {
                throw new IllegalArgumentException("Key field types do not match.");
            }
            int comp = 0;
            Var v1 = keyField.keyVars[i];
            Var v2 = o.keyField.keyVars[i];

            switch (keyField.keyVars[i].getType()) {
                case BINARY:
                    comp = Boolean.compare(v1.getBinary(row), v2.getBinary(o.row));
                    break;
                case TEXT:
                case NOMINAL:
                    comp = v1.getLabel(row).compareTo(v2.getLabel(o.row));
                    break;
                case INDEX:
                case ORDINAL:
                    comp = Integer.compare(v1.getIndex(row), v2.getIndex(o.row));
                    break;
                case STAMP:
                    comp = Long.compare(v1.getStamp(row), v2.getStamp(o.row));
                    break;
                case NUMERIC:
                    comp = Double.compare(v1.getValue(row), v2.getValue(o.row));
            }
            if (comp != 0) {
                return comp;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyValue keyValue = (KeyValue) o;
        return compareTo(keyValue)==0;
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (Var keyVar : keyField.keyVars) {
            result = 31 * result + keyVar.getLabel(row).hashCode();
        }
        return result;
    }
}