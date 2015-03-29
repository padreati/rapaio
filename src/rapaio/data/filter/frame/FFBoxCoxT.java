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
 */

package rapaio.data.filter.frame;

import rapaio.data.Frame;
import rapaio.data.filter.var.VFBoxCoxT;

import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/15/14.
 */
public class FFBoxCoxT extends AbstractFF {

    private final VFBoxCoxT bct;

    public FFBoxCoxT(double lambda, String... varNames) {
        this(lambda, 0, varNames);
    }

    public FFBoxCoxT(double lambda, double shift, String... varNames) {
        super(varNames);
        this.bct = new VFBoxCoxT(lambda, shift);
    }

    @Override
    public void fit(Frame df) {
        checkRangeVars(1, df.varCount(), df, varNames);
    }

    @Override
    public Frame apply(Frame df) {
        List<String> names = parse(df, varNames);
        for (String name : names) {
            bct.fitApply(df.getVar(name));
        }
        return df;
    }
}
