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

import rapaio.data.Frame;
import rapaio.data.VarType;

import java.util.ArrayList;
import java.util.List;

/**
 * Transform a given frame by removing all the variables
 * which does not have the given types.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/5/14.
 */
@Deprecated
public class FFAbstractRetainTypes extends FFAbstract {

    private final VarType[] types;

    public FFAbstractRetainTypes(VarType... types) {
        super();
        if (types == null || types.length == 0)
            throw new IllegalArgumentException("Must provide at least a type to retain");
        this.types = types;
    }

    @Override
    public void fit(Frame df) {
    }

    @Override
    public Frame apply(Frame df) {
        List<String> names = new ArrayList<>();
        for (String name : df.varNames()) {
            for (VarType type : types) {
                if (df.getVar(name).type().equals(type)) {
                    names.add(name);
                    break;
                }
            }
        }
        return df.mapVars(names);
    }
}
