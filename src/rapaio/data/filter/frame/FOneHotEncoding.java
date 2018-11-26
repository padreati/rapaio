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

package rapaio.data.filter.frame;

import rapaio.data.BoundFrame;
import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.Var;
import rapaio.data.VarDouble;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public class FOneHotEncoding extends AbstractFF {

    public static FOneHotEncoding on(String ... varNames) {
        return new FOneHotEncoding(VRange.of(varNames), false, true);
    }

    public static FOneHotEncoding on(boolean lessOne, boolean useNa, String ... varNames) {
        return new FOneHotEncoding(VRange.of(varNames), lessOne, useNa);
    }

    public static FOneHotEncoding on(VRange vRange) {
        return new FOneHotEncoding(vRange, false, true);
    }

    public static FOneHotEncoding on(boolean lessOne, boolean useNa, VRange vRange) {
        return new FOneHotEncoding(vRange, lessOne, useNa);
    }

    private static final long serialVersionUID = 4893532203594639069L;
    private Map<String, List<String>> levels;
    private final boolean lessOne;
    private final boolean useNa;

    private FOneHotEncoding(VRange vRange, boolean lessOne, boolean useNa) {
        super(vRange);
        this.lessOne = lessOne;
        this.useNa = useNa;
    }

    @Override
    public FOneHotEncoding newInstance() {
        return new FOneHotEncoding(vRange, lessOne, useNa);
    }

    @Override
    public void coreFit(Frame df) {
        levels = new HashMap<>();
        for (String varName : varNames) {
            // for each nominal variable
            if (df.rvar(varName).type().isNominal()) {
                levels.put(varName, df.rvar(varName).levels());
            }
        }
    }

    public Frame apply(Frame df) {
        if(varNames==null || varNames.length==0) {
            return df;
        }

        // build a set for fast search
        Set<String> nameSet = Arrays.stream(varNames).collect(Collectors.toSet());

        // list of variables with encoding
        List<Var> vars = new ArrayList<>();

        for (String varName : df.varNames()) {

            // if the variable has been learned
            if (levels.keySet().contains(varName)) {

                // get the learned dictionary
                List<String> dict = levels.get(varName);
                if(!useNa) {
                    dict = dict.subList(1, dict.size());
                }
                if(lessOne) {
                    dict = dict.subList(1, dict.size());
                }

                List<Var> oneHotVars = new ArrayList<>();
                Map<String, Var> index = new HashMap<>();
                // create a new numeric var for each level, filled with 0
                for (String token : dict) {
                    Var v = VarDouble.fill(df.rowCount()).withName(varName + "." + token);
                    oneHotVars.add(v);
                    index.put(token, v);
                }
                // populate encoding variables
                for (int i = 0; i < df.rowCount(); i++) {
                    String level = df.getLabel(i, varName);
                    if (index.containsKey(level)) {
                        index.get(level).setDouble(i, 1.0);
                    }
                }
                vars.addAll(oneHotVars);
            } else {
                vars.add(df.rvar(varName));
            }
        }
        return BoundFrame.byVars(vars);
    }
}
