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

package rapaio.data.join;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import rapaio.data.Frame;
import rapaio.data.VRange;

import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/17/18.
 */
@Deprecated
public class Join {

    public static Frame from(Frame dfLeft, Frame dfRight, VRange leftKeys, VRange rightKeys, Type type) {
        return new Join(dfLeft, dfRight, leftKeys, rightKeys, type).join();
    }


    public enum Type {
        LEFT,
        RIGHT,
        INNER,
        OUTER
    }

    private static final String SEP = "_";

    private final Frame dfLeft;
    private final Frame dfRight;
    private final List<String> leftVarNames;
    private final List<String> rightVarNames;
    private final Type type;


    private Join(Frame dfLeft, Frame dfRight, VRange leftKeys, VRange rightKeys, Type type) {
        this.dfLeft = dfLeft;
        this.dfRight = dfRight;
        this.leftVarNames = leftKeys.parseVarNames(dfLeft);
        this.rightVarNames = rightKeys.parseVarNames(dfRight);
        this.type = type;
    }


    private Frame join() {
        validateKeys();

        Object2IntOpenHashMap<String> idMap = new Object2IntOpenHashMap<>();
        Int2ObjectOpenHashMap<IntList> leftIdToRows = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<IntList> rightIdToRows = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<String> leftRowToId = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<String> rightRowToId = new Int2ObjectOpenHashMap<>();

        process(idMap, leftIdToRows, leftRowToId, dfLeft, leftVarNames);
        process(idMap, rightIdToRows, rightRowToId, dfRight, rightVarNames);

        // TODO depending on join check additional constraints on unique and join after
        return null;
    }

    private void process(Object2IntOpenHashMap<String> idMap, Int2ObjectOpenHashMap<IntList> idToRows, Int2ObjectOpenHashMap<String> rowToId, Frame df, List<String> varNames) {
        for (int i = 0; i < df.rowCount(); i++) {

            StringBuilder sb = new StringBuilder();
            for (String varName : varNames) {
                sb.append(varName).append(SEP).append(df.getLabel(i, varName)).append(SEP);
            }
            String key = sb.toString();
            idMap.putIfAbsent(key, idMap.size());
            int id = idMap.getInt(key);
            idToRows.putIfAbsent(id, new IntArrayList());
            idToRows.get(id).add(i);
            rowToId.put(i, key);
        }
    }

    private void validateKeys() {
        // compare size of keys
        if (leftVarNames.size() != rightVarNames.size()) {
            throw new IllegalArgumentException("Number of keys differ.");
        }
        // compare key types
        for (int i = 0; i < leftVarNames.size(); i++) {
            if (dfLeft.type(leftVarNames.get(i)) != dfRight.type(rightVarNames.get(i))) {
                throw new IllegalArgumentException(String.format(
                        "Variable types differ; left: %s [ %s ], right: %s [ %s ]",
                        leftVarNames.get(i), dfLeft.type(leftVarNames.get(i)),
                        rightVarNames.get(i), dfRight.type(rightVarNames.get(i))
                ));
            }
        }
    }
}
