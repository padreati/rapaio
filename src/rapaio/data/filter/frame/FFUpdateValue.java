/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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
import rapaio.data.filter.FFilter;

import java.util.function.Function;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/15/14.
 */
public class FFUpdateValue extends AbstractFF {

    private static final long serialVersionUID = 3982915877968295381L;

    private final Function<Double, Double> f;

    public FFUpdateValue(Function<Double, Double> f, VRange vRange) {
        super(vRange);
        this.f = f;
    }

    @Override
    public FFilter newInstance() {
        return new FFUpdateValue(f, vRange);
    }

    @Override
    public void train(Frame df) {
        parse(df);
        checkRangeVars(1, df.getVarCount(), df);
    }

    @Override
    public Frame apply(Frame df) {
        for (int i = 0; i < df.getRowCount(); i++) {
            for (String name : varNames) {
                df.setValue(i, name, f.apply(df.getValue(i, name)));
            }
        }
        return df;
    }
}
