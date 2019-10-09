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

package rapaio.data.group.function;

import it.unimi.dsi.fastutil.ints.IntList;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.group.Group;

import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/10/18.
 */
public class GroupFunMax extends DefaultSingleGroupFun {

    public GroupFunMax(int normalizeLevel, List<String> varNames) {
        super("max", normalizeLevel, varNames);
    }

    @Override
    public Var buildVar(Group group, String varName) {
        return group.getFrame().type(varName).newInstance(group.getGroupCount()).withName(varName + SEPARATOR + name);
    }

    @Override
    public void updateSingle(Var aggregate, int aggregateRow, Frame df, int varIndex, IntList rows) {
        switch (aggregate.type()) {
            case DOUBLE:
                double max1 = Double.NaN;
                for (int row : rows) {
                    if (df.isMissing(row, varIndex)) {
                        continue;
                    }
                    double value1 = df.getDouble(row, varIndex);
                    if (Double.isNaN(max1) || max1 < value1) {
                        max1 = value1;
                    }
                }
                aggregate.setDouble(aggregateRow, max1);
                break;
            case INT:
            case BINARY:
                int max2 = Integer.MIN_VALUE;
                for (int row : rows) {
                    if (df.isMissing(row, varIndex)) continue;
                    int value2 = df.getInt(row, varIndex);
                    if (max2 == Integer.MIN_VALUE || max2 < value2) {
                        max2 = value2;
                    }
                }
                aggregate.setInt(aggregateRow, max2);
                break;
            case LONG:
                long max3 = Long.MIN_VALUE;
                for (int row : rows) {
                    if (df.isMissing(row, varIndex)) continue;
                    long value3 = df.getLong(row, varIndex);
                    if (max3 == Long.MIN_VALUE || max3 < value3) {
                        max3 = value3;
                    }
                }
                aggregate.setLong(aggregateRow, max3);
                break;
            default:
                String max4 = null;
                for (int row : rows) {
                    String value4 = df.getLabel(row, varIndex);
                    if (max4 == null || max4.compareTo(value4) < 0) {
                        max4 = value4;
                    }
                }
                aggregate.setLabel(aggregateRow, max4);
        }
    }
}

