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

import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.VarType;

import java.util.ArrayList;
import java.util.List;

/**
 * Transform a given frame by removing all the variables
 * which does not have the given types.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/5/14.
 */
public class FFRetainTypes extends AbstractFF {

    private static final long serialVersionUID = -5152761273156719785L;

    private final VarType[] types;

    public FFRetainTypes(VarType... types) {
        super(VRange.all());
        if (types == null || types.length == 0)
            throw new IllegalArgumentException("Must provide at least a type to retain");
        this.types = types;
    }

    @Override
    public FFRetainTypes newInstance() {
        return new FFRetainTypes(types);
    }

    @Override
    public void train(Frame df) {
        parse(df);
    }

    @Override
    public Frame apply(Frame df) {
        List<String> names = new ArrayList<>();
        for (String name : df.varNames()) {
            for (VarType type : types) {
                if (df.var(name).type().equals(type)) {
                    names.add(name);
                    break;
                }
            }
        }
        return df.mapVars(names);
    }
}
