/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.preprocessing;

import rapaio.data.Frame;
import rapaio.data.VarNominal;
import rapaio.data.VarRange;
import rapaio.data.stream.FSpot;
import rapaio.util.function.SFunction;

public class AddNominalApply extends AbstractTransform {

    public static AddNominalApply from(String targetName, SFunction<FSpot, String> function) {
        return new AddNominalApply(targetName, function);
    }

    private final String targetName;
    private final SFunction<FSpot, String> function;

    private AddNominalApply(String targetName, SFunction<FSpot, String> function) {
        super(VarRange.all());
        this.function = function;
        this.targetName = targetName;
    }

    @Override
    protected void coreFit(Frame df) {
        // nothing
    }

    @Override
    protected Frame coreApply(Frame df) {
        VarNominal target = VarNominal.copy(df.stream().map(function).toArray(String[]::new)).name(targetName);
        return df.bindVars(target);
    }

    @Override
    public Transform newInstance() {
        return new AddNominalApply(targetName, function);
    }
}

