/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.data.filter.frame;

import rapaio.data.*;

import java.util.*;
import java.util.stream.Collectors;

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
public class FFOneHotEncoding extends FFAbstract {

    private static final long serialVersionUID = 4893532203594639069L;

    public FFOneHotEncoding(String...varNames) {
        super(VRange.of(varNames));
    }

    public FFOneHotEncoding(VRange vRange) {
        super(vRange);
    }

    @Override
    public void fit(Frame df) {
        parse(df);
    }

    public Frame apply(Frame df) {
        checkRangeVars(1, df.varCount(), df);

        Set<String> nameSet = Arrays.stream(varNames).collect(Collectors.toSet());
        List<Var> vars = new ArrayList<>();

        for (String varName : df.varNames()) {
            if (nameSet.contains(varName) && df.var(varName).type().isNominal()) {
                // process one hot encoding
                String[] dict = df.var(varName).levels();
                List<Var> oneHotVars = new ArrayList<>();
                for (int i = 1; i < dict.length; i++) {
                    oneHotVars.add(Numeric.fill(df.rowCount()).withName(varName + "." + dict[i]));
                }
                for (int i = 0; i < df.rowCount(); i++) {
                    int index = df.index(i, varName);
                    if (index > 0) {
                        oneHotVars.get(index - 1).setValue(i, 1.0);
                    }
                }
                vars.addAll(oneHotVars);
            } else {
                vars.add(df.var(varName));
            }
        }
        return BoundFrame.newByVars(vars);
    }
}
