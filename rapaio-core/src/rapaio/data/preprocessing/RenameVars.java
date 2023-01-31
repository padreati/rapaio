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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.data.preprocessing;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rapaio.data.BoundFrame;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/30/19.
 */
public class RenameVars extends AbstractTransform {

    @Serial
    private static final long serialVersionUID = 1697029209693507138L;

    public static RenameVars of(VarRange varRange, String... varNames) {
        return new RenameVars(varRange, varNames);
    }

    private final Map<String, Integer> inputVarNamesIndex = new HashMap<>();
    private final String[] outputVarNames;

    public RenameVars(VarRange varRange, String... outputVarNames) {
        super(varRange);
        this.outputVarNames = outputVarNames;
    }

    @Override
    protected void coreFit(Frame df) {
        List<String> inputVarNames = varRange.parseVarNames(df);
        inputVarNamesIndex.clear();
        for (int i = 0; i < inputVarNames.size(); i++) {
            inputVarNamesIndex.put(inputVarNames.get(i), i);
        }
    }

    @Override
    public Frame coreApply(Frame df) {
        List<Var> vars = new ArrayList<>();
        df.varStream().forEach(var -> {
            if (inputVarNamesIndex.containsKey(var.name())) {
                int index = inputVarNamesIndex.get(var.name());
                vars.add(var.name(outputVarNames[index]));
            } else {
                vars.add(var);
            }
        });
        return BoundFrame.byVars(vars);
    }

    @Override
    public RenameVars newInstance() {
        return new RenameVars(varRange, outputVarNames);
    }
}
