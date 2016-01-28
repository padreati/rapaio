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

package rapaio.data.filter.frame;

import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.Var;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/22/16.
 */
public class FFImputeFill extends FFAbstract {

    private static final long serialVersionUID = 281130325474491898L;
    private final double fill;

    public FFImputeFill(double fill, String... varNames) {
        this(fill, VRange.of(varNames));
    }

    public FFImputeFill(double fill, VRange vRange) {
        super(vRange);
        this.fill = fill;
    }

    @Override
    public void fit(Frame df) {
        parse(df);
    }

    @Override
    public Frame apply(Frame df) {
        Set<String> names = Arrays.stream(varNames).collect(Collectors.toSet());

        for (Var var : df.varList()) {
            if (!var.type().isNumeric())
                continue;
            if (!names.contains(var.name()))
                continue;
            for (int i = 0; i < var.rowCount(); i++) {
                if (var.missing(i))
                    var.setValue(i, fill);
            }
        }
        return df;
    }
}
