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

import rapaio.data.Frame;
import rapaio.data.Group;
import rapaio.data.Mapping;
import rapaio.data.Var;

import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/10/18.
 */
public class GroupFunMin extends DefaultSingleGroupFun {

    public GroupFunMin(int normalizeLevel, List<String> varNames) {
        super("min", normalizeLevel, varNames);
    }

    @Override
    public Var buildVar(Group group, String varName) {
        return group.getFrame().type(varName).newInstance(group.getGroupCount()).withName(varName + SEPARATOR + name);
    }


    @Override
    public void updateSingle(Var aggregate, int aggregateRow, Frame df, int varIndex, Mapping rows) {
        switch (aggregate.type()) {
            case DOUBLE:
                double min1 = Double.NaN;
                for (int row : rows) {
                    if (df.isMissing(row, varIndex)) {
                        continue;
                    }
                    double value1 = df.getDouble(row, varIndex);
                    if (Double.isNaN(min1) || min1 > value1) {
                        min1 = value1;
                    }
                }
                aggregate.setDouble(aggregateRow, min1);
                break;
            case INT:
            case BINARY:
                int min2 = Integer.MIN_VALUE;
                for (int row : rows) {
                    if (df.isMissing(row, varIndex)) continue;
                    int value2 = df.getInt(row, varIndex);
                    if (min2 == Integer.MIN_VALUE || min2 > value2) {
                        min2 = value2;
                    }
                }
                aggregate.setInt(aggregateRow, min2);
                break;
            case LONG:
                long min3 = Long.MIN_VALUE;
                for (int row : rows) {
                    if (df.isMissing(row, varIndex)) continue;
                    long value3 = df.getLong(row, varIndex);
                    if (min3 == Long.MIN_VALUE || min3 > value3) {
                        min3 = value3;
                    }
                }
                aggregate.setLong(aggregateRow, min3);
                break;
            default:
                String min4 = null;
                for (int row : rows) {
                    String value4 = df.getLabel(row, varIndex);
                    if (min4 == null || min4.compareTo(value4) > 0) {
                        min4 = value4;
                    }
                }
                aggregate.setLabel(aggregateRow, min4);
        }
    }
}
