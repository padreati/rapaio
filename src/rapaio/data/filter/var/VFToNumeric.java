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

package rapaio.data.filter.var;

import rapaio.data.NumericVar;
import rapaio.data.Var;
import rapaio.data.stream.VSpot;

import java.util.function.Function;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public class VFToNumeric extends AbstractVF {

    private static final long serialVersionUID = -6471901421507667237L;

    public static VFToNumeric byDefault() {
        return new VFToNumeric(
                s -> {
                    if (s.isMissing()) {
                        return Double.NaN;
                    } else {
                        switch (s.getVar().type()) {
                            case TEXT:
                            case NOMINAL:
                                try {
                                    return Double.parseDouble(s.getLabel());
                                } catch (NumberFormatException nfe) {
                                    return Double.NaN;
                                }
                            case ORDINAL:
                            case INDEX:
                            case BINARY:
                                return Integer.valueOf(s.getIndex()).doubleValue();
                            case STAMP:
                                return Long.valueOf(s.getStamp()).doubleValue();
                            default:
                                return s.getValue();
                        }
                    }
                },
                null,
                null,
                null
        );
    }

    public static VFToNumeric bySpot(Function<VSpot, Double> fSpot) {
        return new VFToNumeric(fSpot, null, null, null);
    }

    public static VFToNumeric byValue(Function<Double, Double> fValue) {
        return new VFToNumeric(null, fValue, null, null);
    }

    public static VFToNumeric byIndex(Function<Integer, Double> fIndex) {
        return new VFToNumeric(null, null, fIndex, null);
    }

    public static VFToNumeric byLabel(Function<String, Double> fLabel) {
        return new VFToNumeric(null, null, null, fLabel);
    }

    private final Function<VSpot, Double> fSpot;
    private final Function<Double, Double> fValue;
    private final Function<Integer, Double> fIndex;
    private final Function<String, Double> fLabel;

    private VFToNumeric(Function<VSpot, Double> fSpot,
                        Function<Double, Double> fValue,
                        Function<Integer, Double> fIndex,
                        Function<String, Double> fLabel) {
        this.fSpot = fSpot;
        this.fValue = fValue;
        this.fIndex = fIndex;
        this.fLabel = fLabel;
    }

    @Override
    public void fit(Var... vars) {
        checkSingleVar(vars);
    }

    @Override
    public Var apply(Var... vars) {
        checkSingleVar(vars);
        Var v = vars[0];

        if (fSpot != null) {
            return v.stream().map(fSpot).collect(NumericVar.collector()).withName(v.name());
        }
        if (fValue != null) {
            return v.stream().mapToDouble().boxed()
                    .map(fValue).collect(NumericVar.collector()).withName(v.name());
        }
        if (fIndex != null) {
            return v.stream().mapToInt().boxed()
                    .map(fIndex).collect(NumericVar.collector()).withName(v.name());
        }
        if (fLabel != null) {
            return v.stream().mapToString()
                    .map(fLabel).collect(NumericVar.collector()).withName(v.name());
        }
        throw new RuntimeException("no transform function available");
    }
}
