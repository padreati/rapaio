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

package rapaio.experiment.data;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import rapaio.data.*;

import java.util.List;

/**
 * This frame is similar with bound frame, but obtained from a join operation on matching variable values,
 * rather than rows. This is a read only data frame, if one wants to do operations with it it must solidify
 * it.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/22/19.
 */
public class JoinFrame extends AbstractFrame {

    private static final long serialVersionUID = 8499563251193872775L;

    private final Frame leftDf;
    private final Frame rightDf;
    private final String[] varNames;
    private final boolean[] side;
    private final int[] leftRows;
    private final int[] rightRows;

    private final Object2IntOpenHashMap<String> varIndexTree;

    /**
     * Builds a join data frame from given parameters
     *
     * @param leftDf    left frame
     * @param rightDf   right frame
     * @param varNames  variable names
     * @param side      side of the variable (true for left, false for right)
     * @param leftRows  row numbers for left frame (-1 means no row)
     * @param rightRows row numbers for right frame (-1 means no row)
     */
    public JoinFrame(Frame leftDf, Frame rightDf, String[] varNames, boolean[] side, int[] leftRows, int[] rightRows) {
        this.leftDf = leftDf;
        this.rightDf = rightDf;
        this.varNames = varNames;
        this.side = side;
        this.leftRows = leftRows;
        this.rightRows = rightRows;

        varIndexTree = new Object2IntOpenHashMap<>();
        for (int i = 0; i < varNames.length; i++) {
            varIndexTree.put(varNames[i], i);
        }
    }

    @Override
    public int rowCount() {
        return leftRows.length;
    }

    @Override
    public int varCount() {
        return varNames.length;
    }

    @Override
    public String[] varNames() {
        return varNames;
    }

    @Override
    public String varName(int i) {
        return varNames[i];
    }

    @Override
    public int varIndex(String name) {
        return varIndexTree.getInt(name);
    }

    @Override
    public Var rvar(int pos) {
        Mapping mapping = Mapping.wrap(side[pos] ? leftRows : rightRows);
        return side[pos]
                ? leftDf.rvar(varNames[pos]).mapRows(mapping)
                : rightDf.rvar(varNames[pos]).mapRows(mapping);
    }

    @Override
    public Var rvar(String name) {
        int pos = varIndexTree.getInt(name);
        Mapping mapping = Mapping.wrap(side[pos] ? leftRows : rightRows);
        return MappedVar.byRows(side[pos]
                ? leftDf.rvar(name).mapRows(mapping)
                : rightDf.rvar(name).mapRows(mapping)
        );
    }

    @Override
    public VType type(String name) {
        return side[varIndexTree.getInt(name)] ? leftDf.type(name) : rightDf.type(name);
    }

    @Override
    public Frame bindVars(Var... vars) {
        return BoundFrame.byVars(this, BoundFrame.byVars(vars));
    }

    @Override
    public Frame bindVars(Frame df) {
        return BoundFrame.byVars(this, df);
    }

    @Override
    public Frame mapVars(VRange range) {
        return BoundFrame.byVars(this).mapVars(range);
    }

    @Override
    public Frame addRows(int rowCount) {
        throw new IllegalStateException("This operation is not allowed on join frames.");
    }

    @Override
    public Frame clearRows() {
        throw new IllegalStateException("This operation is not allowed on join frames.");
    }

    @Override
    public Frame bindRows(Frame df) {
        return BoundFrame.byRows(this, df);
    }

    @Override
    public Frame mapRows(Mapping mapping) {
        return MappedFrame.byRow(this, mapping);
    }

    private boolean isMissingRow(int row, boolean side) {
        return side ? leftRows[row] == -1 : rightRows[row] == -1;
    }

    @Override
    public double getDouble(int row, int varIndex) {
        if (isMissingRow(row, side[varIndex])) {
            return VarDouble.MISSING_VALUE;
        }
        return side[varIndex]
                ? leftDf.getDouble(row, varNames[varIndex])
                : rightDf.getDouble(row, varNames[varIndex]);
    }

    @Override
    public double getDouble(int row, String varName) {
        if (isMissingRow(row, side[varIndexTree.getInt(varName)])) {
            return VarDouble.MISSING_VALUE;
        }
        return side[varIndexTree.getInt(varName)]
                ? leftDf.getDouble(row, varName)
                : rightDf.getDouble(row, varName);
    }

    @Override
    public void setDouble(int row, int col, double value) {
        throw new IllegalStateException("This operation is not allowed on join frames.");
    }

    @Override
    public void setDouble(int row, String varName, double value) {
        throw new IllegalStateException("This operation is not allowed on join frames.");
    }

    @Override
    public int getInt(int row, int varIndex) {
        if (isMissingRow(row, side[varIndex])) {
            return VarInt.MISSING_VALUE;
        }
        return side[varIndex]
                ? leftDf.getInt(row, varNames[varIndex])
                : rightDf.getInt(row, varNames[varIndex]);
    }

    @Override
    public int getInt(int row, String varName) {
        if (isMissingRow(row, side[varIndexTree.getInt(varName)])) {
            return VarInt.MISSING_VALUE;
        }
        return side[varIndexTree.getInt(varName)]
                ? leftDf.getInt(row, varName)
                : rightDf.getInt(row, varName);
    }

    @Override
    public void setInt(int row, int col, int value) {
        throw new IllegalStateException("This operation is not allowed on join frames.");
    }

    @Override
    public void setInt(int row, String varName, int value) {
        throw new IllegalStateException("This operation is not allowed on join frames.");
    }

    @Override
    public long getLong(int row, int varIndex) {
        if (isMissingRow(row, side[varIndex])) {
            return VarLong.MISSING_VALUE;
        }
        return side[varIndex]
                ? leftDf.getLong(row, varNames[varIndex])
                : rightDf.getLong(row, varNames[varIndex]);
    }

    @Override
    public long getLong(int row, String varName) {
        if (isMissingRow(row, side[varIndexTree.getInt(varName)])) {
            return VarLong.MISSING_VALUE;
        }
        return side[varIndexTree.getInt(varName)]
                ? leftDf.getLong(row, varName)
                : rightDf.getLong(row, varName);
    }

    @Override
    public void setLong(int row, int col, long value) {
        throw new IllegalStateException("This operation is not allowed on join frames.");
    }

    @Override
    public void setLong(int row, String varName, long value) {
        throw new IllegalStateException("This operation is not allowed on join frames.");
    }

    @Override
    public String getLabel(int row, int varIndex) {
        if (isMissingRow(row, side[varIndex])) {
            return VarNominal.MISSING_VALUE;
        }
        return side[varIndex]
                ? leftDf.getLabel(row, varNames[varIndex])
                : rightDf.getLabel(row, varNames[varIndex]);
    }

    @Override
    public String getLabel(int row, String varName) {
        if (isMissingRow(row, side[varIndexTree.getInt(varName)])) {
            return VarNominal.MISSING_VALUE;
        }
        return side[varIndexTree.getInt(varName)]
                ? leftDf.getLabel(row, varName)
                : rightDf.getLabel(row, varName);
    }

    @Override
    public void setLabel(int row, int col, String value) {
        throw new IllegalStateException("This operation is not allowed on join frames.");
    }

    @Override
    public void setLabel(int row, String varName, String value) {
        throw new IllegalStateException("This operation is not allowed on join frames.");
    }

    @Override
    public List<String> levels(String varName) {
        return side[varIndexTree.getInt(varName)]
                ? leftDf.levels(varName) : rightDf.levels(varName);
    }

    @Override
    public boolean isMissing(int row, int varIndex) {
        if (isMissingRow(row, side[varIndex])) {
            return true;
        }
        return side[varIndex]
                ? leftDf.isMissing(row, varNames[varIndex])
                : rightDf.isMissing(row, varNames[varIndex]);
    }

    @Override
    public boolean isMissing(int row, String varName) {
        if(isMissingRow(row, side[varIndexTree.getInt(varName)])) {
            return true;
        }
        return side[varIndexTree.getInt(varName)]
                ? leftDf.isMissing(row, varName)
                : rightDf.isMissing(row, varName);
    }

    @Override
    public void setMissing(int row, int col) {
        throw new IllegalStateException("This operation is not allowed on join frames.");
    }

    @Override
    public void setMissing(int row, String varName) {
        throw new IllegalStateException("This operation is not allowed on join frames.");
    }
}
