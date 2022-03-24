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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rapaio.data.BoundFrame;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarBinary;
import rapaio.data.VarRange;

/**
 * Replaces specified columns in VRange with numeric one hot
 * encodings. If the specified columns are numeric already, then these
 * columns will not be processed.
 * <p>
 * In order to convert to one hot all the existent nominal columns, than you
 * can specify 'all' value in column range.
 * <p>
 * The given columns will be placed grouped, in the place of
 * the given nominal column.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class FOneHotEncoding extends AbstractFFilter {

    public static FOneHotEncoding on(String... varNames) {
        return new FOneHotEncoding(VarRange.of(varNames), false, true);
    }

    public static FOneHotEncoding on(boolean lessOne, boolean useNa, String... varNames) {
        return new FOneHotEncoding(VarRange.of(varNames), lessOne, useNa);
    }

    public static FOneHotEncoding on(VarRange varRange) {
        return new FOneHotEncoding(varRange, false, true);
    }

    public static FOneHotEncoding on(boolean lessOne, boolean useNa, VarRange varRange) {
        return new FOneHotEncoding(varRange, lessOne, useNa);
    }

    @Serial
    private static final long serialVersionUID = 4893532203594639069L;
    private Map<String, List<String>> levels;
    private final boolean lessOne;
    private final boolean useNa;

    private FOneHotEncoding(VarRange varRange, boolean lessOne, boolean useNa) {
        super(varRange);
        this.lessOne = lessOne;
        this.useNa = useNa;
    }

    @Override
    public FOneHotEncoding newInstance() {
        return new FOneHotEncoding(varRange, lessOne, useNa);
    }

    @Override
    public void coreFit(Frame df) {
        levels = new HashMap<>();
        for (String varName : varNames) {
            // for each nominal variable
            if (df.rvar(varName).type().isNominal()) {
                levels.put(varName, df.rvar(varName).levels());
            }
        }
    }

    public Frame apply(Frame df) {
        FApplyCommon fOneApplyHotEncoding = new FApplyCommon();

        return fOneApplyHotEncoding.applyHotEncoding(df,varNames,levels,useNa,lessOne);
    }
}
