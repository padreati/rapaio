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

package rapaio.data.group.function;

import java.util.List;

import rapaio.data.group.GroupFun;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/21/19.
 */
public abstract class DefaultGroupFun implements GroupFun {

    protected final String name;
    protected final List<String> varNames;

    public DefaultGroupFun(String name, List<String> varNames) {
        this.name = name;
        this.varNames = varNames;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public List<String> varNames() {
        return varNames;
    }

    @Override
    public String toString() {
        return "GroupByFunction{name=" + name() + ",varNames=[" + String.join(",", varNames) + "]}";
    }
}
