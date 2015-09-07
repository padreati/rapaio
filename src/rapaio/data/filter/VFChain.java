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

package rapaio.data.filter;

import rapaio.data.Var;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Filter which enables one to chain multiple var filter
 * under a single instance, in order to fit and/or apply
 * all of them.
 * <p>
 * Contained filter will be fitted or applied in the same
 * order in which they were added to the chain.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/3/14.
 */
public class VFChain extends VFAbstract {

    private static final long serialVersionUID = 7759834710166702798L;

    private final List<VFilter> filters = new ArrayList<>();

    public void clear() {
        filters.clear();
    }

    public VFChain add(VFilter... filter) {
        Collections.addAll(filters, filter);
        return this;
    }

    @Override
    public void fit(Var... vars) {
        for (VFilter filter : filters) {
            filter.fit(vars);
        }
    }

    @Override
    public Var apply(Var... vars) {
        Var current = vars[0];
        for (VFilter filter : filters) {
            current = filter.apply(current);
        }
        return current;
    }
}
