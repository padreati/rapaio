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

import rapaio.data.*;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/22/16.
 */
public class FFillNaDouble extends AbstractFF {

    public static FFillNaDouble on(double fill, VRange vRange) {
        return new FFillNaDouble(fill, vRange);
    }

    public static FFillNaDouble on(double fill, String...varNames) {
        return new FFillNaDouble(fill, VRange.of(varNames));
    }

    private static final long serialVersionUID = 281130325474491898L;
    private final double fill;

    private FFillNaDouble(double fill, VRange vRange) {
        super(vRange);
        this.fill = fill;
    }

    @Override
    public FFillNaDouble newInstance() {
        return new FFillNaDouble(fill, vRange);
    }

    @Override
    protected void coreFit(Frame df) {
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
                if (var.isMissing(i)) {
                    var.setDouble(i, fill);
                }
            }
        }
        return df;
    }
}
