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

package rapaio.data.filter.var;

import rapaio.data.Var;
import rapaio.data.VarInt;
import rapaio.data.filter.VFilter;
import rapaio.data.stream.VSpot;

import java.util.function.Function;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public class VFToIndex implements VFilter {

    private static final long serialVersionUID = -699221182441440988L;

    public static VFToIndex byDefault() {
        return new VFToIndex(
                s -> {
                    if (s.isMissing()) {
                        return Integer.MIN_VALUE;
                    } else {
                        switch (s.rvar().type()) {
                            case TEXT:
                            case NOMINAL:
                                try {
                                    return Integer.parseInt(s.getLabel());
                                } catch (NumberFormatException nfe) {
                                    return Integer.MIN_VALUE;
                                }
                            case INT:
                            case BOOLEAN:
                                return s.getInt();
                            case LONG:
                                return Long.valueOf(s.getLong()).intValue();
                            default:
                                return (int) Math.rint(s.getDouble());
                        }
                    }
                },
                null,
                null,
                null
        );
    }

    public static VFToIndex bySpot(Function<VSpot, Integer> fSpot) {
        return new VFToIndex(fSpot, null, null, null);
    }

    public static VFToIndex byValue(Function<Double, Integer> fValue) {
        return new VFToIndex(null, fValue, null, null);
    }

    public static VFToIndex byIndex(Function<Integer, Integer> fIndex) {
        return new VFToIndex(null, null, fIndex, null);
    }

    public static VFToIndex byLabel(Function<String, Integer> fLabel) {
        return new VFToIndex(null, null, null, fLabel);
    }

    private final Function<VSpot, Integer> fSpot;
    private final Function<Double, Integer> fValue;
    private final Function<Integer, Integer> fIndex;
    private final Function<String, Integer> fLabel;

    private VFToIndex(Function<VSpot, Integer> fSpot,
                        Function<Double, Integer> fValue,
                        Function<Integer, Integer> fIndex,
                        Function<String, Integer> fLabel) {
        this.fSpot = fSpot;
        this.fValue = fValue;
        this.fIndex = fIndex;
        this.fLabel = fLabel;
    }

    @Override
    public void fit(Var var) {
    }

    @Override
    public Var apply(Var var) {
        if (fSpot != null) {
            return var.stream().map(fSpot).collect(VarInt.collector()).withName(var.name());
        }
        if (fValue != null) {
            return var.stream().mapToDouble().boxed()
                    .map(fValue).collect(VarInt.collector()).withName(var.name());
        }
        if (fIndex != null) {
            return var.stream().mapToInt().boxed()
                    .map(fIndex).collect(VarInt.collector()).withName(var.name());
        }
        if (fLabel != null) {
            return var.stream().mapToString()
                    .map(fLabel).collect(VarInt.collector()).withName(var.name());
        }
        throw new RuntimeException("no transform function available");
    }
}
