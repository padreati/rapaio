/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.filter;

import java.io.Serial;
import java.util.Arrays;
import java.util.List;

import rapaio.data.Var;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/21/20.
 */
public class VFilterSequence implements VFilter {

    @Serial
    private static final long serialVersionUID = -2495739155198558730L;
    private final List<VFilter> filters;

    private VFilterSequence(VFilter... filterArray) {
        filters = Arrays.asList(filterArray);
    }

    @Override
    public VFilter fit(Var var) {
        for (VFilter filter : filters) {
            filter.fit(var);
        }
        return this;
    }

    @Override
    public Var apply(Var var) {
        Var result = var;
        for (VFilter filter : filters) {
            result = filter.apply(result);
        }
        return result;
    }

    @Override
    public Var fapply(Var var) {
        Var result = var;
        for (VFilter filter : filters) {
            result = filter.fapply(result);
        }
        return result;
    }
}
