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

package rapaio.data.filter;

import rapaio.data.Frame;
import rapaio.data.VRange;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/22/16.
 */
public class FFillMissing extends AbstractFFilter {

    public static FFillMissing onDouble(double fill, VRange vRange) {
        return new FFillMissing(vRange, Type.DOUBLE, fill, 0, null);
    }

    public static FFillMissing onInt(int fill, VRange vRange) {
        return new FFillMissing(vRange, Type.INT, Double.NaN, fill, null);
    }

    public static FFillMissing onLabel(String fill, VRange vRange) {
        return new FFillMissing(vRange, Type.STRING, Double.NaN, 0, fill);
    }

    private static final long serialVersionUID = 281130325474491898L;
    private final Type type;
    private final double doubleFill;
    private final int intFill;
    private final String stringFill;

    private FFillMissing(VRange vRange, Type type, double doubleFill, int intFill, String stringFill) {
        super(vRange);
        this.type = type;
        this.doubleFill = doubleFill;
        this.intFill = intFill;
        this.stringFill = stringFill;
    }

    @Override
    public FFillMissing newInstance() {
        return new FFillMissing(vRange, type, doubleFill, intFill, stringFill);
    }

    @Override
    protected void coreFit(Frame df) {
    }

    @Override
    public Frame apply(Frame df) {
        for (String name : varNames) {
            var var = df.rvar(name);
            for (int i = 0; i < var.rowCount(); i++) {
                if (var.isMissing(i)) {
                    switch (type) {
                        case DOUBLE:
                            var.setDouble(i, doubleFill);
                            break;
                        case INT:
                            var.setInt(i, intFill);
                            break;
                        case STRING:
                            var.setLabel(i, stringFill);
                            break;
                    }
                }
            }
        }
        return df;
    }

    private enum Type {
        DOUBLE,
        INT,
        STRING
    }
}
