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

package rapaio.data.transform;

import java.util.function.ToDoubleFunction;

import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.data.VarRange;
import rapaio.data.stream.FSpot;

public class AddDoubleApply extends AbstractTransform {

    public static AddDoubleApply from(String targetName, ToDoubleFunction<FSpot> function) {
        return new AddDoubleApply(targetName, function);
    }

    private final String targetName;
    private final ToDoubleFunction<FSpot> function;

    private AddDoubleApply(String targetName, ToDoubleFunction<FSpot> function) {
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
        VarDouble target = VarDouble.wrap(df.stream().mapToDouble(function).toArray()).name(targetName);
        return df.bindVars(target);
    }

    @Override
    public Transform newInstance() {
        return new AddDoubleApply(targetName, function);
    }
}
