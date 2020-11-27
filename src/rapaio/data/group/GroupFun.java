/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

package rapaio.data.group;

import rapaio.data.Group;
import rapaio.data.Var;

import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/9/18.
 */
public interface GroupFun {

    /**
     * Name of the aggregate function
     *
     * @return name of the aggregate function
     */
    String name();

    /**
     * @return the list of var names for which to apply the aggregation
     */
    List<String> varNames();

    /**
     * Computes a list of variables with the aggregated values using variable names
     * given as parameters. Any combination is possible, as long as there is
     * at least one input variable and one output variable.
     *
     * @param group group by data structure
     * @return aggregated variable instance
     */
    List<Var> compute(Group group);
}
