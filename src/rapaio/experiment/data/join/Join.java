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

package rapaio.experiment.data.join;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import rapaio.core.SamplingTools;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.SolidFrame;
import rapaio.data.VRange;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/17/18.
 */
@Deprecated
public class Join {

    public static Frame from(Frame dfLeft, Frame dfRight, VRange leftKeys, VRange rightKeys, Type type) {
        return new Join(dfLeft, dfRight, leftKeys, rightKeys, type).join();
    }

    public static Frame leftJoin(Frame dfLeft, Frame dfRight, VRange leftKeys, VRange rightKeys) {
        return from(dfLeft, dfRight, leftKeys, rightKeys, Type.LEFT);
    }

    public static Frame leftJoin(Frame dfLeft, Frame dfRight, VRange keys) {
        return from(dfLeft, dfRight, keys, keys, Type.LEFT);
    }

    public static Frame leftJoin(Frame dfLeft, Frame dfRight) {
        Set<String> leftVarNames = new HashSet<>(Arrays.asList(dfLeft.varNames()));
        List<String> keys = new ArrayList<>();
        for (String varName : dfRight.varNames()) {
            if (leftVarNames.contains(varName)) {
                keys.add(varName);
            }
        }
        return from(dfLeft, dfRight, VRange.of(keys), VRange.of(keys), Type.LEFT);
    }

    public static Frame rightJoin(Frame dfLeft, Frame dfRight, VRange leftKeys, VRange rightKeys) {
        return from(dfLeft, dfRight, leftKeys, rightKeys, Type.RIGHT);
    }

    public static Frame rightJoin(Frame dfLeft, Frame dfRight, VRange keys) {
        return from(dfLeft, dfRight, keys, keys, Type.RIGHT);
    }

    public static Frame rightJoin(Frame dfLeft, Frame dfRight) {
        Set<String> rightVarNames = new HashSet<>(Arrays.asList(dfRight.varNames()));
        List<String> keys = new ArrayList<>();
        for (String varName : dfLeft.varNames()) {
            if (rightVarNames.contains(varName)) {
                keys.add(varName);
            }
        }
        return from(dfLeft, dfRight, VRange.of(keys), VRange.of(keys), Type.RIGHT);
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
    private final List<String> leftRemainVarNames;
    private final List<String> rightVarNames;
    private final List<String> rightRemainVarNames;
    private final Type type;

    // artifacts
    private final Object2IntOpenHashMap<String> idMap;
    private final Int2ObjectOpenHashMap<IntList> leftIdToRows;
    private final Int2ObjectOpenHashMap<IntList> rightIdToRows;
    private final Int2ObjectOpenHashMap<String> leftRowToId;
    private final Int2ObjectOpenHashMap<String> rightRowToId;


    private Join(Frame dfLeft, Frame dfRight, VRange leftKeys, VRange rightKeys, Type type) {
        this.dfLeft = dfLeft;
        this.dfRight = dfRight;
        this.leftVarNames = leftKeys.parseVarNames(dfLeft);
        this.leftRemainVarNames = leftKeys.parseInverseVarNames(dfLeft);
        this.rightVarNames = rightKeys.parseVarNames(dfRight);
        this.rightRemainVarNames = rightKeys.parseInverseVarNames(dfRight);
        this.type = type;

        this.idMap = new Object2IntOpenHashMap<>();
        this.leftIdToRows = new Int2ObjectOpenHashMap<>();
        this.rightIdToRows = new Int2ObjectOpenHashMap<>();
        this.leftRowToId = new Int2ObjectOpenHashMap<>();
        this.rightRowToId = new Int2ObjectOpenHashMap<>();
    }

    private Frame join() {
        validateKeys();

        process(idMap, leftIdToRows, leftRowToId, dfLeft, leftVarNames);
        process(idMap, rightIdToRows, rightRowToId, dfRight, rightVarNames);

        switch (type) {
            case LEFT:
                return leftJoin();
            case RIGHT:
                return rightJoin();
            case INNER:
            case OUTER:
            default:
                throw new IllegalArgumentException("Join type not implemented");
        }
    }

    private Frame leftJoin() {

        List<Var> keyVars = new ArrayList<>();
        List<Var> leftVars = new ArrayList<>();
        List<Var> rightVars = new ArrayList<>();

        for (String varName : leftVarNames) {
            keyVars.add(dfLeft.rvar(varName).newInstance(0).withName(varName));
        }
        for (String varName : leftRemainVarNames) {
            leftVars.add(dfLeft.rvar(varName).newInstance(0).withName(varName));
        }
        for (String varName : rightRemainVarNames) {
            rightVars.add(dfRight.rvar(varName).newInstance(0).withName(varName));
        }

        for (int id = 0; id < idMap.size(); id++) {
            IntList leftRows = leftIdToRows.get(id);
            IntList rightRows = rightIdToRows.get(id);

            if (leftRows == null || leftRows.isEmpty()) {
                continue;
            }

            if (rightRows == null || rightRows.isEmpty()) {
                for (int i = 0; i < leftVarNames.size(); i++) {
                    appendValues(dfLeft, leftVarNames.get(i), leftRows, keyVars.get(i));
                }
                for (int i = 0; i < leftRemainVarNames.size(); i++) {
                    appendValues(dfLeft, leftRemainVarNames.get(i), leftRows, leftVars.get(i));
                }
                for (int i = 0; i < rightRemainVarNames.size(); i++) {
                    appendMissing(leftRows.size(), rightVars.get(i));
                }
            } else {
                for (int rightRow : rightRows) {
                    for (int i = 0; i < leftVarNames.size(); i++) {
                        appendValues(dfLeft, leftVarNames.get(i), leftRows, keyVars.get(i));
                    }
                    for (int i = 0; i < leftRemainVarNames.size(); i++) {
                        appendValues(dfLeft, leftRemainVarNames.get(i), leftRows, leftVars.get(i));
                    }
                    for (int i = 0; i < rightRemainVarNames.size(); i++) {
                        appendValue(dfRight, rightRemainVarNames.get(i), rightRow, leftRows.size(), rightVars.get(i));
                    }
                }
            }
        }
        List<Var> allVars = new ArrayList<>();
        allVars.addAll(keyVars);
        allVars.addAll(leftVars);
        allVars.addAll(rightVars);
        return SolidFrame.byVars(allVars);
    }

    private Frame rightJoin() {

        List<Var> keyVars = new ArrayList<>();
        List<Var> leftVars = new ArrayList<>();
        List<Var> rightVars = new ArrayList<>();

        for (String varName : rightVarNames) {
            keyVars.add(dfRight.rvar(varName).newInstance(0).withName(varName));
        }
        for (String varName : leftRemainVarNames) {
            leftVars.add(dfLeft.rvar(varName).newInstance(0).withName(varName));
        }
        for (String varName : rightRemainVarNames) {
            rightVars.add(dfRight.rvar(varName).newInstance(0).withName(varName));
        }

        for (int id = 0; id < idMap.size(); id++) {
            IntList leftRows = leftIdToRows.get(id);
            IntList rightRows = rightIdToRows.get(id);

            if (rightRows == null || rightRows.isEmpty()) {
                continue;
            }

            if (leftRows == null || leftRows.isEmpty()) {
                for (int i = 0; i < rightVarNames.size(); i++) {
                    appendValues(dfRight, rightVarNames.get(i), rightRows, keyVars.get(i));
                }
                for (int i = 0; i < rightRemainVarNames.size(); i++) {
                    appendValues(dfRight, rightRemainVarNames.get(i), rightRows, rightVars.get(i));
                }
                for (int i = 0; i < leftRemainVarNames.size(); i++) {
                    appendMissing(rightRows.size(), leftVars.get(i));
                }
            } else {
                for (int leftRow : leftRows) {
                    for (int i = 0; i < rightVarNames.size(); i++) {
                        appendValues(dfRight, rightVarNames.get(i), rightRows, keyVars.get(i));
                    }
                    for (int i = 0; i < rightRemainVarNames.size(); i++) {
                        appendValues(dfRight, rightRemainVarNames.get(i), rightRows, rightVars.get(i));
                    }
                    for (int i = 0; i < leftRemainVarNames.size(); i++) {
                        appendValue(dfLeft, leftRemainVarNames.get(i), leftRow, rightRows.size(), leftVars.get(i));
                    }
                }
            }
        }
        List<Var> allVars = new ArrayList<>();
        allVars.addAll(keyVars);
        allVars.addAll(rightVars);
        allVars.addAll(leftVars);
        return SolidFrame.byVars(allVars);
    }

    private void appendValues(Frame src, String varName, IntList rows, Var dst) {
        int varIndex = src.varIndex(varName);
        for (int row : rows) {
            appendValue(src, varIndex, row, dst);
        }
    }

    private void appendMissing(int times, Var dst) {
        for (int i = 0; i < times; i++) {
            dst.addMissing();
        }
    }

    private void appendValue(Frame src, String varName, int row, int times, Var dst) {
        int varIndex = src.varIndex(varName);
        for (int i = 0; i < times; i++) {
            appendValue(src, varIndex, row, dst);
        }
    }

    private void appendValue(Frame src, int varIndex, int row, Var dst) {
        if (src.isMissing(row, varIndex)) {
            dst.addMissing();
            return;
        }
        switch (dst.type()) {
            case BINARY:
            case INT:
                dst.addInt(src.getInt(row, varIndex));
                return;
            case DOUBLE:
                dst.addDouble(src.getDouble(row, varIndex));
                return;
            default:
                dst.addLabel(src.getLabel(row, varIndex));
        }
    }

    private void process(Object2IntOpenHashMap<String> idMap, Int2ObjectOpenHashMap<IntList> idToRows, Int2ObjectOpenHashMap<String> rowToId, Frame df, List<String> varNames) {
        for (int i = 0; i < df.rowCount(); i++) {
            StringBuilder sb = new StringBuilder();
            for (int k = 0; k < varNames.size(); k++) {
                sb.append(k).append(SEP).append(df.getLabel(i, varNames.get(k))).append(SEP);
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

    public static void main(String[] args) {
        VarInt id = VarInt.empty().withName("id");
        VarDouble x1 = VarDouble.empty().withName("x1");
        VarDouble x2 = VarDouble.empty().withName("x2");

        for (int i = 0; i < 30; i++) {
            id.addInt(i + 1);
            x1.addDouble((i + 1) * (i + 1));
            x2.addDouble(Math.sqrt(i + 1));
        }

        int[] sample = SamplingTools.sampleWOR(id.rowCount(), id.rowCount());

        Frame df1 = SolidFrame.byVars(id, x1);
        Frame df2 = SolidFrame.byVars(id, x2).mapRows(Mapping.wrap(sample)).copy();

        df1.printFullContent();
        df2.printFullContent();

        Join.leftJoin(df1, df2).printFullContent();
    }
}
