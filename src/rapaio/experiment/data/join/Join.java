/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.experiment.data.join;

import rapaio.core.SamplingTools;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/17/18.
 */
@Deprecated
public class Join {

    public static Frame from(Frame dfLeft, Frame dfRight, VarRange leftKeys, VarRange rightKeys, Type type) {
        return new Join(dfLeft, dfRight, leftKeys, rightKeys, type).join();
    }

    public static Frame leftJoin(Frame dfLeft, Frame dfRight, VarRange leftKeys, VarRange rightKeys) {
        return from(dfLeft, dfRight, leftKeys, rightKeys, Type.LEFT);
    }

    public static Frame leftJoin(Frame dfLeft, Frame dfRight, VarRange keys) {
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
        return from(dfLeft, dfRight, VarRange.of(keys), VarRange.of(keys), Type.LEFT);
    }

    public static Frame rightJoin(Frame dfLeft, Frame dfRight, VarRange leftKeys, VarRange rightKeys) {
        return from(dfLeft, dfRight, leftKeys, rightKeys, Type.RIGHT);
    }

    public static Frame rightJoin(Frame dfLeft, Frame dfRight, VarRange keys) {
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
        return from(dfLeft, dfRight, VarRange.of(keys), VarRange.of(keys), Type.RIGHT);
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
    private final HashMap<String, Integer> idMap;
    private final HashMap<Integer, Mapping> leftIdToRows;
    private final HashMap<Integer, Mapping> rightIdToRows;
    private final HashMap<Integer, String> leftRowToId;
    private final HashMap<Integer, String> rightRowToId;


    private Join(Frame dfLeft, Frame dfRight, VarRange leftKeys, VarRange rightKeys, Type type) {
        this.dfLeft = dfLeft;
        this.dfRight = dfRight;
        this.leftVarNames = leftKeys.parseVarNames(dfLeft);
        this.leftRemainVarNames = leftKeys.parseInverseVarNames(dfLeft);
        this.rightVarNames = rightKeys.parseVarNames(dfRight);
        this.rightRemainVarNames = rightKeys.parseInverseVarNames(dfRight);
        this.type = type;

        this.idMap = new HashMap<>();
        this.leftIdToRows = new HashMap<>();
        this.rightIdToRows = new HashMap<>();
        this.leftRowToId = new HashMap<>();
        this.rightRowToId = new HashMap<>();
    }

    private Frame join() {
        validateKeys();

        process(idMap, leftIdToRows, leftRowToId, dfLeft, leftVarNames);
        process(idMap, rightIdToRows, rightRowToId, dfRight, rightVarNames);

        return switch (type) {
            case LEFT -> leftJoin();
            case RIGHT -> rightJoin();
            default -> throw new IllegalArgumentException("Join type not implemented");
        };
    }

    private Frame leftJoin() {

        List<Var> keyVars = new ArrayList<>();
        List<Var> leftVars = new ArrayList<>();
        List<Var> rightVars = new ArrayList<>();

        for (String varName : leftVarNames) {
            keyVars.add(dfLeft.rvar(varName).newInstance(0).name(varName));
        }
        for (String varName : leftRemainVarNames) {
            leftVars.add(dfLeft.rvar(varName).newInstance(0).name(varName));
        }
        for (String varName : rightRemainVarNames) {
            rightVars.add(dfRight.rvar(varName).newInstance(0).name(varName));
        }

        for (int id = 0; id < idMap.size(); id++) {
            Mapping leftRows = leftIdToRows.get(id);
            Mapping rightRows = rightIdToRows.get(id);

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
            keyVars.add(dfRight.rvar(varName).newInstance(0).name(varName));
        }
        for (String varName : leftRemainVarNames) {
            leftVars.add(dfLeft.rvar(varName).newInstance(0).name(varName));
        }
        for (String varName : rightRemainVarNames) {
            rightVars.add(dfRight.rvar(varName).newInstance(0).name(varName));
        }

        for (int id = 0; id < idMap.size(); id++) {
            Mapping leftRows = leftIdToRows.get(id);
            Mapping rightRows = rightIdToRows.get(id);

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

    private void appendValues(Frame src, String varName, Mapping rows, Var dst) {
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
            case BINARY, INT -> {
                dst.addInt(src.getInt(row, varIndex));
                return;
            }
            case DOUBLE -> {
                dst.addDouble(src.getDouble(row, varIndex));
                return;
            }
            default -> dst.addLabel(src.getLabel(row, varIndex));
        }
    }

    private void process(HashMap<String, Integer> idMap,
                         HashMap<Integer, Mapping> idToRows, HashMap<Integer, String> rowToId, Frame df, List<String> varNames) {
        for (int i = 0; i < df.rowCount(); i++) {
            StringBuilder sb = new StringBuilder();
            for (int k = 0; k < varNames.size(); k++) {
                sb.append(k).append(SEP).append(df.getLabel(i, varNames.get(k))).append(SEP);
            }
            String key = sb.toString();
            idMap.putIfAbsent(key, idMap.size());
            int id = idMap.get(key);
            idToRows.putIfAbsent(id, Mapping.empty());
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
        VarInt id = VarInt.empty().name("id");
        VarDouble x1 = VarDouble.empty().name("x1");
        VarDouble x2 = VarDouble.empty().name("x2");

        for (int i = 0; i < 30; i++) {
            id.addInt(i + 1);
            x1.addDouble((i + 1) * (i + 1));
            x2.addDouble(Math.sqrt(i + 1));
        }

        int[] sample = SamplingTools.sampleWOR(id.size(), id.size());

        Frame df1 = SolidFrame.byVars(id, x1);
        Frame df2 = SolidFrame.byVars(id, x2).mapRows(Mapping.wrap(sample)).copy();

        df1.printFullContent();
        df2.printFullContent();

        Join.leftJoin(df1, df2).printFullContent();
    }
}
