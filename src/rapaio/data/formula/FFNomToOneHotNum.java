/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.data.formula;

import rapaio.core.ColRange;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.SolidFrame;
import rapaio.data.Var;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Replaces specified columns in ColRange with numeric one hot
 * encodings. If the specified columns are numeric already, then these
 * columns will not be processed.
 * <p>
 * In order to convert to one hot all the existent nominal columns, than you
 * can specify 'all' value in column range.
 * <p>
 * The given columns will be placed grouped, in the place of
 * the given nominal column.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class FFNomToOneHotNum implements FrameFilter {

    private final ColRange colRange;

    public FFNomToOneHotNum(ColRange colRange) {
        this.colRange = colRange;
    }

    @Override
    public Frame apply(Frame df) {
        Set<String> nameSet = new HashSet<>(colRange.parseColumnNames(df));

        List<Var> vars = new ArrayList<>();
        List<String> names = new ArrayList<>();

        for (String varName : df.colNames()) {
            if (nameSet.contains(varName) && df.col(varName).type().isNominal()) {
                // process one hot encoding
                String[] dict = df.col(varName).dictionary();
                List<Var> oneHotVars = new ArrayList<>();
                List<String> oneHotNames = new ArrayList<>();
                for (int i = 1; i < dict.length; i++) {
                    oneHotVars.add(new Numeric(df.rowCount()));
                    oneHotNames.add(varName + "." + dict[i]);
                }
                for (int i = 0; i < df.rowCount(); i++) {
                    int index = df.index(i, varName);
                    if (index > 0) {
                        oneHotVars.get(index - 1).setValue(i, 1.0);
                    }
                }
                vars.addAll(oneHotVars);
                names.addAll(oneHotNames);
            } else {
                vars.add(df.col(varName));
                names.add(varName);
            }
        }
        return new SolidFrame(df.rowCount(), vars, names);
    }
}
