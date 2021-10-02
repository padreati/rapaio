/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.filter;

import java.io.Serial;

import rapaio.data.Frame;
import rapaio.data.VarRange;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/22/16.
 */
public class FFillMissing extends AbstractFFilter {

    public static FFillMissing onDouble(double fill, VarRange varRange) {
        return new FFillMissing(varRange, Type.DOUBLE, fill, 0, null);
    }

    public static FFillMissing onInt(int fill, VarRange varRange) {
        return new FFillMissing(varRange, Type.INT, Double.NaN, fill, null);
    }

    public static FFillMissing onLabel(String fill, VarRange varRange) {
        return new FFillMissing(varRange, Type.STRING, Double.NaN, 0, fill);
    }

    @Serial
    private static final long serialVersionUID = 281130325474491898L;
    private final Type type;
    private final double doubleFill;
    private final int intFill;
    private final String stringFill;

    private FFillMissing(VarRange varRange, Type type, double doubleFill, int intFill, String stringFill) {
        super(varRange);
        this.type = type;
        this.doubleFill = doubleFill;
        this.intFill = intFill;
        this.stringFill = stringFill;
    }

    @Override
    public FFillMissing newInstance() {
        return new FFillMissing(varRange, type, doubleFill, intFill, stringFill);
    }

    @Override
    protected void coreFit(Frame df) {
    }

    @Override
    public Frame apply(Frame df) {
        for (String name : varNames) {
            var var = df.rvar(name);
            for (int i = 0; i < var.size(); i++) {
                if (var.isMissing(i)) {
                    switch (type) {
                        case DOUBLE -> var.setDouble(i, doubleFill);
                        case INT -> var.setInt(i, intFill);
                        case STRING -> var.setLabel(i, stringFill);
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
