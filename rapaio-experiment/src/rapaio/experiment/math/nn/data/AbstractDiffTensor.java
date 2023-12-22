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

package rapaio.experiment.math.nn.data;

import rapaio.experiment.math.nn.DiffTensor;
import rapaio.experiment.math.nn.gradient.GradientTape;
import rapaio.math.tensor.ByteTensor;
import rapaio.math.tensor.DoubleTensor;
import rapaio.math.tensor.FloatTensor;
import rapaio.math.tensor.IntTensor;
import rapaio.util.NotImplementedException;

public abstract class AbstractDiffTensor implements DiffTensor {

    protected final String name;

    public AbstractDiffTensor(String name, GradientTape tape) {
        this.name = tape.register(name, this);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public DoubleTensor asDouble() {
        return null;
    }

    @Override
    public FloatTensor asFloat() {
        return null;
    }

    @Override
    public IntTensor asInt() {
        return null;
    }

    @Override
    public ByteTensor asByte() {
        return null;
    }

    @Override
    public DiffTensor mul(DiffTensor t, GradientTape tape) {
        throw new NotImplementedException();
    }

    @Override
    public DiffTensor add(DiffTensor t, GradientTape tape) {
        throw new NotImplementedException();
    }
}
